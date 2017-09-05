package grondag.hard_science.library.serialization;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

/**
 * Serializes enum values for enums with <= 256 values
 * @author grondag
 *
 */
public abstract class EnumSerializer<T, V extends Enum<?>> extends AbstractSerializer<T>
{
    public final String tagName;
    private final V[] enumValues;
    
   private final boolean useByte;
   
    public EnumSerializer(boolean isServerSideOnly, String tagName, Class<V> clazz)
    {
        super(isServerSideOnly);
        this.tagName = tagName;
        this.enumValues = clazz.getEnumConstants();
        useByte = this.enumValues.length < 128;
    }

    public V deserializeNBT(NBTTagCompound tag)
    {
        int ordinal = this.useByte ? tag.getByte(tagName) : tag.getInteger(tagName);
        return ordinal < 0 || ordinal > this.enumValues.length ? null : enumValues[ordinal];
    }

    public abstract @Nullable V getValue(T target);
    
    public abstract void setValue(T target, @Nullable V value);

    @Override
    public void fromBytes(T target, PacketBuffer pBuff)
    { 
        int ordinal = this.useByte ? pBuff.readByte() : pBuff.readVarInt();
        setValue(target, ordinal < 0 || ordinal > this.enumValues.length ? null : enumValues[ordinal]);
    }
    
    @Override
    public final void toBytes(T target, PacketBuffer pBuff)
    {
        V value = getValue(target);
        int ordinal = value == null ? -1 : value.ordinal();
        if(this.useByte)
        {
            pBuff.writeByte(ordinal);
        }
        else
        {
            pBuff.writeVarInt(ordinal);
        }
    }
    
    @Override
    public void deserializeNBT(T target, NBTTagCompound tag)
    {
        setValue(target, this.deserializeNBT(tag));
    }
    
    @Override
    public final void serializeNBT(T target, NBTTagCompound tag)
    {
        this.serializeNBT(getValue(target), tag);
    }
    
    public final void serializeNBT(V value, NBTTagCompound tag)
    {
        int ordinal = value == null ? -1 : value.ordinal();
        if(this.useByte)
        {
            tag.setByte(this.tagName, (byte) ordinal);
        }
        else
        {
            tag.setInteger(this.tagName, ordinal);
        }
        
    }
}
