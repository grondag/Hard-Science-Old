package grondag.hard_science.simulator.storage;

import javax.annotation.Nullable;

import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;

public class StorageEvent
{
    protected static abstract class ResourceUpdate<T extends StorageType<T>>
    {
        public final AbstractStorage<T> storage;
        public final IResource<T> resource;
        public final long delta;
        public final IProcurementRequest<T> request;
        
        protected ResourceUpdate(
                AbstractStorage<T> storage, 
                IResource<T> resource, 
                long delta,
                @Nullable IProcurementRequest<T> request)
        {
            this.storage = storage;
            this.resource = resource;
            this.delta = delta;
            this.request = request;
        }
    }
    
    protected static abstract class StorageNotification<T extends StorageType<T>>
    {
        public final AbstractStorage<T> storage;
        
        protected StorageNotification(AbstractStorage<T> storage)
        {
            this.storage = storage;
        }
    }
    
    protected static abstract class CapacityChange<T extends StorageType<T>>
    {
        public final AbstractStorage<T> storage;
        public final long delta;
        
        protected CapacityChange(AbstractStorage<T> storage, long newCapacity)
        {
            this.storage = storage;
            this.delta = newCapacity;
        }
    }
    
    public static void postBeforeStorageDisconnect(ItemStorage storage)
    {
        if(storage.getDomain() == null) return;
        storage.getDomain().eventBus.post(new BeforeItemStorageDisconnect(storage));
    }
    
    public static void postItemStorageConnect(ItemStorage storage)
    {
        if(storage.getDomain() == null) return;
        storage.getDomain().eventBus.post(new AfterItemStorageConnect(storage));
    }
    
    public static void postItemStoredUpdate(
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
    
    public static void postItemAvailableUpdate(
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
    
    public static void postItemCapacityChange(ItemStorage storage, long delta)
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
