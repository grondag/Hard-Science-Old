package grondag.adversity.library.cache.longKey;

import net.minecraft.util.math.MathHelper;


public class LongCompoundLoadingCache<V>
{
    private final LongSimpleLoadingCache<V>[] subs;
    private final int subMask;
    private final int subSize;
    private final LongSimpleCacheLoader<V> loader;

    @SuppressWarnings("unchecked")
    public LongCompoundLoadingCache(LongSimpleCacheLoader<V> loader, int maxSize)
    {
        this.loader = loader;
        subs = (LongSimpleLoadingCache<V>[]) new LongSimpleLoadingCache[MathHelper.smallestEncompassingPowerOfTwo(Runtime.getRuntime().availableProcessors())];
        this.subMask = subs.length - 1;
        this.subSize = Math.max(64, maxSize / subs.length);
        for(int i = 0; i < subs.length; i++)
        {
            subs[i] = new LongSimpleLoadingCache<V>(loader, subSize);
        }
      
        this.clear();
    }

    public void clear()
    {
        subs[0] = new LongSimpleLoadingCache<V>(loader, subSize);
        if(subs.length > 1)
        {
            for(int i = 1; i < subs.length; i++)
            {
                subs[i] = new LongSimpleLoadingCache.NonZeroLongSimpleLoadingCache<V>(loader.createNew(), subSize);
            }    
        }
    }

    public V get(long key)
    {
        return subs[(int) (key & subMask)].get(key);
    }

    /** 
     * Avoid - has to be computed from sub-caches.
     */
    public int size()
    {
        int result = 0;
        for(int i = 0; i < subs.length; i++)
        {
            result += subs[i].size();
        }
        return result;
    }
    
    public LongCompoundLoadingCache<V> createNew(LongSimpleCacheLoader<V> loader, int maxSize)
    {
        return new LongCompoundLoadingCache<V>(loader, maxSize);
    }
}
