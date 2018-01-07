package grondag.hard_science.simulator.storage;

import javax.annotation.Nullable;

import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType.StorageTypePower;
import grondag.hard_science.simulator.storage.StorageEvent.CapacityChange;
import grondag.hard_science.simulator.storage.StorageEvent.ResourceUpdate;
import grondag.hard_science.simulator.storage.StorageEvent.StorageNotification;

public class PowerStorageEvent
{
    public static void postBeforeStorageDisconnect(PowerStorage storage)
    {
        if(storage.getDomain() == null) return;
        storage.getDomain().eventBus.post(new BeforePowerStorageDisconnect(storage));
    }
    
    public static void postAfterStorageConnect(PowerStorage storage)
    {
        if(storage.getDomain() == null) return;
        storage.getDomain().eventBus.post(new AfterPowerStorageConnect(storage));
    }
    
    public static void postStoredUpdate(
            PowerStorage storage, 
            IResource<StorageTypePower> resource, 
            long delta,
            @Nullable IProcurementRequest<StorageTypePower> request)
    {
        if(storage.getDomain() == null) return;
        
            storage.getDomain().eventBus.post(new PowerStoredUpdate(
                    storage,
                    resource,
                    delta,
                    request));
    }
    
    public static void postAvailableUpdate(
            PowerStorage storage, 
            IResource<StorageTypePower> resource, 
            long delta,
            @Nullable IProcurementRequest<StorageTypePower> request)
    {
        if(storage.getDomain() == null) return;
        
            storage.getDomain().eventBus.post(new PowerAvailableUpdate(
                    storage,
                    resource,
                    delta,
                    request));
    }
    
    public static void postCapacityChange(PowerStorage storage, long delta)
    {
        if(storage.getDomain() == null) return;
        storage.getDomain().eventBus.post(new PowerCapacityChange(storage, delta));
    }
    
    public static class BeforePowerStorageDisconnect extends StorageNotification<StorageTypePower>
    {
        private BeforePowerStorageDisconnect(AbstractStorage<StorageTypePower> storage)
        {
            super(storage);
        }
    }
    
    public static class AfterPowerStorageConnect extends StorageNotification<StorageTypePower>
    {
        private AfterPowerStorageConnect(AbstractStorage<StorageTypePower> storage)
        {
            super(storage);
        }
    }
    
    public static class PowerCapacityChange extends CapacityChange<StorageTypePower>
    {
        private PowerCapacityChange(AbstractStorage<StorageTypePower> storage, long delta)
        {
            super(storage, delta);
        }
    }
    
    /**
     * Notifies of changes to quantity <em>stored</em>.
     */
    public static class PowerStoredUpdate extends ResourceUpdate<StorageTypePower>
    {
        private PowerStoredUpdate(
                AbstractStorage<StorageTypePower> storage, 
                IResource<StorageTypePower> resource, 
                long delta,
                @Nullable IProcurementRequest<StorageTypePower> request)
        {
            super(storage, resource, delta, request);
        }
    }
    
    /**
     * Notifies of changes to quantity <em>available</em>.
     */
    public static class PowerAvailableUpdate extends ResourceUpdate<StorageTypePower>
    {
        private PowerAvailableUpdate(
                AbstractStorage<StorageTypePower> storage, 
                IResource<StorageTypePower> resource, 
                long delta,
                @Nullable IProcurementRequest<StorageTypePower> request)
        {
            super(storage, resource, delta, request);
        }
    }
}
