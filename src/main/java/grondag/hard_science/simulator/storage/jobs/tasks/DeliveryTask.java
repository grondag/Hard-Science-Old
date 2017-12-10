package grondag.hard_science.simulator.storage.jobs.tasks;

import javax.annotation.Nonnull;

import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.storage.StorageManager;
import grondag.hard_science.simulator.storage.StorageWithQuantity;
import grondag.hard_science.simulator.storage.jobs.AbstractTask;
import grondag.hard_science.simulator.storage.jobs.TaskType;

/**
 * Transports the result of a procurement request to the player
 * TODO: current implementation is a stub.
 *
 */
public class DeliveryTask<V extends StorageType<V>> extends AbstractTask
{
   private int procurementTaskID;
    
    /** 
     * Don't use directly - lazily deserialized.
     */
    private ProcurementTask<V> procurementTask;
    
    /**
     * Use for new instances. Automatically
     * make procurement task dependent on this task.
     */
    public DeliveryTask(@Nonnull ProcurementTask<V> procurementTask)
    {
        super(true);
        this.procurementTaskID = procurementTask.getId();
        this.procurementTask = procurementTask;
        AbstractTask.link(procurementTask, this);
    }
    
    /** Use for deserialization */
    public DeliveryTask()
    {
        super(false);
    }

    @Override
    public TaskType requestType()
    {
        return TaskType.DELIVERY;
    }

    @SuppressWarnings("unchecked")
    public ProcurementTask<V> procurementTask()
    {
        if(this.procurementTask == null)
        {
            this.procurementTask = (ProcurementTask<V>) this.getDomain().domainManager().assignedNumbersAuthority().taskIndex().get(procurementTaskID);
        }
        return this.procurementTask;
    }

    @Override
    public void complete()
    {
        // TODO this is a stub / mock up

        // claim self and make active prevent griping by complete
        this.claim();
        
        super.complete();
        StorageManager<V> sm = this.getDomain().getStorageManager(this.procurementTask.storageType());
        
        for(AbstractResourceWithQuantity<V> rwq : this.procurementTask.allocatedDemands())
        {
            long allocation = rwq.getQuantity();
            
            for(StorageWithQuantity<V> swq : sm.getLocations(rwq.resource()))
            {
                allocation -= swq.storage.takeUpTo(rwq.resource(), allocation, false, this.procurementTask);
                if(allocation == 0) break;
            }
        }
    }
    
    
}
