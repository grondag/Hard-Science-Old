package grondag.hard_science.machines;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.ItemStorage;
import grondag.hard_science.simulator.storage.StorageWithQuantity;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.PortType;
import grondag.hard_science.simulator.transport.management.LogisticsService;
import grondag.hard_science.simulator.transport.routing.Route;
import net.minecraft.init.Items;

public class TransportTestMachine extends ItemStorage
{
    private final static ItemResource resource = ItemResource.fromStack(Items.BEEF.getDefaultInstance());
    public TransportTestMachine()
    {
        super(CarrierLevel.BOTTOM, PortType.DIRECT);
        super.setCapacity(Integer.MAX_VALUE);
        super.add(resource, Integer.MAX_VALUE, false, null);
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
            long avail = this.getQuantityStored(resource);
            if(avail > 0)
            {
                List<StorageWithQuantity<StorageTypeStack>> list 
                    = this.getDomain().itemStorage.findSpaceFor(resource, avail, null);
                if(list.isEmpty()) return;
                
                for(StorageWithQuantity<StorageTypeStack> rwq : list)
                {
                    ImmutableList<Route> routes = 
                            LogisticsService.ITEM_SERVICE.findRoutesNow(this, rwq.storage);
                    
                    if(routes.isEmpty()) continue;
                    
                    avail -= LogisticsService.ITEM_SERVICE
                            .sendResourceNow(routes.get(0), resource, avail, this, rwq.storage, false, false, null);
                    
                    if(avail <= 0) break;
                }
            }
        }
    }

    @Override
    public synchronized long add(IResource<StorageTypeStack> resource, long howMany, boolean simulate, IProcurementRequest<StorageTypeStack> request)
    {
        // can't put stuff in it
        return 0;
    }
    
    
    
    
}
