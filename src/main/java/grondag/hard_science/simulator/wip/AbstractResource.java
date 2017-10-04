package grondag.hard_science.simulator.wip;

import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractResource<V extends StorageType<V>> implements IResource<V>
{
    public AbstractResource(NBTTagCompound nbt)
    {
        if(nbt != null) this.deserializeNBT(nbt);
    }

    protected AbstractResource() {};
    
    @Override
    public int hashCode()
    {
        return this.computeResourceHashCode();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object other)
    {
        boolean result = false;
        try
        {
            result = this.isResourceEqual((IResource<V>) other);
        } finally {}
       
        return result;
    }
}
