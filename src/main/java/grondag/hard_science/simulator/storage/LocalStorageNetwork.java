package grondag.hard_science.simulator.storage;

import java.util.HashSet;

import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.domain.IDomainMember;
import grondag.hard_science.simulator.resource.ITypedStorage;
import grondag.hard_science.simulator.resource.StorageType;

/**
 * Tracks physical storage within a physically connected
 * region of a domain.  Also tracks pending deliveries in/out
 * to prevent 
 */
public class LocalStorageNetwork<T extends StorageType<T>> implements IDomainMember, ITypedStorage<T>
{
    private final Domain domain;
    private final T storageType;
    
    protected final HashSet<IStorage<T>> stores = new HashSet<IStorage<T>>();
    
    public LocalStorageNetwork(Domain domain, T storageType)
    {
        this.domain = domain;
        this.storageType = storageType;
    }
    
    @Override
    public Domain getDomain()
    {
        return this.domain;
    }
    
    @Override
    public T storageType()
    {
        return this.storageType;
    }

}
