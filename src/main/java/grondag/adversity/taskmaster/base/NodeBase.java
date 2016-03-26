package grondag.adversity.taskmaster.base;

import net.minecraft.nbt.NBTTagCompound;

public abstract class NodeBase
{
    public abstract int getID();
    public abstract boolean isSaveDirty();
    public abstract void setSaveDirty(boolean isDirty);
    public abstract void readFromNBT(NBTTagCompound nbt);
    public abstract void writeToNBT(NBTTagCompound nbt);    
}
