package grondag.hard_science.simulator.transport.carrier;

import java.util.concurrent.atomic.AtomicInteger;

import grondag.hard_science.simulator.Simulator;
import grondag.hard_science.simulator.resource.ITypedStorage;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.StoragePacket;
import grondag.hard_science.simulator.transport.endpoint.TransportNode;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * Lowest layer of transport stack - represent the physical 
 * transport medium.  Subclasses imply a particular topology
 * for function. All non-abstract implementing classes should 
 * map to a physical, in-game device.
 */
public abstract class TransportCarrier<T extends StorageType<T>> implements ITypedStorage<T>
{
    private static final AtomicInteger nextAddress = new AtomicInteger(1);
    
    private final int mediaAddress = nextAddress.getAndIncrement();
    
    private final Int2ObjectOpenHashMap<TransportNode> nodes
        = new Int2ObjectOpenHashMap<TransportNode>();
    
    protected final long capacityPerTick;
    
    protected long lastTickSeen = -1;
    
    protected long utilization = 0;
    
    protected TransportCarrier(long capacityPerTick)
    {
        this.capacityPerTick = capacityPerTick;
    }
    
//    private Collection<TransportNode<T>> nodesReadOnly;
    
    /**
     * Transient unique identifier for network links/buses/gateways. 
     * Not type-specific, not persisted. 
     * Immutable in game session unless or until physical media structure
     * is disrupted. <p>
     * 
     * Used to build dynamically-constructed routing information.
     */
    public int mediaAddress()
    {
        return this.mediaAddress;
    }
    
//    /**
//     * All nodes that are physically attached to this media and thus
//     * directly reachable from any other directly attached node.<p>
//     * 
//     * Some illustrations: <p>
//     * Will include gateways nodes, but does not reflect any nodes
//     * reachable via those gateways.  Point-to-point links will include
//     * only two nodes in the list.  Stand-alone devices or buses with
//     * only a single attached node will include a single node in the list
//     * that is only reachable from itself.
//     */
//    public Collection<TransportNode<T>> nodes()
//    {
//        if(this.nodesReadOnly == null)
//        {
//            this.nodesReadOnly = Collections.unmodifiableCollection(this.nodes.values());
//        }
//        return this.nodesReadOnly;
//    }
    
    public synchronized void attach(TransportNode node)
    {
        this.nodes.put(node.nodeAddress(), node);
    }

    public synchronized void dettach(TransportNode node)
    {
        this.nodes.remove(node.nodeAddress());
    }
    
    /**
     * Alternative to searching result of {@link #nodes()}.
     * Some implementations may be faster than that approach.
     */
    public boolean isAttached(TransportNode node)
    {
        return this.nodes.containsKey(node.nodeAddress());
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
    public synchronized boolean transport(StoragePacket<T> packet, TransportNode fromNode, TransportNode toNode, boolean force)
    {
        this.refreshUtilization();
        long cost = this.costForPacket(packet, fromNode, toNode);
        if(force || (cost + this.utilization) <= this.capacityPerTick)
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
    protected long costForPacket(StoragePacket<T> packet, TransportNode fromNode, TransportNode toNode)
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
                    - this.capacityPerTick * (currentTick - this.lastTickSeen));
        }
    }

    /**
     * Called by packet to refunds the cost of earlier {@link #transport(StoragePacket, TransportNode, TransportNode)}
     * if packet cannot be delivered.<p>
     * 
     * MUST be called during same simulation tick or will be shifting capacity
     * across ticks.
     */
    public synchronized void refund(long cost)
    {
        this.utilization = Math.max(0, this.utilization - cost);
    }
        
}
