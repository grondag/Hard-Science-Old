package grondag.adversity.library.cache.longKey;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


public class LongCacheState<V>
{
    protected final int capacity;
    protected final int maxFill;
    protected AtomicInteger size = new AtomicInteger(0);
    protected final AtomicReference<V> zeroValue = new AtomicReference<V>();
    protected final long[] keys;
    protected final V[] values;
    protected final int positionMask;

    
    @SuppressWarnings("unchecked")
    public LongCacheState(int capacityIn)
    {
        this.capacity = capacityIn;
        this.keys = new long[capacity];
        this.values = (V[]) new Object[capacity];
        this.positionMask = capacity - 1;
        this.maxFill = (int) (capacity * LongSimpleLoadingCache.LOAD_FACTOR);
    }
}