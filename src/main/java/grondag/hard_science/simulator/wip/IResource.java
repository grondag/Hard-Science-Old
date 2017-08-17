package grondag.hard_science.simulator.wip;

import grondag.hard_science.simulator.persistence.IReadWriteNBT;

/** 
 * A resource is something that can be produced and consumed.
 * Most resources can be stored. (Computation can't.)
 * Resources with a storage type can also have a location.
 * Time is not a resource because it cannot be produced.
 */
public interface IResource<V extends StorageType<V>> extends IReadWriteNBT
{
    public V storageType();
    
    public int computeResourceHashCode();
    public boolean isResourceEqual(IResource<V> other);
    public String displayName();
    
    public default ResourceWithQuantity<V> withQuantity(long quantity)
    {
        return new ResourceWithQuantity<V>(this, quantity);
    }
}
