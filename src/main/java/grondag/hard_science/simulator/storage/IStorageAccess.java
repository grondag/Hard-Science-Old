package grondag.hard_science.simulator.storage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;

import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

/**
 * Shared logic for classes that contain one or more
 * storages (exposed as a list) for searching those 
 * storages.
 */
public interface IStorageAccess<T extends StorageType<T>>
{
    /**
     * Snapshot of stores currently part of this instance.
     */
    public ImmutableList<IStorage<T>> stores();
    
    /**
     * Orders results to encourage clustering of item storage.
     * Stores with the largest count of the resource (but still with empty space)
     * come first, followed by stores with available space in descending order.
     */
    public default ImmutableList<IStorage<T>> findSpaceFor(IResource<T> resource, long quantity)
    {
        return this.stores().stream()
            .filter(p -> p.availableCapacityFor(resource) > 0)
            .sorted((IStorage<T> a, IStorage<T>b) 
                    -> ComparisonChain.start()
                        .compare(b.getQuantityStored(resource), a.getQuantityStored(resource))
                        .compare(b.availableCapacityFor(resource), a.availableCapacityFor(resource))
                        .result()
                    )
            .collect(ImmutableList.toImmutableList());
    }
    
    /**
     * Returns all locations where the resource is stored.
     * Note that the resource may be allocated so the stored
     * quantities may not be available for use, but allocations
     * are not stored by location.<p>
     * 
     * Results are sorted with lowest counts first to encourage
     * emptying of small amounts so that items are clustered.
     */
    public default ImmutableList<IStorage<T>> getLocations(IResource<T> resource)
    {
        return this.stores().stream()
                .filter(p -> p.getQuantityStored(resource) > 0)
                .sorted((IStorage<T> a, IStorage<T>b) 
                        -> Long.compare(a.getQuantityStored(resource), b.getQuantityStored(resource)))
                .collect(ImmutableList.toImmutableList());
    }
    
    /**
     * Aggregate version of {@link IStorage#add(IResource, long, boolean, IProcurementRequest)}
     */
    public default long add(@Nonnull IResource<T> resource, final long howMany, boolean simulate, @Nullable IProcurementRequest<T> request)
    {
        if(howMany <= 0) return 0;
        ImmutableList<IStorage<T>> stores = this.findSpaceFor(resource, howMany);
        if(stores.isEmpty()) return 0;
        if(stores.size() == 1) return stores.get(0).add(resource, howMany, simulate, request);
        
        long demand = howMany;
        long result = 0;
        
        for(IStorage<T> store : stores)
        {
            long added = store.add(resource, demand, simulate, request);
            if(added > 0)
            {
                demand -= added;
                result += added;
                if(demand == 0) break;
            }
        }
        return result;
    }
    
    /**
     * Aggregate version of {@link IStorage#takeUpTo(IResource, long, boolean, IProcurementRequest)}
     */
    public default long takeUpTo(@Nonnull IResource<T> resource, final long howMany, boolean simulate, @Nullable IProcurementRequest<T> request)
    {
        if(howMany <= 0) return 0;
        ImmutableList<IStorage<T>> stores = this.getLocations(resource);
        if(stores.isEmpty()) return 0;
        if(stores.size() == 1) return stores.get(0).takeUpTo(resource, howMany, simulate, request);
        
        long demand = howMany;
        long result = 0;
        
        for(IStorage<T> store : stores)
        {
            long taken = store.takeUpTo(resource, demand, simulate, request);
            if(taken > 0)
            {
                demand -= taken;
                result += taken;
                if(demand == 0) break;
            }
        }
        return result;
    }
}
