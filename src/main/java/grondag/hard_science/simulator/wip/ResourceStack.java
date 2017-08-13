package grondag.hard_science.simulator.wip;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;

public class ResourceStack<V extends StorageType> implements IResourceStack<V>
{
    protected final IResource<V> resource;
    protected long quantity;
    
    public static <T extends StorageType> ResourceStack<T> create(IResource<T> resource, long quantity)
    {
        return new ResourceStack<T>(resource, quantity);
    }
    
    public ResourceStack(@Nonnull IResource<V> resource, long quantity)
    {
        this.resource = resource;
        this.quantity = quantity;
    }
    
    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound tag = this.resource.serializeNBT();
        tag.setLong("qty", this.quantity);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        this.quantity = nbt.getLong("qty");
        this.resource.deserializeNBT(nbt);
    }

    @Override
    public IResource<V> resource()
    {
        return this.resource;
    }

    @Override
    public long getQuantity()
    {
        return this.quantity;
    }

    @Override
    public boolean isEmpty()
    {
        return this.quantity == 0;
    }

    @Override
    public synchronized long takeUpTo(long limit)
    {
        if(limit < 1) return 0;
        
        long taken = Math.min(this.quantity, limit);
        this.quantity -= taken;
        return taken;
    }

    @Override
    public synchronized long add(long howMany)
    {
        if(howMany < 1) return this.quantity;
        
        this.quantity += howMany;
        
        return this.quantity;
    }

}
