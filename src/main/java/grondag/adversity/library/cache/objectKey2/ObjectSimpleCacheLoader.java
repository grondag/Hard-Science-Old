package grondag.adversity.library.cache.objectKey2;

public interface ObjectSimpleCacheLoader<K, V>
{
    abstract public V load(K key);
    
    abstract ObjectSimpleCacheLoader<K, V> createNew();
}
