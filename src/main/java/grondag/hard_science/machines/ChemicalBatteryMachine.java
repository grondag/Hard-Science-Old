package grondag.hard_science.machines;

import grondag.hard_science.simulator.storage.PowerStorage;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.PortType;

public class ChemicalBatteryMachine extends PowerStorage
{
    public ChemicalBatteryMachine()
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
