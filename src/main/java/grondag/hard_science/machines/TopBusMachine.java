package grondag.hard_science.machines;

import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.PortType;

public class TopBusMachine extends AbstractSimpleMachine
{
    public TopBusMachine()
    {
        super(CarrierLevel.TOP, PortType.BRIDGE);
    }
}
