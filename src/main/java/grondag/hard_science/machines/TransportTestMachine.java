package grondag.hard_science.machines;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.simulator.demand.IProcurementRequest;
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
import net.minecraft.nbt.NBTTagCompound;

public class TransportTestMachine extends AbstractSimpleMachine
{
    private final static ItemResource resource = ItemResource.fromStack(Items.BEEF.getDefaultInstance());
    
    protected final ItemStorage itemStorage;
    
    public TransportTestMachine()
    {
        super(CarrierLevel.BOTTOM, PortType.CARRIER);
        this.itemStorage = new ItemStorage(this);
        this.itemStorage.setCapacity(Integer.MAX_VALUE);
        this.itemStorage.add(resource, Integer.MAX_VALUE, false, null);
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
            long avail = this.itemStorage.getQuantityStored(resource);
            if(avail > 0)
            {
                List<IStorage<StorageTypeStack>> list 
                    = this.getDomain().itemStorage.findSpaceFor(resource, avail);
                if(list.isEmpty()) return;
                
                for(IStorage<StorageTypeStack> store : list)
                {
                    ImmutableList<Route> routes = 
                            LogisticsService.ITEM_SERVICE.findRoutesNow(this, store.device());
                    
                    if(routes.isEmpty()) continue;
                    
                    avail -= LogisticsService.ITEM_SERVICE
                            .sendResourceNow(routes.get(0), resource, avail, this, store.device(), false, false, null);
                    
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
    
    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        super.deserializeNBT(tag);
        this.itemStorage.deserializeNBT(tag);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        super.serializeNBT(tag);
        this.itemStorage.serializeNBT(tag);
    }

    @Override
    public void onConnect()
    {
        super.onConnect();
        this.itemStorage.onConnect();
    }

    @Override
    public void onDisconnect()
    {
        this.itemStorage.onDisconnect();
        super.onDisconnect();
    }
    
    @Override
    public ItemStorage itemStorage()
    {
        return this.itemStorage;
    }
}
