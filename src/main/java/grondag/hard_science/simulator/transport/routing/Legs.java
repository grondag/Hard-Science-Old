package grondag.hard_science.simulator.transport.routing;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;

import grondag.hard_science.simulator.transport.carrier.CarrierCircuit;

/**
 * Describes upward routes available from a circuit
 */
public class Legs
{
    public static final Legs EMPTY_LEGS = new Legs();
    
    /**
     * Max circuit bridge version for all included circuits at time of create.
     * If any included circuit has a version higher than this, then
     * this information is no longer valid. See {@link CarrierCircuit#bridgeVersion()}.
     */
    private final int maxBridgeVersion;
    
    /**
     * All circuits referenced by this instance.
     */
    public final ImmutableSet<CarrierCircuit> circuits;
    
    /**
     * All legs originating from a circuit or device (for compounded Legs).
     * This is a list of lists. The top level list has en entry for 
     * every unique end circuit for which there is a leg, and is sorted by
     * carrier level (lowest first) and then by the address of the end circuit.
     * 
     * This structure allows for fast pair-wise iterations through two
     * leg lists during route formation.
     */
    private final ImmutableList<ImmutableList<Leg>> legs;
    
    /**
     * Unique Top-level circuits across all legs.
     * For two devices to be connected, they must share at
     * least one island. Allows for connectivity checking
     * without building specific routes.
     */
    public final ImmutableSet<CarrierCircuit> islands;
    
    public Legs(CarrierCircuit forCircuit)
    {
        int maxVersion = forCircuit.bridgeVersion();
        
        ImmutableSet.Builder<CarrierCircuit> circuitBuilder = ImmutableSet.builder();
        circuitBuilder.add(forCircuit);
        
        LegBuilder legBuilder = new LegBuilder();
        
        Leg firstLeg = Leg.create(forCircuit);
        legBuilder.add(firstLeg);
        
        ImmutableSet.Builder<CarrierCircuit> islandBuilder = ImmutableSet.builder();
        
        if(forCircuit.parents().isEmpty())
        {
            // no parents, so we are our own island
            islandBuilder.add(forCircuit);
        }
        else
        {
            // have parents, so let's expand legs
            LinkedList<Leg> workList = new LinkedList<Leg>();
            workList.add(firstLeg);
            while(!workList.isEmpty())
            {
                Leg workLeg = workList.poll();
                
                // not checking for empty because won't be in
                // worklist if parents is empty
                for(CarrierCircuit c : workLeg.end().parents())
                {
                    // don't extend legs with circuits we can directly access
                    // but can't appy this check to the starting node
                    // because then we wouldn't get the first tier of legs
                    if(workLeg != firstLeg && forCircuit.parents().contains(c)) continue;
                    
                    // add to unique circuit list
                    circuitBuilder.add(c);
                    maxVersion = Math.max(maxVersion, c.bridgeVersion());
                    
                    // extend leg and add to legs
                    Leg newLeg = workLeg.append(c);
                    
                    legBuilder.add(newLeg);
                    
                    // check end of this leg for another level of parents
                    if(newLeg.end().parents().isEmpty())
                    {
                        // no parents, so this circuit is an island
                        islandBuilder.add(newLeg.end());
                    }
                    else
                    {
                        // found more parents, so add to work list and keep going
                        workList.add(newLeg);
                    }
                }
            }
            
        }
        
        this.legs = legBuilder.build();
        this.circuits = circuitBuilder.build();
        this.islands = islandBuilder.build();
        this.maxBridgeVersion = maxVersion;
    }
    
