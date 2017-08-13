package grondag.hard_science.simulator.scratch;

import java.util.List;

import grondag.hard_science.simulator.scratch.IResourceStack.ILocatedResourceStack;
import grondag.hard_science.simulator.scratch.StorageType.ITypedStorage;
import grondag.hard_science.simulator.wip.Domain.IDomainMember;

/**
 * Responsibilities: <br>
 * + Tracking the location of all resources for a storage type within a domain.<br>
 * + Tracking all empty storage for a storage type within a domain. <br>
 * + Storing and retrieving items.
 * + Answering inquiries about storage of a given type based on tracking. <br>
 * + Notifies listeners when total storage changes
 *<br>
 *Not responsible for optimizing storage.
 */
public abstract class AbstractStorageManager<V extends StorageType> implements IDomainMember, ITypedStorage<V>
{
    public abstract IResourceStack<V> getTotal(IResource<V> target);
    
    public abstract List<ILocatedResourceStack<V>> searchForResouce(IResource<V> target);
    
    
    /**
     * Alternate syntax for {@link #store(IResourceStack, IResourceLocation)}
     */
    public IResourceStack<V> store(ILocatedResourceStack<V> stack)
    {
        return this.store(stack, stack);
    }
    
    /** 
     * Attempts to store the given resource in the associated location.
     * Returns a resource stack containing any resources that could not be stored.
     * @param stack
     * @return
     */
    public abstract IResourceStack<V> store(IResourceStack<V> stack, IResourceLocation<V> location);
}
