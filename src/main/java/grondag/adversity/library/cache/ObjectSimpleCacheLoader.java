package grondag.adversity.library.cache;

public interface ObjectSimpleCacheLoader<K, V>
{
    abstract public V load(K key);
    
    /** for benchmark testing */
    default ObjectSimpleCacheLoader<K, V> createNew() { return null; };
}
