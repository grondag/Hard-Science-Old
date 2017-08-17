package grondag.hard_science.simulator.wip;

import java.util.List;
import java.util.function.Predicate;

import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.library.world.Location.ILocated;
import grondag.hard_science.simulator.persistence.IReadWriteNBT;
import grondag.hard_science.simulator.wip.AssignedNumbersAuthority.IIdentified;
import grondag.hard_science.simulator.wip.DomainManager.IDomainMember;
import grondag.hard_science.simulator.wip.StorageType.ITypedStorage;

public interface IStorage<T extends StorageType<T>> extends IReadWriteNBT, ILocated, IDomainMember, ISizedContainer, ITypedStorage<T>, IIdentified
{
    long getQuantityStored(IResource<T> resource);
    
    void setOwner(AbstractStorageManager<T> owner);
    
    /**
     * Override if this storage can hold only certain resources.
     */
    default long availableCapacityFor(IResource<T> resource)
    {
        return this.availableCapacity();
    }
    
     /**
     * Increases quantity and returns quantity actually added.
     * If simulate==true, will return forecasted result without making changes.
     * Intended to be thread-safe.
     */
    long add(IResource<T> resource, long howMany, boolean simulate);

    /**
     * Takes up to limit from this stack and returns how many were actually taken.
     * If simulate==true, will return forecasted result without making changes.
     * Intended to be thread-safe.
     */
    long takeUpTo(IResource<T> resource, long limit, boolean simulate);

    /**
     * Returned resource stacks are disconnected from this collection.
     * Changing them will have no effect on storage contents.
     */
    List<AbstractResourceWithQuantity<T>> find(Predicate<IResource<T>> predicate);
  
    public default StorageWithQuantity<T> withQuantity(long quantity)
    {
        return new StorageWithQuantity<T>(this, quantity);
    }
    
    public default StorageWithResourceAndQuantity<T> withResourceAndQuantity(IResource<T> resource, long quantity)
    {
        return new StorageWithResourceAndQuantity<T>(this, resource, quantity);
    }
    
    /**
     * Simple data class for returning store-related inquiry results.
     */
    public static class StorageWithQuantity<T extends StorageType<T>>
    {
        public final IStorage<T> storage;
        public final long quantity;
        
        public StorageWithQuantity(IStorage<T> storage, long quantity)
        {
            this.storage = storage;
            this.quantity = quantity;
        }
    }
    
    /**
     * Simple data class for returning store-related inquiry results.
     */
    public static class StorageWithResourceAndQuantity<T extends StorageType<T>> extends StorageWithQuantity<T>
    {
        public final IResource<T> resource;
        
        public StorageWithResourceAndQuantity(IStorage<T> storage, IResource<T> resource, long quantity)
        {
            super(storage, quantity);
            this.resource = resource;
        }
    }
    
    /**
     * Create this in your instance and interface will handle the rest of the implementation
     * for storage listener support.
     */
    public SimpleUnorderedArrayList<IStorageListener<T>> listeners();

    /**
     * IStorage should call back with full refresh followed by updates as needed.
     */
    public default void addListener(IStorageListener<T> listener)
    {
        synchronized(this)
        {
            this.listeners().addIfNotPresent(listener);
            listener.handleStorageRefresh(this, this.find(this.storageType().MATCH_ANY));
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
    
    @SuppressWarnings("unchecked")
    public default void refreshAllListeners()
    {
        synchronized(this)
        {
            SimpleUnorderedArrayList<IStorageListener<T>> listeners = this.listeners();
            
            if(listeners.isEmpty()) return;
            
            List<AbstractResourceWithQuantity<T>> refresh = this.find(this.storageType().MATCH_ANY);
            for(Object listener : listeners.toArray())
            {
                IStorageListener<T> l = (IStorageListener<T>)listener;
                if(l.isClosed())
                {
                    this.removeListener(l);
                }
                else
                {
                    l.handleStorageRefresh(this, refresh);
                }
            }
        }
    }
    
    /**
     * Assumes caller already has the quantity because it knew we need an update...
     */
    public default void updateListeners(AbstractResourceWithQuantity<T> update)
    {
        SimpleUnorderedArrayList<IStorageListener<T>> listeners = this.listeners();
        
        if(listeners.isEmpty()) return;
        for(Object listener : listeners.toArray())
        {
            IStorageListener<T> l = (IStorageListener<T>)listener;
            if(l.isClosed())
            {
                this.removeListener(l);
            }
            else
            {
                l.handleStorageUpdate(this, update);
            }
        }
    }
    
}