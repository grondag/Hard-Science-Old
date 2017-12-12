package grondag.hard_science.simulator.storage;

import java.util.List;

import grondag.hard_science.simulator.resource.AbstractResourceDelegate;
import grondag.hard_science.simulator.resource.StorageType;

/**
 * For classes that want to receive updates when the contents of an IStorage changes.
 * Doesn't send in/out, just gives the new quantityStored.
 * This works because IStorage doesn't expose slots or locations, only resource and quantityStored.
 * Update does identify which storage is calling, in case a listener subscribes to more than one.
 */
public interface IStorageListener<T extends StorageType<T>>
{
    /**
     * Sends entire contents of the storage.
     * Will be called after listener subscribes, or if there are mass updates afterwards.
     */
    public void handleStorageRefresh(IListenableStorage<T> sender, List<AbstractResourceDelegate<T>> update, long capacity);

    /**
     * Sends updates since last refresh. Quantity replaces whatever was before.
     * Quantity 0 means item no longer present.
     */
    void handleStorageUpdate(IListenableStorage<T> sender, AbstractResourceDelegate<T> update);
    
    
    /**
     * Will be called if the storage is destroyed or goes offline.
     * Storage will not send a refresh to remove all items - listener should handle that.
     */
    public void handleStorageDisconnect(IListenableStorage<T> storage);
    
    /**
     * Used by IStorage to remove orphaned/dead listeners.
     */
    public boolean isClosed();

    
}
