package grondag.hard_science.simulator.storage.jobs;

import java.util.Iterator;

import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.superblock.placement.Build;
import grondag.hard_science.superblock.virtual.ExcavationRenderTracker;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.domain.DomainManager;
import grondag.hard_science.simulator.domain.IDomainMember;
import grondag.hard_science.simulator.persistence.AssignedNumber;
import grondag.hard_science.simulator.persistence.IIdentified;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * Collection of tasks - typically does not do any meaningful by itself.
 * Responsible for serializing tasks contained within it.
 */
public class Job implements Iterable<AbstractTask>, IIdentified, IReadWriteNBT, IDomainMember
{
    protected RequestPriority priority = RequestPriority.MEDIUM;
    
    /**
     * If assigned means this is a construction job.
     */
    private int buildID = IIdentified.UNASSIGNED_ID;
    
    /**
     * Dimension in which work is done, if applies.
     */
    private int dimensionID = 0;
    
    /**
     * Lazily set from dimensionID
     */
    private World world = null;
    
    /**
     * Used by job manager to know prior sort bucket when priority changes.
     */
    private RequestPriority effectivePriority = null;
    
    /**
     * Used by job manager to know prior status when status changes.
     */
    private RequestStatus effectiveStatus = null;
    
    private String userName;
    private JobManager jobManager = NullJobManager.INSTANCE;
    private RequestStatus status;
    
    /**
     * True if child task status may have changed and thus need to 
     * recompute status of this container request.
     */
    protected boolean isTaskStatusDirty = false;
    
    protected int readyWorkCount = 0;
    
    private final SimpleUnorderedArrayList<AbstractTask> tasks =  new SimpleUnorderedArrayList<AbstractTask>();
    
    private int id = IIdentified.UNASSIGNED_ID;
    
    private boolean isHeld;
    
    public Job() {};
    
    public Job(RequestPriority priority, EntityPlayer player)
    {
        this.priority = priority;
        this.userName = player.getName();
        DomainManager.INSTANCE.assignedNumbersAuthority().jobIndex().register(this);
    }
    
    /**
     * Called by job manager during deserialization.
     */
    public Job(JobManager manager, NBTTagCompound tag)
    {
        this.jobManager = manager;
        this.deserializeNBT(tag);
    }
    
    /**
     * Called when a new job is added to the job manager.
     * Is not called during deserialization.
     */
    public void onJobAdded(JobManager manager)
    {
        this.jobManager = manager;
        this.onLoaded();
        this.setDirty();
    }
    
    /**
     * Called just before a new job is added to the job manager
     * and when a job is deserialized right before tasks are available to be executed.
     * Called only 1X in each case.
     */
    protected void onLoaded()
    {
        if(this.buildID != IIdentified.UNASSIGNED_ID)
        {
            ExcavationRenderTracker.INSTANCE.add(this);
        }
    }
    
    protected void updateReadyWork(int delta)
    {
        if(delta > 0)
        {
            if(this.readyWorkCount == 0)
            {
                this.readyWorkCount = delta;
                this.jobManager.notifyReadyStatus(this);
            }
            else
            {
                this.readyWorkCount += delta;
            }
        }
        else if(delta < 0)
        {
            if(this.readyWorkCount > 0)
            {
                this.readyWorkCount += delta;
                if(this.readyWorkCount < 0)
                {
                    this.readyWorkCount = 0;
                    Log.warn("Job tried to decrement ready work count below zero. This is probably a bug.");
                }
                if(this.readyWorkCount == 0)
                {
                    this.jobManager.notifyReadyStatus(this);
                }
            }
            else
            {
                Log.warn("Job tried to decrement ready work count below zero. This is probably a bug.");
            }
        }
    }
    public void addTask(AbstractTask task)
    {
        synchronized(this.tasks)
        {
            this.isTaskStatusDirty = true;
            tasks.add(task);
            if(task.initialize(this))
            {
                this.updateReadyWork(1);
            }
        }
        this.setDirty();
    }
    
    public void addTasks(AbstractTask... tasks)
    {
        synchronized(this.tasks)
        {
            if(tasks.length > 0)
            {
                this.isTaskStatusDirty = true;
                int readyCount = 0;
                for(AbstractTask t : tasks)
                {
                    this.tasks.add(t);
                    if(t.initialize(this)) readyCount++;
                }
                if(readyCount > 0) this.updateReadyWork(readyCount);
            }
        }
        this.setDirty();
    }
    
