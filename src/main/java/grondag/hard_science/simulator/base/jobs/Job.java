package grondag.hard_science.simulator.base.jobs;

import java.util.Iterator;

import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.simulator.base.AssignedNumber;
import grondag.hard_science.simulator.base.DomainManager;
import grondag.hard_science.simulator.base.DomainManager.Domain;
import grondag.hard_science.simulator.base.DomainManager.IDomainMember;
import grondag.hard_science.superblock.placement.AbstractPlacementSpec;
import grondag.hard_science.superblock.placement.PlacementSpecType;
import grondag.hard_science.simulator.base.IIdentified;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/**
 * Collection of tasks - typically does not do any meaningful by itself.
 * Responsible for serializing tasks contained within it.
 */
public class Job implements Iterable<AbstractTask>, IIdentified, IReadWriteNBT, IDomainMember
{
    protected RequestPriority priority = RequestPriority.MEDIUM;
    
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
    
    /**
     * For construction jobs only.  Will be null otherwise.
     */
    private AbstractPlacementSpec spec;
    
    protected int readyWorkCount = 0;
    
    private final SimpleUnorderedArrayList<AbstractTask> tasks =  new SimpleUnorderedArrayList<AbstractTask>();
    
    private int id = IIdentified.UNASSIGNED_ID;
    
    private boolean isHeld;
    
    protected Job() {};
    
    public Job(RequestPriority priority, EntityPlayer player)
    {
        this(priority, player, null);
    }
    
    public Job(RequestPriority priority, EntityPlayer player, AbstractPlacementSpec spec)
    {
        this.priority = priority;
        this.userName = player.getName();
        this.spec = spec;
        DomainManager.INSTANCE.assignedNumbersAuthority().jobIndex().register(this);
    }
    
    public Job(JobManager manager, NBTTagCompound tag)
    {
        this.jobManager = manager;
        this.deserializeID(tag);
    }
    
    public void setJobManager(JobManager manager)
    {
        this.jobManager = manager;
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
        this.isTaskStatusDirty = true;
        tasks.add(task);
        if(task.initialize(this))
        {
            this.updateReadyWork(1);
        }
    }
    
    public void addTasks(AbstractTask... tasks)
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
        }
    }
    
    public void hold()
    {
        if(!this.isHeld)
        {
            this.isHeld = true;
            this.jobManager.notifyHoldChange(this);
        }
    }
    
    public void cancel()
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

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        this.tasks.clear();
        
        this.deserializeID(tag);
        this.priority = Useful.safeEnumFromTag(tag, ModNBTTag.REQUEST_PRIORITY, RequestPriority.CRITICAL);
        this.userName = tag.getString(ModNBTTag.REQUEST_USER_NAME);
        this.status = Useful.safeEnumFromTag(tag, ModNBTTag.REQUEST_STATUS, RequestStatus.NEW);
        this.isHeld = tag.getBoolean(ModNBTTag.JOB_HELD_FLAG);
        
        DomainManager.INSTANCE.assignedNumbersAuthority().jobIndex().register(this);
        
        NBTTagList nbtTasks = tag.getTagList(ModNBTTag.REQUEST_CHILDREN, 10);
        if( nbtTasks != null && !nbtTasks.hasNoTags())
        {
            int readyCount = 0;
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
            if(readyCount > 0) this.updateReadyWork(readyCount);
        }
        
        if(tag.hasKey(ModNBTTag.PLACEMENT_ENTRY_DATA))
        {
            NBTTagCompound subTag = tag.getCompoundTag(ModNBTTag.PLACEMENT_ENTRY_DATA);
            this.spec = PlacementSpecType.deserializeSpec(subTag);
        }
        else
        {
            this.spec = null;
        }
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
        if(!this.tasks.isEmpty())
        {
            NBTTagList nbtTasks = new NBTTagList();
            
            for(AbstractTask t : this.tasks)
            {
                nbtTasks.appendTag(TaskType.serializeTask(t));
            }
            tag.setTag(ModNBTTag.REQUEST_CHILDREN, nbtTasks);
        }
        
        if(this.spec != null)
        {
            tag.setTag(ModNBTTag.PLACEMENT_ENTRY_DATA, PlacementSpecType.serializeSpec(this.spec));
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
    
    /**
     * For construction jobs only.  Will be null otherwise.
     */
    public AbstractPlacementSpec spec()
    {
        return this.spec;
    }


}
