package grondag.hard_science.simulator.storage;

import com.google.common.eventbus.Subscribe;

import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.storage.FluidStorageEvent.AfterFluidStorageConnect;
import grondag.hard_science.simulator.storage.FluidStorageEvent.BeforeFluidStorageDisconnect;
import grondag.hard_science.simulator.storage.FluidStorageEvent.FluidCapacityChange;
import grondag.hard_science.simulator.storage.FluidStorageEvent.FluidStoredUpdate;

/**
 * Main purpose is to hold type-specific event handlers.
 */
public class FluidStorageManager extends StorageManager<StorageTypeFluid>
{
    public FluidStorageManager(Domain domain)
    {
        super(StorageType.FLUID, domain);
        this.domain.eventBus.register(this);
    }

    @Subscribe
    public void afterStorageConnect(AfterFluidStorageConnect event)
    {
        this.addStore(event.storage);
    }
    
    @Subscribe
    public void beforeFluidStorageDisconnect(BeforeFluidStorageDisconnect event)
    {
        this.removeStore(event.storage);
    }
    
    @Subscribe
    public void onFluidUpdate(FluidStoredUpdate event)
    {
        if(event.delta > 0)
        {
            this.notifyAdded(event.storage, event.resource, event.delta, event.request);
        }
        else
        {
            this.notifyTaken(event.storage, event.resource, -event.delta, event.request);
        }
    }
    
    @Subscribe
    public void onCapacityChange(FluidCapacityChange event)
    {
        this.notifyCapacityChanged(event.delta);
    }
}
