package grondag.hard_science.machines;

import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.PortType;

public class SolarCableMachine extends AbstractSimpleMachine
{
    public SolarCableMachine()
    {
        super(CarrierLevel.BOTTOM, PortType.CARRIER);
    }
}
