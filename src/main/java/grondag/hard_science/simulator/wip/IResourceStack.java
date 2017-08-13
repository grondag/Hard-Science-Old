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

    /**
     * Takes up to limit from this stack and returns how many were actually taken.
     * Intended to be thread-safe.
     */
    public long takeUpTo(long limit);
    
    /**
     * Increases quantity and returns new quantity in stack.
     * Intended to be thread-safe.
     */
    public long add(long howMany);
    
//    public ILocatedResourceStack<V> withLocation(IResourceLocation<V> location);
//   
//    /**
//     * A resource stack at a specific location.
//     *
//     */
//    public interface ILocatedResourceStack<V extends StorageType> extends IResourceStack<V>, IResourceLocation<V>
//    {
//        
//    }
}
