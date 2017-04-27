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
            return state.zeroValue.get();
        }
        
        int position = (int) (Useful.longHash(key) & state.positionMask);
        
        do
        {
            if(state.keys[position] == key) return state.values[position];
            if(state.keys[position] == 0) return null;
            position = (position + 1) & state.positionMask;
        } while(true);
    }
}