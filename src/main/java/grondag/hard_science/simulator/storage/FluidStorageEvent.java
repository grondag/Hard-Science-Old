package grondag.hard_science.simulator.storage;

import javax.annotation.Nullable;

import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.storage.StorageEvent.CapacityChange;
import grondag.hard_science.simulator.storage.StorageEvent.ResourceUpdate;
import grondag.hard_science.simulator.storage.StorageEvent.StorageNotification;

public class FluidStorageEvent
{
    public static void postBeforeStorageDisconnect(FluidStorage storage)
    {
        if(storage.getDomain() == null) return;
        storage.getDomain().eventBus.post(new BeforeFluidStorageDisconnect(storage));
    }
    
    public static void postAfterStorageConnect(FluidStorage storage)
    {
        if(storage.getDomain() == null) return;
        storage.getDomain().eventBus.post(new AfterFluidStorageConnect(storage));
    }
    
    public static void postStoredUpdate(
            FluidStorage storage, 
            IResource<StorageTypeFluid> resource, 
            long delta,
            @Nullable IProcurementRequest<StorageTypeFluid> request)
    {
        if(storage.getDomain() == null) return;
        
            storage.getDomain().eventBus.post(new FluidStoredUpdate(
                    storage,
                    resource,
                    delta,
                    request));
    }
    
    public static void postAvailableUpdate(
            FluidStorage storage, 
            IResource<StorageTypeFluid> resource, 
            long delta,
            @Nullable IProcurementRequest<StorageTypeFluid> request)
    {
        if(storage.getDomain() == null) return;
        
            storage.getDomain().eventBus.post(new FluidAvailableUpdate(
                    storage,
                    resource,
                    delta,
                    request));
    }
    
    public static void postCapacityChange(FluidStorage storage, long delta)
    {
        if(storage.getDomain() == null) return;
        storage.getDomain().eventBus.post(new FluidCapacityChange(storage, delta));
    }
    
    public static class BeforeFluidStorageDisconnect extends StorageNotification<StorageTypeFluid>
    {
        private BeforeFluidStorageDisconnect(AbstractStorage<StorageTypeFluid> storage)
        {
            super(storage);
        }
    }
    
    public static class AfterFluidStorageConnect extends StorageNotification<StorageTypeFluid>
    {
        private AfterFluidStorageConnect(AbstractStorage<StorageTypeFluid> storage)
        {
            super(storage);
        }
    }
    
    public static class FluidCapacityChange extends CapacityChange<StorageTypeFluid>
    {
        private FluidCapacityChange(AbstractStorage<StorageTypeFluid> storage, long delta)
        {
            super(storage, delta);
        }
    }
    
    /**
     * Notifies of changes to quantity <em>stored</em>.
     */
    public static class FluidStoredUpdate extends ResourceUpdate<StorageTypeFluid>
    {
        private FluidStoredUpdate(
                AbstractStorage<StorageTypeFluid> storage, 
                IResource<StorageTypeFluid> resource, 
                long delta,
                @Nullable IProcurementRequest<StorageTypeFluid> request)
        {
            super(storage, resource, delta, request);
        }
    }
    
    /**
     * Notifies of changes to quantity <em>available</em>.
     */
    public static class FluidAvailableUpdate extends ResourceUpdate<StorageTypeFluid>
    {
        private FluidAvailableUpdate(
                AbstractStorage<StorageTypeFluid> storage, 
                IResource<StorageTypeFluid> resource, 
                long delta,
                @Nullable IProcurementRequest<StorageTypeFluid> request)
        {
            super(storage, resource, delta, request);
        }
    }
}
