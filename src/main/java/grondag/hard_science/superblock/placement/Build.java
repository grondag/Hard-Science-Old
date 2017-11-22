package grondag.hard_science.superblock.placement;

import javax.annotation.Nullable;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.base.AssignedNumber;
import grondag.hard_science.simulator.base.DomainManager;
import grondag.hard_science.simulator.base.DomainManager.Domain;
import grondag.hard_science.simulator.base.DomainManager.IDomainMember;
import grondag.hard_science.simulator.base.IIdentified;
import grondag.hard_science.simulator.base.jobs.Job;
import grondag.hard_science.simulator.base.jobs.RequestPriority;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class Build implements IReadWriteNBT, IDomainMember, IIdentified
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
        
        private AbstractPlacementSpec spec;
        
        /** 
         * If set, means build is under construction. Persisted. 
         */
        private int jobID = IIdentified.UNASSIGNED_ID;
        
        /**
         * Use #job() because is lazy lookup after deserialization.
         */
        private Job job;
        
        private Build(BuildManager buildManager)
        {
            super();
            this.buildManager = buildManager;
        };
        
        public Build(BuildManager buildManager, World inWorld, AbstractPlacementSpec spec)
        {
            this(buildManager);
            this.dimensionID = inWorld.provider.getDimension();
            this.world = inWorld;
            this.spec = spec;
        }
        
        public Build(BuildManager buildManager, NBTTagCompound tag)
        {
            this(buildManager);
            this.deserializeNBT(tag);
            
            // re-launch if prior launch failed to complete
            if(this.jobID == IIdentified.SIGNAL_ID) this.launch();
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
         * Submits spec world task to world task manager
         * which compiles affected positions and then 
         * optimizes build order depending on the nature of the build.</p>
         * 
         * After the spec is bound to the world
         * creates a new job and submits it to the job manager.
         * 
         * Job ID is used to determine if we have started 
         * but not completed.  If world stops before completed
         * then will restart from the beginning on reload.
         * Unassigned job ID means not running.
         * Any other job ID means launch completed and job was submitted.
         */
        public void launch()
        {
            // abort if already launched or assigned
            if(this.jobID != IIdentified.UNASSIGNED_ID) return; 
            
            this.jobID = IIdentified.SIGNAL_ID;
            this.buildManager.setDirty();
            
            //TODO, create a new spec from this build and submit it to world task queue
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
            
            NBTTagCompound nbtSpec = tag.getCompoundTag(ModNBTTag.BUILD_PLACEMENT_SPECS);
            this.spec = PlacementSpecType.deserializeSpec(nbtSpec);
        }

        @Override
        public void serializeNBT(NBTTagCompound tag)
        {
            this.serializeID(tag);
            tag.setInteger(ModNBTTag.BUILD_JOB_ID, this.jobID);
            tag.setInteger(ModNBTTag.BUILD_DIMENSION_ID,  this.dimensionID);
            tag.setTag(ModNBTTag.BUILD_PLACEMENT_SPECS, PlacementSpecType.serializeSpec(this.spec));
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