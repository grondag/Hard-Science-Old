package grondag.hard_science.simulator.transport.carrier;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableSet;

import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.simulator.Simulator;
import grondag.hard_science.simulator.transport.StoragePacket;
import grondag.hard_science.simulator.transport.endpoint.PortMode;
import grondag.hard_science.simulator.transport.endpoint.PortState;
import grondag.hard_science.simulator.transport.management.ConnectionManager;
import grondag.hard_science.simulator.transport.routing.Legs;

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
 * there are only two ports so far.)<p>
 * 
 * Methods related to network topology should ONLY be
 * called from the connection manager thread and will 
 * throw an exception if called otherwise.  This avoids
 * the need for synchronization of these methods.
 *
 */
public class CarrierCircuit
{
    public final Carrier carrier;
    
    public final int channel;

    private final PortTracker ports;
    
    private Legs legs = null;
    
    private static final AtomicInteger nextAddress = new AtomicInteger(1);
    
    private final int carrierAddress = nextAddress.getAndIncrement();
    
    protected long lastTickSeen = -1;
    
    protected long utilization = 0;
    
//    /**
//     * Collection of nodes on this carrier keyed by device ID.
//     * Relies on the fact that all ports for a given device
//     * on the same carrier have the same node.  The extra ports
//     * are only used for fault tolerance.
//     */
//    private final Int2ObjectOpenHashMap<TransportNode> nodesByDeviceID
//        = new Int2ObjectOpenHashMap<TransportNode>();
    
    public CarrierCircuit(Carrier carrier, int channel)
    {
        this.carrier = carrier;
        this.channel = channel;
        this.ports = new PortTracker(this);
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
     * Mode should be set on port before this is called so
     * can detect bridges or handle other mode-specific behavior<p>
     * 
     * @param portInstance port to add
     * @param isInternal if true, will check for compatibility with internal side
     * of port. If false, will check external side.
     * @return True if successfully attached.<p>
     * 
     * SHOULD ONLY BE CALLED FROM CONNECTION MANAGER THREAD.
     */
    public boolean attach(PortState portInstance, boolean isInternal)
    {
        assert ConnectionManager.confirmNetworkThread() : "Transport logic running outside transport thread";
        
        if(Configurator.logTransportNetwork) 
            Log.info("CarrierCircuit.attach: Attaching port %s (%s) to circuit %d.",
                    portInstance.portName(),
                    isInternal ? "internal" : "external",
                    this.carrierAddress());
        
        assert !this.ports.contains(portInstance)
            : "Carrier attach request from existing port on carrier.";
        
        // note this check covers both storage type and level
        // check for mode is because bridge ports have a lower-tier
        // external carrier but they use their internal carrier as the
        // when operating in bridge mode
        Carrier portCarrier = isInternal 
                ? portInstance.port().internalCarrier
                : portInstance.port().externalCarrier(portInstance.portMode());
        
        if(portCarrier != this.carrier)
        {
            if(Configurator.logTransportNetwork) 
                Log.info("CarrierCircuit.attach: abandoning attempt due to mismatched parents.");
            return false;
        }
        
        if(!this.carrier.level.isTop())
        {
            if(portInstance.portMode() == PortMode.CARRIER
                    || (isInternal && portInstance.portMode().isBridge()))
            {
                // channel must match for non-top carrier ports 
                // and the internal side of bridge ports
                if(portInstance.getConfiguredChannel() != this.channel)
                {
                    if(Configurator.logTransportNetwork) 
                        Log.info("CarrierCircuit.attach: abandoning attempt due to mismatched channels.");
                    return false;
                }
            }
            
        }
        
        this.ports.add(portInstance);
        
        return true;
    }

    /**
     * Removes reference to portInstance from this circuit but does NOT
     * update port to remove reference to this circuit. Is expected to 
     * be called from {@link PortState#detach()} which handles that. <p>
     * 
     * SHOULD ONLY BE CALLED FROM CONNECTION MANAGER THREAD.
     */
    public void detach(PortState portInstance)
    {
        assert ConnectionManager.confirmNetworkThread() : "Transport logic running outside transport thread";
        
        if(Configurator.logTransportNetwork) 
            Log.info("CarrierCircuit.detach: Removing port %s from circuit %d.",
                    portInstance.portName(),
                    this.carrierAddress());
        
        assert this.ports.contains(portInstance)
            : "Carrier dettach request from port not on carrier.";
        
        this.ports.remove(portInstance);
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
    public synchronized boolean consumeCapacity(StoragePacket<?> packet, boolean force)
    {
        this.refreshUtilization();
        long cost = this.costForPacket(packet);
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
    protected long costForPacket(StoragePacket<?> packet)
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
    
    /**
     * All ports that were on this circuit are transferred
     * to the new circuit. There are no checks or notifications
     * to devices or transport nodes, because the ports/nodes/circuit
     * was already assumed to be valid.<p>
     * 
     * SHOULD ONLY BE CALLED FROM CONNECTION MANAGER THREAD.
     * 
     */
    public void mergeInto(CarrierCircuit into)
    {
        assert ConnectionManager.confirmNetworkThread() : "Transport logic running outside transport thread";
        
        for(PortState port : this.ports)
        {
            port.swapCircuit(this, into);
            
        }
        into.ports.addAll(this.ports);
        this.ports.clear();
    }

    public int portCount()
    {
        return this.ports.size();
    }

    /**
     * Moves ports in this circuit matching the provided predicate
     * to the new circuit.<p>
     * 
     * SHOULD ONLY BE CALLED FROM CONNECTION MANAGER THREAD.
     */
    public void movePorts(CarrierCircuit into, Predicate<PortState> predicate)
    {
        assert ConnectionManager.confirmNetworkThread() : "Transport logic running outside transport thread";
        
        Iterator<PortState> it = this.ports.iterator();
        while(it.hasNext())
        {
            PortState port = it.next();
            if(predicate.test(port))
            {
                port.swapCircuit(this, into);
                into.ports.add(port);
                it.remove();
            }
        }
    }
    
    /**
     * All upward carrier circuits accessible from this circuit via bridge ports.
     */
    public ImmutableSet<CarrierCircuit> parents()
    {
        return this.ports.parents();
    }
    
    /**
     * For use by PortTracker to inform peer instances of bridge circuit changes.
     */
    protected PortTracker portTracker()
    {
        return this.ports;
    }

    /**
     * Use to track currency of information in routing data.
     * All circuits share a globally unique, monotonically increasing
     * bridge version sequence. This means that any given set of circuits, 
     * will have a max version value at the time the set is created, 
     * and if any circuit in the set changes, the new max must be
     * greater than the old max. <p>
     * 
     * Bridge version is incremented to the next global value
     * any time a bridge is added or removed from this circuit.
     * Actual implementation is in PortTracker, which handles all
     * the bridge tracking.
     */
    public int bridgeVersion()
    {
        return this.ports.bridgeVersion();
    }
    
    /**
     * Retrieves upward routing information for this circuit.
     * If information is not current, will be automatically refreshed.
     */
    public Legs legs()
    {
        if(this.legs == null || !this.legs.isCurrent())
        {
            this.legs = new Legs(this);
        }
        return this.legs;
    }
}
