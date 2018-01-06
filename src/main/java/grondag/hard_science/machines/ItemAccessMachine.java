package grondag.hard_science.machines;

import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.PortType;

/**
 * Provides IStorage interface on top of domain storage so can 
 * inquire and take storage actions at a domain level.
 */
public class ItemAccessMachine extends AbstractSimpleMachine
{
    protected ItemAccessMachine()
    {
        super(CarrierLevel.BOTTOM, PortType.CARRIER);
    }

    @Override
    public boolean hasOnOff()
    {
        return false;
    }

    @Override
    public boolean hasRedstoneControl()
    {
        return false;
    }
}
