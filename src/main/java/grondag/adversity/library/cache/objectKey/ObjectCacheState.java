package grondag.adversity.library.cache.objectKey;

public class ObjectCacheState<K, V>
{
    protected K[] keys;
    protected V[] values;
    protected int positionMask;
    protected int nullLocation;
}
