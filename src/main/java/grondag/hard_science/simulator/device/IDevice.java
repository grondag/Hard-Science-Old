package grondag.hard_science.simulator.device;

import grondag.hard_science.Configurator;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.varia.Base32Namer;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.library.world.Location.ILocated;
import grondag.hard_science.simulator.ISimulationTickable;
import grondag.hard_science.simulator.domain.IDomainMember;
import grondag.hard_science.simulator.persistence.AssignedNumber;
import grondag.hard_science.simulator.persistence.IIdentified;

public interface IDevice extends 
    IIdentified, ILocated, IDomainMember, ISimulationTickable, IReadWriteNBT
{
    public default boolean doesPersist() { return true; }
    
    public default boolean doesUpdateOnTick() { return false; }
    public default boolean doesUpdateOffTick() { return false; }

    /**
     * Signal that device should perform internal
     * initialization and register device blocks
     * via {@link DeviceManager#addDeviceBlock}
     * 
     * Called exactly once after a device is added to the device manager
     * via {@link DeviceManager#addDevice(IDevice)} and
     * after deserialization, after all simulation 
     * components are deserialized.<p>
     */
    public void onConnect();
    
    public void onDisconnect();
    
    @Override
    public default void doOnTick() {}

    @Override
    public default void doOffTick() {}
    
    @Override
    public default AssignedNumber idType()
    {
        return AssignedNumber.DEVICE;
    }
    
    /**
     * May want to cache in implementation if frequently used.
     */
    public default String machineName()
    {
        long l = Useful.longHash(this.getLocation().world().getSeed() ^ this.getId());
        return Base32Namer.makeName(l, Configurator.MACHINES.filterOffensiveMachineNames);
    }
    
    public default void setDirty()
    {
        DeviceManager.instance().setDirty();
    }
}
