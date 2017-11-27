package grondag.hard_science.machines.base;

import grondag.hard_science.Configurator;
import grondag.hard_science.library.varia.Base32Namer;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.library.world.Location.ILocated;
import grondag.hard_science.simulator.base.IDomainMember;
import grondag.hard_science.simulator.base.IIdentified;

public interface IMachine extends IIdentified, IDomainMember, ILocated
{
    
    /**
     * May want to cache in implementation if frequently used.
     */
    public default String machineName()
    {
        long l = Useful.longHash(this.getLocation().world().getSeed() ^ this.getId());
        return Base32Namer.makeName(l, Configurator.MACHINES.filterOffensiveMachineNames);
    }
}
