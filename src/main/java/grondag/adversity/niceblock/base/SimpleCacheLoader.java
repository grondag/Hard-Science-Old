package grondag.adversity.niceblock.base;

public interface SimpleCacheLoader<V>
{
    abstract public V load(long key);
}
