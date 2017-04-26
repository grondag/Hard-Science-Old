package grondag.adversity.library.cache.longKey;

import java.util.concurrent.atomic.AtomicInteger;

public class LongCacheState<V>
{
    protected volatile int capacity;
    protected volatile int maxFill;
    protected AtomicInteger size = new AtomicInteger(0);
    protected V zeroValue;
    protected long[] keys;
    protected V[] values;
    protected int positionMask;
}