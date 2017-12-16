package grondag.hard_science.simulator.storage;

import javax.annotation.Nullable;

import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

/**
* Responsibilities: <br>
* + Tracking the location of all resources for a storage type within a domain.<br>
* + Tracking all empty storage for a storage type within a domain. <br>
* + Storing and retrieving items.
* + Answering inquiries about storage of a given type based on tracking. <br>
* + Notifies listeners when total storage changes
*<br>
*Not responsible for optimizing storage.
*/
public class StorageManager<T extends StorageType<T>> extends BaseStorageManager<T> 
{
    public StorageManager(T storageType, Domain domain)
    {
        super(storageType, domain);
    }
    
    @Override
    public synchronized void addStore(IStorage<T> store)
    {
        super.addStore(store);
        
        //TODO: add for sub networks
        
    }
    
    @Override
    public synchronized void removeStore(IStorage<T> store)
    {
        super.removeStore(store);
        
        //TODO: remove for sub networks
    }
    
    /**
     * Called by storage instances, or by self when a storage is removed.
     * If request is non-null, then the amount taken reduces any allocation to that request.
     */
    @Override
    public synchronized void notifyTaken(IStorage<T> storage, IResource<T> resource, long taken, @Nullable IProcurementRequest<T> request)
    {
        if(taken == 0) return;
        
        super.notifyTaken(storage, resource, taken, request);
        
        //TODO: pass to subnetworks WITHOUT the request
        //because storage allocation happens separately
    }

    /**
     * If request is non-null, then the amount added is immediately allocated to that request.
     */
    @Override
    public synchronized void notifyAdded(IStorage<T> storage, IResource<T> resource, long added, @Nullable IProcurementRequest<T> request)
    {
        if(added == 0) return;

        super.notifyAdded(storage, resource, added, request);
    
        //TODO: pass to subnetworks WITHOUT the request
        //because storage allocation happens separately
    }

    @Override
    public synchronized void notifyCapacityChanged(long delta)
    {
        if(delta == 0) return;

        super.notifyCapacityChanged(delta);
        
        //TODO: pass to subnetworks
    }
}
