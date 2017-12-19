package grondag.hard_science.simulator.storage;

import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

/**
 * For classes that need to monitor the available quantityIn of 
 * a discrete resource within a domain storage manager.  
 */
public interface IStorageResourceListener<T extends StorageType<T>>
{
    /**
     * Called whenever current available quantityIn changes.
     * @param resource  Identifies the resource being monitored
     * @param availableDelta  Current available (unallocated) resource quantityIn.
     */
    public void onAvailabilityChange
    (
        IResource<T> resource,
        long availableQuantity
    );
}
