package grondag.hard_science.simulator.transport.endpoint;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.device.IDeviceComponent;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

/**
 * Characteristics of a network endpoint on a device.
 * Associated with a port on a carrier.<p>
 * 
 * Relation to port types as follows...<p>
 * Carrier Ports and Direct Ports: a single node 
 * will represent all the carrier and direct ports 
 * on the same carrier circuit for a given device.
 * They should never be mixed because direct ports and
 * carrier ports on the same end point device will 
 * not have the same carrier level and will thus have
 * different carriers.<p>
 * 
 * When there are redundant mated ports then the
 * first port is used for the node and extra ports
 * are used as backup ports so that the node remains 
 * active so long as one port remains attached to the carrier.<p>
 * 
 * Bridge Ports: Because bridge ports are only
 * used to connect transport devices (and not end point
 * devices) no transport nodes are created or associated
 * with bridge ports for either carrier (internal & external). 
 */
public class TransportNode implements IDeviceComponent
{
    private static final AtomicInteger NEXT_NODE_ID = new AtomicInteger(1);
    
    private final int address = NEXT_NODE_ID.getAndIncrement();
    
    private PortState primaryPort;
    
    private SimpleUnorderedArrayList<PortState> ports
         = new SimpleUnorderedArrayList<PortState>();
    
    public TransportNode(@Nonnull PortState firstPort)
    {
        assert firstPort.port().portType != PortType.BRIDGE 
                : "Unsupported port type in Transport Node";

        this.primaryPort = firstPort;
        this.ports.add(firstPort);
    }
    
    public synchronized void addPort(@Nonnull PortState newPort)
    {
        assert !this.ports.contains(newPort) : "Request to add redundant port to transport node";
        
        assert this.primaryPort == null || this.primaryPort.port().storageType == newPort.port().storageType
                : "Mismatched storage type for port added to transport node";

        assert this.primaryPort == null || this.primaryPort.externalCircuit() == newPort.externalCircuit()
                : "Mismatched circuit for port added to transport node";
        
        assert this.primaryPort == null || this.primaryPort.port().portType == newPort.port().portType
                : "Mismatched port type for port added to transport node";
        
        this.ports.addIfNotPresent(newPort);
        
        if(this.primaryPort == null) this.primaryPort = newPort;
    }
    
    public void removePort(PortState targetPort)
    {
        assert this.ports.contains(targetPort) : "Request to remove missing port from transport node";
        
        this.ports.removeIfPresent(targetPort);
        
        if(this.primaryPort == targetPort)
        {
            this.primaryPort = this.ports.isEmpty() ? null : this.ports.get(0);
        }
    }
    
    public boolean isConnected()
    {
        return this.primaryPort != null 
                && this.primaryPort.isAttached();
    }
    
    /**
     * Transient unique id. 
     */
    public int nodeAddress()
    {
        return this.address;
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
    public final long produce(IResource<?> resource, long quantity, boolean allowPartial, boolean simulate)
    {
        return this.isConnected() 
                ? this.primaryPort.device().onProduce(resource, quantity, allowPartial, simulate)
                : 0;
    }
    
    /**
     * Convenience version of {@link #produce(IResource, long, boolean, boolean)} that 
     * assumes allowPartial = false.  Returns true if exactly the given quantityIn
     * are produced.  Otherwise returns false and no resources are produced.
     */
    public final boolean produce(IResource<?> resource, long quantity, boolean simulate)
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
    public final long consume(IResource<?> resource, long quantity, boolean allowPartial, boolean simulate)
    {
        return !this.isConnected() 
                ? this.primaryPort.device().onConsume(resource, quantity, allowPartial, simulate)
                : 0;
    }

    /**
     * Convenience version of {@link #consume(IResource, long, boolean, boolean)} that 
     * assumes allowPartial = false.  Returns true if exactly the given quantityIn
     * are consumed.  Otherwise returns false and no resources are consumed.
     */
    public final boolean consume(IResource<?> resource, long quantity, boolean simulate)
    {
        return this.consume(resource, quantity, false, simulate) == quantity;
    }

    @Override
    public IDevice device()
    {
        return this.primaryPort.device();
    }
    
    public StorageType<?> storageType()
    {
        return this.isConnected() ? this.primaryPort.port().storageType : StorageType.NONE;
    }
}
