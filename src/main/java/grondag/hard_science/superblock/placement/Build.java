package grondag.hard_science.superblock.placement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.library.world.ChunkMap;
import grondag.hard_science.simulator.base.AssignedNumber;
import grondag.hard_science.simulator.base.DomainManager;
import grondag.hard_science.simulator.base.DomainManager.Domain;
import grondag.hard_science.simulator.base.DomainManager.IDomainMember;
import grondag.hard_science.simulator.base.IIdentified;
import grondag.hard_science.simulator.base.jobs.Job;
import grondag.hard_science.simulator.base.jobs.RequestPriority;
import grondag.hard_science.superblock.placement.AbstractPlacementSpec.PlacementSpecEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class Build extends ChunkMap<BuildChunk> implements IReadWriteNBT, IDomainMember, IIdentified
    {
        private final BuildManager buildManager;

        private int id = IIdentified.UNASSIGNED_ID;
        
        private int dimensionID;
        
        /**
         * Don't use directly. Use {@link #world()} instead.
         * Is lazily retrieved after deserialization.
         */
        @Deprecated
        private World world;
        
        private ArrayList<AbstractPlacementSpec> specs  = new ArrayList<AbstractPlacementSpec>();
        
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

//        /**
//         * True if we are providing state update to virtual state manager
//         */
//        private boolean isVirtualTracking = false;
        
        Build(BuildManager buildManager)
        {
            super();
            this.buildManager = buildManager;
        };
        
        Build(BuildManager buildManager, World inWorld)
        {
            this(buildManager);
            this.dimensionID = inWorld.provider.getDimension();
            this.world = inWorld;
        }
        
        @Override
        protected BuildChunk newEntry(BlockPos pos)
        {
            return new BuildChunk(this, pos);
        }
        
        public int dimensionID()
        {
            return this.dimensionID;
        }
        
        public World world()
        {
            if(this.world == null)
            {
                this.world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(this.dimensionID);
            }
            return this.world;
        }
        
        /**
         * If not already in construction, creates a new
         * job in this build's domain.  Build can no longer be 
         * changed unless the job is cancelled.
         * @return
         */
        public synchronized Job construct(RequestPriority priority, EntityPlayer player)
        {
            Job result = new Job(priority, player);
            this.buildManager.domain.JOB_MANAGER.addJob(result);
            this.jobID = result.getId();
            this.job = result;
            this.buildManager.setDirty();
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
                    
                    this.listeners.add(player);
                    this.sendCompiledSnapshot(player);
//                    this.checkVirtualTracking();
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
                this.listeners.removeIfPresent(player);
                
                // check virtual tracking when last listener leaves 
//                if(this.listeners.isEmpty()) this.checkVirtualTracking();;
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
        
//        /**
//         * Notifies virtual block tracker to start tracking virtual blocks for all
//         * buildEntries in this build.
//         */
//        private void startVirtualTracking()
//        {
//            if(!this.isVirtualTracking)
//            {
//                this.isVirtualTracking = true;
//                VirtualStateManager.INSTANCE.startTrackingBuild(this);
//            }
//        }
        
//        /**
//         * Call when AFFECTED block buildEntries are added to this build
//         * so that virtual block tracker is notified.
//         * Does NOT need to be called if the build outcome changes,
//         * UNLESS the outcome is to revert to current world state.
//         * <p>
//         * Has no effect if called while virtual state tracking is off
//         * for this build, so can be safely called each time build state
//         * is modified without checking. DO NOT CALL DIRECTLY.  Call {@link #checkVirtualTracking()} instead.
//         */
//        private void startVirtualTracking(BlockPos pos)
//        {
//            if(this.isVirtualTracking)
//            {
//                VirtualStateManager.INSTANCE.startBuildTracking(this, pos);
//            }
//        }
        
//        /**
//         * Call when AFFECTED block buildEntries are removed from this build.
//         * See {@link #startVirtualTracking(BlockPos)}. 
//         */
//        private void stopVirtualTracking(BlockPos pos)
//        {
//            if(this.isVirtualTracking)
//            {
//                VirtualStateManager.INSTANCE.stopBuildTracking(this, pos);
//            }
//        }
        
//        /**
//         * Notifies virtual block tracker to stop tracking virtual blocks for all
//         * buildEntries in this build. DO NOT CALL DIRECTLY.  Call {@link #checkVirtualTracking()} instead.
//         */
//        private void stopVirtualTracking()
//        {
//            if(this.isVirtualTracking)
//            {
//                this.isVirtualTracking = false;
//                VirtualStateManager.INSTANCE.stopTrackingBuild(this);
//            }
//        }
        
        //TODO: need to call this when a build in progress is deserialized
        //TODO: need to call this when a build in progress is terminated
        /**
         * Turns virtual tracking on or off as necessary.
         * Call after something changes that might affect tracking status.
         */
//        private void checkVirtualTracking()
//        {
//            boolean needsIt = this.needsVirtualTracking();
//            if(needsIt != this.isVirtualTracking)
//            {
//                if(needsIt) 
//                    this.startVirtualTracking();
//                else 
//                    this.stopVirtualTracking();
//            }
//        }
        
//        /**
//         * True if block buildEntries in this build should be tracked
//         * by the virtual block tracker for virtual block placement
//         * and world state lookup.
//         */
//        public boolean needsVirtualTracking()
//        {
//            return !this.listeners.isEmpty() || this.isUnderConstruction();
//        }
        
        public synchronized void addSpec(AbstractPlacementSpec spec)
        {
            this.specs.add(spec);
            this.registerPositions(spec);
            this.buildManager.setDirty();
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
                this.buildManager.setDirty();
            }
        }
        
        @Override
        public synchronized BuildChunk getOrCreate(BlockPos pos)
        {
            return super.getOrCreate(pos);
        }

        @Override
        public synchronized void remove(BlockPos pos)
        {
            super.remove(pos);
        }

        private void registerPositions(AbstractPlacementSpec spec)
        {
            BuildManager.EXECUTOR.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    for(PlacementSpecEntry specEntry : spec.entries())
                    {
                        BuildChunk chunk = Build.this.getOrCreate(specEntry.pos());
                        if(chunk == null)
                        {
                            Log.warn("Unable to find or create build chunk for unknown reason. This is a bug.");
                            continue;
                        }
                        chunk.addSpec(specEntry, spec);
                    }
                }
            });
        }
        
        private void unregisterPositions(AbstractPlacementSpec spec, int index)
        {
            BuildManager.EXECUTOR.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    for(PlacementSpecEntry specEntry : spec.entries())
                    {
                        BuildChunk chunk = Build.this.getIfExists(specEntry.pos());
                        if(chunk != null)
                        {
                            chunk.remove(specEntry.pos());
                            if(chunk.isEmpty())
                            {
                                Build.this.remove(specEntry.pos());
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
            return this.buildManager.getDomain();
        }

        @Override
        public void deserializeNBT(NBTTagCompound tag)
        {
            this.deserializeID(tag);
            DomainManager.INSTANCE.assignedNumbersAuthority().buildIndex().register(this);
            this.jobID = tag.getInteger(ModNBTTag.BUILD_JOB_ID);
            this.dimensionID = tag.getInteger(ModNBTTag.BUILD_DIMENSION_ID);
            
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
            tag.setInteger(ModNBTTag.BUILD_DIMENSION_ID,  this.dimensionID);

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