package grondag.hard_science.machines.impl.logistics;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.simulator.fobs.NewProcurementTask;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.ContainerUsage;
import grondag.hard_science.simulator.storage.IResourceContainer;
import grondag.hard_science.simulator.storage.ItemContainer;
import grondag.hard_science.simulator.transport.management.LogisticsService;
import grondag.hard_science.simulator.transport.routing.Route;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;

public class TransportTestMachine extends AbstractSimpleMachine
{
    private final static ItemResource resource = ItemResource.fromStack(Items.BEEF.getDefaultInstance());
    
    protected final ItemContainer itemStorage;
    
    public TransportTestMachine()
    {
        super();
        this.itemStorage = new ItemContainer(this, ContainerUsage.BUFFER_OUT);
        this.itemStorage.setCapacity(64);
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
            this.itemStorage.add(resource, 16, false, null);
            
            LogisticsService.ITEM_SERVICE.executor.execute(() ->
            {
                long remaining = this.itemStorage.getQuantityStored(resource);
                
                List<IResourceContainer<StorageTypeStack>> list 
                    = this.getDomain().itemStorage.findSpaceFor(resource, this);
                if(list.isEmpty()) return;
                
                for(IResourceContainer<StorageTypeStack> store : list)
                {
                    ImmutableList<Route<StorageTypeStack>> routes = 
                            LogisticsService.ITEM_SERVICE.findRoutesNow(this, store.device(), null);
                    
                    if(routes.isEmpty()) continue;
                    
                    remaining -= LogisticsService.ITEM_SERVICE
                            .sendResourceOnRouteNow(routes.get(0), resource, remaining, this, store.device(), false, false, null);
                    
                    if(remaining <= 0) break;
                }
            });
        }
    }

    @Override
    public long onConsumeImpl(IResource<?> resource, long quantity, boolean simulate, NewProcurementTask<?> request)
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
    public ItemContainer itemStorage()
    {
        return this.itemStorage;
    }
}
