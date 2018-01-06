package grondag.hard_science.simulator.storage;

import com.google.common.eventbus.Subscribe;

import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.StorageEvent.AfterItemStorageConnect;
import grondag.hard_science.simulator.storage.StorageEvent.BeforeItemStorageDisconnect;
import grondag.hard_science.simulator.storage.StorageEvent.ItemCapacityChange;
import grondag.hard_science.simulator.storage.StorageEvent.ItemStoredUpdate;

/**
 * Main purpose is to hold type-specific event handlers.
 */
public class ItemStorageManager extends StorageManager<StorageTypeStack>
{
    public ItemStorageManager(Domain domain)
    {
        super(StorageType.ITEM, domain);
        this.domain.eventBus.register(this);
    }

    @Subscribe
    public void afterStorageConnect(AfterItemStorageConnect event)
    {
        this.addStore(event.storage);
    }
    
    @Subscribe
    public void beforeItemStorageDisconnect(BeforeItemStorageDisconnect event)
    {
        this.removeStore(event.storage);
    }
    
    @Subscribe
    public void onItemUpdate(ItemStoredUpdate event)
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
    public void onCapacityChange(ItemCapacityChange event)
    {
        this.notifyCapacityChanged(event.delta);
    }
}
