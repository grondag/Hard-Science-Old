package grondag.hard_science.machines;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.concurrency.ConcurrentForwardingList;
import grondag.hard_science.simulator.wip.IResource;
import grondag.hard_science.simulator.wip.ResourceWithQuantity;
import grondag.hard_science.simulator.wip.StorageType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Maintains a view of a remote inventory on client for inquiry in GUI and displays.
 */
@SideOnly(Side.CLIENT)
public class RemoteStorageProxy<T extends StorageType<T>>
{
    
    public HashMap<IResource<T>, ResourceWithQuantity<T>> MAP = new HashMap<IResource<T>, ResourceWithQuantity<T>>();
    
    public ConcurrentForwardingList<ResourceWithQuantity<T>> LIST = new ConcurrentForwardingList<ResourceWithQuantity<T>>(Collections.emptyList());
    
    public void refreshList()
    {
        
        LIST.setDelegate(ImmutableList.copyOf(MAP.values().stream().sorted(new Comparator<ResourceWithQuantity<T>>(){
        
            @Override
            public int compare(ResourceWithQuantity<T> o1, ResourceWithQuantity<T> o2)
            {
                String s1 = o1.resource().displayName();
                String s2 = o2.resource().displayName();
                if(s1 == null)
                {
                    if(s2 == null) return 0;
                    return 1;
                }
                else if(s2 == null) return -1;
                
                return s1.compareTo(s2);
                
            }}).collect(Collectors.toList())));
    }
}
