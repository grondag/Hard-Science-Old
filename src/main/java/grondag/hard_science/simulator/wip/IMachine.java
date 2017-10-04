package grondag.hard_science.simulator.wip;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.world.Location.ILocated;
import grondag.hard_science.simulator.wip.DomainManager.IDomainMember;

public interface IMachine extends IReadWriteNBT, ILocated, IDomainMember, IIdentified
{

}
