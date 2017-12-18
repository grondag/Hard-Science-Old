package grondag.hard_science.simulator.device;

import javax.annotation.Nullable;

import grondag.hard_science.simulator.domain.IDomainMember;
import grondag.hard_science.simulator.transport.L1.IConnector;
import net.minecraft.util.EnumFacing;

/**
 * In-world delegate of a device at a given block
 * position. Some devices could have multiple delegates.
 * Devices should register delegates when connected to 
 * to the device manager.
 * 
 */
public interface IDeviceBlock extends IDomainMember
{
    public long packedBlockPos();
    public int dimensionID();
    
    /**
     * Get connector on the given face.  Returns null if none.
     */
    @Nullable
    public IConnector getConnector(EnumFacing face);
    
}
