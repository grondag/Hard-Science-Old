package grondag.hard_science.simulator.device.blocks;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.CarrierCircuit;
import grondag.hard_science.simulator.transport.endpoint.PortState;
import net.minecraft.util.EnumFacing;

/**
 * Manages the device block delegates for a device.
 * 
 * Hierarchical structure
 *  Device
 *      DeviceBlock(s)
 *          Connector Instance : Connection Instance
 *              Port Instance : Transport Node
 * 
 */
public interface IDeviceBlockManager
{
    /**
     * All device blocks for this device.
     */
    public Collection<IDeviceBlock> blocks();

    /** 
     * Will be called by owning device when added to world.
     * Should register all device blocks with DeviceWorldManager.
     * Happens before transport manager connect.
     */
    public void connect();

    /** 
     * Will be called by owning device when removed from world. 
     * Should unregister all device blocks with DeviceWorldManager.
     */
    public void disconnect();

    /**
     * If device item ports have an internal carrier circuit,
     * the current circuit.  Null if no connections or doesn't have.
     * For TOP display to support debugging.
     */
    public default CarrierCircuit itemCircuit() { return null; }

    /**
     * If device power ports have an internal carrier circuit,
     * the current circuit.  Null if no connections or doesn't have.
     * For TOP display to support debugging.
     */
    public default CarrierCircuit powerCircuit() { return null; }

    /**
     * Get all ports on this device with the given StorageType. 
     *
     * @param storageType  Matches ports of this type.
     * @param attachedOnly If true, only attached ports will be included.
     */
    @Nonnull
    public default List<PortState> getPorts(StorageType<?> storageType, boolean attachedOnly)
    {
        ImmutableList.Builder<PortState> builder = ImmutableList.builder();
        
        for(IDeviceBlock block : this.blocks())
        {
            for(EnumFacing face : EnumFacing.VALUES)
            {
                for(PortState port : block.getPorts(face))
                {
                    if(port.port().storageType == storageType 
                            && (!attachedOnly || port.isAttached()))
                    {
                        builder.add(port);
                    }
                }
            }
            for(PortState port : block.getPorts(null))
            {
                if(port.port().storageType == storageType 
                        && (!attachedOnly || port.isAttached()))
                {
                    builder.add(port);
                }
            }
        }
        return builder.build();
    }
}
