package grondag.adversity.library.cache;

public interface SimpleCacheLoader<V>
{
    abstract public V load(long key);
}
