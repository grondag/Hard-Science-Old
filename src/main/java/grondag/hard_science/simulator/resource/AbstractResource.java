package grondag.hard_science.simulator.resource;

public abstract class AbstractResource<V extends StorageType<V>> implements IResource<V>
{
    protected AbstractResource() {};
    
    @Override
    public int hashCode()
    {
        return this.handle();
    }

    @Override
    public boolean equals(Object other)
    {
        return this == other;
    }
    
    public abstract AbstractResourceDelegate<V> getDelegate();
}
