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
        
        public ResourceUpdate(AbstractStorage<T> storage, IResource<T> resource, long delta)
        {
            this.storage = storage;
            this.resource = resource;
            this.delta = delta;
        }
    }
    
    protected static abstract class AfterStorageConnect<T extends StorageType<T>>
    {
        public final AbstractStorage<T> storage;
        
        public AfterStorageConnect(AbstractStorage<T> storage)
        {
            this.storage = storage;
        }
    }
    
    protected static abstract class BeforeStorageDisconnect<T extends StorageType<T>>
    {
        public final AbstractStorage<T> storage;
        
        public BeforeStorageDisconnect(AbstractStorage<T> storage)
        {
            this.storage = storage;
        }
    }
    
    public static class BeforeItemStorageDisconnect extends BeforeStorageDisconnect<StorageTypeStack>
    {
        public BeforeItemStorageDisconnect(AbstractStorage<StorageTypeStack> storage)
        {
            super(storage);
        }
    }
    
    public static class AfterItemStorageConnect extends AfterStorageConnect<StorageTypeStack>
    {
        public AfterItemStorageConnect(AbstractStorage<StorageTypeStack> storage)
        {
            super(storage);
        }
    }
    
    public static class ItemResourceUpdate extends ResourceUpdate<StorageTypeStack>
    {
        public ItemResourceUpdate(AbstractStorage<StorageTypeStack> storage, IResource<StorageTypeStack> resource, long delta)
        {
            super(storage, resource, delta);
        }
    }
}
