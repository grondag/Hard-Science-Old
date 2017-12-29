package grondag.hard_science.machines;

import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.storage.ItemStorage;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.PortType;
import net.minecraft.init.Items;

public class TransportTestMachine extends ItemStorage
{
    public TransportTestMachine()
    {
        super(CarrierLevel.BOTTOM, PortType.CARRIER);
        this.setCapacity(Integer.MAX_VALUE);
        this.add(ItemResource.fromStack(Items.BEEF.getDefaultInstance()), Integer.MAX_VALUE, false, null);
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
