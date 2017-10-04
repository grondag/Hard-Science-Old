package grondag.hard_science.library.serialization;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public interface ISerializer<T>
{
    public abstract void fromBytes(T target, PacketBuffer pBuff);
    public abstract void toBytes(T target, PacketBuffer pBuff);
    public abstract void deserializeNBT(T target, NBTTagCompound tag);
    public abstract void serializeNBT(T target, NBTTagCompound tag);
}