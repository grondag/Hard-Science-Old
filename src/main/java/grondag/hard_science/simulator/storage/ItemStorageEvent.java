package grondag.hard_science.simulator.storage;

import javax.annotation.Nullable;

import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.StorageEvent.CapacityChange;
import grondag.hard_science.simulator.storage.StorageEvent.ResourceUpdate;
import grondag.hard_science.simulator.storage.StorageEvent.StorageNotification;

public class ItemStorageEvent
{
    public static void postBeforeStorageDisconnect(ItemStorage storage)
    {
        if(storage.getDomain() == null) return;
        storage.getDomain().eventBus.post(new BeforeItemStorageDisconnect(storage));
    }
    
    public static void postAfterStorageConnect(ItemStorage storage)
    {
        if(storage.getDomain() == null) return;
        storage.getDomain().eventBus.post(new AfterItemStorageConnect(storage));
    }
    
    public static void postStoredUpdate(
            ItemStorage storage, 
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
    
    public static void postAvailableUpdate(
            ItemStorage storage, 
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
    
    public static void postCapacityChange(ItemStorage storage, long delta)
    {
        if(storage.getDomain() == null) return;
        storage.getDomain().eventBus.post(new ItemCapacityChange(storage, delta));
    }
    
    public static class BeforeItemStorageDisconnect extends StorageNotification<StorageTypeStack>
    {
        private BeforeItemStorageDisconnect(AbstractStorage<StorageTypeStack> storage)
        {
            super(storage);
        }
    }
    
    public static class AfterItemStorageConnect extends StorageNotification<StorageTypeStack>
    {
        private AfterItemStorageConnect(AbstractStorage<StorageTypeStack> storage)
        {
            super(storage);
        }
    }
    
    public static class ItemCapacityChange extends CapacityChange<StorageTypeStack>
    {
        private ItemCapacityChange(AbstractStorage<StorageTypeStack> storage, long delta)
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
                AbstractStorage<StorageTypeStack> storage, 
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
                AbstractStorage<StorageTypeStack> storage, 
                IResource<StorageTypeStack> resource, 
                long delta,
                @Nullable IProcurementRequest<StorageTypeStack> request)
        {
            super(storage, resource, delta, request);
        }
    }
}
