package grondag.hard_science.simulator.wip;

import grondag.hard_science.library.world.Location.ILocated;
import grondag.hard_science.simulator.wip.DomainManager.IDomainMember;
import grondag.hard_science.simulator.wip.StorageType.ITypedStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Place where a resource is or may be located.
 * Domain will be null if not stored in a domain, loose on ground, etc.
 */
public interface IResourceLocation<V extends StorageType> extends ILocated, IDomainMember, ITypedStorage<V>, INBTSerializable<NBTTagCompound>
{
    /**
     * Generic sublocation ID. 
     * If -1, item is on the ground, falling or slot does not apply to this location.
     */
    public default int storageSlot() { return -1; }
}
