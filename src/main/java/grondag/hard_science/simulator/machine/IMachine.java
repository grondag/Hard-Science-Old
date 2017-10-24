package grondag.hard_science.simulator.machine;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.world.Location.ILocated;
import grondag.hard_science.simulator.base.IIdentified;
import grondag.hard_science.simulator.base.DomainManager.IDomainMember;

public interface IMachine extends IReadWriteNBT, ILocated, IDomainMember, IIdentified
{

}
