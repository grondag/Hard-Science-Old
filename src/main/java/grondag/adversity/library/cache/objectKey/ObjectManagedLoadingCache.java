package grondag.adversity.library.cache.objectKey;


public class ObjectManagedLoadingCache<K,V> implements IObjectLoadingCache<K, V>
{
    private final ObjectSimpleCacheLoader<K, V> loader;
    private final int maxCapacity;
    private final int startingCapacity;
    private volatile ObjectSimpleLoadingCache<K, V> activeCache;
    
    public ObjectManagedLoadingCache(ObjectSimpleCacheLoader<K, V> loader, int startingCapacity, int maxCapacity)
    {
        this.loader = loader;
        this.maxCapacity = maxCapacity;
        this.startingCapacity = startingCapacity;
        this.clear();
    }
    
    @Override
    public void clear()
    {
        this.activeCache = new ObjectSimpleLoadingCache<K, V>(loader, startingCapacity);
    }

    @Override
    public V get(K key)
    {
        V result = activeCache.get(key);
        if(activeCache.getSize() >= maxCapacity)
        {
            activeCache = new ObjectSimpleLoadingCache<K, V>(new BackupCacheLoader<K, V>(loader, activeCache.getStaticCache()), maxCapacity);
        }
        return result;
    }

    private static class BackupCacheLoader<K, V> implements ObjectSimpleCacheLoader<K, V>
    {
        private final ObjectSimpleCacheLoader<K, V> primaryLoader;
        private final IObjectLoadingCache<K, V> backupCache;
        
        private BackupCacheLoader(ObjectSimpleCacheLoader<K, V> primaryLoader, IObjectLoadingCache<K, V> backupCache)
        {
            this.primaryLoader = primaryLoader;
            this.backupCache = backupCache;
        }
        
        @Override
        public V load(K key)
        {
            V result = backupCache.get(key);
            if(result == null) result = primaryLoader.load(key);
            return result;
        }
        
    }
}
