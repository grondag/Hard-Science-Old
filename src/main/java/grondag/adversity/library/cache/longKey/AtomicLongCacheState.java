package grondag.adversity.library.cache.longKey;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class AtomicLongCacheState<V>
{
    protected volatile int capacity;
    protected volatile int maxFill;
    protected AtomicInteger size = new AtomicInteger(0);
    protected AtomicLongArray keys;
    protected AtomicReferenceArray<V> values;
    protected int positionMask;
    protected int zeroLocation;
}