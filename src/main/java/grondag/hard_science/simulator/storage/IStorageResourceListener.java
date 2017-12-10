package grondag.hard_science.simulator.storage;

import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

/**
 * For classes that need to monitor the available quantity of 
 * a discrete resource within a domain storage manager.  
 */
public interface IStorageResourceListener<T extends StorageType<T>>
{
    /**
     * Called whenever current available quantity changes.
     * @param resource  Identifies the resource being monitored
     * @param availableDelta  Current available (unallocated) resource quantity.
     */
    public void onAvailabilityChange
    (
        IResource<T> resource,
        long availableQuantity
    );
}
