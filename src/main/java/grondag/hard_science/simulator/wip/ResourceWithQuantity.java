package grondag.hard_science.simulator.wip;

import javax.annotation.Nonnull;

import grondag.hard_science.simulator.persistence.IReadWriteNBT;
import grondag.hard_science.simulator.wip.IStorage.StorageWithResourceAndQuantity;
import net.minecraft.nbt.NBTTagCompound;

public class ResourceWithQuantity<V extends StorageType<V>> implements IReadWriteNBT
{
    protected final IResource<V> resource;
    protected long quantity;
     
    public ResourceWithQuantity(@Nonnull IResource<V> resource, long quantity)
    {
        this.resource = resource;
        this.quantity = quantity;
    }
    
    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        this.resource.serializeNBT(tag);
        tag.setLong("qty", this.quantity);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        this.quantity = nbt.getLong("qty");
        this.resource.deserializeNBT(nbt);
    }

    public IResource<V> resource()
    {
        return this.resource;
    }

    public long getQuantity()
    {
        return this.quantity;
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
     * Increases quantity and returns quantity actually added.
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
}
