package grondag.hard_science.simulator.wip;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.Log;
import grondag.hard_science.library.concurrency.ConcurrentForwardingList;
import grondag.hard_science.simulator.wip.StorageType.StorageTypeStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Maintains a view of an IStorage on client for currently open container.
 */
@SideOnly(Side.CLIENT)
public class OpenContainerStorageProxy<T extends StorageType<T>> implements IStorageListener<T>
{
    
    public static final Comparator<AbstractResourceWithQuantity<?>> SORT_BY_NAME = new Comparator<AbstractResourceWithQuantity<?>>()
    {
        @Override
        public int compare(AbstractResourceWithQuantity<?> o1, AbstractResourceWithQuantity<?> o2)
        {
            if(o1 == null)
            {
                //FIXME: remove
                Log.warn("null resource in sort?");
                if(o2 == null) 
                {
                    //FIXME: remove
                    Log.warn("null resource in sort?");
                    return 0;
                }
                return 1;
            }
            else if(o2 == null) 
            {
                //FIXME: remove
                Log.warn("null resource in sort?");

                return -1;
            }
            
            String s1 = o1.resource().displayName();
            String s2 = o2.resource().displayName();
            return s1.compareTo(s2);
        }
    };
    
    // needs to come *after* the sort declaration, dumbass...
    public static OpenContainerStorageProxy<StorageTypeStack> ITEM_PROXY = new OpenContainerStorageProxy<StorageTypeStack>();
    
    private OpenContainerStorageProxy() {};

    private HashMap<IResource<T>, AbstractResourceWithQuantity<T>> MAP = new HashMap<IResource<T>, AbstractResourceWithQuantity<T>>();
    
    public final ConcurrentForwardingList<AbstractResourceWithQuantity<T>> LIST = new ConcurrentForwardingList<AbstractResourceWithQuantity<T>>(Collections.emptyList());
    
    private boolean isDirty = false;
    
    private Comparator<AbstractResourceWithQuantity<?>> sort = SORT_BY_NAME;
    
    /**
     * Incorporates changes and updates sort order.
     * Returns true if a refresh was performed.
     */
    public boolean refreshListIfNeeded()
    {
        if(!this.isDirty) return false;
        
        LIST.setDelegate(ImmutableList.copyOf(MAP.values().stream().sorted(this.sort).collect(Collectors.toList())));
        
        this.isDirty = false;
        
        return true;
    }
    
    public void setSort(Comparator<AbstractResourceWithQuantity<?>> sort)
    {
        if(sort != this.sort)
        {
            this.sort = sort;
            this.isDirty = true;
        }
    }
  

    @Override
    public void handleStorageRefresh(IStorage<T> sender, List<AbstractResourceWithQuantity<T>> update)
    {
        this.MAP.clear();
        for(AbstractResourceWithQuantity<T> item : update )
        {
            this.MAP.put(item.resource(), item);
        }
        this.isDirty = true;
        this.refreshListIfNeeded();
    }

    @Override
    public void handleStorageUpdate(IStorage<T> sender, AbstractResourceWithQuantity<T> update)
    {
        if(update.quantity == 0)
        {
            this.MAP.remove(update.resource());
        }
        else
        {
            this.MAP.put(update.resource(), update);
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
}
