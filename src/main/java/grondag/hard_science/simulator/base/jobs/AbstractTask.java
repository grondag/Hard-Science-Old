package grondag.hard_science.simulator.base.jobs;

import javax.annotation.Nonnull;

import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.simulator.base.AssignedNumber;
import grondag.hard_science.simulator.base.DomainManager;
import grondag.hard_science.simulator.base.IIdentified;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractTask implements IReadWriteNBT, IIdentified
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

    public void setJob(@Nonnull Job job)
    {
        this.job = job;
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

    public RequestStatus getStatus()
    {
        return this.status;
    }

    protected void setStatus(RequestStatus newStatus)
    {
        if(newStatus != this.status)
        {
            RequestStatus oldStatus = this.status;
            this.status = newStatus;
            if(newStatus.isTerminated)this.notifyConsequents();
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

    protected synchronized void notifyConsequents()
    {
        SimpleUnorderedArrayList<AbstractTask> consequents = this.consequents();
        
        if(consequents.isEmpty()) return;
        
        for(AbstractTask r : consequents)
        {
            r.onAntecedentTerminated(this);;
        }
    }

    public synchronized void addConsequent(AbstractTask consequent)
    {
        this.consequents().addIfNotPresent(consequent);
    }

    public synchronized void addAntecedent(AbstractTask antecedent)
    {
        this.antecedents().addIfNotPresent(antecedent);
    }

    public synchronized boolean areAllAntecedentsMet()
    {
        SimpleUnorderedArrayList<AbstractTask> antecedents = this.antecedents();
        
        if(antecedents.isEmpty()) return true;
        for(AbstractTask r : antecedents)
        {
            if(!r.getStatus().isComplete()) return false;
        }
        return true;
    }

    public void onAntecedentTerminated(AbstractTask antecedent)
    {
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
        else if(this.areAllAntecedentsMet())
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
                    AbstractTask conReq = DomainManager.INSTANCE.taskIndex().get(con);
                    if(conReq != null) consequents.add(conReq);
                }
                else if(con == this.id)
                {
                    AbstractTask antReq = DomainManager.INSTANCE.taskIndex().get(ant);
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
}