package grondag.hard_science.simulator.transport.endpoint;

import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.transport.carrier.CarrierCircuit;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;


public class DirectPortState extends PortState
{
    /**
     * Physical device on which this port is present.
     */
    private final IDevice device;
    
    public DirectPortState(Port port, IDevice device, BlockPos pos, EnumFacing face)
    {
        super(port, pos, face);
        this.device = device;
        assert port.portType == PortType.DIRECT
                : "Mismatched port type for port state.";
    }

    @Override
    public CarrierCircuit internalCircuit()
    {
        return null;
    }
    
    @Override
    public IDevice device()
    {
        return this.device;
    }
}
