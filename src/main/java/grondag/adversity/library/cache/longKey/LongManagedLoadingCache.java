package grondag.adversity.library.cache.longKey;

import java.util.concurrent.atomic.AtomicInteger;

public class LongManagedLoadingCache<V> implements ILongLoadingCache<V>, CacheOversizeHandler
{
    private final LongSimpleCacheLoader<V> loader;
    private final int maxCapacity;
    private volatile ILongLoadingCache<V> activeCache;
    
    public LongManagedLoadingCache(ILongLoadingCache<V> startingCache, int maxCapacity)
    {
        startingCache.setOversizeHandler(this);
        this.activeCache = startingCache;
        this.loader = startingCache.getLoader();
        this.maxCapacity = maxCapacity;
        this.clear();
    }
    
    @Override
    public int size() { return activeCache.size(); }
    
    @Override
    public void clear()
    {
        this.activeCache.clear();
    }

    @Override
    public V get(long key)
    {
        return activeCache.get(key);
    }

    private static class BackupCacheLoader<V> implements LongSimpleCacheLoader<V>
    {
        private final LongSimpleCacheLoader<V> primaryLoader;
        private volatile ILongLoadingCache<V> backupCache;
        private final AtomicInteger backupMisses = new AtomicInteger(100);
        private final int missAllowance;
                
        
        private BackupCacheLoader(LongSimpleCacheLoader<V> primaryLoader, ILongLoadingCache<V> backupCache)
        {
            this.primaryLoader = primaryLoader;
            this.backupCache = backupCache;
            this.missAllowance = backupCache.size() / 2;
        }
        
        @Override
        public V load(long key)
        {
            ILongLoadingCache<V> backup = this.backupCache;
            if(backup == null)
            {
                return primaryLoader.load(key);
            }
            else
            {
                V result = backup.get(key);
                if(result == null)
                {
                    if(backupMisses.incrementAndGet() > missAllowance && this.backupCache != null) 
                    {
                        this.backupCache = null;
                    }
                    return primaryLoader.load(key);
                }
                else
                {
                    return result;
                }
            }
        }
        
    }

    @Override
    public void notifyOversize()
    {
        ILongLoadingCache<V> newCache = activeCache.createNew(new BackupCacheLoader<V>(loader, activeCache.getStaticCache()), maxCapacity);
        newCache.setOversizeHandler(this);
        activeCache = newCache;
    }
}
