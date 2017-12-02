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
 * to identify resources involved in a transaction.
 * 
 */
public interface IResource<V extends StorageType<V>>
{
    public V storageType();
    public String displayName();
    public AbstractResourceWithQuantity<V> withQuantity(long quantity);
    /**
     * Transient identifier, assigned at run time to uniquely 
     * identify resources across client/server and as a an
     * efficient hash code.
     */
    public int handle();
}
