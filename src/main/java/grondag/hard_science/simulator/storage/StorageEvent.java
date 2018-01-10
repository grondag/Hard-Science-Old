package grondag.hard_science.simulator.storage;

import javax.annotation.Nullable;

import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

public class StorageEvent
{
    protected static abstract class ResourceUpdate<T extends StorageType<T>>
    {
        public final IStorage<T> storage;
        public final IResource<T> resource;
        public final long delta;
        public final IProcurementRequest<T> request;
        
        protected ResourceUpdate(
                IStorage<T> storage, 
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
        public final IStorage<T> storage;
        
        protected StorageNotification(IStorage<T> storage)
        {
            this.storage = storage;
        }
    }
    
    protected static abstract class CapacityChange<T extends StorageType<T>>
    {
        public final IStorage<T> storage;
        public final long delta;
        
        protected CapacityChange(IStorage<T> storage, long newCapacity)
        {
            this.storage = storage;
            this.delta = newCapacity;
        }
    }
}
