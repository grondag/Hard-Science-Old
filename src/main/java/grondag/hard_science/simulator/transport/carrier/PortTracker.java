package grondag.hard_science.simulator.transport.carrier;

import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.ImmutableSet;

import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.simulator.transport.endpoint.PortState;

/**
 * Encapsulated set used by CarrierCircuit to
 * track ports.  Main feature in addition to set
 * functionality is maintaining a list of all
 * upward-connected bridge ports.
 */
public class PortTracker implements Iterable<PortState>
{
    /**
     * See {@link CarrierCircuit#bridgeVersion()}
     * Don't access directly, use {@link #updateBridgeVersion()}.
     */
    private static final AtomicInteger BRIDGE_VERSION_COUNTER = new AtomicInteger(0);

    /**
     * See {@link CarrierCircuit#bridgeVersion()}
     */
    private int bridgeVersion;
    
    private final HashSet<PortState> ports = new HashSet<PortState>();
    
    /**
     * Circuit that owns this tracker.  Used to know what side
     * of bridge we are on.
     */
    private final CarrierCircuit owner;

    /**
     * List of bridge ports where our owner is the external circuit.
     * These should mean that we are on the low side of the bridge,
     * because the internal circuit will belong to the bridge device.
     */
    private final SimpleUnorderedArrayList<PortState> bridges
        = new SimpleUnorderedArrayList<PortState>();
    
    /**
     * Unique upwards carrier circuits accessible from our
     * owner via bridge ports.
     */
    ImmutableSet<CarrierCircuit> parents = ImmutableSet.of();
    
    public PortTracker(CarrierCircuit owner)
    {
        this.owner = owner;
        this.updateBridgeVersion();
    }
    
    private void updateBridgeVersion()
    {
        this.bridgeVersion = BRIDGE_VERSION_COUNTER.incrementAndGet();
    }
    
    public boolean contains(PortState portInstance)
    {
        return this.ports.contains(portInstance);
    }

    private void addBridge(PortState p)
    {
        assert p.internalCircuit().carrier.level == this.owner.carrier.level.above()
                : "Invalid bridge port levels";
        this.bridges.addIfNotPresent(p);
        this.updateBridgeVersion();
        this.refreshParents();
    }
    
    private void removeBridge(PortState p)
    {
        assert p.internalCircuit().carrier.level == this.owner.carrier.level.above()
                : "Invalid bridge port levels";
        
        this.bridges.removeIfPresent(p);
        this.updateBridgeVersion();
        this.refreshParents();
    }
    
   
    public void add(PortState p)
    {
        if(this.ports.add(p))
        {
            // Might be preferable to detect this from the downward side of the 
            // bridge so that don't have to call into other port tracker
            // but the downward port will be in carrier mode and if it attached
            // first then the bridge port won't necessarily have its mode set yet 
            // so we can't reliably check it.  So instead having the bridge
            // port notify the external carrier directly, which is most easily
            // accomplished by making the port tracker for it accessible. 
            if(p.portMode().isBridge() && p.internalCircuit() == this.owner)
            {
                p.externalCircuit().portTracker().addBridge(p);
                
                // don't need to track the bridge on the internal circuit
                // but we do need to mark it dirty for route tracking
                p.internalCircuit().portTracker().updateBridgeVersion();
            }
        }
    }

    public void remove(PortState p)
    {
        if(this.ports.remove(p))
        {
            // see notes in add
            // can't rely on internalCircuit here to be the same as owner 
            // as it was during add, so we try to remove all bridge ports
            // even if we aren't sure.  (Not sure how we can't be, but
            // I barely understand how most of this code works even though
            // I wrote it... networks are hard.)
            if(p.portMode().isBridge())
            {
                p.externalCircuit().portTracker().removeBridge(p);
                
                // don't need to track the bridge on the internal circuit
                // but we do need to mark it dirty for route tracking
                p.internalCircuit().portTracker().updateBridgeVersion();
            }
        }
    }

    public void addAll(Iterable<PortState> other)
    {
        other.forEach(p -> this.add(p));
    }

    @Override
    public Iterator<PortState> iterator()
    {
        return this.ports.iterator();
    }

    public void clear()
    {
        this.ports.clear();
        this.bridges.clear();
        this.refreshParents();
    }

    public int size()
    {
        return this.ports.size();
    }
    
    private void refreshParents()
    {
        this.parents = null;
    }
    
    /**
     * All upward carrier circuits accessible from owning carrier via bridge ports.
     */
    public ImmutableSet<CarrierCircuit> parents()
    {
        if(this.parents == null)
        {
            if(this.bridges.isEmpty())
            {
                this.parents = ImmutableSet.of();
            }
            else if(this.bridges.size() == 1)
            {
                this.parents = ImmutableSet.of(this.bridges.get(0).internalCircuit());
            }
            else
            {
                ImmutableSet.Builder<CarrierCircuit> builder = ImmutableSet.builder();
                this.bridges.forEach(p -> builder.add(p.internalCircuit()));
                this.parents = builder.build();
            }
        }
        return this.parents;
    }

    /**
     * See {@link CarrierCircuit#bridgeVersion()}
     */
    public int bridgeVersion()
    {
        return this.bridgeVersion;
    }
    
}
