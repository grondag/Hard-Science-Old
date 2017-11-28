package grondag.hard_science.simulator.storage;

import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import net.minecraft.nbt.NBTTagCompound;
/**
 * Tracks all instances and claims of a single resource within a domain.
 * TODO: replace StorageSummary with this class

 */
public abstract class AbstractResourceManager<T extends StorageType<T>> implements IReadWriteNBT
{
    /**
     * Resource this instance manages.
     */
    public final IResource<T> resource;
    
    /**
     * Total quantityStored of this resoure within the domain.
     */
    private long quantityStored;
    
    /**
     * List of all storage instances in the domain that contain this resource.
     */
    private final SimpleUnorderedArrayList<IStorage<T>> stores = new SimpleUnorderedArrayList<IStorage<T>>();

    public AbstractResourceManager(IResource<T> resource, long quantity, IStorage<T> firstStorage)
    {
        this.resource = resource;
        this.quantityStored = quantity;
        if(firstStorage != null) this.stores.add(firstStorage);
    }
    
    public int quantity()
    {
        return this.quantity();
    }
    
    public synchronized void notifyTaken(IStorage<T> storage, long taken, int claimID)
    {
        if(taken == 0) return;
        
        // update resource qty
        this.quantityStored -= taken;
        if(this.quantityStored < 0)
        {
            this.quantityStored = 0;
            Log.warn("Resource manager encounted negative inventory level.  This is a bug.");
        }
        
        // remove storage from list if no longer holding resource
        if(storage.getQuantityStored(resource) == 0)
        {
            stores.removeIfPresent(storage);
        }
    }

    public synchronized void notifyAdded(IStorage<T> storage, long added, int claimID)
    {
        if(added == 0) return;

        // update resource qty
        this.quantityStored += added;
        
        // track store for this resource
        this.stores.addIfNotPresent(storage);
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        this.resource.deserializeNBT(tag);
        
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        // TODO Auto-generated method stub
        
    }
}
