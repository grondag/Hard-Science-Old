package grondag.adversity.library.cache;

public interface ILoadingCache<V>
{

    void clear();

    V get(long key);

}