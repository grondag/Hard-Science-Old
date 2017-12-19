package grondag.hard_science.simulator.storage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;

import grondag.hard_science.Log;
import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.simulator.Simulator;
import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

public class StorageResourceManager<T extends StorageType<T>>
{
    /**
     * Resource this instance manages.
     */
    public final IResource<T> resource;
    
    /**
     * Total quantityStored of this resource within the domain.
     */
    private long quantityStored;
    
    private long quantityAllocated;
    
    /**
     * List of all storage instances in the domain that contain this resource.
     */
    private final SimpleUnorderedArrayList<IStorage<T>> stores = new SimpleUnorderedArrayList<IStorage<T>>();

    /**
     * Single listener or list of resource listeners.
     */
    private Object listeners = null;

    /**
     * True if listener notification is needed. Set false
     * at start of notification process.  Prevents
     * redundant spamming of listeners when lots of changes
     * happening at once.
     */
    private volatile boolean isListenerNotificationDirty = false;
    
    /**
     * Pair or Map of all allocations for this resource.
     */
    private Object allocations = null;
    
    /**
     * If request is non-null, initial quantityIn will be allocated to that request.
     * Quantity is NOT used to track overall storage amount - that is retrieved from
     * the storage, if non-null.
     * Useful when fabrication is storing items made for procurement requests.
     */
    public StorageResourceManager(IResource<T> resource, IStorage<T> firstStorage, long allocatedQuantity, @Nullable IProcurementRequest<T> request)
    {
        this.resource = resource;
        if(firstStorage != null)
        {
            this.quantityStored = firstStorage.getQuantityStored(resource);
            this.stores.add(firstStorage);
        }
        
        if(request != null)
        {
            this.setAllocation(request, allocatedQuantity);
        }
    }
    
    /**
     * Adds delta to the allocation of this resource for the given request.
     * Return value is the quantityIn removed or added, which could be different than
     * amount requested if not enough is available or would reduce allocation below 0.
     * Total quantityIn allocated can be different from return value if request already had an allocation.
     * Provides no notification to the request.
     */
    public synchronized long changeAllocation(@Nonnull IProcurementRequest<T> request, long quantityRequested)
    {
        long allocated = this.getAllocation(request);
        long newAllocation = Useful.clamp(allocated + quantityRequested, 0, allocated + this.quantityAvailable());
        return this.setAllocation(request, newAllocation) - allocated;
    }
    
    /**
     * Returns current allocation for the given request.
     */
    public synchronized long getAllocation(@Nonnull IProcurementRequest<T> request)
    {
        if(this.allocations == null) return 0;
        
        if(this.allocations instanceof Pair)
        {
            @SuppressWarnings("unchecked")
            Pair<IProcurementRequest<T>, Long> pair = (Pair<IProcurementRequest<T>, Long>)this.allocations;
            return pair.getLeft() == request ? pair.getRight() : 0;
        }
        else
        {
            @SuppressWarnings("unchecked")
            Object2LongOpenHashMap<IProcurementRequest<T>> map = (Object2LongOpenHashMap<IProcurementRequest<T>>)this.allocations;
            return map.getLong(request);
        }
    }
    
