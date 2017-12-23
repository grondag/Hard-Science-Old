package grondag.hard_science.simulator.device.blocks;

import javax.annotation.Nullable;

import grondag.hard_science.library.world.PackedBlockPos;
import grondag.hard_science.simulator.device.DeviceManager;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.transport.endpoint.Port;
import grondag.hard_science.simulator.transport.endpoint.PortState;
import net.minecraft.util.EnumFacing;

/**
 * In-world delegate of a device at a given block
 * position. Some devices could have multiple delegates.
 * Devices should register delegates when connected to 
 * to the device manager.
 * 
 */
public interface IDeviceBlock
{
    public long packedBlockPos();
    public int dimensionID();
    public IDevice device();
    
    /**
     * Get ports on the given face.  Returns null if none.
     */
    @Nullable
    public Iterable<PortState> getPorts(EnumFacing face);
    
    /**
     * Get specific port instance on the given face.  Returns null if not present.
     */
    @Nullable
    public PortState getPort(Port port, EnumFacing face);
    
    /**
     * Called by device block manager immediately after this block is removed from the world.
     * Use this to tear down connections, notify neighbors as needed, etc.
     */
    public void onRemoval();
    
    @Nullable
    public default IDeviceBlock getNeighbor(EnumFacing face)
    {
        return DeviceManager.blockManager().getBlockDelegate(
                this.dimensionID(), 
                PackedBlockPos.offset(this.packedBlockPos(), face));
    }
    
}
