package grondag.hard_science.simulator.base;

/**
 * Simple data class for returning store-related inquiry results.
 */
public class StorageWithQuantity<T extends StorageType<T>>
{
    public final IStorage<T> storage;
    public final long quantity;
    
    public StorageWithQuantity(IStorage<T> storage, long quantity)
    {
        this.storage = storage;
        this.quantity = quantity;
    }
}