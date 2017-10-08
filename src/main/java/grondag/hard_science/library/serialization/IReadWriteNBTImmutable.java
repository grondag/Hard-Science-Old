package grondag.hard_science.library.serialization;

import net.minecraft.nbt.NBTTagCompound;

/**
 * NBT read/write interface for classes with immutable values
 */
public interface IReadWriteNBTImmutable<T>
{
    /**
     * Should return the instance used to invoke the method if tag is not present or invalid
     */
    public abstract T deserializeNBT(NBTTagCompound tag);
    
    public abstract void serializeNBT(NBTTagCompound tag);
    
    default public NBTTagCompound serializeNBT()
    {
        NBTTagCompound result = new NBTTagCompound();
        this.serializeNBT(result);
        return result;
    }
}
