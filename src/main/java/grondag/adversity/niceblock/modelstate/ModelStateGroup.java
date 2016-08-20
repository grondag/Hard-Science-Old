package grondag.adversity.niceblock.modelstate;

import java.util.BitSet;
import java.util.HashMap;


public class ModelStateGroup
{
    private static HashMap<BitSet, ModelStateGroup> groups = new HashMap<BitSet, ModelStateGroup>();
    private static int nextOrdinal = 0;
    
    public static int getGroupCount() { return nextOrdinal; }
    
    public static ModelStateGroup find(ModelStateComponent<?,?>... components)
    {
        BitSet key = new BitSet();
        for(ModelStateComponent<?,?> c : components)
        {
            key.set(c.getOrdinal());
        }
        
        ModelStateGroup result; 
        synchronized(groups)
        {
            result = groups.get(key);
            if(result == null)
            {
                result = new ModelStateGroup(components);
                groups.put(key, result);
            }
        }
        return result;
    }
    
    private final ModelStateComponent<?,?>[] components;
    private final int ordinal;
    
    private ModelStateGroup(ModelStateComponent<?, ?>... components)
    {
        this.components = components;
        this.ordinal = nextOrdinal++;
    }
    
    public ModelStateComponent<?,?>[] getComponents()
    {
        return components;
    }
    
    public int getOrdinal() { return this.ordinal; }
}
