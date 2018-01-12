package grondag.hard_science.simulator.storage;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.machines.support.ThroughputRegulator;
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

    default boolean isResourceAllowed(@Nonnull IResource<T> resource)
    {
        return this.getContentPredicate() == null || this.getContentPredicate().test(resource);
    }

    /**
     * If this is a single-resource container, the resource it 
     * currently contains or is configured to contain.<p>
     * 
     * Null for multi-resource containers, or if the container is
     * empty and has no configured resource.
     */
    @Nullable
    default IResource<T> resource() { return null; }
    
    default long availableCapacityFor(@Nonnull IResource<T> resource)
    {
        return this.isResourceAllowed(resource) ? this.availableCapacity() : 0;
    }
    
    /**
     * Governs synchronization and thread access restrictions for this container.
     */
    ContainerUsage containerUsage();
    
    /**
     * Implemented for some container types to collect stats
     * and/or limit rate of input/output.
     */
    ThroughputRegulator getRegulator();
    
    /**
     * Used by some container types to collect stats
     * and/or limit rate of input/output.
     */
    void setRegulator(ThroughputRegulator regulator);
    
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
    
    /**
     * Setting to a non-null value will configure this
     * container to accept only resources that match the given predicate.  
     * Setting to null will remove this constraint.
     * Container must be empty when set to a non-null value.
     */
    public void setContentPredicate(Predicate<IResource<T>> predicate);
    
    /**
     * See {@link #setContentPredicate(Predicate)}
     */
    public Predicate<IResource<T>> getContentPredicate();
    
    /**
     * Presents a list of all unique resources in this container, with quantities.
     * List will not be modifiable.  Useful for implementing IItemHandler.
     */
    public List<AbstractResourceWithQuantity<T>> slots();
}