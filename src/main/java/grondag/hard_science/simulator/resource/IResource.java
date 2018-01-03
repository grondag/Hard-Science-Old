package grondag.hard_science.simulator.resource;

/** 
 * A resource is something that can be produced and consumed.
 * Most resources can be stored. (Computation can't.)
 * Resources with a storage type can also have a location.
 * Time is not a resource because it cannot be produced.<p>
 * 
 * Instances must be cached or statically declared so
 * that each unique resource has exactly one instance.  This is
 * because IResource instance is used as a key in an IdentityHashMap
 * within the storage manager, and more generally because
 * equality operations on some resources (ItemStacks) are expensive.<p>
 * 
 * Client-side references should use a delegate class with same interface
 * and any client/server communication should use {@link #handle()}
 * to identify resources involved in a transaction.<p>
 * 
 * Implements Predicate interface as equality test for self.
 * 
 */
public interface IResource<V extends StorageType<V>> extends IResourcePredicate<V>
{
    public V storageType();
    public String displayName();
    public AbstractResourceWithQuantity<V> withQuantity(long quantity);
    public int computeResourceHashCode();
    public boolean isResourceEqual(IResource<V> other);
    
    /**
     * Per-session unique identifier for this <em>instance.</em>  
     * Two separate instances of resource with equals() == true
     * will not have the same handle values.<p>
     * 
     * Primary usage is as a client-side identifier that can be reliably
     * transmitted back and forth in lieu of item stacks. Item stacks
     * with large NBT value are not fully transmitted to clients and so 
     * require a different way to reliably identify resource targets for
     * actions initiated on the client.  Item slot number does not work
     * because of potentially concurrent changes to slot layout in storage.<p>
     * 
     * When using handle value as a surrogate key for a resource, must
     * take care to ensure that all comparison of handle values will be 
     * with the same instance from which the handle values were taken.
     * This is best done by grabbing the handle from the resource that 
     * is already used as the key within the collection.  May also be necessary
     * to ensure that referenced handle values are not removed while 
     * any potential message could be received that references the value.
     */
    public int handle();
    
    @Override
    public default boolean test(IResource<V> t)
    {
        return t.equals(this);
    }
    
    @Override
    public default boolean isEqualityPredicate()
    {
        return true;
    }
}
