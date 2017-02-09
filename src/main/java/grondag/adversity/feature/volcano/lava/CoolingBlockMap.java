package grondag.adversity.feature.volcano.lava;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import grondag.adversity.Adversity;


/** Works just like a linked list, except won't add an element if already included in the list */

public class CoolingBlockMap
{
    private HashSet<AgedBlockPos> set = new HashSet<AgedBlockPos>();
    private LinkedList<AgedBlockPos> list = new LinkedList<AgedBlockPos>();
    
    public boolean isEmpty()
    {
        return set.isEmpty();
    }
    
    public int size()
    {
        return set.size();
    }
    
    public AgedBlockPos pollFirst()
    {
        AgedBlockPos result = list.pollFirst();
        set.remove(result);
        
        if(Adversity.DEBUG_MODE && set.size() != list.size())
        {
            Adversity.log.warn("basalt tracking error in pollfirst");
        }
        return result;
    }
    
    public void add(AgedBlockPos apos)
    {
        if(set.add(apos))
        {
            list.add(apos);
        }
        if(Adversity.DEBUG_MODE && set.size() != list.size())
        {
            Adversity.log.warn("basalt tracking error in add");
        }
    }
    
    public void clear()
    {
        this.set.clear();
        this.list.clear();
    }
    
    public Collection<AgedBlockPos> values()
    {
        return this.list;
    }
}
