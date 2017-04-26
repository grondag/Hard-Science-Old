package grondag.adversity.library.cache.longKey;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import grondag.adversity.library.Useful;

public class LongCacheState<V>
{
    protected final int capacity;
    protected final int maxFill;
    protected AtomicInteger size = new AtomicInteger(0);
    protected final AtomicReference<V> zeroValue = new AtomicReference<V>();
    protected final long[][] keys;
    protected final V[][] values;
    protected final int positionMask;
    protected final int segmentShift;
    protected final int indexMask;
    
    @SuppressWarnings("unchecked")
    public LongCacheState(int capacityIn)
    {
        this.capacity = capacityIn;
        this.keys = new long[64][capacity >> 5];
        this.values = (V[][]) new Object[64][capacity >> 5];
        this.positionMask = capacity - 1;
        this.segmentShift = Useful.bitLength(capacityIn) - 5;
        this.indexMask = Useful.intBitMask(segmentShift);
        this.maxFill = (int) (capacity * LongSimpleLoadingCache.LOAD_FACTOR);
    }
}