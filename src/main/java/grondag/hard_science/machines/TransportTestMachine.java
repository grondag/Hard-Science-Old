package grondag.hard_science.machines;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.IStorage;
import grondag.hard_science.simulator.storage.ItemStorage;
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
        super(CarrierLevel.MIDDLE, PortType.DIRECT);
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
        if(this.isOn() && this.getDomain() != null)
        {
            long avail = this.getQuantityStored(resource);
            if(avail > 0)
            {
                List<IStorage<StorageTypeStack>> list 
                    = this.getDomain().itemStorage.findSpaceFor(resource, avail);
                if(list.isEmpty()) return;
                
                for(IStorage<StorageTypeStack> store : list)
                {
                    ImmutableList<Route> routes = 
                            LogisticsService.ITEM_SERVICE.findRoutesNow(this, (IDevice)store);
                    
                    if(routes.isEmpty()) continue;
                    
                    avail -= LogisticsService.ITEM_SERVICE
                            .sendResourceNow(routes.get(0), resource, avail, this, (IDevice)store, false, false, null);
                    
                    if(avail <= 0) break;
                }
            }
        }
    }

    @Override
    public long onConsumeImpl(IResource<?> resource, long quantity, boolean simulate, IProcurementRequest<?> request)
    {
        // can't put stuff in it
        return 0;
    }
}
