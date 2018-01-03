package grondag.hard_science.simulator.storage;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.magicwerk.brownies.collections.Key2List;
import org.magicwerk.brownies.collections.function.IFunction;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.domain.IDomainMember;
import grondag.hard_science.simulator.resource.AbstractResourceDelegate;
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
    implements ITypedStorage<T>, IDomainMember, ISizedContainer, IListenableStorage<T>
{
    protected final HashSet<IStorage<T>> stores = new HashSet<IStorage<T>>();
    protected Domain domain;
    protected final T storageType;
    
    protected SimpleUnorderedArrayList<IStorageListener<T>> listeners 
        = new SimpleUnorderedArrayList<IStorageListener<T>>();
    
    protected final static IFunction<StorageResourceManager<?>, Integer> RESOURCE_HANDLE_MAPPER
    = new IFunction<StorageResourceManager<?>, Integer>() {
       @Override
       public Integer apply(StorageResourceManager<?> elem)
       {
           return elem.resource().handle();
       }};
       
    /**
     * All unique resources contained in this domain
     */
    protected Key2List<StorageResourceManager<T>, IResource<T>, Integer> slots 
        = new Key2List.Builder<StorageResourceManager<T>, IResource<T>, Integer>().
              withPrimaryKey1Map(StorageResourceManager::resource).
              withUniqueKey2Map(RESOURCE_HANDLE_MAPPER).
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

    public List<StorageWithQuantity<T>> findSpaceFor(IResource<T> resource, long quantity, @Nullable IProcurementRequest<T> request)
    {
        ImmutableList.Builder<StorageWithQuantity<T>> builder = ImmutableList.builder();
        
        for(IStorage<T> store : this.stores)
        {
            long space = store.add(resource, quantity, true, request);
            if(space > 0)
            {
                builder.add(store.withQuantity(space));
            }
        }
        
        return builder.build();
    }

    /**
     * Returns all locations where the resource is stored.
     * Note that the resource may be allocated so the stored
     * quantities may not be available for use, but allocations
     * are not stored by location.
     */
    public List<StorageWithQuantity<T>> getLocations(IResource<T> resource)
    {
        StorageResourceManager<T> summary = this.slots.getByKey1(resource);
        
        if(summary == null || summary.quantityStored() == 0) return Collections.emptyList();
    
        return summary.getLocations(resource);
    }

    @Override
    public Domain getDomain()
    {
        return this.domain;
    }

    public synchronized void addStore(AbstractStorage<T> store)
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
    
    public synchronized void removeStore(AbstractStorage<T> store)
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
    
    /**
     * Returns a list of stores that have the given resource with quantityIn included.
     */
    public List<StorageWithResourceAndQuantity<T>> findStorageWithQuantity(Predicate<IResource<T>> predicate)
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
            this.cleanupEmptySlots();
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
            this.cleanupEmptySlots();
        }
    }
    
    /**
     * Removes all empty slots if there are no active listeners
     * and {@link #hasEmptySlots} is true.  We do not want to
     * remove empty slots while there are active listeners
     * because resource handles are instance-specific and 
     * we would lose resource handles that may be held by listeners.
     */
    protected synchronized void cleanupEmptySlots()
    {
        if(this.hasEmptySlots && this.listeners.isEmpty())
        {
            this.hasEmptySlots = false;
            
            Iterator<StorageResourceManager<T>> iterator 
                 = this.slots.iterator();
            while(iterator.hasNext())
            {
                if(iterator.next().isEmpty())
                {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public SimpleUnorderedArrayList<IStorageListener<T>> listeners()
    {
        return this.listeners;
    }
    
    @Override
    public synchronized void removeListener(IStorageListener<T> listener)
    {
        IListenableStorage.super.removeListener(listener);
        this.cleanupEmptySlots();
    }
    
    public int getHandleForResource(IResource<T> resource)
    {
        StorageResourceManager<T> summary = this.slots.getByKey1(resource);
        return summary == null ? -1 : summary.resource().handle();
    }

    public IResource<T> getResourceForHandle(int handle)
    {
        StorageResourceManager<T> summary = this.slots.getByKey2(handle);
        return summary == null ? null : summary.resource();
    }
    
    public List<AbstractResourceDelegate<T>> findDelegates(Predicate<IResource<T>> predicate)
    {
        ImmutableList.Builder<AbstractResourceDelegate<T>> builder = ImmutableList.builder();
        
        for(StorageResourceManager<T> summary : this.slots)
        {
            if(predicate.test(summary.resource()))
            {
                builder.add(this.storageType.createDelegate(summary.resource(), 
                        summary.resource().handle(), summary.quantityStored()));                                        
            }
        }
        
        return builder.build();
    }
}