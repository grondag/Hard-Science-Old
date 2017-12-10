package grondag.hard_science.simulator.demand;

import java.util.Collection;
import grondag.hard_science.simulator.resource.StorageType;
/**
 * Manages allocation and production for one or more resources.
 * Receives resource requests and then finds the material in 
 * storage or makes the demand visible to fabrication services. <P>
 * 
 * Tracks how much of which demands are currently WIP in fabrication
 * so that fabrication does not over-produce.<P>
 * 
 * Also chooses which resource to supply for demands that can 
 * accept alternative resources. <P>
 * 
 * When demand is available in inventory, notifies resource request
 * to attempt to allocate resources, but continues to track all 
 * requests until the request is met.
 */
public interface IBroker<V extends StorageType<V>>
{
    /**
     * Starts tracking the given request if was not already tracked.
     */
    public void registerRequest(IProcurementRequest<V> request);
    
    /**
     * Stops tracking the given request if was being tracked.
     * Called by requests when they cancelled or all demand
     * is allocated or WIP.<p>
     * 
     * If being called due to cancellation, does NOT unallocate. 
     * Task is responsible for unclaiming any allocated resources.
     */
    public void unregisterRequest(IProcurementRequest<V> request);
    
    /**
     * Called by request if priority changes but should still be 
     * tracked by this broker.  
     */
    public void notifyPriorityChange(IProcurementRequest<V> request);
    
    /**
     * Called by request if new demands are added, perhaps because
     * WIP was cancelled. Signals broker to wake up any producers
     * that have given up on existing demands.
     */
    public void notifyNewDemand(IProcurementRequest<V> request);
    
    /**
     * Returns a collection of all requests registered with this broker
     * that have open demands, ordered by priority (highest first)
     * and seniority (earliest first).
     */
    public Collection<IProcurementRequest<V>> openRequests();
    
    /**
     * Informs broker that producer should be notified when there are
     * new demands for this broker.
     */
    public void registerProducer(IProducer<V> producer);
    
    /**
     * Informs broker that producer should no longer be notified when 
     * there are new demands for this broker.
     */
    public void unregisterProducer(IProducer<V> producer);
}
