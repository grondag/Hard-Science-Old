package grondag.hard_science.machines.support;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.concurrency.ConcurrentForwardingList;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.IStorage;
import grondag.hard_science.simulator.storage.IStorageListener;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Maintains a view of an IStorage on client for currently open container.
 */
@SideOnly(Side.CLIENT)
public class OpenContainerStorageProxy<T extends StorageType<T>> implements IStorageListener<T>
{    
    // needs to come *after* the sort declaration, dumbass...
    public static OpenContainerStorageProxy<StorageTypeStack> ITEM_PROXY = new OpenContainerStorageProxy<StorageTypeStack>();
    
    private OpenContainerStorageProxy() {};

    private HashMap<IResource<T>, AbstractResourceWithQuantity<T>> MAP = new HashMap<IResource<T>, AbstractResourceWithQuantity<T>>();
    
    public final ConcurrentForwardingList<AbstractResourceWithQuantity<T>> LIST = new ConcurrentForwardingList<AbstractResourceWithQuantity<T>>(Collections.emptyList());
    
    private boolean isDirty = false;
    
    private int sortIndex = 0;
    
    private long capacity;
    private long usedCapacity;
    
    /**
     * Incorporates changes and updates sort order.
     * Returns true if a refresh was performed.
     */

    public boolean refreshListIfNeeded()
    {
        if(!this.isDirty) return false;
        
        @SuppressWarnings("unchecked")
        Comparator<AbstractResourceWithQuantity<?>> sort = AbstractResourceWithQuantity.SORT[this.sortIndex];
        
        LIST.setDelegate(ImmutableList.copyOf(MAP.values().stream().sorted(sort).collect(Collectors.toList())));
        
        this.isDirty = false;
        
        return true;
    }
    
    @Override
    public void handleStorageRefresh(IStorage<T> sender, List<AbstractResourceWithQuantity<T>> update, long capacity)
    {
        this.MAP.clear();
        this.capacity = capacity;
        this.usedCapacity = 0;
        for(AbstractResourceWithQuantity<T> item : update )
        {
            this.MAP.put(item.resource(), item);
            this.usedCapacity += item.getQuantity();
        }
        this.isDirty = true;
        this.refreshListIfNeeded();
    }

    @Override
    public void handleStorageUpdate(IStorage<T> sender, AbstractResourceWithQuantity<T> update)
    {
        AbstractResourceWithQuantity<T> prior = this.MAP.get(update.resource());
        if(prior != null) this.usedCapacity -= prior.getQuantity();

        if(update.getQuantity() == 0)
        {
            this.MAP.remove(update.resource());
        }
        else
        {
            this.MAP.put(update.resource(), update);
            this.usedCapacity += update.getQuantity();
        }
        this.isDirty = true;
    }

    @Override
    public void handleStorageDisconnect(IStorage<T> storage)
    {
        this.MAP.clear();
        this.LIST.setDelegate(Collections.emptyList());
        this.isDirty = false;
    }

    @Override
    public boolean isClosed()
    {
        return false;
    }

    public int getSortIndex()
    {
        return this.sortIndex;
    }

    public void setSortIndex(int sortIndex)
    {
        this.sortIndex = sortIndex;
        this.isDirty = true;
    }

    public long capacity()
    {
        return capacity;
    }

    public long usedCapacity()
    {
        return usedCapacity;
    }
    
    public int fillPercentage()
    {
        return this.capacity == 0 ? 0 : (int) (this.usedCapacity * 100 / this.capacity);
    }
}
