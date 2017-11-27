package grondag.hard_science.simulator.base;

/**
 * Simple data class for returning store-related inquiry results.
 */
public class StorageWithResourceAndQuantity<T extends StorageType<T>> extends StorageWithQuantity<T>
{
    public final IResource<T> resource;
    
    public StorageWithResourceAndQuantity(IStorage<T> storage, IResource<T> resource, long quantity)
    {
        super(storage, quantity);
        this.resource = resource;
    }
}