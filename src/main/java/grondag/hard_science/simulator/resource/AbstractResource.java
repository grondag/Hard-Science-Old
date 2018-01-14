package grondag.hard_science.simulator.resource;

public abstract class AbstractResource<V extends StorageType<V>> implements IResource<V>
{
    protected AbstractResource() {};

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
}
