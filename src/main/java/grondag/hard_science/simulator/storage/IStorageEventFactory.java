package grondag.hard_science.simulator.storage;

import javax.annotation.Nullable;

import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

public interface IStorageEventFactory<T extends StorageType<T>>
{
    public void postBeforeStorageDisconnect(IStorage<T> storage);
    
    public void postAfterStorageConnect(IStorage<T> storage);
    
    public void postStoredUpdate(
            IStorage<T> storage, 
            IResource<T> resource, 
            long delta,
            @Nullable IProcurementRequest<T> request);
    
    public void postAvailableUpdate(
            IStorage<T> storage, 
            IResource<T> resource, 
            long delta,
            @Nullable IProcurementRequest<T> request);
    
    public void postCapacityChange(IStorage<T> storage, long delta);
}
