package grondag.hard_science.simulator.base.jobs;

import javax.annotation.Nonnull;

import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.simulator.base.AssignedNumber;
import grondag.hard_science.simulator.base.DomainManager;
import grondag.hard_science.simulator.base.DomainManager.Domain;
import grondag.hard_science.simulator.base.DomainManager.IDomainMember;
import grondag.hard_science.simulator.base.IIdentified;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractTask implements IReadWriteNBT, IIdentified, IDomainMember
{
    protected Job job = NullJob.INSTANCE;
    private int id = IIdentified.UNASSIGNED_ID;
    protected RequestStatus status = RequestStatus.NEW;
    
    /**
     * Contains consequent and antecedent data from deserialization.
     * References are obtained lazily, because some will not be available 
     * during deserialization itself.  Null if no dependencies or after references
     * have been obtained.  Data are in pairs, so array size will always be an even number. 
     * index 0 = antecedent , index 1 = consequent.  If element 0 = this.id then
     * element 1 is a consequent of this request.  Otherwise, element 0 
     * will be an antecedent of element 1.
     */
    private int[] dependencyData = null;
    
    /** DO NOT REFERENCE DIRECTLY! Use {@link #consequents()} */
    private final SimpleUnorderedArrayList<AbstractTask> consequents = new SimpleUnorderedArrayList<AbstractTask>();
    
    /** DO NOT REFERENCE DIRECTLY! Use {@link #antecedents()} */
    private final SimpleUnorderedArrayList<AbstractTask> antecedents = new SimpleUnorderedArrayList<AbstractTask>();

    private static int spamCount = 0;
    
    /**
     * @param isNew set false on deserialization.
     */
    protected AbstractTask(boolean isNew)
    {
        if(isNew) this.onLoaded();
    }
    
    /**
     * Called by job when task is added.
     * All dependencies should be added before calling.
     * Should not be called otherwise nor called more than once.
     * Does NOT call job to notify of status change.
     * Return true if the task is in a ready state.
     */
    public boolean initialize(@Nonnull Job job)
    {
        if(this.job != NullJob.INSTANCE && spamCount < 100)
        {
            Log.warn("Task initialized more than once.  This is probably a bug.");
            spamCount++;
        }
        
        this.job = job;
        if(this.getStatus() != RequestStatus.NEW  && spamCount < 100)
        {
            Log.warn("Task initialized in a state other than NEW.  This is probably a bug.");
            spamCount++;
        }
        
        if(this.antecedents().isEmpty())
        {
            // access directly to avoid callback to job
            this.status = RequestStatus.READY;
            return true;
        }
        else
        {
            // access directly to avoid callback to job
            this.status = RequestStatus.WAITING;
            return false;
        }
    }
    
    /**
     * Moves status from READY to ACTIVE.  
     * Called by job manager when assigning work.
     */
    public void claim()
    {
        if(this.getStatus() != RequestStatus.READY)
            Log.warn("Task claimed in a state other than READY.  This is probably a bug.");
        this.setStatus(RequestStatus.ACTIVE);
    }
   
    /**
     * Moves status from ACTIVE back to READY.  
     * Called by worker when task must be abandoned.
     */
    public void abandon()
    {
        if(this.getStatus() != RequestStatus.ACTIVE)
            Log.warn("Task unclaimed in a state other than ACTIVE.  This is probably a bug.");
        this.setStatus(RequestStatus.READY);
    }
    
    public abstract TaskType requestType();
    
    @Override
    public AssignedNumber idType()
    {
        return AssignedNumber.TASK;
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

    public Job job()
    {
        return this.job;
    }
    
    public RequestStatus getStatus()
    {
        return this.status;
    }

    private void setStatus(RequestStatus newStatus)
    {
        if(newStatus != this.status)
        {
            RequestStatus oldStatus = this.status;
            this.status = newStatus;
            if(newStatus.isTerminated && !oldStatus.isTerminated)this.onTerminated();
            this.job.notifyTaskStatusChange(this, oldStatus);
        }
    }

    /** 
     * Convenient shorthand for getStatus().isTerminated 
     */
    public boolean isTerminated()
    {
        return this.getStatus().isTerminated;
    }
    
    public void cancel()
    {
        this.setStatus(this.requiresCleanupOnCancel() ? RequestStatus.CANCEL_IN_PROGRESS : RequestStatus.CANCELLED);
    }
    
    /** to limit log spam in real world */
    private static int completionSpamCount = 0;
    
    /**
     * Should be called on a claimed, active task to move it to completion.
     */
    public void complete()
    {
        if(this.status != RequestStatus.ACTIVE && completionSpamCount < 100)
        {
            completionSpamCount++;
            Log.warn("Invalid task state transition from %s to COMPLETE. This is probably a bug.", this.status.toString());
        }
        this.setStatus(RequestStatus.COMPLETE);
    }

    /**
     * True if this request needs to do some cleanup when cancelled and should
     * therefore continue to execute. Means {@link #cancel()} will result in
     * a CANCEL_REQUESTED status instead of CANCEL.
     */
    public boolean requiresCleanupOnCancel() { return false; }

    /**
     * Called after instance is first created or after deserialized.
     * Deserialization call is made by external helper class
     * so that it happens after any subclass deserialization.
     */
    protected synchronized void onLoaded()
    {
        DomainManager.INSTANCE.assignedNumbersAuthority().taskIndex().register(this);
    }
    
    protected synchronized void onTerminated()
    {
        SimpleUnorderedArrayList<AbstractTask> consequents = this.consequents();
        
        if(consequents.isEmpty()) return;
        
        for(AbstractTask r : consequents)
        {
            r.onAntecedentTerminated(this);;
        }
        
        consequents.clear();
    }

    public static void link(AbstractTask antecedent, AbstractTask consequent)
    {
        antecedent.addConsequent(consequent);
        consequent.addAntecedent(antecedent);
    }
    
    private synchronized void addConsequent(AbstractTask consequent)
    {
        this.consequents().addIfNotPresent(consequent);
    }

    private synchronized void addAntecedent(AbstractTask antecedent)
    {
        this.antecedents().addIfNotPresent(antecedent);
    }

    public void onAntecedentTerminated(AbstractTask antecedent)
    {
        this.antecedents.removeIfPresent(antecedent);
        if(antecedent.getStatus().didEndWithoutCompleting())
        {
            switch(this.status)
            {
            case ACTIVE:
            case NEW:
            case READY:
            case WAITING:
                this.cancel();
                break;
            default:
                break;
            }
        }
        else if(this.antecedents.isEmpty())
        {
            switch(this.status)
            {
            case NEW:
            case WAITING:
                this.setStatus(RequestStatus.READY);
                break;
            default:
                break;
            }
        }
    }

    protected SimpleUnorderedArrayList<AbstractTask> consequents()
    {
        this.deserializeDependenciesIfNeeded();
        return this.consequents;
    }

    protected SimpleUnorderedArrayList<AbstractTask> antecedents()
    {
        this.deserializeDependenciesIfNeeded();
        return this.antecedents;
    }

    protected void deserializeDependenciesIfNeeded()
    {
        if(this.dependencyData != null)
        {
            SimpleUnorderedArrayList<AbstractTask> antecedents = this.antecedents();
            SimpleUnorderedArrayList<AbstractTask> consequents = this.consequents();
            int i = 0;
            while(i < this.dependencyData.length)
            {
                int ant = this.dependencyData[i++];
                int con = this.dependencyData[i++];
                
                if(ant == this.id)
                {
                    AbstractTask conReq = DomainManager.INSTANCE.assignedNumbersAuthority().taskIndex().get(con);
                    if(conReq != null) consequents.add(conReq);
                }
                else if(con == this.id)
                {
                    AbstractTask antReq = DomainManager.INSTANCE.assignedNumbersAuthority().taskIndex().get(ant);
                    if(antReq != null) antecedents.add(antReq);
                }
                
            }
            this.dependencyData = null;
        }
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        this.deserializeID(tag);
        this.status = Useful.safeEnumFromTag(tag, ModNBTTag.REQUEST_STATUS, RequestStatus.NEW);
        this.dependencyData = tag.getIntArray(ModNBTTag.REQUEST_DEPENDENCIES);
        
        // discard depData if empty or not an even number (latter implies corrupt data)
        if(this.dependencyData.length == 0 || (this.dependencyData.length & 0x1) == 0x1) this.dependencyData = null;
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        this.serializeID(tag);
        Useful.saveEnumToTag(tag, ModNBTTag.REQUEST_STATUS, this.status);
        
        SimpleUnorderedArrayList<AbstractTask> antecedents = this.antecedents();
        SimpleUnorderedArrayList<AbstractTask> consequents = this.consequents();
        
        int dependencyCount = antecedents.size() + consequents.size();
        if(dependencyCount > 0)
        {
            int[] depData = new int[dependencyCount * 2];
            int i = 0;
            
            if(!consequents.isEmpty())
            {
                for(AbstractTask r : consequents)
                {
                    depData[i++] = this.getId();
                    depData[i++] = r.getId();
                }
            }
            
            if(!antecedents.isEmpty())
            {
                for(AbstractTask r : antecedents)
                {
                    depData[i++] = r.getId();
                    depData[i++] = this.getId();
                }
            }
            
            tag.setIntArray(ModNBTTag.REQUEST_DEPENDENCIES, depData);
        }
    }
    
    @Override
    public Domain getDomain()
    {
        return this.job.getDomain();
    }
}