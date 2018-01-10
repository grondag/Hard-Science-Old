package grondag.hard_science.simulator.storage;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.device.IDeviceComponent;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ITypedStorage;
import grondag.hard_science.simulator.resource.StorageType;

public interface IResourceContainer<T extends StorageType<T>> 
    extends ISizedContainer, ITypedStorage<T>, IDeviceComponent, IReadWriteNBT
{

    /**
     * Returned resource stacks are disconnected from this collection.
     * Changing them will have no effect on storage contents.
     */
    List<AbstractResourceWithQuantity<T>> find(@Nonnull Predicate<IResource<T>> predicate);
    
    long getQuantityStored(@Nonnull IResource<T> resource);

    boolean isResourceAllowed(@Nonnull IResource<T> resource);

    default long availableCapacityFor(@Nonnull IResource<T> resource)
    {
        return this.isResourceAllowed(resource) ? this.availableCapacity() : 0;
    }
    
    /**
     * Increases quantityStored and returns quantityStored actually added.
     * If simulate==true, will return forecasted result without making changes.
     * Thread safety depends on implementation. <p>
     * 
     * If request is non-null and not simulated, and this container supports
     * allocation, the amount added will be allocated (domain-wide) to the given request.
     */
    public long add(
            @Nonnull IResource<T> resource, 
            long howMany, 
            boolean simulate, 
            boolean allowPartial, 
            @Nullable IProcurementRequest<T> request);

    /**
     * Alternate syntax for {@link #add(IResource, long, boolean, boolean, IProcurementRequest)}
     * that assumes allowPartial == true.
     */
    public default long add(
            @Nonnull IResource<T> resource, 
            long howMany, 
            boolean simulate, 
            @Nullable IProcurementRequest<T> request)
    {
        return this.add(resource, howMany, simulate, true, request);
    }

    /**
     * Alternate syntax for {@link #add(IResource, long, boolean, boolean, IProcurementRequest)}
     * that assumes allowPartial == true and request == null.
     */
    public default long add(
            @Nonnull IResource<T> resource, 
            long howMany, 
            boolean simulate)
    {
        return this.add(resource, howMany, simulate, true, null);
    }
    
    /** Alternate syntax for {@link #add(IResource, long, boolean, IProcurementRequest)} */
    default long add(@Nonnull AbstractResourceWithQuantity<T> resourceWithQuantity, boolean simulate, @Nullable IProcurementRequest<T> request)
    {
        return this.add(resourceWithQuantity.resource(), resourceWithQuantity.getQuantity(), simulate, request);
    }
    
    /**
     * Takes up to limit from this stack and returns how many were actually taken.
     * If simulate==true, will return forecasted result without making changes.
     * Thread-safety depends on implementation. <p>
     * 
     * If request is non-null and not simulated, and this container supports allocation,
     * the amount taken will reduce any allocation (domain-wide) to the given request.
     */
    public long takeUpTo(
            @Nonnull IResource<T> resource, 
            long limit, 
            boolean simulate, 
            boolean allowPartial, 
            @Nullable IProcurementRequest<T> request);
    
    /**
     * Alternate syntax for {@link #takeUpTo(IResource, long, boolean, boolean, IProcurementRequest)}
     * which assumed allowPartial == true.
     */
    public default long takeUpTo(
            @Nonnull IResource<T> resource, 
            long limit, 
            boolean simulate, 
            @Nullable IProcurementRequest<T> request)
    {
        return this.takeUpTo(resource, limit, simulate, true, request);
    }
    
    /**
     * Alternate syntax for {@link #takeUpTo(IResource, long, boolean, boolean, IProcurementRequest)}
     * which assumes allowPartial == true and request == null.
     */
    public default long takeUpTo(
            @Nonnull IResource<T> resource, 
            long limit, 
            boolean simulate)
    {
        return this.takeUpTo(resource, limit, simulate, true, null);
    }

    /**
     * Call from device connect.
     */
    default void onConnect() {}

    /**
     * Call from device disconnect.
     */

    default void onDisconnect() {}

}