package grondag.adversity.library.cache.objectKey;

/**
 * Read-only version of an existing cache.
 * Performs no loads and returns null for keys not found.
 * Used by managed cache.
 * @author grondag
 */
public class ObjectSimpleStaticCache<K, V> implements IObjectLoadingCache<K, V>
{
    private final ObjectCacheState<K,V> state;

    public ObjectSimpleStaticCache(ObjectCacheState<K, V> stateIn)
    {
        this.state = stateIn;
    }
    
    @Override
    public void clear()
    {
        //NOOP
    }

    @Override
    public V get(K key)
    {
        
        // Null value normally indicates an unused spot in key array
        // so requires special handling to prevent search weirdness.
        if(key == null)
        {
            return state.values[state.nullLocation];
        }
        
        int keyHash = key.hashCode();
        int position = (int) (keyHash & state.positionMask);
        K currentKey = state.keys[position];       
     
        if(currentKey == null) return null;

        if(currentKey.equals(key)) 
        {
            return state.values[position];
        }

        while (true) 
        {
            position = (position + 1) & state.positionMask;
            currentKey = state.keys[position];
            
            if(currentKey == null) return null;
            
            if(currentKey.equals(key))
            {
                return state.values[position];
            }
        }
    }

}