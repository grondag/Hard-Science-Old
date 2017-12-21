package grondag.hard_science.simulator.device.blocks;

import javax.annotation.Nullable;

import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.transport.endpoint.ConnectorInstance;
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
     * Get connector on the given face.  Returns null if none.
     */
    @Nullable
    public ConnectorInstance getConnector(EnumFacing face);
    
    /**
     * Called by device block manager immediately after this block is removed from the world.
     * Use this to tear down connections, notify neighbors as needed, etc.
     */
    public void onRemoval();
    
}
