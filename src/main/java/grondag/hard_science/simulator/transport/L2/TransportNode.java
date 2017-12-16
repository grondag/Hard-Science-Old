package grondag.hard_science.simulator.transport.L2;

import grondag.hard_science.simulator.resource.StorageType;
import net.minecraft.nbt.NBTTagCompound;

public abstract class TransportNode<T extends StorageType<T>> implements ITransportNode<T>
{
    private int id;
    
    @Override
    public int getIdRaw()
    {
        return this.id;
    }

    @Override
    public void setId(int id)
    {
        this.id = id;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        this.deserializeID(tag);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        this.serializeID(tag);
    }
}
