package grondag.hard_science.simulator.domain;

import com.google.common.eventbus.Subscribe;

import grondag.hard_science.Log;
import grondag.hard_science.simulator.storage.StorageEvent.AfterItemStorageConnect;
import grondag.hard_science.simulator.storage.StorageEvent.BeforeItemStorageDisconnect;
import grondag.hard_science.simulator.storage.StorageEvent.ItemResourceUpdate;

public class DomainEventTester
{
    @Subscribe
    public void onItemUpdate(ItemResourceUpdate event)
    {
        Log.info("onItemUpdate %s  %s  %d", 
                event.storage.machineName(),
                event.resource.displayName(),
                event.delta);
    }
    
    @Subscribe
    public void afterItemStorageConnect(AfterItemStorageConnect event)
    {
        Log.info("afterItemStorageConnect %s", event.storage.machineName());
    }
    
    @Subscribe
    public void beforeItemStorageDisconnect(BeforeItemStorageDisconnect event)
    {
        Log.info("beforeItemStorageDisconnect %s", event.storage.machineName());
    }
}
