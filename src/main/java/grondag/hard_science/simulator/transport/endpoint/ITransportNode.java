package grondag.hard_science.simulator.transport.endpoint;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.domain.IDomainMember;
import grondag.hard_science.simulator.persistence.AssignedNumber;
import grondag.hard_science.simulator.persistence.IIdentified;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ITypedStorage;
import grondag.hard_science.simulator.resource.StorageType;

/**
 * Characteristics of a physical network node.
 * Every continuously connected network
 */
public interface ITransportNode<T extends StorageType<T>> 
    extends IDomainMember, IIdentified, ITypedStorage<T>, IReadWriteNBT
{
    
//    public int getLocalTransportSubNet();
    
//    public void setTransportSubNet(@Nonnull TransportSubNet net);
    
    @Override
    public default AssignedNumber idType()
    {
        return this.storageType().nodeIdType;
    }
    
    /**
     * Owning device
     */
    public IDevice device();
    
    /**
     * True if this node can transmit resources on the transport network.<p>
     * 
     * If false, {@link #produce(IResource, long, boolean, boolean)} will
     * always return zero.  
     */
    public boolean canProduce();
    
    /**
     * Removes given resource from this node so that it can be
     * transported to a different node or put into the world. 
     * Does not do anything other than remove the resource from the
     * node. (Though this can have side effects depending on the node.)<p>
     * 
     * Implementations should be thread-safe. <p>
     * 
     * @param resource  Identifies resource to be extracted
     * @param quantityIn  Limits how many of the resource are extracted
     * @param allowPartial  If false, will extract nothing unless full quantityIn can be extracted
     * @param simulate  If true, will return forecasted result without making changes.
     * @return the number of resources actually extracted
     */
    public long produce(IResource<T> resource, long quantity, boolean allowPartial, boolean simulate);

    /**
     * Convenience version of {@link #produce(IResource, long, boolean, boolean)} that 
     * assumes allowPartial = false.  Returns true if exactly the given quantityIn
     * are produced.  Otherwise returns false and no resources are produced.
     */
    public default boolean produce(IResource<T> resource, long quantity, boolean simulate)
    {
        return this.produce(resource, quantity, false, simulate) == quantity;
    }
    
    /**
     * True if this node can transmit resources on the transport network.<p>
     * 
     * If false, {@link #produce(IResource, long, boolean, boolean)} will
     * always return zero.  
     */
    public boolean canConsume();
    
    /**
     * Inserts given resource into this node.  Receiving
     * node might store the resource, consume it immediately,
     * or eject it into the world.  In any case, the caller
     * is not expected to know or care what actually happens 
     * to the resource.  Caller only knows that accepted resources
     * can be removed from the transport network. <p>
     * 
     * Implementations should be thread-safe. <p>
     * 
     * @param resource  Identifies resource to be stored/consumed
     * @param quantityIn  Limits how many of the resource are to be accepted
     * @param allowPartial  If false, will accept nothing unless full quantityIn can be accepted
     * @param simulate  If true, will return forecasted result without making changes.
     * @return the number of resources actually accepted
     */
    public long consume(IResource<T> resource, long quantity, boolean allowPartial, boolean simulate);
    
    /**
     * Convenience version of {@link #consume(IResource, long, boolean, boolean)} that 
     * assumes allowPartial = false.  Returns true if exactly the given quantityIn
     * are consumed.  Otherwise returns false and no resources are consumed.
     */
    public default boolean consume(IResource<T> resource, long quantity, boolean simulate)
    {
        return this.consume(resource, quantity, false, simulate) == quantity;
    }
}
