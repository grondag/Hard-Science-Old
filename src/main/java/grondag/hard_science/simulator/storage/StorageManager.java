package grondag.hard_science.simulator.storage;

import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.magicwerk.brownies.collections.Key1List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.domain.IDomainMember;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ITypedStorage;
import grondag.hard_science.simulator.resource.StorageType;

/**
* Responsibilities:
* <li>Tracking the location of all resources for a storage type within a domain.
* <li>Tracking all empty storage for a storage type within a domain.
* <li>Storing and retrieving items.
* <li>Answering inquiries about storage of a given type based on tracking.
* <li>Notifies listeners when total storage changes</li><p>
*
* Not responsible for optimizing storage.
*/
public class StorageManager<T extends StorageType<T>> 
    implements ITypedStorage<T>, IDomainMember, ISizedContainer, IStorageAccess<T>
{
    protected final HashSet<IStorage<T>> stores = new HashSet<IStorage<T>>();
    
    protected Domain domain;
    protected final T storageType;
       
    /**
     * All unique resources contained in this domain
     */
    protected Key1List<StorageResourceManager<T>, IResource<T>> slots 
        = new Key1List.Builder<StorageResourceManager<T>, IResource<T>>().
              withPrimaryKey1Map(StorageResourceManager::resource).
              build();
    
    protected long capacity = 0;
    protected long used = 0;

    /**
     * Set to true whenever an existing slot becomes empty.  Set false when 
     * {@link #cleanupEmptySlots()} runs without any active listeners.
     */
    protected boolean hasEmptySlots = false;
    
    public StorageManager(T storageType, Domain domain)
    {
        super();
        this.storageType = storageType;
        this.domain = domain;
    }

    @Override
    public T storageType()
    {
        return this.storageType;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation in storage manager exploits the StorageResourceManager objects
     * to provide better performance in large storage networks. 
     */
    @Override
    public ImmutableList<IStorage<T>> getLocations(IResource<T> resource)
    {
        StorageResourceManager<T> summary = this.slots.getByKey1(resource);
        
        if(summary == null || summary.quantityStored() == 0) return ImmutableList.of();
    
        return summary.getLocations(resource).stream()
                .sorted((StorageWithQuantity<T> a, StorageWithQuantity<T>b) 
                        -> Long.compare(a.quantity, b.quantity))
                .map(p -> p.storage)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public Domain getDomain()
    {
        return this.domain;
    }

    protected synchronized void addStore(IStorage<T> store)
    {
        assert !stores.contains(store)
            : "Storage manager received request to add store it already has.";
        
        this.stores.add(store);
        this.capacity += store.getCapacity();
        
        for(AbstractResourceWithQuantity<T> stack : store.find(this.storageType.MATCH_ANY))
        {
            this.notifyAdded(store, stack.resource(), stack.getQuantity(), null);
        }
    }
    
    public synchronized void removeStore(IStorage<T> store)
    {
        assert stores.contains(store)
         : "Storage manager received request to remove store it doesn't have.";
        
        for(AbstractResourceWithQuantity<T> stack : store.find(this.storageType.MATCH_ANY))
        {
            this.notifyTaken(store, stack.resource(), stack.getQuantity(), null);
        }
        
        this.stores.remove(store);
        this.capacity -= store.getCapacity();
    }
    
    public long getQuantityStored(IResource<T> resource)
    {
        StorageResourceManager<T> stored = this.slots.getByKey1(resource);
        return stored == null ? 0 : stored.quantityStored();
    }
    
    public long getQuantityAvailable(IResource<T> resource)
    {
        StorageResourceManager<T> stored = this.slots.getByKey1(resource);
        return stored == null ? 0 : stored.quantityAvailable();
    }
    
    public long getQuantityAllocated(IResource<T> resource)
    {
        StorageResourceManager<T> stored = this.slots.getByKey1(resource);
        return stored == null ? 0 : stored.quantityAllocated();
    }
    
    public List<AbstractResourceWithQuantity<T>> findQuantityAvailable(Predicate<IResource<T>> predicate)
    {
        ImmutableList.Builder<AbstractResourceWithQuantity<T>> builder = ImmutableList.builder();
        
        for(StorageResourceManager<T> entry : this.slots)
        {
            if(predicate.test(entry.resource))
            {
                builder.add(entry.resource.withQuantity(entry.quantityAvailable()));
            }
        }
        
        return builder.build();
    }
    
    public List<AbstractResourceWithQuantity<T>> findQuantityStored(Predicate<IResource<T>> predicate)
    {
        ImmutableList.Builder<AbstractResourceWithQuantity<T>> builder = ImmutableList.builder();
        
        for(StorageResourceManager<T> entry : this.slots)
        {
            if(predicate.test(entry.resource))
            {
                builder.add(entry.resource.withQuantity(entry.quantityStored()));
            }
        }
        
        return builder.build();
    }
    
    /**
     * Returns a list of stores that have the given resource with quantityIn included.
     * Currently used only for testing.
     */
    public ImmutableList<StorageWithResourceAndQuantity<T>> findStorageWithQuantity(Predicate<IResource<T>> predicate)
    {
        ImmutableList.Builder<StorageWithResourceAndQuantity<T>> builder = ImmutableList.builder();
        
        for(StorageResourceManager<T> entry : this.slots)
        {
            if(predicate.test(entry.resource))
            {
                entry.addStoragesWithQuantityToBuilder(builder);
            }
        }
        
        return builder.build();
    }
    
    /**
     * Read-only snapshot of all stores in the domain.
     */
    @Override
    public ImmutableList<IStorage<T>> stores()
    {
        return ImmutableList.copyOf(this.stores);
    }
    
    @Override
    public long getCapacity()
    {
        return capacity;
    }

    @Override
    public long usedCapacity()
    {
        return this.used;
    }
    
    /**
     * Called by storage instances, or by self when a storage is removed.
     * If request is non-null, then the amount taken reduces any allocation to that request.
     */
    //FIXME
    public synchronized void notifyTaken(IStorage<T> storage, IResource<T> resource, long taken, @Nullable IProcurementRequest<T> request)
    {
        if(taken == 0) return;
        
        StorageResourceManager<T> summary = this.slots.getByKey1(resource);
        
        assert summary != null
            : "Storage manager encounted missing resource on resource removal.";
        
        if(summary == null) return;
        
        summary.notifyTaken(storage, taken, request);
        
        // update overall qty
        this.used -= taken;
        
        assert used >= 0
                : "Storage manager encounted negative inventory level.";
        
        if(this.used < 0) used = 0;
        
        if(summary.isEmpty()) 
        {
            this.hasEmptySlots = true;
        }
    }

    /**
     * If request is non-null, then the amount added is immediately allocated to that request.
     */
    public synchronized void notifyAdded(IStorage<T> storage, IResource<T> resource, long added, @Nullable IProcurementRequest<T> request)
    {
        if(added == 0) return;

        StorageResourceManager<T> summary = this.slots.getByKey1(resource);
        if(summary == null)
        {
            summary = new StorageResourceManager<T>(resource, storage, added, request);
            this.slots.add(summary);
        }
        else
        {
            summary.notifyAdded(storage, added, request);
        }
        
        // update total quantityStored
        this.used += added;
        
        assert this.used <= this.capacity
            : "Storage manager usage greater than total storage capacity.";
    }
    
    public synchronized void notifyCapacityChanged(long delta)
    {
        if(delta == 0) return;
        
        this.capacity += delta;
        
        assert this.capacity >= 0
            : "Storage manager encounted negative capacity level.";
    }
    
    /**
     * Sets allocation for the given request to the provided, non-negative value.
     * Returns allocation that was actually set.  Will not set negative allocations
     * and will not set allocation so that total allocated is more the total available.
     * Provides no notification to the request.
     */
    public long setAllocation(
            @Nonnull IResource<T> resource, 
            @Nonnull IProcurementRequest<T> request, 
            long requestedAllocation)
    {
        assert requestedAllocation >= 0
                : "AbstractStorageManager.setAllocation got negative allocation request.";
        
        StorageResourceManager<T> stored = this.slots.getByKey1(resource);
        return stored == null ? 0 : stored.setAllocation(request, requestedAllocation);    
    }
    
    /**
     * Adds delta to the allocation of this resource for the given request.
     * Return value is the quantityIn removed or added, which could be different than
     * amount requested if not enough is available or would reduce allocation below 0.
     * Total quantityIn allocated can be different from return value if request already had an allocation.
     * Provides no notification to the request.
     */
    public long changeAllocation(
            @Nonnull IResource<T> resource,
            long quantityRequested, 
            @Nonnull IProcurementRequest<T> request)
    {        
        StorageResourceManager<T> stored = this.slots.getByKey1(resource);
        return stored == null ? 0 : stored.changeAllocation(request, quantityRequested);    
    }
    
    public synchronized void registerResourceListener(IResource<T> resource, IStorageResourceListener<T> listener)
    {
        StorageResourceManager<T> summary = this.slots.getByKey1(resource);
        if(summary == null)
        {
            summary = new StorageResourceManager<T>(resource, null, 0, null);
            this.slots.add(summary);
        }
        summary.registerResourceListener(listener);
    }
    
    public synchronized void unregisterResourceListener(IResource<T> resource, IStorageResourceListener<T> listener)
    {
        StorageResourceManager<T> summary = this.slots.getByKey1(resource);
        if(summary != null)
        {
            summary.unregisterResourceListener(listener);
        }
    }

}