package grondag.adversity.library.cache.objectKey;

public interface ObjectSimpleCacheLoader<K, V>
{
    abstract public V load(K key);
    
    abstract ObjectSimpleCacheLoader<K, V> createNew();
}