    /**
     * Sets allocation for the given request to the provided, non-negative value.
     * Returns allocation that was actually set.  Will not set negative allocations
     * and will not set allocation so that total allocated is more the total available.
     * Provides no notification to the request.
     */
    public synchronized long setAllocation(@Nonnull IProcurementRequest<T> request, long requestedAllocation)
    {
        if(requestedAllocation < 0)
        {
            Log.warn("StorageResourceManager received request to set resource allocation less than zero. This is a bug.");
            requestedAllocation = 0;
        }

        // exit if no change to allocation for this request
        final long currentAllocation = this.getAllocation(request);
        long delta = requestedAllocation - currentAllocation;
        if(delta == 0) return currentAllocation;
        
        long startingAvailable = this.quantityAvailable();
        
        // don't allocate more than we are storing
        if(delta > startingAvailable)
        {
            delta = startingAvailable;
            requestedAllocation = currentAllocation + delta;
        }
        
        if(delta == 0) return currentAllocation;
        
        // update tracking total
        this.quantityAllocated += delta;
        
        if(this.allocations == null)
        {
            // first allocation is stored as a Pair
            if(requestedAllocation > 0)
            {
                this.allocations = Pair.of(request, requestedAllocation);
            }
        }
        else if(this.allocations instanceof Pair)
        {
            @SuppressWarnings("unchecked")
            Pair<IProcurementRequest<T>, Long> pair = (Pair<IProcurementRequest<T>, Long>)this.allocations;
            if(pair.getLeft() == request)
            {
                // already tracking this request as the only allocation
                if(requestedAllocation == 0)
                {
                    this.allocations = null;
                }
                else
                {
                    pair.setValue(requestedAllocation);
                }
            }
            else
            {
                // need to start tracking more than one allocation
                // upgrade tracking data structure to HashMap
                Object2LongOpenHashMap<IProcurementRequest<T>> map = new Object2LongOpenHashMap<IProcurementRequest<T>>();
                map.put(pair.getLeft(), pair.getRight());
                map.put(request, requestedAllocation);
                this.allocations = map;
            }
        }
        else
        {
            //already using a hash map
            @SuppressWarnings("unchecked")
            Object2LongOpenHashMap<IProcurementRequest<T>> map = (Object2LongOpenHashMap<IProcurementRequest<T>>)this.allocations;
            if(requestedAllocation == 0)
            {
                map.remove(request);
            }
            else
            {
                map.put(request, requestedAllocation);
            }
        }
        
        // asynch - returns immediately
        if(this.quantityAvailable() != startingAvailable) this.notifyListenersOfAvailability();
        
        return requestedAllocation;
    }
    
    /**
     * Does not include allocated amounts.
     */
    public synchronized int quantityAvailable()
    {
        return (int) Math.max(0, this.quantityStored - this.quantityAllocated);
    }
    
    /**
     * Includes allocated amounts.
     */
    public synchronized int quantityStored()
    {
        return (int) this.quantityStored;
    }
    
    /**
     * Amount of stored resource that is allocated to procurement tasks.
     */
    public synchronized int quantityAllocated()
    {
        return (int) this.quantityAllocated;
    }
    
