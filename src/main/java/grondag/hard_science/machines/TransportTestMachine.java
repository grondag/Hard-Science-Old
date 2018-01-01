package grondag.hard_science.machines;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.Log;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.ItemStorage;
import grondag.hard_science.simulator.storage.StorageWithQuantity;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.PortType;
import grondag.hard_science.simulator.transport.management.ConnectionManager;
import grondag.hard_science.simulator.transport.routing.Route;
import net.minecraft.item.ItemStack;

public class TransportTestMachine extends ItemStorage
{
    private final ItemResource resource;
    public TransportTestMachine(ItemStack stack)
    {
        super(CarrierLevel.BOTTOM, PortType.DIRECT);
        this.setCapacity(Integer.MAX_VALUE);
        this.resource = ItemResource.fromStack(stack);
        this.add(resource, Integer.MAX_VALUE, false, null);
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
    
    @Override
    public boolean doesUpdateOffTick() { return true; }
    
    @Override
    public void doOffTick()
    {
        super.doOffTick();
        if(this.isOn())
        {
            long avail = this.getQuantityStored(this.resource);
            if(avail > 0)
            {
                List<StorageWithQuantity<StorageTypeStack>> list 
                    = this.getDomain().itemStorage.findSpaceFor(this.resource, avail, null);
                if(list.isEmpty()) return;
                
                for(StorageWithQuantity<StorageTypeStack> store : list)
                {
                    ImmutableList<Route> routes = 
                            ConnectionManager.findRoutes(StorageType.ITEM, this, store.storage);
                    
                    if(routes.isEmpty()) continue;
                    
                    Log.info(routes.get(0).toString());
                }
            }
        }
    }
}
