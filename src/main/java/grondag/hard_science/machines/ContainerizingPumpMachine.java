package grondag.hard_science.machines;

import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.PortType;

public class ContainerizingPumpMachine extends AbstractSimpleMachine
{
    protected ContainerizingPumpMachine()
    {
        super(CarrierLevel.BOTTOM, PortType.CARRIER);
    }
    
    @Override
    public boolean hasOnOff()
    {
        return true;
    }

    @Override
    public boolean hasRedstoneControl()
    {
        return true;
    }
}
