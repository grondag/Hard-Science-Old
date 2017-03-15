package grondag.adversity.library.cache;

import grondag.adversity.Adversity;

public class ManagedLoadingCache<V> implements ILoadingCache<V>
{
    private final SimpleCacheLoader<V> loader;
    private final int maxCapacity;
    private final int startingCapacity;
    private volatile SimpleLoadingCache<V> activeCache;
    
    public ManagedLoadingCache(SimpleCacheLoader<V> loader, int startingCapacity, int maxCapacity)
    {
        this.loader = loader;
        this.maxCapacity = maxCapacity;
        this.startingCapacity = startingCapacity;
        this.clear();
    }
    
    @Override
    public void clear()
    {
        this.activeCache = new SimpleLoadingCache<V>(loader, startingCapacity);
    }

    @Override
    public V get(long key)
    {
        V result = activeCache.get(key);
        if(activeCache.getSize() >= maxCapacity)
        {
            activeCache = new SimpleLoadingCache<V>(new BackupCacheLoader<V>(loader, activeCache.getStaticCache()), maxCapacity);
        }
        return result;
    }

    private static class BackupCacheLoader<V> implements SimpleCacheLoader<V>
    {
        private final SimpleCacheLoader<V> primaryLoader;
        private final ILoadingCache<V> backupCache;
        
        private BackupCacheLoader(SimpleCacheLoader<V> primaryLoader, ILoadingCache<V> backupCache)
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
