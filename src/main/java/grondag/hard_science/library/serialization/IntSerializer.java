package grondag.hard_science.library.serialization;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public abstract class IntSerializer<T> extends AbstractSerializer<T>
{
    public final String tagName;
 
    public IntSerializer(boolean isServerSideOnly, String tagName)
    {
        super(isServerSideOnly);
        this.tagName = tagName;
    }

    public int deserializeNBT(NBTTagCompound tag)
    {
        return this.getTargetTag(tag).getInteger(tagName);
    }

    public abstract int getValue(T target);
    public abstract void setValue(T target, int value);

    @Override
    public void fromBytes(T target, PacketBuffer pBuff)
    {
        setValue(target, pBuff.readInt());
    }
    
    @Override
    public final void toBytes(T target, PacketBuffer pBuff)
    {
        pBuff.writeInt(getValue(target));
    }
    
    @Override
    public void deserializeNBT(T target, NBTTagCompound tag)
    {
        setValue(target, this.getTargetTag(tag).getInteger(tagName));
    }
    
    @Override
    public final void serializeNBT(T target, NBTTagCompound tag)
    {
        this.getTargetTag(tag).setInteger(tagName, getValue(target));
    }
}