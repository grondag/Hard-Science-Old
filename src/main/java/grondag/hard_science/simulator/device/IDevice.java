package grondag.hard_science.simulator.device;

import javax.annotation.Nullable;

import grondag.hard_science.Configurator;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.varia.Base32Namer;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.library.world.Location.ILocated;
import grondag.hard_science.simulator.ISimulationTickable;
import grondag.hard_science.simulator.device.blocks.IDeviceBlockManager;
import grondag.hard_science.simulator.domain.IDomainMember;
import grondag.hard_science.simulator.persistence.AssignedNumber;
import grondag.hard_science.simulator.persistence.IIdentified;
import grondag.hard_science.simulator.transport.management.ITransportManager;

public interface IDevice extends 
    IIdentified, ILocated, IDomainMember, ISimulationTickable, IReadWriteNBT
{
    public default boolean doesPersist() { return true; }
    
    public default boolean doesUpdateOnTick() { return false; }
    public default boolean doesUpdateOffTick() { return false; }
    
    /**
     * Set to true at the end of {@link #onConnect()} and 
     * set to false at the end of {@link #onDisconnect()}.
     */
    public boolean isConnected();
    
    /**
     * Null if this device has no world block delegates.
     */
    @Nullable 
    public default IDeviceBlockManager blockManager() { return null; }

    public default boolean hasBlockManager() { return this.blockManager() != null; }
    
    /**
     * Null if this device has no transport facilities.
     */
    @Nullable 
    public default ITransportManager tranportManager() { return null; }

    public default boolean hasTransportManager() { return this.tranportManager() != null; }
    
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
    public default void onConnect()
    {
        if(this.hasBlockManager()) this.blockManager().connect();
        if(this.hasTransportManager()) this.tranportManager().connect();
    }
    
    public default void onDisconnect()
    {
        if(this.hasTransportManager()) this.tranportManager().disconnect();
        if(this.hasBlockManager()) this.blockManager().disconnect();
    }
    
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
