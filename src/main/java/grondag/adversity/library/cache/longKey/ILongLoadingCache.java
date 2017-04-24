package grondag.adversity.library.cache.longKey;

public interface ILongLoadingCache<V>
{

    void clear();

    V get(long key);
    
    int size();
    
    public default ILongLoadingCache<V> createNew(LongSimpleCacheLoader<V> loader, int startingCapacity)
    {
        System.out.println("ILongLoadingCache: unsupported call to createNew - not implemented.");
        return null;
    }

    public default ILongLoadingCache<V> getStaticCache()
    {
        System.out.println("ILongLoadingCache: unsupported call to getStaticCache - not implemented.");
        return null;
    }

    public default LongSimpleCacheLoader<V> getLoader()
    {
        System.out.println("ILongLoadingCache: unsupported call to getLoader - not implemented.");
        return null;
    }
}