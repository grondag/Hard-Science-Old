package grondag.hard_science.superblock.placement;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.simulator.base.AssignedNumber;
import grondag.hard_science.simulator.base.DomainManager;
import grondag.hard_science.simulator.base.DomainManager.Domain;
import grondag.hard_science.simulator.base.DomainManager.IDomainMember;
import grondag.hard_science.simulator.base.IIdentified;
import grondag.hard_science.simulator.base.jobs.Job;
import grondag.hard_science.simulator.base.jobs.RequestPriority;
import grondag.hard_science.simulator.persistence.IDirtListener;
import grondag.hard_science.simulator.persistence.IDirtListener.NullDirtListener;
import grondag.hard_science.superblock.placement.AbstractPlacementSpec.PlacementSpecEntry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;


public class BuildManager implements IReadWriteNBT, IDomainMember
{
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(
            new ThreadFactory()
            {
                private AtomicInteger count = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r)
                {
                    Thread thread = new Thread(r, "Hard Science Build Manager Thread -" + count.getAndIncrement());
                    thread.setDaemon(true);
                    return thread;
                }
            });
    
    protected Domain domain;

    protected IDirtListener dirtListener = NullDirtListener.INSTANCE;
    
    private Int2ObjectOpenHashMap<Build> builds = new Int2ObjectOpenHashMap();
    
    @Override
    public Domain getDomain()
    {
        return this.domain;
    }
    
    public void setDomain(Domain domain)
    {
        this.domain = domain;
        this.dirtListener = domain.getDirtListener();
    }
    
    private void setDirty()
    {
        this.dirtListener.setDirty();
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        NBTTagList nbtBuilds = tag.getTagList(ModNBTTag.BUILD_MANAGER_BUILDS, 10);
        if( nbtBuilds != null && !nbtBuilds.hasNoTags())
        {
            for(NBTBase subTag : nbtBuilds)
            {
                if(subTag != null)
                {
                    Build b = new Build();
                    b.deserializeID((NBTTagCompound) subTag);
                    this.builds.put(b.getId(), b);
                }
            }   
        }        
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        if(!this.builds.isEmpty())
        {
            NBTTagList nbtJobs = new NBTTagList();
            
            for(Build b : this.builds.values())
            {
                nbtJobs.appendTag(b.serializeNBT());
            }
            tag.setTag(ModNBTTag.BUILD_MANAGER_BUILDS, nbtJobs);
        }        
    }
    
    public class Build implements IReadWriteNBT, IDomainMember, IIdentified
    {
        private int id = IIdentified.UNASSIGNED_ID;
        
        private ArrayList<AbstractPlacementSpec> specs  = new ArrayList<AbstractPlacementSpec>();
        
        /**
         * Map of (packed) positions affected by this build, with list of indexes to 
         * placement operations for each position.
         */
        private Long2ObjectOpenHashMap<IntArrayList> positions;
        
        /** 
         * If set, means build is under construction. Persisted. 
         */
        private int jobID = IIdentified.UNASSIGNED_ID;
        
        /**
         * Use #job() because is lazy lookup after deserialization.
         */
        private Job job;
        
        /**
         * List of players who are currently viewing this build
         * and should thus receive client updates.
         */
        private SimpleUnorderedArrayList<EntityPlayer> listeners = new SimpleUnorderedArrayList<EntityPlayer>();

        private Build() {};
        
        /**
         * If not already in construction, creates a new
         * job in this build's domain.  Build can no longer be 
         * changed unless the job is cancelled.
         * @return
         */
        public synchronized Job construct(RequestPriority priority, EntityPlayer player)
        {
            Job result = new Job(priority, player);
            domain.JOB_MANAGER.addJob(result);
            this.jobID = result.getId();
            this.job = result;
            setDirty();
            return result;
        }
        
        /**
         * Currently assigned job.
         * Will return null if no job or no longer exists
         * but will return terminated jobs.
         */
        @Nullable
        public Job job()
        {
            if(this.job == null && this.jobID != IIdentified.UNASSIGNED_ID)
            {
                this.job = DomainManager.INSTANCE.assignedNumbersAuthority().jobIndex().get(this.jobID);
            }
            return this.job;
        }
        
        /**
         * True if a job has been submitted for this build,
         * the job still exists, and the job has not terminated.
         */
        public boolean isUnderConstruction()
        {
            Job j = this.job();
            return j != null && !j.getStatus().isTerminated;
        }
        
        /**
         * Called server-side when player makes this their active build.
         * Will cause player to receive client-side data for rendering.
         * Does NOT call {@link #stopListening(EntityPlayer)} on any
         * previously active build.
         */
        public void startListening(EntityPlayer player)
        {
            synchronized(this.listeners)
            {
                if(!this.listeners.contains(player))
                {
                    boolean wasTracking = this.needsVirtualTracking();
                    
                    this.listeners.add(player);
                    this.sendCompiledSnapshot(player);
                    
                    /** enable virtual blocks on client side when we have first listener
                     * unless was already tracked due to construction in progress. */
                    if(!wasTracking) this.startVirtualTracking();
                }
            }
        }
        
        /**
         * Called server-side when player no longer needs client-side data for rendering.
         * Call when they select a different build or select no build.
         */
        public void stopListening(EntityPlayer player)
        {
            synchronized(this.listeners)
            {
                boolean wasTracking = this.needsVirtualTracking();
                
                this.listeners.removeIfPresent(player);
                
                /** disable virtual blocks on client side when last listener leaves 
                 *  unless build is under construction */
                if(wasTracking && !this.needsVirtualTracking()) this.stopVirtualTracking();
            }
        }
        
        /**
         * Sends all currently compiled placements to the player.
         * Used when player first starts listening.
         */
        private void sendCompiledSnapshot(EntityPlayer player)
        {
            //TODO
        }
        
        /**
         * Notifies virtual block tracker to start tracking virtual blocks for all
         * positions in this build.
         */
        private void startVirtualTracking()
        {
            //TODO
            //TODO: need to call this when a build in progress is deserialized
        }
        
        /**
         * Call when AFFECTED block positions are added or removed from this build
         * so that virtual block tracker is notified.
         * Does NOT need to be called if the build outcome changes,
         * UNLESS the outcome is to revert to current world state.
         */
        private void updateVirtualTracking(BlockPos pos)
        {
            //TODO
        }
        
        /**
         * Notifies virtual block tracker to stop tracking virtual blocks for all
         * positions in this build.
         */
        private void stopVirtualTracking()
        {
            //TODO
            //TODO: need to call this when a build in progress is terminated
        }
        
        /**
         * True if block positions in this build should be tracked
         * by the virtual block tracker for virtual block placement
         * and world state lookup.
         */
        public boolean needsVirtualTracking()
        {
            return !this.listeners.isEmpty() || this.isUnderConstruction();
        }
        
        public synchronized void addSpec(AbstractPlacementSpec spec)
        {
            this.specs.add(spec);
            this.registerPositions(spec, this.specs.size() - 1);
            setDirty();
        }
        
        /** 
         * Discards the most recent placement operation.
         *  Has no effect if there are no operations
         */
        public synchronized void undo()
        {
            if(!this.specs.isEmpty())
            {
                int index = this.specs.size() - 1;
                this.unregisterPositions(this.specs.remove(index), index);
                setDirty();
            }
        }
        
        private void registerPositions(AbstractPlacementSpec spec, int index)
        {
            EXECUTOR.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    synchronized(positions)
                    {
                        for(PlacementSpecEntry entry : spec.entries())
                        {
                            long packed = entry.pos().toLong();
                            IntArrayList list = positions.get(packed);
                            if(list == null)
                            {
                                list = new IntArrayList();
                                positions.put(packed, list);
                            }
                            list.add(index);
                        }
                    }
                }
            });
        }
        
        private void unregisterPositions(AbstractPlacementSpec spec, int index)
        {
            EXECUTOR.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    synchronized(positions)
                    {
                        for(PlacementSpecEntry entry : spec.entries())
                        {
                            long packed = entry.pos().toLong();
                            IntArrayList list = positions.get(packed);
                            if(list != null)
                            {
                                for(int i = list.size() - 1; i >= 0; i--)
                                {
                                    if(list.getInt(i) == index)
                                    {
                                        list.removeInt(i);
                                        break;
                                    }
                                }
                                
                                if(list.isEmpty())
                                {
                                    positions.remove(packed);
                                }
                            }
                        }
                    }
                }
            });
        }
        
        public List<AbstractPlacementSpec> specs()
        {
            return Collections.unmodifiableList(specs);
        }
        
        @Override
        public Domain getDomain()
        {
            return BuildManager.this.getDomain();
        }

        @Override
        public void deserializeNBT(NBTTagCompound tag)
        {
            this.deserializeID(tag);
            DomainManager.INSTANCE.assignedNumbersAuthority().buildIndex().register(this);
            this.jobID = tag.getInteger(ModNBTTag.BUILD_JOB_ID);
            
            this.specs.clear();
            NBTTagList nbtSpecs = tag.getTagList(ModNBTTag.BUILD_PLACEMENT_SPECS, 10);
            if( nbtSpecs != null && !nbtSpecs.hasNoTags())
            {
                for(NBTBase subTag : nbtSpecs)
                {
                    if(subTag != null)
                    {
                        AbstractPlacementSpec spec = PlacementSpecType.deserializeSpec((NBTTagCompound)subTag);
                        this.addSpec(spec);
                    }
                }   
            } 
        }

        @Override
        public void serializeNBT(NBTTagCompound tag)
        {
            this.serializeID(tag);
            tag.setInteger(ModNBTTag.BUILD_JOB_ID, this.jobID);

            if(!this.specs.isEmpty())
            {
                NBTTagList nbtSpecs = new NBTTagList();
                
                for(AbstractPlacementSpec spec : this.specs)
                {
                    nbtSpecs.appendTag(PlacementSpecType.serializeSpec(spec));
                }
                tag.setTag(ModNBTTag.BUILD_PLACEMENT_SPECS, nbtSpecs);
            }  
        }

        @Override
        public int getIdRaw()
        {
            return this.id;
        }

        @Override
        public void setId(int id)
        {
            this.id = id;
        }

        @Override
        public AssignedNumber idType()
        {
            return AssignedNumber.BUILD;
        }
        
    }
}
