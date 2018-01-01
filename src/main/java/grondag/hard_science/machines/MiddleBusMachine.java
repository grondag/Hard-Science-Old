package grondag.hard_science.machines;

import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.PortType;

public class MiddleBusMachine extends AbstractSimpleMachine
{
    public MiddleBusMachine()
    {
        super(CarrierLevel.MIDDLE, PortType.BRIDGE);
    }
}
