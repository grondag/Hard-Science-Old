package grondag.hard_science.simulator.wip;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.Log;
import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.simulator.persistence.IDirtListener;
import grondag.hard_science.simulator.persistence.IDirtListener.IDirtNotifier;
import grondag.hard_science.simulator.persistence.IReadWriteNBT;
import grondag.hard_science.simulator.wip.DomainManager.Domain;
import grondag.hard_science.simulator.wip.DomainManager.IDomainMember;
import grondag.hard_science.simulator.wip.IStorage.StorageWithQuantity;
import grondag.hard_science.simulator.wip.IStorage.StorageWithResourceAndQuantity;
import grondag.hard_science.simulator.wip.StorageType.ITypedStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public abstract class AbstractStorageManager<T extends StorageType<T>> 
    implements IDirtNotifier, IDirtListener, IDomainMember, ISizedContainer, IReadWriteNBT, ITypedStorage<T>
{
    protected final HashMap<IResource<T>, StorageSummary> map = new HashMap<IResource<T>, StorageSummary>();
    protected final HashSet<IStorage<T>> stores = new HashSet<IStorage<T>>();
    protected long capacity = 0;
    protected long used = 0;
    protected IDirtListener dirtListener = NullDirtListener.INSTANCE;
    protected Domain domain;
    protected final T storageType;
    
    public AbstractStorageManager(T storageType)
    {
        this.storageType = storageType;
    }

    private class StorageSummary
    {
        private final IResource<T> resource;
        private long quantity;
        private final SimpleUnorderedArrayList<IStorage<T>> stores = new SimpleUnorderedArrayList<IStorage<T>>();

        private StorageSummary(IResource<T> resource, long quantity, IStorage<T> firstStorage)
        {
            this.resource = resource;
            this.quantity = quantity;
            if(firstStorage != null) this.stores.add(firstStorage);
        }
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
        DomainManager.INSTANCE.STORAGE_INDEX.register(store);
        
        this.capacity += store.getCapacity();
        
        for(AbstractResourceWithQuantity<T> stack : store.find(this.storageType.MATCH_ANY))
        {
            this.notifyAdded(store, stack.resource(), stack.quantity);
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
            this.notifyTaken(store, stack.resource(), stack.quantity);
        }
        store.setOwner(null);
        DomainManager.INSTANCE.STORAGE_INDEX.unregister(store);
        this.stores.remove(store);
        this.capacity -= store.getCapacity();
        this.setDirty();
    }
    
    public long getQuantityStored(IResource<T> resource)
    {
        StorageSummary stored = map.get(resource);
        return stored == null ? 0 : stored.quantity;
    }
    
    public List<AbstractResourceWithQuantity<T>> findQuantity(Predicate<IResource<T>> predicate)
    {
        ImmutableList.Builder<AbstractResourceWithQuantity<T>> builder = ImmutableList.builder();
        
        for(StorageSummary entry : map.values())
        {
            if(predicate.test(entry.resource))
            {
                builder.add(entry.resource.withQuantity(entry.quantity));
            }
        }
        
        return builder.build();
    }
    
    public List<StorageWithResourceAndQuantity<T>> findStorageWithQuantity(Predicate<IResource<T>> predicate)
    {
        ImmutableList.Builder<StorageWithResourceAndQuantity<T>> builder = ImmutableList.builder();
        
        for(StorageSummary entry : map.values())
        {
            if(predicate.test(entry.resource))
            {
                for(int i = entry.stores.size() - 1; i >= 0; i--)
                {
                    IStorage<T> store = entry.stores.get(i);
                    long quantity = store.getQuantityStored(entry.resource);
                    if(quantity > 0)
                    {
                        builder.add(store.withResourceAndQuantity(entry.resource, quantity));
                    }
                }
                
            }
        }
        
        return builder.build();
    }
    
    public List<StorageWithQuantity<T>> findSpaceFor(IResource<T> resource, long quantity)
    {
        ImmutableList.Builder<StorageWithQuantity<T>> builder = ImmutableList.builder();
        
        for(IStorage<T> store : this.stores)
        {
            long space = store.add(resource, quantity, true);
            if(space > 0)
            {
                builder.add(store.withQuantity(space));
            }
        }
        
        return builder.build();
    }
    
    public List<StorageWithQuantity<T>> getLocations(IResource<T> resource)
    {
        StorageSummary summary = map.get(resource);
        
        if(summary == null || summary.stores.isEmpty()) return Collections.emptyList();

        ImmutableList.Builder<StorageWithQuantity<T>> builder = ImmutableList.builder();
        for(int i = summary.stores.size() - 1; i >= 0; i--)
        {
            IStorage<T> store = summary.stores.get(i);
            long quantity = store.getQuantityStored(resource);
            if(quantity > 0)
            {
                builder.add(store.withQuantity(quantity));
            }
        }
        return builder.build();
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
            nbt.setTag("stores", nbtStores);
        }
    }

    protected abstract IStorage<T> makeStorage(NBTTagCompound nbt);
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        this.stores.clear();
        this.capacity = 0;
        this.used = 0;
        NBTTagList nbtStores = nbt.getTagList("stores", 10);
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
    
    protected void setDomain(Domain domain)
    {
        this.domain = domain;
        this.dirtListener = domain == null ? null : domain.getDirtListener();
    }
    
    @Override
    public Domain getDomain()
    {
        return this.domain;
    }

    @Override
    public void setDirtListener(IDirtKeeper listener)
    {
        this.dirtListener = listener;
    }
    
    @Override
    public void setDirty()
    {
        this.dirtListener.setDirty();
    }
    
    public synchronized void notifyTaken(IStorage<T> storage, IResource<T> resource, long taken)
    {
        if(taken == 0) return;
        
        StorageSummary summary = this.map.get(resource);
        if(summary == null)
        {
            Log.warn("Storage manager encounted missing resource on resource removal.  This is a bug.");
            return;
        }
        
        // update resource qty
        summary.quantity -= taken;
        if(summary.quantity < 0)
        {
            summary.quantity = 0;
            Log.warn("Storage manager encounted negative inventory level.  This is a bug.");
        }
        
        // remove storage from list if no longer holding resource
        if(storage.getQuantityStored(resource) == 0)
        {
            summary.stores.removeIfPresent(storage);
        }
        
        // update overall qty
        this.used -= taken;
        if(this.used < 0) 
        {
            used = 0;
            Log.warn("Storage manager encounted negative inventory level.  This is a bug.");
        }
        
        this.setDirty();
    }

    public synchronized void notifyAdded(IStorage<T> storage, IResource<T> resource, long added)
    {
        if(added == 0) return;

        StorageSummary summary = this.map.get(resource);
        if(summary == null)
        {
            summary = new StorageSummary(resource, added, storage);
            this.map.put(resource, summary);
        }
        else
        {
            // update resource qty
            summary.quantity += added;
            
            // track store for this resource
            summary.stores.addIfNotPresent(storage);
        }
        
        // update total quantity
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
}
