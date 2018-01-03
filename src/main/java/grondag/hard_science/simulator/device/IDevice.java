package grondag.hard_science.simulator.device;

import javax.annotation.Nullable;

import grondag.hard_science.Configurator;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.varia.Base32Namer;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.library.world.Location.ILocated;
import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.device.blocks.IDeviceBlockManager;
import grondag.hard_science.simulator.domain.IDomainMember;
import grondag.hard_science.simulator.persistence.AssignedNumber;
import grondag.hard_science.simulator.persistence.IIdentified;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.management.ITransportManager;

public interface IDevice extends 
    IIdentified, ILocated, IDomainMember, IReadWriteNBT
{
    public default boolean doesPersist() { return true; }
    
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
     * If null, implies device has no transport nodes.
     */
    @Nullable 
    public default ITransportManager<?> tranportManager(StorageType<?> storageType) { return null; }

    public default  boolean hasTransportManager(StorageType<?> storageType) { return this.tranportManager(storageType) != null; }
    
    /**
     * Signal that device should perform internal
     * initialization and register device blocks
     * via {@link DeviceManager#addDeviceBlock}.
     * Device should already be added to device 
     * manager when this is called.
     * 
     * Called exactly once either by ...<br>
     * 1) Device Manager after all simulation components are deserialized.<br>
     * ...or...<br>
     * 2) Block placement logic after domain /location are set and 
     * and deserialization from stack (if applies) is complete.
     */
    public default void onConnect()
    {
        if(this.hasBlockManager()) this.blockManager().connect();
    }
    
    public default void onDisconnect()
    {
        if(this.hasBlockManager()) this.blockManager().disconnect();
    }
    
    /**
     * If true, then {@link #doOnTick()} will be called during 
     * world tick from server thread. Is checked only when devices
     * are added or removed from device manager so result should not be dynamic.
     */
    public default boolean doesUpdateOnTick() { return false; }
    
    /**
     * See {@link #doesUpdateOnTick()}
     */
    public default void doOnTick() {}
    
    /**
     * If true, then {@link #doOffTick()} will be called once per server tick 
     * from simulation thread pool. Is checked only when devices
     * are added or removed from device manager so result should not be dynamic.
     */
    public default boolean doesUpdateOffTick() { return false; }
    
    /**
     * See {@link #doesUpdateOffTick()}
     */
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
    
    /**
     * Called by transport system for this device to handle outbound transport requests.
     * SHOULD ONLY BE CALLED FROM LOGISTICS SERVICE to ensure consistency of results.
     */
    public long onProduce(IResource<?> resource, long quantity, boolean simulate, @Nullable IProcurementRequest<?> request);

    /**
     * Called by transport system for this device to handle inbound transport requests
     * SHOULD ONLY BE CALLED FROM LOGISTICS SERVICE to ensure consistency of results.
     */
    public long onConsume(IResource<?> resource, long quantity, boolean simulate, @Nullable IProcurementRequest<?> request);

    /**
     * Called after ports on this device are attached
     * or detached to notify transport manager to update transport
     * addressability for this device.  Has no effect if this device
     * lacks transport facilities for the given storage type.
     * 
     * SHOULD ONLY BE CALLED FROM CONNECTION MANAGER THREAD.
     */
    public default void refreshTransport(StorageType<?> storageType)
    {
        if(this.hasTransportManager(storageType))
        {
            this.tranportManager(storageType).refreshTransport();
        }
    }

    /**
     * True if supports channels for transport circuit segregation.
     */
    public default boolean hasChannel() { return this.getChannel() != CHANNEL_UNSUPPORTED; }
    
    public static final int CHANNEL_UNSUPPORTED = -1;
    
    /**
     * Configured transport channel for connection segregation.
     * Will return {@link #CHANNEL_UNSUPPORTED} if not supported.  
     * Is generally the block meta/species.
     * Default implementation is no channel support.
     */
    public default int getChannel() { return CHANNEL_UNSUPPORTED; }
}

