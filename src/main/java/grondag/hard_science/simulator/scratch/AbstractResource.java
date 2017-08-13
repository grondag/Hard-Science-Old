package grondag.hard_science.simulator.scratch;


public abstract class AbstractResource<V extends StorageType> implements IResource<V>
{
    private final AbstractResourceBroker<V> broker;
    
    protected AbstractResource(AbstractResourceBroker<V> broker)
    {
        this.broker = broker;
    }
    
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
    
    @Override
    public AbstractResourceBroker<V> resourceBroker()
    {
        return this.broker;
    }
}
