package grondag.hard_science.library.serialization;

import javax.annotation.Nonnull;

import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.IMultiSerializable.IMultiSerializableNotifying;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public abstract class ObjectSerializer<T, V extends IMultiSerializable> extends AbstractSerializer<T>
{
    private final Class<V> clazz;
    
    public ObjectSerializer(boolean isServerSideOnly, Class<V> clazz)
    {
        super(isServerSideOnly);
        this.clazz = clazz;
    }

    public V deserializeNBT(NBTTagCompound tag)
    {
        try
        {
            V result = clazz.newInstance();
            result.deserializeNBT(this.getTargetTag(tag));
            return result;
        }
        catch (Exception e)
        {
            Log.error("Unable to create new instance of serialized object. This is a probably a bug.", e);
            return null;
        }
    }
    
    public abstract @Nonnull V getValue(T target);

    @Override
    public void fromBytes(T target, PacketBuffer pBuff)
    {
        getValue(target).fromBytes(pBuff);
    }

    @Override
    public final void toBytes(T target, PacketBuffer pBuff)
    {
        getValue(target).toBytes(pBuff);
    }

    @Override
    public void deserializeNBT(T target, NBTTagCompound tag)
    {
        getValue(target).deserializeNBT(this.getTargetTag(tag));
    }

    @Override
    public final void serializeNBT(T target, NBTTagCompound tag)
    {
        getValue(target).serializeNBT(this.getTargetTag(tag));
    }

    public static abstract class NotifyingObjectSerializer<T, V extends IMultiSerializableNotifying> extends ObjectSerializer<T, V>
    {

        /**
         * Called when deserialization via {@link #fromBytes(Object, PacketBuffer)} 
         * or {@link #deserializeNBT(Object, NBTTagCompound)} results in a state change.
         */
        
        public abstract void notifyChanged(T target);
        public NotifyingObjectSerializer(boolean isServerSideOnly, Class<V> clazz)
        {
            super(isServerSideOnly, clazz);
        }
        
        @Override
        public final void fromBytes(T target, PacketBuffer pBuff)
        {
            if(getValue(target).fromBytesDetectChanges(pBuff)) notifyChanged(target);
        }
        
        @Override
        public final void deserializeNBT(T target, NBTTagCompound tag)
        {
            if(getValue(target).deserializeNBTDetectChanges(tag)) notifyChanged(target);
        }
    }
}
