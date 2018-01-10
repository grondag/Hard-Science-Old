package grondag.hard_science.simulator.storage;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.StorageType;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractResourceContainer<T extends StorageType<T>> implements IResourceContainer<T>
{
    protected final IDevice owner;
    protected long capacity = 2000;
    protected long used = 0;

    public AbstractResourceContainer(IDevice owner)
    {
        this.owner = owner;
    }

    @Override
    public void serializeNBT(NBTTagCompound nbt)
    {
        nbt.setLong(ModNBTTag.STORAGE_CAPACITY, this.capacity);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        this.capacity = nbt.getLong(ModNBTTag.STORAGE_CAPACITY);
    }

    public IDevice device()
    {
        return this.owner;
    }

    @Override
    public long getCapacity()
    {
        return capacity;
    }

    @Override
    public void setCapacity(long capacity)
    {
        this.capacity = capacity;
    }

    @Override
    public long usedCapacity()
    {
        return this.used;
    }

}