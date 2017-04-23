package grondag.adversity.library.cache.longKey;

public class LongManagedLoadingCache<V> implements ILongLoadingCache<V>
{
    private final LongSimpleCacheLoader<V> loader;
    private final int maxCapacity;
    private final int startingCapacity;
    private volatile LongSimpleLoadingCache<V> activeCache;
    
    public LongManagedLoadingCache(LongSimpleCacheLoader<V> loader, int startingCapacity, int maxCapacity)
    {
        this.loader = loader;
        this.maxCapacity = maxCapacity;
        this.startingCapacity = startingCapacity;
        this.clear();
    }
    
    @Override
    public void clear()
    {
        this.activeCache = new LongSimpleLoadingCache<V>(loader, startingCapacity);
    }

    @Override
    public V get(long key)
    {
        V result = activeCache.get(key);
        if(activeCache.getSize() >= maxCapacity)
        {
            synchronized(activeCache)
            {
                if(activeCache.getSize() >= maxCapacity)
                {
                    activeCache = new LongSimpleLoadingCache<V>(new BackupCacheLoader<V>(loader, activeCache.getStaticCache()), maxCapacity);
                }
            }
        }
        return result;
    }

    private static class BackupCacheLoader<V> implements LongSimpleCacheLoader<V>
    {
        private final LongSimpleCacheLoader<V> primaryLoader;
        private final ILongLoadingCache<V> backupCache;
        
        private BackupCacheLoader(LongSimpleCacheLoader<V> primaryLoader, ILongLoadingCache<V> backupCache)
        {
            this.primaryLoader = primaryLoader;
            this.backupCache = backupCache;
        }
        
        @Override
        public V load(long key)
        {
            V result = backupCache.get(key);
            if(result == null) result = primaryLoader.load(key);
            return result;
        }
        
    }
}
