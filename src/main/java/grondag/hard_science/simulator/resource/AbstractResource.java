package grondag.hard_science.simulator.resource;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractResource<V extends StorageType<V>> implements IResource<V>
{
    /** see {@link #handle()} */
    private static final AtomicInteger nextHandle = new AtomicInteger(1);
    
    /** see {@link #handle()} */
    private int handle = nextHandle.getAndIncrement();
    
    protected AbstractResource() {};
    
    @Override
    public int handle() { return this.handle; }

    @Override
    public int hashCode()
    {
        return this.computeResourceHashCode();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object other)
    {
        boolean result = false;
        try
        {
            result = this.isResourceEqual((IResource<V>) other);
        } finally {}
       
        return result;
    }
    
    public abstract AbstractResourceDelegate<V> getDelegate();
}
