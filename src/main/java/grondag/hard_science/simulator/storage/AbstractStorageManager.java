package grondag.hard_science.simulator.storage;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.domain.IDomainMember;
import grondag.hard_science.simulator.persistence.IDirtKeeper;
import grondag.hard_science.simulator.persistence.IDirtListener;
import grondag.hard_science.simulator.persistence.IDirtNotifier;
import grondag.hard_science.simulator.persistence.NullDirtListener;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ITypedStorage;
import grondag.hard_science.simulator.resource.StorageType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

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
public abstract class AbstractStorageManager<T extends StorageType<T>> 
    implements IDirtNotifier, IDirtListener, IDomainMember, 
               ISizedContainer, IReadWriteNBT, ITypedStorage<T>
{
    protected final IdentityHashMap<IResource<T>, StorageResourceManager<T>> map = new IdentityHashMap<IResource<T>, StorageResourceManager<T>>();
    protected final HashSet<IStorage<T>> stores = new HashSet<IStorage<T>>();
    protected long capacity = 0;
    protected long used = 0;
    protected IDirtListener dirtListener = NullDirtListener.INSTANCE;
    protected Domain domain;
    protected final T storageType;
    
    public AbstractStorageManager(T storageType, Domain domain)
    {
        this.storageType = storageType;
        this.domain = domain;
        this.dirtListener = domain == null ? NullDirtListener.INSTANCE : domain.getDirtListener();
    }

    @Override
    public T storageType()
    {
        return this.storageType;
    }
    
    public synchronized void addStore(IStorage<T> store)
    {
        if(stores.contains(store))
        {
            Log.warn("Storage manager received request to add store it already has.  This is a bug.");
            return;
        }
        
        this.stores.add(store);
        this.domain.domainManager().assignedNumbersAuthority().storageIndex().register(store);
        
        this.capacity += store.getCapacity();
        
        for(AbstractResourceWithQuantity<T> stack : store.find(this.storageType.MATCH_ANY))
        {
            this.notifyAdded(store, stack.resource(), stack.getQuantity(), null);
        }
        store.setOwner(this);
        this.setDirty();
    }
    
    public synchronized void removeStore(IStorage<T> store)
    {
        if(!stores.contains(store))
        {
            Log.warn("Storage manager received request to remove store it doesn't have.  This is a bug.");
            return;
        }
        
        for(AbstractResourceWithQuantity<T> stack : store.find(this.storageType.MATCH_ANY))
        {
            this.notifyTaken(store, stack.resource(), stack.getQuantity(), null);
        }
        store.setOwner(null);
        this.domain.domainManager().assignedNumbersAuthority().storageIndex().unregister(store);
        this.stores.remove(store);
        this.capacity -= store.getCapacity();
        this.setDirty();
    }
    
    public long getQuantityStored(IResource<T> resource)
    {
        StorageResourceManager<T> stored = map.get(resource);
        return stored == null ? 0 : stored.quantityStored();
    }
    
    public long getQuantityAvailable(IResource<T> resource)
    {
        StorageResourceManager<T> stored = map.get(resource);
        return stored == null ? 0 : stored.quantityAvailable();
    }
    
    public long getQuantityAllocated(IResource<T> resource)
    {
        StorageResourceManager<T> stored = map.get(resource);
        return stored == null ? 0 : stored.quantityAllocated();
    }
    
    public List<AbstractResourceWithQuantity<T>> findQuantityAvailable(Predicate<Object> predicate)
    {
        ImmutableList.Builder<AbstractResourceWithQuantity<T>> builder = ImmutableList.builder();
        
        for(StorageResourceManager<T> entry : map.values())
        {
            if(predicate.test(entry.resource))
            {
                builder.add(entry.resource.withQuantity(entry.quantityAvailable()));
            }
        }
        
        return builder.build();
    }
    
    /**
     * Returns a list of stores that have the given resource with quantity included.
     */
    public List<StorageWithResourceAndQuantity<T>> findStorageWithQuantity(Predicate<Object> predicate)
    {
        ImmutableList.Builder<StorageWithResourceAndQuantity<T>> builder = ImmutableList.builder();
        
        for(StorageResourceManager<T> entry : map.values())
        {
            if(predicate.test(entry.resource))
            {
                entry.addStoragesWithQuantityToBuilder(builder);
            }
        }
        
        return builder.build();
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
        StorageResourceManager<T> summary = map.get(resource);
        
        if(summary == null || summary.quantityStored() == 0) return Collections.emptyList();

        return summary.getLocations(resource);
    }
    
    @Override
    public void serializeNBT(NBTTagCompound nbt)
    {
        if(!this.stores.isEmpty())
        {
            NBTTagList nbtStores = new NBTTagList();
            
            for(IStorage<T> store : stores)
            {
                nbtStores.appendTag(store.serializeNBT());
            }
            nbt.setTag(ModNBTTag.STORAGE_MANAGER_STORES, nbtStores);
        }
    }

    protected abstract IStorage<T> makeStorage(NBTTagCompound nbt);
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        this.stores.clear();
        this.capacity = 0;
        this.used = 0;
        NBTTagList nbtStores = nbt.getTagList(ModNBTTag.STORAGE_MANAGER_STORES, 10);
        if( nbtStores != null && !nbtStores.hasNoTags())
        {
            for (int i = 0; i < nbtStores.tagCount(); ++i)
            {
                NBTTagCompound subTag = nbtStores.getCompoundTagAt(i);
                if(subTag != null)
                {
                    IStorage<T> store = makeStorage(subTag);
                    this.addStore(store);
                }
            }   
        }
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
    
    @Override
    public Domain getDomain()
    {
        return this.domain;
    }

    @Override
    public void setDirtKeeper(IDirtKeeper listener)
    {
        this.dirtListener = listener;
    }
    
    @Override
    public void setDirty()
    {
        this.dirtListener.setDirty();
    }
    
    /**
     * Called by storage instances, or by self when a storage is removed.
     * If request is non-null, then the amount taken reduces any allocation to that request.
     */
    public synchronized void notifyTaken(IStorage<T> storage, IResource<T> resource, long taken, @Nullable IProcurementRequest<T> request)
    {
        if(taken == 0) return;
        
        StorageResourceManager<T> summary = this.map.get(resource);
        
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
            this.map.remove(resource);
        
        this.setDirty();
    }

    /**
     * If request is non-null, then the amount added is immediately allocated to that request.
     */
    public synchronized void notifyAdded(IStorage<T> storage, IResource<T> resource, long added, @Nullable IProcurementRequest<T> request)
    {
        if(added == 0) return;

        StorageResourceManager<T> summary = this.map.get(resource);
        if(summary == null)
        {
            summary = new StorageResourceManager<T>(resource, storage, added, request);
            this.map.put(resource, summary);
        }
        else
        {
            summary.notifyAdded(storage, added, request);
        }
        
        // update total quantityStored
        this.used += added;
        if(this.used > this.capacity) 
        {
            Log.warn("Storage manager usage greater than total storage capacity.  This is a bug.");
        }
        
        this.setDirty();
    }

    public synchronized void notifyCapacityChanged(long delta)
    {
        if(delta == 0) return;
        
        this.capacity += delta;
        if(this.capacity < 0)
        {
            this.capacity = 0;
            Log.warn("Storage manager encounted negative capacity level.  This is a bug.");
        }
        this.setDirty();
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
        
        StorageResourceManager<T> stored = map.get(resource);
        return stored == null ? 0 : stored.setAllocation(request, requestedAllocation);    
    }
    
    /**
     * Adds delta to the allocation of this resource for the given request.
     * Return value is the quantity removed or added, which could be different than
     * amount requested if not enough is available or would reduce allocation below 0.
     * Total quantity allocated can be different from return value if request already had an allocation.
     * Provides no notification to the request.
     */
    public long changeAllocation(
            @Nonnull IResource<T> resource,
            long quantityRequested, 
            @Nonnull IProcurementRequest<T> request)
    {        
        StorageResourceManager<T> stored = map.get(resource);
        return stored == null ? 0 : stored.changeAllocation(request, quantityRequested);    
    }

    public synchronized void registerResourceListener(IResource<T> resource, IStorageResourceListener<T> listener)
    {
        StorageResourceManager<T> summary = this.map.get(resource);
        if(summary == null)
        {
            summary = new StorageResourceManager<T>(resource, null, 0, null);
            this.map.put(resource, summary);
        }
        summary.registerResourceListener(listener);
    }
    
    public synchronized void unregisterResourceListener(IResource<T> resource, IStorageResourceListener<T> listener)
    {
        StorageResourceManager<T> summary = this.map.get(resource);
        if(summary != null)
        {
            summary.unregisterResourceListener(listener);
            if(summary.isEmpty()) 
                this.map.remove(resource);
        }
    }
    
  
}
