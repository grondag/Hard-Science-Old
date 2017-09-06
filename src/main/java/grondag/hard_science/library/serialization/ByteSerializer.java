package grondag.hard_science.library.serialization;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public abstract class ByteSerializer<T> implements ISerializer<T>
{
    public final String tagName;
 
    public ByteSerializer(String tagName)
    {
        this.tagName = tagName;
    }

    public byte deserializeNBT(NBTTagCompound tag)
    {
        return tag.getByte(tagName);
    }

    public abstract byte getValue(T target);
    
    public abstract void setValue(T target, byte value);

    @Override
    public void fromBytes(T target, PacketBuffer pBuff)
    {
        setValue(target, pBuff.readByte());
    }
    
    @Override
    public final void toBytes(T target, PacketBuffer pBuff)
    {
        pBuff.writeByte(getValue(target));
    }
    
    @Override
    public void deserializeNBT(T target, NBTTagCompound tag)
    {
        setValue(target, tag.getByte(this.tagName));
    }
    
    @Override
    public final void serializeNBT(T target, NBTTagCompound tag)
    {
        this.serializeNBT(getValue(target), tag);
    }
    
    public final void serializeNBT(byte value, NBTTagCompound tag)
    {
        tag.setByte(this.tagName, value);
    }
}