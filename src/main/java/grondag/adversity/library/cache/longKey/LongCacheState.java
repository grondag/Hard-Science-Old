package grondag.adversity.library.cache.longKey;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


public class LongCacheState<V>
{

    protected AtomicInteger size = new AtomicInteger(0);
    protected final AtomicReference<V> zeroValue = new AtomicReference<V>();
    protected final long[] keys;
    protected final V[] values;


    
    @SuppressWarnings("unchecked")
    public LongCacheState(int capacityIn)
    {
        this.keys = new long[capacityIn];
        this.values = (V[]) new Object[capacityIn];
    }
}