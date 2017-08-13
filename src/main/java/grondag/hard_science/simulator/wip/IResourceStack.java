package grondag.hard_science.simulator.wip;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * A specific instance of resource in the world.
 * Defines the amount and other characteristics of the resource.
 */
public interface IResourceStack<V extends StorageType> extends INBTSerializable<NBTTagCompound>
{
    public IResource<V> resource();
    public long getQuantity();
    public boolean isEmpty();
    public ILocatedResourceStack<V> withLocation(IResourceLocation<V> location);

    /**
     * Takes up to limit from this stack and returns the result.  
     * If simulate is false, this stack may be mutated.
     */
    public IResourceStack<V> takeUpTo(long limit, boolean simulate);
    
   
    /**
     * A resource stack at a specific location.
     *
     */
    public interface ILocatedResourceStack<V extends StorageType> extends IResourceStack<V>, IResourceLocation<V>
    {
        
    }
}
