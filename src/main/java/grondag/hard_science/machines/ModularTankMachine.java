package grondag.hard_science.machines;

import grondag.hard_science.simulator.storage.FluidStorage;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.PortType;

public class ModularTankMachine extends FluidStorage
{
    public ModularTankMachine()
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
        return false;
    }
}
