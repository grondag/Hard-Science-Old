package grondag.adversity.library;

public interface SimpleCacheLoader<V>
{
    abstract public V load(long key);
}
