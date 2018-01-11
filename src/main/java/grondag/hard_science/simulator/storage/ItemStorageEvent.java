package grondag.hard_science.simulator.storage;

import javax.annotation.Nullable;

import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.StorageEvent.CapacityChange;
import grondag.hard_science.simulator.storage.StorageEvent.ResourceUpdate;
import grondag.hard_science.simulator.storage.StorageEvent.StorageNotification;

public class ItemStorageEvent implements IStorageEventFactory<StorageTypeStack>
{
    public static final ItemStorageEvent INSTANCE = new ItemStorageEvent();
    
    public void postBeforeStorageDisconnect(IResourceContainer<StorageTypeStack> storage)
    {
        if(storage.getDomain() == null) return;
        storage.getDomain().eventBus.post(new BeforeItemStorageDisconnect(storage));
    }
    
    public void postAfterStorageConnect(IResourceContainer<StorageTypeStack> storage)
    {
        if(storage.getDomain() == null) return;
        storage.getDomain().eventBus.post(new AfterItemStorageConnect(storage));
    }
    
    public void postStoredUpdate(
            IResourceContainer<StorageTypeStack> storage, 
            IResource<StorageTypeStack> resource, 
            long delta,
            @Nullable IProcurementRequest<StorageTypeStack> request)
    {
        if(storage.getDomain() == null) return;
        
            storage.getDomain().eventBus.post(new ItemStoredUpdate(
                    storage,
                    resource,
                    delta,
                    request));
    }
    
    public void postAvailableUpdate(
            IResourceContainer<StorageTypeStack> storage, 
            IResource<StorageTypeStack> resource, 
            long delta,
            @Nullable IProcurementRequest<StorageTypeStack> request)
    {
        if(storage.getDomain() == null) return;
        
            storage.getDomain().eventBus.post(new ItemAvailableUpdate(
                    storage,
                    resource,
                    delta,
                    request));
    }
    
    public void postCapacityChange(IResourceContainer<StorageTypeStack> storage, long delta)
    {
        if(storage.getDomain() == null) return;
        storage.getDomain().eventBus.post(new ItemCapacityChange(storage, delta));
    }
    
    public static class BeforeItemStorageDisconnect extends StorageNotification<StorageTypeStack>
    {
        private BeforeItemStorageDisconnect(IResourceContainer<StorageTypeStack> storage)
        {
            super(storage);
        }
    }
    
    public static class AfterItemStorageConnect extends StorageNotification<StorageTypeStack>
    {
        private AfterItemStorageConnect(IResourceContainer<StorageTypeStack> storage)
        {
            super(storage);
        }
    }
    
    public static class ItemCapacityChange extends CapacityChange<StorageTypeStack>
    {
        private ItemCapacityChange(IResourceContainer<StorageTypeStack> storage, long delta)
        {
            super(storage, delta);
        }
    }
    
    /**
     * Notifies of changes to quantity <em>stored</em>.
     */
    public static class ItemStoredUpdate extends ResourceUpdate<StorageTypeStack>
    {
        private ItemStoredUpdate(
                IResourceContainer<StorageTypeStack> storage, 
                IResource<StorageTypeStack> resource, 
                long delta,
                @Nullable IProcurementRequest<StorageTypeStack> request)
        {
            super(storage, resource, delta, request);
        }
    }
    
    /**
     * Notifies of changes to quantity <em>available</em>.
     */
    public static class ItemAvailableUpdate extends ResourceUpdate<StorageTypeStack>
    {
        private ItemAvailableUpdate(
                IResourceContainer<StorageTypeStack> storage, 
                IResource<StorageTypeStack> resource, 
                long delta,
                @Nullable IProcurementRequest<StorageTypeStack> request)
        {
            super(storage, resource, delta, request);
        }
    }
}
