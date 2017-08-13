package grondag.hard_science.simulator.take2;

import grondag.hard_science.simulator.domain.Domain.IDomainMember;
import grondag.hard_science.simulator.domain.Location.ILocated;
import grondag.hard_science.simulator.take2.StorageType.ITypedStorage;

/**
 * Place where a resource is or may be located.
 * Domain will be null if not stored in a domain, loose on ground, etc.
 */
public interface IResourceLocation<V extends StorageType> extends ILocated, IDomainMember, ITypedStorage<V>
{
    /**
     * Generic sublocation ID. 
     * If -1, item is on the ground, falling or slot does not apply to this location.
     */
    public default int storageSlot() { return -1; }
}
