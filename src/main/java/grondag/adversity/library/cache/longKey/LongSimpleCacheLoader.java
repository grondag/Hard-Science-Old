package grondag.adversity.library.cache.longKey;

public interface LongSimpleCacheLoader<V>
{
    abstract public V load(long key);
    
    // for testing only
    public default LongSimpleCacheLoader<V> createNew() { return null; };
}
