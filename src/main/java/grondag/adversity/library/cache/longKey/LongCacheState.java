package grondag.adversity.library.cache.longKey;

public class LongCacheState<V>
{
    protected long[] keys;
    protected V[] values;
    protected int positionMask;
    protected int zeroLocation;
}
