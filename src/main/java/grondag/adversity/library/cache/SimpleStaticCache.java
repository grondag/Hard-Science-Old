package grondag.adversity.library.cache;

import grondag.adversity.library.Useful;

public class SimpleStaticCache<V> implements ILoadingCache<V>
{
    private final CacheState<V> state;

    public SimpleStaticCache(CacheState<V> stateIn)
    {
        this.state = stateIn;
    }
    
    @Override
    public void clear()
    {
        //NOOP
    }

    @Override
    public V get(long key)
    {
        
        // Zero value normally indicates an unused spot in key array
        // so requires special handling to prevent search weirdness.
        if(key == 0)
        {
            return state.values[state.zeroLocation];
        }
        
        long keyHash = Useful.longHash(key);
        int position = (int) (keyHash & state.positionMask);
        long currentKey = state.keys[position];       
     
        if(currentKey == key) 
        {
            return state.values[position];
        }
        
        if(currentKey == 0) return null;

        while (true) 
        {
            position = (position + 1) & state.positionMask;
            currentKey = state.keys[position];
            
            if(currentKey == 0) return null;
            
            if(currentKey == key)
            {
                return state.values[position];
            }
        }
    }

}
