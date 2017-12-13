package grondag.hard_science.simulator.storage;

import javax.annotation.Nullable;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.persistence.IDirtKeeper;
import grondag.hard_science.simulator.persistence.IDirtListener;
import grondag.hard_science.simulator.persistence.IDirtNotifier;
import grondag.hard_science.simulator.persistence.NullDirtListener;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
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
public abstract class StorageManager<T extends StorageType<T>> extends BaseStorageManager<T> 
    implements IDirtNotifier, IDirtListener, IReadWriteNBT
{
   
    protected IDirtListener dirtListener = NullDirtListener.INSTANCE;
    public StorageManager(T storageType, Domain domain)
    {
        super(storageType, domain);
        this.dirtListener = domain == null ? NullDirtListener.INSTANCE : domain.getDirtListener();
    }

    /** Convenience factory */
    public static StorageManager<StorageTypeStack> itemStorage(Domain domain)
    {
        return new StorageManager<StorageTypeStack>(StorageType.ITEM, domain) 
        {
            @Override
            protected IStorage<StorageTypeStack> makeStorage(NBTTagCompound nbt)
            {
                return new ItemStorage(nbt);
            }
        };
    }
    
    @Override
    public synchronized void addStore(IStorage<T> store)
    {
        super.addStore(store);

        this.domain.domainManager().assignedNumbersAuthority().register(store);
        
        //TODO: add for sub networks
        
        store.setOwner(this);
        this.setDirty();
    }
    
    @Override
    public synchronized void removeStore(IStorage<T> store)
    {
        super.removeStore(store);
        
        //TODO: remove for sub networks
        
        store.setOwner(null);
        this.domain.domainManager().assignedNumbersAuthority().unregister(store);
        this.setDirty();
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
    @Override
    public synchronized void notifyTaken(IStorage<T> storage, IResource<T> resource, long taken, @Nullable IProcurementRequest<T> request)
    {
        if(taken == 0) return;
        
        super.notifyTaken(storage, resource, taken, request);
        
        this.setDirty();
        
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
    
        this.setDirty();
        
        //TODO: pass to subnetworks WITHOUT the request
        //because storage allocation happens separately
    }

    @Override
    public synchronized void notifyCapacityChanged(long delta)
    {
        if(delta == 0) return;

        super.notifyCapacityChanged(delta);
        
        this.setDirty();
        
        //TODO: pass to subnetworks
    }
}
