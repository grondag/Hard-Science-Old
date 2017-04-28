package grondag.adversity.library.cache.longKey;

public interface LongSimpleCacheLoader<V>
{
    abstract public V load(long key);
    
    abstract LongSimpleCacheLoader<V> createNew();
}
