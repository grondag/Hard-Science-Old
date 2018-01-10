package grondag.hard_science.simulator.storage;

import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

/**
 * All input and output for an IStorage must happen on the
 * service thread for the storage type given by {@link #storageType()}.<p>
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
}