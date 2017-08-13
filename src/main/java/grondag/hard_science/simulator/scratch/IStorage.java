package grondag.hard_science.simulator.scratch;

import grondag.hard_science.library.world.Location.ILocated;
import grondag.hard_science.simulator.scratch.StorageType.ITypedStorage;
import grondag.hard_science.simulator.wip.Domain.IDomainMember;

public interface IStorage<V extends StorageType> extends ILocated, IDomainMember, ITypedStorage<V>
{
    public int slotCount();
}
