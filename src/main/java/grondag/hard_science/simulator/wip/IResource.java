package grondag.hard_science.simulator.wip;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

/** 
 * A resource is something that can be produced and consumed.
 * Most resources can be stored. (Computation can't.)
 * Resources with a storage type can also have a location.
 * Time is not a resource because it cannot be produced.
 */
public interface IResource<V extends StorageType> extends INBTSerializable<NBTTagCompound>
{
    public V storageType();
    
    public int computeResourceHashCode();
    public boolean isResourceEqual(IResource<V> other);
}
