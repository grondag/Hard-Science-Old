package grondag.hard_science.simulator.transport.endpoint;

import javax.annotation.Nonnull;

import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.transport.carrier.CarrierCircuit;

public class DirectPortState extends PortState
{
    /**
     * Physical device on which this port is present.
     */
    private final IDevice device;
    
    public DirectPortState(Port port, IDevice device)
    {
        super(port);
        this.device = device;
        assert port.portType == PortType.DIRECT
                : "Mismatched port type for port state.";
    }

    @Override
    public boolean attach(@Nonnull CarrierCircuit externalCircuit, @Nonnull PortState mate)
    {
        // direct ports require a carrier port as mate
        if(mate.port().portType != PortType.CARRIER) return false;
        return super.attach(externalCircuit, mate);
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
