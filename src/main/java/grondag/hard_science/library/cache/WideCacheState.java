package grondag.hard_science.library.cache;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


public class WideCacheState<V>
{
    protected AtomicInteger size = new AtomicInteger(0);
    protected final AtomicReference<V> zeroValue = new AtomicReference<V>();
    protected final long[] keys;
    protected final V[] values;
    
    @SuppressWarnings("unchecked")
    public WideCacheState(int capacityIn)
    {
        this.keys = new long[capacityIn * 2];
        this.values = (V[]) new Object[capacityIn];
    }
}