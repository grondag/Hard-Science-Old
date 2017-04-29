package grondag.adversity.library.cache.objectKey2;

import java.util.concurrent.atomic.AtomicInteger;


public class ObjectCacheState
{
    protected AtomicInteger size = new AtomicInteger(0);
    protected final Object[] kv;

    public ObjectCacheState(int capacityIn)
    {
        this.kv = new Object[capacityIn * 2];
    }
}