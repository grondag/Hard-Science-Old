package grondag.hard_science.simulator.demand;

import java.util.List;

import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.IResourcePredicateWithQuantity;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.storage.jobs.ITask;

/**
 * Implement in all resource procurement tasks.  Specifies
 * the methods needed by resource brokers and storage manager
 * to track and fulfill all resource demands.
 */
public interface IProcurementRequest <V extends StorageType<V>> extends ITask
{
    /**
     * List of resources with quantities that would satisfy this request.
     * Should be in order of preference, best to worst.
     */
    public List<IResourcePredicateWithQuantity<V>> allDemands();
    
    /**
     * Same as {@link #allDemands()} but less resources that are allocated or WIP.
     * Should be in order of preference, best to worst.
     */
    public List<IResourcePredicateWithQuantity<V>> openDemands();
    
    /**
     * Same as {@link #allDemands()} but only resources and amounts that are allocated.
     * These are always actual resources, not predicates.
     * Order here has no meaning. 
     */
    public List<AbstractResourceWithQuantity<V>> allocatedDemands();
    
    /**
     * Same as {@link #allDemands()} but only resources and amounts that are WIP.
     * These are always actual resources, not predicates
     * Order here has no meaning. 
     */
    public List<AbstractResourceWithQuantity<V>> wipDemands();
    
    /**
     * Called by a producer when starting to produce a resource for this request.
     * Will cause openDemands to refresh (no longer includes the WIP.)
     * Retains a reference to producer so can notify producer is request is cancelled.
     * Returns a positive, non-zero quantity of WIP successfully claimed.
     * Returns zero if no WIP could be claimed.
     */
    public long startWIP(IResource<V> resource, long startedQuantity, IProducer<V> producer);
    
    /**
     * Called by a producer if it can no longer continue with WIP.
     * Will cause openDemands to refresh (add back the cancelled WIP.)
     */
    public void cancelWIP(IResource<V> resource, long cancelledQuantity, IProducer<V> producer);
    
    /**
     * Called by a producer when WIP is completed and available in inventory.
     * The producer will allocate the resource to the request when it places the resource into storage.
     * The amount provided will reduce WIP and increase allocated demand.
     * If WIP is zero, this request will stop tracking WIP for this producer.
     */
    public void completeWIP(IResource<V> resource, long completedQuantity, IProducer<V> producer);
    
    /**
     * Called by storage system if an allocated resource becomes unavailable.
     * Will cause openDemands to refresh.  Should update broker when this occurs.
     */
    public void breakAllocation(IResource<V> resource, long newAllocation);
    
}