    public RequestPriority getPriority()
    {
        return this.priority;
    }

    public void setPriority(RequestPriority priority)
    {
        if(priority != this.priority)
        {
            this.priority = priority;
            this.jobManager.notifyPriorityChange(this);
            this.setDirty();
        }
    }
    
    public String userName()
    {
        return this.userName;
    }

    protected void setStatus(RequestStatus newStatus)
    {
        if(newStatus != this.status)
        {
            this.status = newStatus;
            if(newStatus.isTerminated)
            {
                this.jobManager.notifyTerminated(this);
            }
            this.setDirty();
        }
    }
    
    protected void updateTaskStatusIfNeeded()
    {
        if(this.isTaskStatusDirty)
        {
            this.isTaskStatusDirty = false;

            @SuppressWarnings("unused")
            int abend = 0, active = 0, ready = 0, cancelled = 0, cancel_ip = 0, complete = 0, is_new = 0, waiting = 0;
            for(AbstractTask t : this.tasks)
            {
                switch(t.getStatus())
                {
                case ABEND:
                    abend++;
                    break;
                case ACTIVE:
                    active++;
                    break;
                case READY:
                    ready++;
                    break;
                case CANCELLED:
                    cancelled++;
                    break;
                case CANCEL_IN_PROGRESS:
                    cancel_ip++;
                    break;
                case COMPLETE:
                    complete++;
                    break;
                case NEW:
                    is_new++;
                    break;
                case WAITING:
                    waiting++;
                    break;
                default:
                    break;
                
                }
            }
            
            if(complete + cancelled + abend == tasks.size())
            {
                // no active tasks
                
                if(this.status == RequestStatus.CANCEL_IN_PROGRESS)
                {
                    this.setStatus(RequestStatus.CANCELLED);
                }
                else
                {
                    this.setStatus(abend == 0 ? RequestStatus.COMPLETE : RequestStatus.ABEND);
                }
            }
            // if one or more active tasks and cancel is in progress, result can only be cancel in progress
            // meaning we continue to wait for the active tasks to end
            else if(this.status != RequestStatus.CANCEL_IN_PROGRESS)
            {
                if(is_new == tasks.size())
                {
                    this.setStatus(RequestStatus.NEW);
                }
                else if(active > 0)
                {
                    this.setStatus(RequestStatus.ACTIVE);
                }
                else if(ready > 0)
                {
                    this.setStatus(RequestStatus.READY);
                }
                else
                {
                    this.setStatus(RequestStatus.WAITING);
                }
            }
        }
    }
    public RequestStatus getStatus()
    {
        this.updateTaskStatusIfNeeded();
        return this.status;
    }
    
    /**
     * True if job is held - will not process.
     */
    public boolean isHeld()
    {
        return this.isHeld;
    }
    
    /**
     * Allows a held job to run.
     */
    public void release()
    {
        if(this.isHeld)
        {
            this.isHeld = false;
            this.jobManager.notifyHoldChange(this);
            this.setDirty();
        }
    }
    
    public void hold()
    {
        if(!this.isHeld)
        {
            this.isHeld = true;
            this.jobManager.notifyHoldChange(this);
            this.setDirty();
        }
    }
    
    public void cancel()
    {
        synchronized(this.tasks)
        {
            if(this.tasks.isEmpty())
            {
                setStatus(RequestStatus.CANCELLED);
            }
            else
            {
                this.isTaskStatusDirty = true;
                this.setStatus(RequestStatus.CANCEL_IN_PROGRESS);
                for(AbstractTask t : this.tasks)
                {
                    if(!t.isTerminated()) t.cancel();
                }
            }
        }
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        this.tasks.clear();
        
        this.deserializeID(tag);
        this.priority = Useful.safeEnumFromTag(tag, ModNBTTag.REQUEST_PRIORITY, RequestPriority.CRITICAL);
        this.userName = tag.getString(ModNBTTag.REQUEST_USER_NAME);
        this.status = Useful.safeEnumFromTag(tag, ModNBTTag.REQUEST_STATUS, RequestStatus.NEW);
        this.isHeld = tag.getBoolean(ModNBTTag.JOB_HELD_FLAG);
        this.buildID = tag.getInteger(ModNBTTag.BUILD_ID);
        this.dimensionID = tag.getInteger(ModNBTTag.BUILD_DIMENSION_ID);
        
        DomainManager.INSTANCE.assignedNumbersAuthority().jobIndex().register(this);
        
        int readyCount = 0;
        NBTTagList nbtTasks = tag.getTagList(ModNBTTag.REQUEST_CHILDREN, 10);
        if( nbtTasks != null && !nbtTasks.hasNoTags())
        {
            for(NBTBase subTag : nbtTasks)
            {
                if(subTag != null)
                {
                    AbstractTask t = TaskType.deserializeTask((NBTTagCompound) subTag, this);
                    if(t != null)
                    {
                        this.tasks.add(t);
                        if(t.getStatus() == RequestStatus.READY) readyCount++;
                    }
                }
            }  
        }
        
        this.isTaskStatusDirty = true;
        
        if(!this.getStatus().isTerminated) this.onLoaded();
        
        if(readyCount > 0) this.updateReadyWork(readyCount);
        
    }

