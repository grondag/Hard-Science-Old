package grondag.hard_science.simulator.storage;

import javax.annotation.Nonnull;

import grondag.hard_science.Log;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

/**
 * All input and output for an IStorage must happen on the
 * service thread for the storage type given by {@link #storageType()}.
 * This restriction does NOT apply if the device is not connected.<p>
 * 
 * This allows resource transfers between storage contains to occur
 * without synchronization or any type of two-phase commit protocol.
 * It means, for example, that a storage with capacity to accept a resource
 * at the start of a transport operation can guarantee that it will accept 
 * delivery. <p>
 *
 * Any in-world or user-initiated operations involving a storage ARE subject
 * to this constraint. Handle these by submitting them as privileged tasks
 * so that they don't wait on simulator tasks that may be in the queue.
 */
public interface IStorage<T extends StorageType<T>> extends IResourceContainer<T>
{
    public default StorageWithQuantity<T> withQuantity(long quantity)
    {
        return new StorageWithQuantity<T>(this, quantity);
    }
    
    public default StorageWithResourceAndQuantity<T> withResourceAndQuantity(IResource<T> resource, long quantity)
    {
        return new StorageWithResourceAndQuantity<T>(this, resource, quantity);
    }
    
    /**
     * Submits privileged execution to service thread and waits.  
     * For use by in-game user interactions.
     * Fulfills constraint that all storage interactions must occur
     * on service thread for that storage type.
     */
    public default long addInteractively(
            @Nonnull IResource<T> resource, 
            long howMany, 
            boolean simulate)
    {
        try
        {
            return this.storageType().service().executor.submit( () ->
            {
                return this.add(resource, howMany, simulate, true, null);
            }, true).get();
        }
        catch (Exception e)
        {
            Log.error("Error in storage interaction", e);
            return 0;
        }
    }
    
    /** Alternate syntax for {@link #addInteractively(IResource, long, boolean)} */
    default long addInteractively(@Nonnull AbstractResourceWithQuantity<T> resourceWithQuantity, boolean simulate)
    {
        return this.addInteractively(resourceWithQuantity.resource(), resourceWithQuantity.getQuantity(), simulate);
    }
}