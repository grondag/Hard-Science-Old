package grondag.hard_science.simulator.transport.carrier;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.simulator.Simulator;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.transport.StoragePacket;
import grondag.hard_science.simulator.transport.endpoint.PortState;
import grondag.hard_science.simulator.transport.endpoint.PortType;
import grondag.hard_science.simulator.transport.endpoint.TransportNode;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * Represents physically (perhaps wirelessly) connected 
 * transport media shared by multiple port instances. 
 * Is pathway for actual transport. All ports on the circuit
 * must be of the same type and channel, and must be
 * connected in a way appropriate for the carrier type.<p>
 * 
 * References to a CarrierCircuit are held by the ports
 * on a DeviceBlock or (for wireless device) directly by
 * a device.  When a port becomes connected to another ports
 * with a compatible carrier, the port obtains a shared 
 * reference to the carrier circuit formed by connecting 
 * with other ports. (Or a new, isolated circuit if 
 * there are only two ports so far.)
 *
 */
public class CarrierCircuit
{
    public final Carrier carrier;
    
    public final int channel;
    
    private HashSet<PortState> ports;
    
    private static final AtomicInteger nextAddress = new AtomicInteger(1);
    
    private final int carrierAddress = nextAddress.getAndIncrement();
    
    protected long lastTickSeen = -1;
    
    protected long utilization = 0;
    
    /**
     * Collection of nodes on this carrier keyed by device ID.
     * Relies on the fact that all ports for a given device
     * on the same carrier have the same node.  The extra ports
     * are only used for fault tolerance.
     */
    private final Int2ObjectOpenHashMap<TransportNode> nodesByDeviceID
        = new Int2ObjectOpenHashMap<TransportNode>();
    
    public CarrierCircuit(Carrier carrier, int channel)
    {
        this.carrier = carrier;
        this.channel = channel;
    }
    
    /**
     * Transient unique identifier for network links/buses/gateways. 
     * Not type-specific, not persisted. 
     * Immutable in game session unless or until physical media structure
     * is disrupted. <p>
     * 
     * Used to build dynamically-constructed routing information.
     */
    public int carrierAddress()
    {
        return this.carrierAddress;
    }
    
    /**
     * Note this does NOT set the carrier circuit on the port. 
     * Simply registers the port with this carrier if
     * it is compatible and returns true if so.<p>
     * 
     * @param portInstance port to add
     * @param isInternal if true, will check for compatibility with internal side
     * of port. If false, will check external side.
     * @return True if successfully attached.
     */
    public synchronized boolean attach(PortState portInstance, boolean isInternal)
    {
        assert !this.ports.contains(portInstance)
            : "Carrier attach request from existing port on carrier.";
        
        Carrier portCarrier = isInternal ? portInstance.port().internalCarrier
                : portInstance.port().externalCarrier;
        
        if(portCarrier != this.carrier) return false;
        
        if(!this.carrier.level.isTop())
        {
            if(portInstance.port().portType == PortType.CARRIER
                    || (isInternal && portInstance.port().portType == PortType.BRIDGE))
            {
                // channel must match for non-top carrier ports 
                // and the internal side of bridge ports
                if(portInstance.getConfiguredChannel() != this.channel) return false;
            }
            
        }
        
        this.ports.add(portInstance);
        
        IDevice device = portInstance.device();
        
        TransportNode node = this.nodesByDeviceID.get(device.getId());
        if(node == null)
        {
            node = new TransportNode(portInstance);
            this.nodesByDeviceID.put(device.getId(), node);
            device.addTransportNode(node);
        }
        else
        {
            node.addPort(portInstance);
        }
        
        return true;
    }

    /**
     * Removes reference to portInstance from this circuit but does NOT
     * update port to remove reference to this circuit. Is expected to 
     * be called from {@link PortState#detach()} which handles that.
     */
    public synchronized void detach(PortState portInstance)
    {
        assert this.ports.contains(portInstance)
            : "Carrier dettach request from port not on carrier.";
        
        this.ports.remove(portInstance);
        
        IDevice device = portInstance.device();
        
        TransportNode node = this.nodesByDeviceID.get(device.getId());
        node.removePort(portInstance);
        
        if(!node.isConnected())
        {
            device.removeTransportNode(node);
            this.nodesByDeviceID.remove(device.getId());
        }
    }
    
    /**
     * Returns transport node for the given device if it
     * is attached to this circuit.
     */
    @Nullable
    public TransportNode getNodeForDevice(@Nonnull IDevice device)
    {
        return this.nodesByDeviceID.get(device.getId());
    }
    
    /**
     * Incurs cost of transporting the packet and 
     * updates packet with link cost if successful. 
     * Thread-safe<p>
     * 
     * If force == true, will incur cost and return true
     * even if link is already saturated.  Overage will be
     * carried over into subsequent ticks until no-longer
     * saturated.
     */
    public synchronized boolean consumeCapacity(StoragePacket<?> packet, TransportNode fromNode, TransportNode toNode, boolean force)
    {
        this.refreshUtilization();
        long cost = this.costForPacket(packet, fromNode, toNode);
        if(force || (cost + this.utilization) <= this.carrier.capacityPerTick)
        {
            this.utilization += cost;
            packet.updateLinkCost(this, cost);
            return true;
        }
        return false;
    }

    /**
     * Override if needs to be something other than the packet unit cost.
     */
    protected long costForPacket(StoragePacket<?> packet, TransportNode fromNode, TransportNode toNode)
    {
        return packet.basePacketCost();
    }
    
    /**
     * Decays utilization based on current simulation tick.
     * Call before any capacity-dependent operation.
     * Should only be called from synchronized state.
     */
    protected void refreshUtilization()
    {
        long currentTick = Simulator.instance().getTick();
        
        if(currentTick > this.lastTickSeen)
        {
            this.utilization = Math.max(0, this.utilization 
                    - this.carrier.capacityPerTick * (currentTick - this.lastTickSeen));
        }
    }

    /**
     * Called by packet to refunds the cost of earlier {@link #transport(StoragePacket, TransportNode, TransportNode)}
     * if packet cannot be delivered.<p>
     * 
     * MUST be called during same simulation tick or will be shifting capacity
     * across ticks.
     */
    public synchronized void refundCapacity(long cost)
    {
        this.utilization = Math.max(0, this.utilization - cost);
    }
}
