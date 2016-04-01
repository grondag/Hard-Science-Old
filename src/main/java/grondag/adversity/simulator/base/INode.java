package grondag.adversity.simulator.base;

import net.minecraft.nbt.NBTTagCompound;

public interface INode
{
    public abstract int getID();
    public abstract boolean isSaveDirty();
    public abstract void setSaveDirty(boolean isDirty);
    public abstract void readFromNBT(NBTTagCompound nbt);
    public abstract void writeToNBT(NBTTagCompound nbt);    
}
