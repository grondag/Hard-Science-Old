package grondag.hard_science.simulator.storage;

import javax.annotation.Nullable;

import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType.StorageTypePower;
import grondag.hard_science.simulator.storage.StorageEvent.CapacityChange;
import grondag.hard_science.simulator.storage.StorageEvent.ResourceUpdate;
import grondag.hard_science.simulator.storage.StorageEvent.StorageNotification;

public class PowerStorageEvent implements IStorageEventFactory<StorageTypePower>
{
    public static final PowerStorageEvent INSTANCE = new PowerStorageEvent();
    
    public void postBeforeStorageDisconnect(IResourceContainer<StorageTypePower> storage)
    {
        if(storage.getDomain() == null) return;
        storage.getDomain().eventBus.post(new BeforePowerStorageDisconnect(storage));
    }
    
    public void postAfterStorageConnect(IResourceContainer<StorageTypePower> storage)
    {
        if(storage.getDomain() == null) return;
        storage.getDomain().eventBus.post(new AfterPowerStorageConnect(storage));
    }
    
    public void postStoredUpdate(
            IResourceContainer<StorageTypePower> storage, 
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
    
    public void postAvailableUpdate(
            IResourceContainer<StorageTypePower> storage, 
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
    
    public void postCapacityChange(IResourceContainer<StorageTypePower> storage, long delta)
    {
        if(storage.getDomain() == null) return;
        storage.getDomain().eventBus.post(new PowerCapacityChange(storage, delta));
    }
    
    public static class BeforePowerStorageDisconnect extends StorageNotification<StorageTypePower>
    {
        private BeforePowerStorageDisconnect(IResourceContainer<StorageTypePower> storage)
        {
            super(storage);
        }
    }
    
    public static class AfterPowerStorageConnect extends StorageNotification<StorageTypePower>
    {
        private AfterPowerStorageConnect(IResourceContainer<StorageTypePower> storage)
        {
            super(storage);
        }
    }
    
    public static class PowerCapacityChange extends CapacityChange<StorageTypePower>
    {
        private PowerCapacityChange(IResourceContainer<StorageTypePower> storage, long delta)
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
                IResourceContainer<StorageTypePower> storage, 
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
                IResourceContainer<StorageTypePower> storage, 
                IResource<StorageTypePower> resource, 
                long delta,
                @Nullable IProcurementRequest<StorageTypePower> request)
        {
            super(storage, resource, delta, request);
        }
    }
}