    @Override
    public AssignedNumber idType()
    {
        return AssignedNumber.JOB;
    }
    
    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        this.serializeID(tag);
        Useful.saveEnumToTag(tag, ModNBTTag.REQUEST_PRIORITY, this.priority);
        tag.setString(ModNBTTag.REQUEST_USER_NAME, this.userName);
        Useful.saveEnumToTag(tag, ModNBTTag.REQUEST_STATUS, this.status);
        tag.setBoolean(ModNBTTag.JOB_HELD_FLAG, this.isHeld);
        tag.setInteger(ModNBTTag.BUILD_ID, this.buildID);
        tag.setInteger(ModNBTTag.BUILD_DIMENSION_ID, this.dimensionID);
        
        if(!this.tasks.isEmpty())
        {
            NBTTagList nbtTasks = new NBTTagList();
            
            for(AbstractTask t : this.tasks)
            {
                nbtTasks.appendTag(TaskType.serializeTask(t));
            }
            tag.setTag(ModNBTTag.REQUEST_CHILDREN, nbtTasks);
        }
    }

    /**
     * Called by contained tasks when they have a status change.
     * Should NOT be called when task is first initialized.
     */
    public void notifyTaskStatusChange(AbstractTask abstractTask, RequestStatus priorStatus)
    {
        this.isTaskStatusDirty = true;
        
        if(priorStatus == RequestStatus.READY)
        {
            this.updateReadyWork(-1);
        }
        else if(abstractTask.getStatus() == RequestStatus.READY)
        {
            this.updateReadyWork(1);
        }
    }
    
    /**
     * Task iterator is read-only.
     */
    @Override
    public Iterator<AbstractTask> iterator()
    {
        return this.tasks.iterator();
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

    /**
     * For use by job manager.
     */
    public RequestPriority effectivePriority()
    {
        return effectivePriority;
    }

    /**
     * Causes {@link #effectivePriority()} to match {@link #getPriority()}
     */
    public void updateEffectivePriority()
    {
        this.effectivePriority = this.getPriority();
    }
    
    /**
     * For use by job manager.
     */
    public RequestStatus effectiveStatus()
    {
        return effectiveStatus;
    }

    /**
     * Causes {@link #effectiveStatus()} to match {@link #getStatus()}
     */
    public void updateEffectiveStatus()
    {
        this.effectiveStatus = this.getStatus();
    }

    /**
     * True if this job has inactive tasks ready to execute
     * and the job should be included in the work backlog.
     */
    public boolean hasReadyWork()
    {
        return this.readyWorkCount > 0;
    }

    @Override
    public Domain getDomain()
    {
        return this.jobManager.getDomain();
    }

    public int getBuildID()
    {
        return buildID;
    }

    public void setBuildID(int buildID)
    {
        this.buildID = buildID;
        this.setDirty();
    }
    
    public Build getBuild()
    {
        return this.getDomain().domainManager().assignedNumbersAuthority().buildIndex().get(buildID);
    }

    public int getDimensionID()
    {
        return dimensionID;
    }

    public void setDirty()
    {
        this.jobManager.setDirty();
    }
    
    public void setDimensionID(int dimensionID)
    {
        this.dimensionID = dimensionID;
        this.world = null;
        this.setDirty();
    }
    
    public World world()
    {
        if(this.world == null)
        {
            this.world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(this.dimensionID);
        }
        return this.world;
    }
}
