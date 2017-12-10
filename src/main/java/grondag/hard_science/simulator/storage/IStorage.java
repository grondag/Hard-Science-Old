package grondag.hard_science.simulator.storage;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.library.world.Location.ILocated;
import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.domain.IDomainMember;
import grondag.hard_science.simulator.persistence.IIdentified;
import grondag.hard_science.simulator.resource.AbstractResourceDelegate;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ITypedStorage;
import grondag.hard_science.simulator.resource.StorageType;

public interface IStorage<T extends StorageType<T>> extends IReadWriteNBT, ILocated, IDomainMember, ISizedContainer, ITypedStorage<T>, IIdentified
{
    long getQuantityStored(IResource<T> resource);
    
    void setOwner(StorageManager<T> owner);
    
    default boolean isResourceAllowed(IResource<T> resource) { return true; }
    
    /**
     * Override if this storage can hold only certain resources.
     */
    default long availableCapacityFor(IResource<T> resource)
    {
        return this.availableCapacity();
    }
    
     /**
     * Increases quantityStored and returns quantityStored actually added.
     * If simulate==true, will return forecasted result without making changes.
     * Intended to be thread-safe. <p>
     * 
     * If request is non-null and not simulated, the amount added will be
     * allocated (domain-wide) to the given request.
     */
    long add(IResource<T> resource, long howMany, boolean simulate, @Nullable IProcurementRequest<T> request);

    /** Alternative syntax for {@link #add(IResource, long, boolean)} */
    default long add(AbstractResourceWithQuantity<T> resourceWithQuantity, boolean simulate, @Nullable IProcurementRequest<T> request)
    {
        return this.add(resourceWithQuantity.resource(), resourceWithQuantity.getQuantity(), simulate, request);
    }
    
    /**
     * Takes up to limit from this stack and returns how many were actually taken.
     * If simulate==true, will return forecasted result without making changes.
     * Intended to be thread-safe. <p>
     * 
     * If request is non-null and not simulated, the amount taken will reduce any
     * allocation (domain-wide) to the given request.
     */
    long takeUpTo(IResource<T> resource, long limit, boolean simulate, @Nullable IProcurementRequest<T> request);

    /**
     * Returned resource stacks are disconnected from this collection.
     * Changing them will have no effect on storage contents.
     */
    List<AbstractResourceWithQuantity<T>> find(Predicate<IResource<T>> predicate);
  
    /**
     * Just like {@link #find(Predicate)} but returns delegates instead.
     */
    List<AbstractResourceDelegate<T>> findDelegates(Predicate<IResource<T>> predicate);
    
    public default StorageWithQuantity<T> withQuantity(long quantity)
    {
        return new StorageWithQuantity<T>(this, quantity);
    }
    
    public default StorageWithResourceAndQuantity<T> withResourceAndQuantity(IResource<T> resource, long quantity)
    {
        return new StorageWithResourceAndQuantity<T>(this, resource, quantity);
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
        //FIXME: remove
        Log.info("adding listener to storage " + this.getId());
        
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
        //FIXME: remove
        Log.info("removing listener from storage " + this.getId());
        
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
            
            //FIXME: remove
            Log.info(String.format("refreshing %d listeners for storage %d", listeners.size(), this.getId()));
            
            List<AbstractResourceDelegate<T>> refresh = this.findDelegates(this.storageType().MATCH_ANY);
            for(Object listener : listeners.toArray())
            {
                IStorageListener<T> l = (IStorageListener<T>)listener;
                if(l.isClosed())
                {
                    //FIXME: remove
                    Log.info("removing listener from storage " + this.getId());
                    
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
        
        //FIXME: remove
        Log.info(String.format("updating %d listeners for storage %d", listeners.size(), this.getId()));

        AbstractResourceDelegate<T> delegate = update.toDelegate();
        
        for(Object listener : listeners.toArray())
        {
            @SuppressWarnings("unchecked")
            IStorageListener<T> l = (IStorageListener<T>)listener;
            if(l.isClosed())
            {
                //FIXME: remove
                Log.info("removing listener from storage " + this.getId());

                this.removeListener(l);
            }
            else
            {
                l.handleStorageUpdate(this, delegate);
            }
        }
    }
}