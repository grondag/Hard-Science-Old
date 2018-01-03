package grondag.hard_science.simulator.storage;

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
        
        protected ResourceUpdate(AbstractStorage<T> storage, IResource<T> resource, long delta)
        {
            this.storage = storage;
            this.resource = resource;
            this.delta = delta;
        }
    }
    
    protected static abstract class AfterStorageConnect<T extends StorageType<T>>
    {
        public final AbstractStorage<T> storage;
        
        protected AfterStorageConnect(AbstractStorage<T> storage)
        {
            this.storage = storage;
        }
    }
    
    protected static abstract class BeforeStorageDisconnect<T extends StorageType<T>>
    {
        public final AbstractStorage<T> storage;
        
        protected BeforeStorageDisconnect(AbstractStorage<T> storage)
        {
            this.storage = storage;
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
    
    public static void postItemStoredUpdate(ItemStorage storage, IResource<StorageTypeStack> resource, long delta)
    {
        if(storage.getDomain() == null) return;
        
            storage.getDomain().eventBus.post(new ItemStoredUpdate(
                    storage,
                    resource,
                    delta));
    }
    
    public static void postItemAvailableUpdate(ItemStorage storage, IResource<StorageTypeStack> resource, long delta)
    {
        if(storage.getDomain() == null) return;
        
            storage.getDomain().eventBus.post(new ItemAvailableUpdate(
                    storage,
                    resource,
                    delta));
    }
    public static class BeforeItemStorageDisconnect extends BeforeStorageDisconnect<StorageTypeStack>
    {
        private BeforeItemStorageDisconnect(AbstractStorage<StorageTypeStack> storage)
        {
            super(storage);
        }
    }
    
    public static class AfterItemStorageConnect extends AfterStorageConnect<StorageTypeStack>
    {
        private AfterItemStorageConnect(AbstractStorage<StorageTypeStack> storage)
        {
            super(storage);
        }
    }
    
    /**
     * Notifies of changes to quantity <em>stored</em>.
     */
    public static class ItemStoredUpdate extends ResourceUpdate<StorageTypeStack>
    {
        private ItemStoredUpdate(AbstractStorage<StorageTypeStack> storage, IResource<StorageTypeStack> resource, long delta)
        {
            super(storage, resource, delta);
        }
    }
    
    /**
     * Notifies of changes to quantity <em>available</em>.
     */
    public static class ItemAvailableUpdate extends ResourceUpdate<StorageTypeStack>
    {
        private ItemAvailableUpdate(AbstractStorage<StorageTypeStack> storage, IResource<StorageTypeStack> resource, long delta)
        {
            super(storage, resource, delta);
        }
    }
}
