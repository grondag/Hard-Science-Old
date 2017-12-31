package grondag.hard_science.simulator.transport.carrier;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.simulator.transport.endpoint.PortState;

/**
 * Encapsulated set used by CarrierCircuit to
 * track ports.  Main feature in addition to set
 * functionality is maintaining a list of all
 * upward-connected bridge ports.
 */
public class PortTracker
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

   
    public void add(PortState p)
    {
        if(Configurator.logTransportNetwork) 
            Log.info("PortTracker.add: circuit = %d, portState = %s",
                    this.owner.carrierAddress(),
                    p.portName());
        
        assert p.internalCircuit() == this.owner 
                || p.externalCircuit() == this.owner
                : "PortTracker.add: port state circuits are bothh null or do not match this circuit.";
                    
        if(this.ports.add(p))
        {
            if(p.getMode().isBridge())
            {
                if(p.externalCircuit() == this.owner)
                {
                    Log.info("PortTracker.add: circuit = %d, downward side - updating bridges and version",
                            this.owner.carrierAddress());
                    
                    this.bridges.addIfNotPresent(p);
                    this.updateBridgeVersion();
                    this.refreshParents();
                }
                else
                {
                    Log.info("PortTracker.add: circuit = %d, upward side - updating version only",
                            this.owner.carrierAddress());
                    
                    // opening assertion implies internalCircuit is our owner
                    // don't need to track the bridge on the internal circuit
                    // but we do need to mark it dirty for route tracking
                    this.updateBridgeVersion();
                }
            }
        }
    }

    public void remove(PortState p)
    {
        if(Configurator.logTransportNetwork) 
            Log.info("PortTracker.remove: circuit = %d, portState = %s",
                    this.owner.carrierAddress(),
                    p.portName());
        
        assert p.internalCircuit() == this.owner 
                || p.externalCircuit() == this.owner
                : "PortTracker.add: port state circuits are both null or do not match this circuit.";
        
        if(this.ports.remove(p))
        {
            
            if(p.getMode().isBridge())
            {
                if(p.externalCircuit() == this.owner)
                {
                    Log.info("PortTracker.remove: circuit = %d, downward side - updating bridges and version",
                            this.owner.carrierAddress());
                    
                    this.bridges.removeIfPresent(p);
                    this.updateBridgeVersion();
                    this.refreshParents();
                }
                else 
                {
                    Log.info("PortTracker.remove: circuit = %d, upward side - updating version only",
                            this.owner.carrierAddress());
                    
                    // opening assertion implies internalCircuit is our owner
                    // don't need to track the bridge on the internal circuit
                    // but we do need to mark it dirty for route tracking
                    this.updateBridgeVersion();
                }
            }
        }
    }

    public void addAll(Iterable<PortState> other)
    {
        other.forEach(p -> this.add(p));
    }

    /**
     * Returns immutable list of current ports.
     * Allows for iteration while ensuring all updates occur via
     * {@link #add(PortState)} and {@link #remove(PortState)} adhering
     * to all logic and preventing concurrent modification exception. <p>
     */
    public ImmutableList<PortState> snapshot()
    {
        return ImmutableList.copyOf(this.ports);
    }

    public void clear()
    {
        if(Configurator.logTransportNetwork) 
            Log.info("PortTracker.clear: circuit = %d",
                    this.owner.carrierAddress());
        
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
    
    /**
     * Handles implementation of {@link CarrierCircuit#mergeInto(CarrierCircuit)}
     */
    protected void mergeInto(PortTracker into)
    {
        if(Configurator.logTransportNetwork) 
            Log.info("PortTracker.mergeInto: from = %d, to = %d",
                    this.owner.carrierAddress(),
                    into.owner.carrierAddress());
        
        this.movePorts(ImmutableList.copyOf(this.ports), into);
    }
    
    /**
     * Moves ports in the given list from this tracker into the other,
     * Remove all the ports before swapping, and does all swaps before adding. 
     * If we did ports individuals then carrier group ports would no longer 
     * be associated with this circuit during removal and would fail assertion checks
     */
    private void movePorts(List<PortState> targets, PortTracker into)
    {
        targets.forEach(p -> this.remove(p));
        
        targets.forEach(p -> p.swapCircuit(this.owner, into.owner));
        
        into.addAll(targets);
    }
    
    /**
     * Handles implementation of {@link CarrierCircuit#movePorts(CarrierCircuit, Predicate)}
     */
    public void movePorts(PortTracker into, Predicate<PortState> predicate)
    {
        if(Configurator.logTransportNetwork) 
            Log.info("PortTracker.movePorts: from = %d, to = %d",
                    this.owner.carrierAddress(),
                    into.owner.carrierAddress());
        
        this.movePorts(this.ports.stream().filter(predicate).collect(Collectors.toList()), into);
    }
}
