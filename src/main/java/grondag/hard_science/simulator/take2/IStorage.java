package grondag.hard_science.simulator.take2;

import grondag.hard_science.simulator.domain.Domain.IDomainMember;
import grondag.hard_science.simulator.domain.Location.ILocated;
import grondag.hard_science.simulator.take2.StorageType.ITypedStorage;

public interface IStorage<V extends StorageType> extends ILocated, IDomainMember, ITypedStorage<V>
{
    public int slotCount();
}
