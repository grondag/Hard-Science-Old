package grondag.hard_science.simulator.storage;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ITypedStorage;
import grondag.hard_science.simulator.resource.StorageType;

public interface IStorage<T extends StorageType<T>>
    extends ISizedContainer, ITypedStorage<T>
{
    long getQuantityStored(IResource<T> resource);
    
    default boolean isResourceAllowed(IResource<T> resource) { return true; }
    
    /**
     * Override if this storage can hold only certain resources.
     */
    default long availableCapacityFor(IResource<T> resource)
    {
        return this.availableCapacity();
    }
    
     /**
     * Increases quantityStored and returns quantityStored actually added.
     * If simulate==true, will return forecasted result without making changes.
     * Intended to be thread-safe. <p>
     * 
     * If request is non-null and not simulated, the amount added will be
     * allocated (domain-wide) to the given request.
     */
    long add(IResource<T> resource, long howMany, boolean simulate, @Nullable IProcurementRequest<T> request);

    /** Alternative syntax for {@link #add(IResource, long, boolean)} */
    default long add(AbstractResourceWithQuantity<T> resourceWithQuantity, boolean simulate, @Nullable IProcurementRequest<T> request)
    {
        return this.add(resourceWithQuantity.resource(), resourceWithQuantity.getQuantity(), simulate, request);
    }
    
    /**
     * Takes up to limit from this stack and returns how many were actually taken.
     * If simulate==true, will return forecasted result without making changes.
     * Intended to be thread-safe. <p>
     * 
     * If request is non-null and not simulated, the amount taken will reduce any
     * allocation (domain-wide) to the given request.
     */
    long takeUpTo(IResource<T> resource, long limit, boolean simulate, @Nullable IProcurementRequest<T> request);

    /**
     * Returned resource stacks are disconnected from this collection.
     * Changing them will have no effect on storage contents.
     */
    List<AbstractResourceWithQuantity<T>> find(Predicate<IResource<T>> predicate);
   
    public default StorageWithQuantity<T> withQuantity(long quantity)
    {
        return new StorageWithQuantity<T>(this, quantity);
    }
    
    public default StorageWithResourceAndQuantity<T> withResourceAndQuantity(IResource<T> resource, long quantity)
    {
        return new StorageWithResourceAndQuantity<T>(this, resource, quantity);
    }
 
}