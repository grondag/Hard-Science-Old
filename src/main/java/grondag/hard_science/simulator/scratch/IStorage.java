package grondag.hard_science.simulator.scratch;

import grondag.hard_science.library.world.Location.ILocated;
import grondag.hard_science.simulator.wip.StorageType;
import grondag.hard_science.simulator.wip.Domain.IDomainMember;
import grondag.hard_science.simulator.wip.StorageType.ITypedStorage;

public interface IStorage<V extends StorageType> extends ILocated, IDomainMember, ITypedStorage<V>
{
    public int slotCount();
}
