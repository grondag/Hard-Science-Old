package grondag.hard_science.simulator.persistence;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Slightly more flexible version of INBTSerializable that allows for
 * writing to an existing tag instead of always creating a new one.
 */
public interface IReadWriteNBT extends INBTSerializable<NBTTagCompound>
{
    public abstract void deserializeNBT(NBTTagCompound tag);
    
    public abstract void serializeNBT(NBTTagCompound tag);
    
    default public NBTTagCompound serializeNBT()
    {
        NBTTagCompound result = new NBTTagCompound();
        this.serializeNBT(result);
        return result;
    }
}
