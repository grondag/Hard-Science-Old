package grondag.adversity.library.cache.longKey;

public interface ILongLoadingCache<V>
{

    void clear();

    V get(long key);

}