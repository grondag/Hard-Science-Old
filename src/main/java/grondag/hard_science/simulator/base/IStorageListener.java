package grondag.hard_science.simulator.base;

import java.util.List;

/**
 * For classes that want to receive updates when the contents of an IStorage changes.
 * Doesn't send in/out, just gives the new quantity.
 * This works because IStorage doesn't expose slots or locations, only resource and quantity.
 * Update does identify which storage is calling, in case a listener subscribes to more than one.
 */
public interface IStorageListener<T extends StorageType<T>>
{
    /**
     * Sends entire contents of the storage.
     * Will be called after listener subscribes, or if there are mass updates afterwards.
     */
    public void handleStorageRefresh(IStorage<T> sender, List<AbstractResourceWithQuantity<T>> update, long capacity);

    /**
     * Sends updates since last refresh. Quantity replaces whatever was before.
     * Quantity 0 means item no longer present.
     */
    void handleStorageUpdate(IStorage<T> sender, AbstractResourceWithQuantity<T> update);
    
    
    /**
     * Will be called if the storage is destroyed or goes offline.
     * Storage will not send a refresh to remove all items - listener should handle that.
     */
    public void handleStorageDisconnect(IStorage<T> storage);
    
    /**
     * Used by IStorage to remove orphaned/dead listeners.
     */
    public boolean isClosed();

    
}
