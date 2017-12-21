package grondag.hard_science.simulator.transport.endpoint;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

import grondag.hard_science.simulator.device.IDeviceComponent;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

/**
 * Characteristics of a physical network node.
 */
public abstract class TransportNode implements IDeviceComponent
{
    private static final AtomicInteger NEXT_NODE_ID = new AtomicInteger(1);
    
    private final int address = NEXT_NODE_ID.getAndIncrement();
    
    private final Port port;
    
    protected TransportNode(@Nonnull Port port)
    {
        this.port = port;
    }
    
    /**
     * Transient unique id. 
     */
    public int nodeAddress()
    {
        return this.address;
    }
    
    /**
     * Port type for this node.
     */
    public Port port()
    {
        return this.port;
    }
    
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
    public abstract long produce(IResource<?> resource, long quantity, boolean allowPartial, boolean simulate);

    /**
     * Convenience version of {@link #produce(IResource, long, boolean, boolean)} that 
     * assumes allowPartial = false.  Returns true if exactly the given quantityIn
     * are produced.  Otherwise returns false and no resources are produced.
     */
    public boolean produce(IResource<?> resource, long quantity, boolean simulate)
    {
        return this.produce(resource, quantity, false, simulate) == quantity;
    }
    
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
    public abstract long consume(IResource<?> resource, long quantity, boolean allowPartial, boolean simulate);
    
    /**
     * Convenience version of {@link #consume(IResource, long, boolean, boolean)} that 
     * assumes allowPartial = false.  Returns true if exactly the given quantityIn
     * are consumed.  Otherwise returns false and no resources are consumed.
     */
    public boolean consume(IResource<?> resource, long quantity, boolean simulate)
    {
        return this.consume(resource, quantity, false, simulate) == quantity;
    }

    public StorageType<?> storageType()
    {
        return this.port.storageType;
    }

    /**
     * Called right before a Node is discarded due to 
     * a change in channel or world state.
     */
    public void disconnect()
    {
        
    }
}