    /** 
     * Combines legs from multiple circuits - used to represent information 
     * for a device attached to more than one circuit.
      */
    public Legs(Iterable<CarrierCircuit> circuits)
    {
        int maxVersion = 0;
        
        ImmutableSet.Builder<CarrierCircuit> circuitBuilder = ImmutableSet.builder();
        
        LegBuilder legBuilder = new LegBuilder();
        
        ImmutableSet.Builder<CarrierCircuit> islandBuilder = ImmutableSet.builder();
        
        for(CarrierCircuit c : circuits)
        {
            Legs legs = c.legs();
            
            maxVersion = Math.max(maxVersion, legs.maxBridgeVersion);
            circuitBuilder.addAll(legs.circuits);
            
            for(ImmutableList<Leg> list : legs.legs)
            {
                for(Leg leg : list)
                {
                    legBuilder.add(leg);
                }
            }
            islandBuilder.addAll(legs.islands);
        }
        
        this.legs = legBuilder.build();
        this.circuits = circuitBuilder.build();
        this.islands = islandBuilder.build();
        this.maxBridgeVersion = maxVersion;
    }
    
    /** for empty legs */
    private Legs()
    {
        this.circuits = ImmutableSet.of();
        this.legs = ImmutableList.of();
        this.islands = ImmutableSet.of();
        this.maxBridgeVersion = Integer.MAX_VALUE;
    }
    
    
    public boolean isCurrent()
    {
        if(this.circuits.isEmpty()) return true;
        
        for(CarrierCircuit c : this.circuits)
        {
            if(c.bridgeVersion() > this.maxBridgeVersion) return false;
        }
        return true;
    }
    
    public ImmutableList<ImmutableList<Leg>> legs()
    {
        return this.legs;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for(ImmutableList<Leg> list : this.legs)
        {
            for(Leg leg : list)
            {
                if(sb.length() > 0) sb.append(", ");
                sb.append(leg.toString());
            }
        }
        sb.append(" Islands: ");
        {
            int i = 0;
            for(CarrierCircuit c : this.islands)
            {
                sb.append(c.carrierAddress());
                if(++i < this.islands.size())
                {
                    sb.append(", ");
                }
            }
        }
        sb.append(" Circuits: ");
        {
            int i = 0;
            for(CarrierCircuit c : this.circuits)
            {
                sb.append(c.carrierAddress());
                if(++i < this.circuits.size())
                {
                    sb.append(", ");
                }
            }
        }
        sb.append(" ver: ");
        sb.append(this.maxBridgeVersion);
        return sb.toString();
    }
    
    private static class LegBuilder
    {
        // create map of leg builders keyed by end circuit
        // and add builder for first circuit
        private HashMap<CarrierCircuit, ImmutableList.Builder<Leg>> legBuilders
         = new HashMap<CarrierCircuit, ImmutableList.Builder<Leg>>();
        
        protected void add(Leg leg)
        {
            ImmutableList.Builder<Leg> builder = legBuilders.get(leg.end());
            if(builder == null)
            {
                builder = new ImmutableList.Builder<>();
                legBuilders.put(leg.end(), builder);
            }
            builder.add(leg);
        }
        
        protected ImmutableList<ImmutableList<Leg>> build()
        {
            ImmutableList.Builder<ImmutableList<Leg>> lastLegBuilder = ImmutableList.builder();
            
            // here's the reason for this whole mess: sort the lists by level, end carrier
            legBuilders
            .entrySet()
            .stream()
            .sorted(new Comparator<Map.Entry<CarrierCircuit, ImmutableList.Builder<Leg>>>()
            {
                @Override
                public int compare(Entry<CarrierCircuit, Builder<Leg>> o1, Entry<CarrierCircuit, Builder<Leg>> o2)
                {
                    CarrierCircuit c1 = o1.getKey();
                    CarrierCircuit c2 = o2.getKey();
                    return ComparisonChain
                            .start()
                            .compare(c1.carrier.level.ordinal(),
                                    c2.carrier.level.ordinal())
                            .compare(c1.carrierAddress(), c2.carrierAddress())
                            .result();
                }
                
            })
            .forEach(e -> lastLegBuilder.add(e.getValue().build()));
            
            return lastLegBuilder.build();
        }
    }
}
