package grondag.hard_science.simulator.storage;

import java.util.List;
import java.util.function.Predicate;

import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.simulator.resource.AbstractResourceDelegate;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ITypedStorage;
import grondag.hard_science.simulator.resource.StorageType;

/**
 * Implement on classes that can have client-side storage
 * display via IStorageListener
 */
public interface IListenableStorage<T extends StorageType<T>> extends ITypedStorage<T>, ISizedContainer
{
    /**
     * Create this in your instance and interface will handle the rest of the implementation
     * for storage listener support.
     */
    public SimpleUnorderedArrayList<IStorageListener<T>> listeners();
    
    /**
     * Just like {@link IStorage#find(Predicate)} but returns delegates instead.
     */
    List<AbstractResourceDelegate<T>> findDelegates(Predicate<IResource<T>> predicate);
 
    /**
     * IStorage should call back with full refresh followed by updates as needed.
     */
    public default void addListener(IStorageListener<T> listener)
    {
        synchronized(this)
        {
            // doing this here prevents buildup of dead listeners if there are no storage changes
            clearClosedListeners();
            
            this.listeners().addIfNotPresent(listener);
            listener.handleStorageRefresh(this, this.findDelegates(this.storageType().MATCH_ANY), this.getCapacity());
        }
    }
    
    /**
     * Notice to stop sending updates.
     */
    public default void removeListener(IStorageListener<T> listener)
    {
        synchronized(this)
        {
            this.listeners().removeIfPresent(listener);
        }
    }
    
    public default void clearClosedListeners()
    {
        synchronized(this)
        {
            SimpleUnorderedArrayList<IStorageListener<T>> listeners = this.listeners();
            
            if(listeners.isEmpty()) return;
            
            for(Object listener : listeners.toArray())
            {
                @SuppressWarnings("unchecked")
                IStorageListener<T> l = (IStorageListener<T>)listener;
                if(l.isClosed())
                {
                    this.removeListener(l);
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public default void refreshAllListeners()
    {
        synchronized(this)
        {
            SimpleUnorderedArrayList<IStorageListener<T>> listeners = this.listeners();
            
            if(listeners.isEmpty()) return;
            
            List<AbstractResourceDelegate<T>> refresh = this.findDelegates(this.storageType().MATCH_ANY);
            for(Object listener : listeners.toArray())
            {
                IStorageListener<T> l = (IStorageListener<T>)listener;
                if(l.isClosed())
                {
                    this.removeListener(l);
                }
                else
                {
                    l.handleStorageRefresh(this, refresh, this.getCapacity());
                }
            }
        }
    }
    
    public default void updateListeners(AbstractResourceWithQuantity<T> update)
    {
        SimpleUnorderedArrayList<IStorageListener<T>> listeners = this.listeners();
        
        if(listeners.isEmpty()) return;
        
        AbstractResourceDelegate<T> delegate = update.toDelegate(this.getHandleForResource(update.resource()));
        
        for(Object listener : listeners.toArray())
        {
            @SuppressWarnings("unchecked")
            IStorageListener<T> l = (IStorageListener<T>)listener;
            if(l.isClosed())
            {
                this.removeListener(l);
            }
            else
            {
                l.handleStorageUpdate(this, delegate);
            }
        }
    }
    
    /**
     * Generate surrogate key that can be used client side to refer
     * to specific resources within this instance when sending actions
     * from client.  Necessary because client-side stacks may not
     * have full NBT info and thus won't be reliably comparable to 
     * server-side stacks.<p>
     * 
     * This handle is specific to this instance and cannot be used with
     * any other instance. Also transient and should never be persisted.
     */
     public int getHandleForResource(IResource<T> resource);
     
     /**
      * Inverse of {@link #getHandleForResource(IResource)}
      */
     public IResource<T> getResourceForHandle(int handle);
     
}
