package grondag.hard_science.library.serialization;

import javax.annotation.Nonnull;

import grondag.hard_science.Log;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

/**
 * Base version is for objects that can be mutated.
 * @author grondag
 *
 * @param <T> class that will hold reference to object being serialized
 * @param <V> class of the object being serialized
 */
public abstract class ObjectSerializer<T, V extends IMultiSerializable> extends AbstractSerializer<T>
{
    private final Class<V> clazz;
    
    public ObjectSerializer(boolean isServerSideOnly, Class<V> clazz)
    {
        super(isServerSideOnly);
        this.clazz = clazz;
    }
    
    public abstract @Nonnull V getValue(T target);
  
    
    /**
     * Will be called when value has been changed via a deserialization method.
     */
    public abstract void notifyChanged(T target);
     
    
    protected V newInstance()
    {
        try
        {
            V result = clazz.newInstance();
            return result;
        }
        catch (Exception e)
        {
            Log.error("Unable to create new instance of serialized object. This is probably a bug.", e);
            return null;
        }
    }
    
    /**
     * Instance to be used for deserialization. Default is to use result of getValue, 
     * implying that the serialized object is mutable.
     */
    protected V deserializtionInstance(T target)
    {
        return getValue(target);
    }

    public V fromBytes(PacketBuffer pBuff)
    {
        V result = newInstance();
        if(result != null) result.fromBytes(pBuff);
        return result;
    }
    
    @Override
    public void fromBytes(T target, PacketBuffer pBuff)
    {
        if(getValue(target).fromBytesDetectChanges(pBuff)) notifyChanged(target);
    }

    @Override
    public final void toBytes(T target, PacketBuffer pBuff)
    {
        getValue(target).toBytes(pBuff);
    }
    
    public V deserializeNBT(NBTTagCompound tag)
    {
        V result = newInstance();
        if(result != null) result.deserializeNBT(tag);
        return result;
    }

    @Override
    public void deserializeNBT(T target, NBTTagCompound tag)
    {
        if(getValue(target).deserializeNBTDetectChanges(tag)) notifyChanged(target);
    }

    @Override
    public final void serializeNBT(T target, NBTTagCompound tag)
    {
        getValue(target).serializeNBT(tag);
    }
    
    public final void serializeNBT(V value, NBTTagCompound tag)
    {
        value.serializeNBT(tag);
    }
}