    /**
     * Called by storage when resources are removed.
     * If request is non-null, then the amount taken reduces any allocation to that request.
     */
    public synchronized void notifyTaken(IStorage<T> storage, long taken, @Nullable IProcurementRequest<T> request)
    {
        if(taken == 0) return;
        
        if(taken > this.quantityStored)
        {
            taken = this.quantityStored;
            Log.warn("Resource manager encounted request to take more than current inventory level.  This is a bug.");
        }
        
        // remove storage from list if no longer holding resource
        if(storage.getQuantityStored(resource) == 0)
        {
            stores.removeIfPresent(storage);
        }
        
        /**
         * To check for availability notification at end
         */
        final long startingAvailable = this.quantityAvailable();
        
        // update resource qty
        this.quantityStored -= taken;
        
        // update allocation if request provided
        if(request != null) this.changeAllocation(request, -taken);
        
        // check for and notify of broken allocations
        if(this.quantityStored < this.quantityAllocated)
        {
            if(this.allocations == null)
            {
                Log.warn("Storage Resource Manager tracking non-zero allocation but has no allocation requests. This is a bug.");
            }
            else if(this.allocations instanceof Pair)
            {
                // single allocation, set allocation to total stored and notify
                @SuppressWarnings("unchecked")
                Pair<IProcurementRequest<T>, Long> pair = (Pair<IProcurementRequest<T>, Long>)this.allocations;
                long newAllocation = this.setAllocation(pair.getLeft(), this.quantityStored);
                pair.getLeft().breakAllocation(this.resource, newAllocation);
            }
            else
            {
                // Multiple allocations, have to decide which one is impacted.
                @SuppressWarnings("unchecked")
                Object2LongOpenHashMap<IProcurementRequest<T>> map = (Object2LongOpenHashMap<IProcurementRequest<T>>)this.allocations;
                
                // Sort by priority and seniority
                List<Entry<IProcurementRequest<T>>> list =
                map.object2LongEntrySet().stream()
                .sorted(new Comparator<Entry<IProcurementRequest<T>>>() 
                {
                    @Override
                    public int compare(Entry<IProcurementRequest<T>> o1, Entry<IProcurementRequest<T>> o2)
                    {
                        // note reverse order
                        IProcurementRequest<T> k1 = o2.getKey();
                        IProcurementRequest<T> k2 = o1.getKey();
                        
                        return ComparisonChain.start()
                                .compare(k1.job().getPriority().ordinal(), k2.job().getPriority().ordinal())
                                .compare(k1.getId(), k2.getId())
                                .result();
                    }
                })
                .collect(Collectors.toList());
                
                // Break allocations until allocation total is within the stored amount.
                for(Entry<IProcurementRequest<T>> entry : list)
                {
                    long gap = this.quantityAllocated - this.quantityStored;
                    if(gap <= 0) break;
                    
                    long delta = Math.min(gap, entry.getLongValue());
                    if(delta > 0)
                    {
                        IProcurementRequest<T> brokenRequest = entry.getKey();
                        if(this.changeAllocation(brokenRequest, -delta) != 0)
                        {
                            brokenRequest.breakAllocation(this.resource, this.getAllocation(brokenRequest));
                        }
                    }
                }
            }
        }
        
        // asynch - returns immediately
        if(startingAvailable != this.quantityAvailable()) this.notifyListenersOfAvailability();
    }

    /**
     * If request is non-null, then the amount added is immediately allocated to that request.
     */
    public synchronized void notifyAdded(IStorage<T> storage, long added, @Nullable IProcurementRequest<T> request)
    {
        if(added == 0) return;
        
        // track store for this resource
        if(this.stores.addIfNotPresent(storage))
        {
            // if store was added, confirm amount added
            // matches the total amount in the store
            if(storage.getQuantityStored(resource) != added)
            {
                Log.warn("Storage Resource Manager encountered request to add a quanity in a new storage"
                        + " that did not match the quantityIn in the storage. This is a bug.");
            }
        }
        
        /**
         * To check for availability notification at end
         */
        final long startingAvailable = this.quantityAvailable();
        
        // update resource qty
        this.quantityStored += added;
        
        // update allocation if request provided
        if(request != null) this.changeAllocation(request, added);
        
        // asynch - returns immediately
        if(startingAvailable != this.quantityAvailable()) this.notifyListenersOfAvailability();
    }
    
    /**
     * Returns all locations where the resource is stored,
     * irrespective of allocation.
     */
    public synchronized List<StorageWithQuantity<T>> getLocations(IResource<T> resource)
    {
        ImmutableList.Builder<StorageWithQuantity<T>> builder = ImmutableList.builder();
        for(IStorage<T> store : this.stores)
        {
            long quantity = store.getQuantityStored(resource);
            if(quantity > 0)
            {
                builder.add(store.withQuantity(quantity));
            }
        }
        return builder.build();
    }
    
