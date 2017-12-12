package grondag.hard_science.machines.support;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.concurrency.ConcurrentForwardingList;
import grondag.hard_science.simulator.resource.AbstractResourceDelegate;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.IListenableStorage;
import grondag.hard_science.simulator.storage.IStorageListener;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Maintains a view of an IStorage on client for currently open container.
 */
@SideOnly(Side.CLIENT)
public class OpenContainerStorageProxy<T extends StorageType<T>> implements IStorageListener<T>
{    
    public static OpenContainerStorageProxy<StorageTypeStack> ITEM_PROXY = new OpenContainerStorageProxy<StorageTypeStack>();
    
    private OpenContainerStorageProxy() {};

    private Int2ObjectOpenHashMap<AbstractResourceDelegate<T>> MAP = new Int2ObjectOpenHashMap<AbstractResourceDelegate<T>>();
    
    public final ConcurrentForwardingList<AbstractResourceDelegate<T>> LIST = new ConcurrentForwardingList<AbstractResourceDelegate<T>>(Collections.emptyList());
    
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
        Comparator<AbstractResourceDelegate<?>> sort = AbstractResourceDelegate.SORT[this.sortIndex];
        
        LIST.setDelegate(ImmutableList.copyOf(MAP.values().stream().sorted(sort).collect(Collectors.toList())));
        
        this.isDirty = false;
        
        return true;
    }
    
    @Override
    public void handleStorageRefresh(IListenableStorage<T> sender, List<AbstractResourceDelegate<T>> update, long capacity)
    {
        this.MAP.clear();
        this.capacity = capacity;
        this.usedCapacity = 0;
        for(AbstractResourceDelegate<T> item : update )
        {
            this.MAP.put(item.handle(), item);
            this.usedCapacity += item.quantity();
        }
        this.isDirty = true;
        this.refreshListIfNeeded();
    }

    @Override
    public void handleStorageUpdate(IListenableStorage<T> sender, AbstractResourceDelegate<T> update)
    {
        AbstractResourceDelegate<T> prior = this.MAP.get(update.handle());
        if(prior != null) this.usedCapacity -= prior.quantity();

        if(update.quantity() == 0)
        {
            this.MAP.remove(update.handle());
        }
        else
        {
            this.MAP.put(update.handle(), update);
            this.usedCapacity += update.quantity();
        }
        this.isDirty = true;
    }

    @Override
    public void handleStorageDisconnect(IListenableStorage<T> storage)
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
