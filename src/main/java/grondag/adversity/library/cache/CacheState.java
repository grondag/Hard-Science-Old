package grondag.adversity.library.cache;

public class CacheState<V>
{
    protected long[] keys;
    protected V[] values;
    protected int positionMask;
    protected int zeroLocation;
}
