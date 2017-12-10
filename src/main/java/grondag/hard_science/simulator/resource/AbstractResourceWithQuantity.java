package grondag.hard_science.simulator.resource;

import javax.annotation.Nonnull;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.storage.IStorage;
import grondag.hard_science.simulator.storage.StorageWithResourceAndQuantity;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractResourceWithQuantity<V extends StorageType<V>> 
implements ITypedStorage<V>, IResourcePredicateWithQuantity<V>
{
    private IResource<V> resource;
    protected long quantity;
     
    @Override
    public IResourcePredicate<V> predicate()
    {
        return this.resource;
    }

    public AbstractResourceWithQuantity(@Nonnull IResource<V> resource, long quantity)
    {
        this.resource = resource;
        this.quantity = quantity;
    }
    
    // needed for IMessage support
    public AbstractResourceWithQuantity()
    {
        this.resource = this.storageType().fromNBT(null);
    }
    
    public AbstractResourceWithQuantity(NBTTagCompound tag)
    {
        this.quantity = tag.getLong(ModNBTTag.RESOURCE_QUANTITY);
        this.resource = this.storageType().fromNBT(tag.getCompoundTag(ModNBTTag.RESOURCE_IDENTITY));
    }
    
    public abstract AbstractResourceDelegate<V> toDelegate();
    
    public NBTTagCompound toNBT()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag(ModNBTTag.RESOURCE_IDENTITY, this.storageType().toNBT(this.resource));
        tag.setLong(ModNBTTag.RESOURCE_QUANTITY, this.quantity);
        return tag;
    }

    public IResource<V> resource()
    {
        return this.resource;
    }

    public long getQuantity()
    {
        return this.quantity;
    }
    
    public void setQuanity(long quantity)
    {
        this.quantity = quantity;
    }
    
    /**
     * returns new value
     */
    @Override
    public long changeQuantity(long delta)
    {
        this.quantity += delta;
        return this.quantity;
    }

    @Override
    public void setQuantity(long quantity)
    {
        this.quantity = quantity;
    }
    
    public boolean isEmpty()
    {
        return this.quantity == 0;
    }

    /**
     * Takes up to limit from this stack and returns how many were actually taken.
     * Intended to be thread-safe.
     */
    public synchronized long takeUpTo(long limit)
    {
        if(limit < 1) return 0;
        
        long taken = Math.min(this.quantity, limit);
        this.quantity -= taken;
        return taken;
    }

    /**
     * Increases quantityStored and returns quantityStored actually added.
     * Intended to be thread-safe.
     */
    public synchronized long add(long howMany)
    {
        if(howMany < 1) return 0;
        
        this.quantity += howMany;
        
        return howMany;
    }

    public StorageWithResourceAndQuantity<V> withStorage(IStorage<V> storage)
    {
        return new StorageWithResourceAndQuantity<V>(storage, this.resource, this.quantity);
    }

    @Override
    public String toString()
    {
        return String.format("%,d x ", this.getQuantity()) + this.resource.toString();
    }
    
    @Override
    public AbstractResourceWithQuantity<V> clone()
    {
        return this.resource.withQuantity(quantity);
    }
    
}
