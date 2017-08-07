package grondag.hard_science.simulator.persistence;

import net.minecraft.nbt.NBTTagCompound;

public interface IReadWriteNBT
{
    public abstract void readFromNBT(NBTTagCompound tag);
    
    public abstract void writeToNBT(NBTTagCompound tag);
}