    /**
     * Adds all non-zero storage locations to the given list builder.
     */
    public synchronized void addStoragesWithQuantityToBuilder(ImmutableList.Builder<StorageWithResourceAndQuantity<T>> builder)
    {
        for(IStorage<T> store : this.stores)
        {
            long quantity = store.getQuantityStored(this.resource);
            if(quantity > 0)
            {
                builder.add(store.withResourceAndQuantity(this.resource, quantity));
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public synchronized void registerResourceListener(IStorageResourceListener<T> listener)
    {
        if(this.listeners == null)
        {
            this.listeners = listener;
        }
        else if(this.listeners instanceof IStorageResourceListener)
        {
            if(this.listeners != listener)
            {
            // upgrade to list
                SimpleUnorderedArrayList<IStorageResourceListener<T>> list
                     = new SimpleUnorderedArrayList<IStorageResourceListener<T>>();
                list.add((IStorageResourceListener<T>)this.listeners);
                list.add(listener);
            }
        }
        else
        {
            // should already be using list
            ((SimpleUnorderedArrayList<IStorageResourceListener<T>>)this.listeners)
                .addIfNotPresent(listener);
        }
    }
    
    public synchronized void unregisterResourceListener(IStorageResourceListener<T> listener)
    {
        if(this.listeners == null)
        {
            return;
        }
        else if(this.listeners instanceof IStorageResourceListener)
        {
            // should already be using list
            if(this.listeners == listener)
            {
                this.listeners = null;
            }
        }
        else
        {
            @SuppressWarnings("unchecked")
            SimpleUnorderedArrayList<IStorageResourceListener<T>> list
             = (SimpleUnorderedArrayList<IStorageResourceListener<T>>)this.listeners;
            list.removeIfPresent(listener);
            if(list.isEmpty()) this.listeners = null;
        }
    }
    
    /**
     * Called whenever availability changes.  Returns immediately
     * and schedules notification to happen asynchronously. This
     * prevents a chain of notification-driven changes involving
     * listener actions that would all have to be resolved before
     * the initiating listener gets a result.<p>
     * 
     * Sets the {@link #isListenerNotificationDirty} flag so that
     * it is not necessary to do this in caller.
     */
    @SuppressWarnings({ "unchecked" })
    protected void notifyListenersOfAvailability()
    {
        if(this.listeners == null)
        {
            return;
        }
        
        this.isListenerNotificationDirty = true;
        
        Simulator.SIMULATION_POOL.execute(new Runnable()
        {
            @Override
            public void run()
            {
                // could get submitted multiple times before we get executed, 
                // but only need to run once until there is another change
                if(!StorageResourceManager.this.isListenerNotificationDirty) return;
                
                StorageResourceManager.this.isListenerNotificationDirty = false;
                
                // just in case listeners reference changes while we are executing
                Object listeners = StorageResourceManager.this.listeners;
                
                if(listeners == null) return;
                
                if(listeners instanceof IStorageResourceListener)
                {
                    ((IStorageResourceListener<T>)listeners).onAvailabilityChange(resource, quantityAvailable());
                }
                else
                {
                    // Using an array copy of the list because
                    // a listener might unregister as a result of being called 
                    // and then the order could change and we'd skip a listener.
                    for(Object listener : ((SimpleUnorderedArrayList<IStorageResourceListener<T>>)listeners).toArray())
                    {
                        // Note that we query available quantityIn each time
                        // because some listeners might take action that would 
                        // change the availability.
                        ((IStorageResourceListener<T>)listener).onAvailabilityChange(resource, quantityAvailable());
                    }
                }
            }
    
        });
    }
    
    /**
     * If true, has no storages and no listeners and can thus be safely discarded by the storage manager.
     * Implies all quantities are zero.
     */
    @SuppressWarnings("unchecked")
    public boolean isEmpty()
    {
        if(this.listeners == null && this.stores.isEmpty())
        {
            assert(this.allocations == null 
                    || ((Object2LongOpenHashMap<IProcurementRequest<T>>)this.allocations).isEmpty())
                : "StorageResourceManager empty with non-null allocations";
            
            assert(this.quantityAllocated() == 0)
                : "StorageResourceManager empty with non-zero allocations";
            
            assert(this.quantityAvailable() == 0)
                : "StorageResourceManager empty with non-zero available";

            assert(this.quantityStored() == 0)
                : "StorageResourceManager empty with non-zero stored";

            return true;
        }
        return false;
    }
}