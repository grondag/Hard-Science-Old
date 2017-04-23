package grondag.adversity.library.cache.objectKey;

public interface IObjectLoadingCache<K, V>
{

    void clear();

    V get(K key);

}
