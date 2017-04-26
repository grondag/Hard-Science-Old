package grondag.adversity.library.cache.longKey;

import grondag.adversity.library.Useful;

/**
 * Read-only version of an existing cache.
 * Performs no loads and returns null for keys not found.
 * Used by managed cache.
 * @author grondag
 */
public class LongSimpleStaticCache<V> implements ILongLoadingCache<V>
{
    private final LongCacheState<V> state;

    public LongSimpleStaticCache(LongCacheState<V> stateIn)
    {
        this.state = stateIn;
    }
    
    @Override
    public int size() { return state.size.get(); }
    
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
            return state.zeroValue;
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