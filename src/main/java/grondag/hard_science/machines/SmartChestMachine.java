package grondag.hard_science.machines;

import grondag.hard_science.simulator.storage.ItemStorage;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.PortType;

public class SmartChestMachine extends ItemStorage
{
    public SmartChestMachine()
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
