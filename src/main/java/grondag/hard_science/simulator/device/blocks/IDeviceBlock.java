package grondag.hard_science.simulator.device.blocks;

import javax.annotation.Nullable;

import grondag.hard_science.library.world.PackedBlockPos;
import grondag.hard_science.simulator.device.DeviceManager;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.StorageType;
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
    public Iterable<PortState> getPorts(StorageType<?> storageType, EnumFacing face);
    
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
    
    public default String description()
    {
        return String.format("Device Block for %s @ %d,%d,%d in dim %d", 
                this.device().machineName(),
                PackedBlockPos.getX(this.packedBlockPos()),
                PackedBlockPos.getY(this.packedBlockPos()),
                PackedBlockPos.getZ(this.packedBlockPos()),
                this.dimensionID());
    }
}
