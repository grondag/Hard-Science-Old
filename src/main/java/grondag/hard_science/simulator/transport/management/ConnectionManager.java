package grondag.hard_science.simulator.transport.management;

import static grondag.hard_science.simulator.transport.management.ConnectionResult.BRIDGE_CARRIER;
import static grondag.hard_science.simulator.transport.management.ConnectionResult.BRIDGE_DIRECT;
import static grondag.hard_science.simulator.transport.management.ConnectionResult.CARRIER_BRIDGE;
import static grondag.hard_science.simulator.transport.management.ConnectionResult.CARRIER_CARRIER;
import static grondag.hard_science.simulator.transport.management.ConnectionResult.CARRIER_DIRECT;
import static grondag.hard_science.simulator.transport.management.ConnectionResult.DIRECT_BRIDGE;
import static grondag.hard_science.simulator.transport.management.ConnectionResult.DIRECT_CARRIER;
import static grondag.hard_science.simulator.transport.management.ConnectionResult.FAIL_CHANNEL_MISMATCH;
import static grondag.hard_science.simulator.transport.management.ConnectionResult.FAIL_INCOMPATIBLE;
import static grondag.hard_science.simulator.transport.management.ConnectionResult.FAIL_LEVEL_GAP;
import static grondag.hard_science.simulator.transport.management.ConnectionResult.FAIL_STORAGE_TYPE;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.library.concurrency.PrivilegedExecutor;
import grondag.hard_science.simulator.transport.carrier.CarrierCircuit;
import grondag.hard_science.simulator.transport.endpoint.Port;
import grondag.hard_science.simulator.transport.endpoint.PortMode;
import grondag.hard_science.simulator.transport.endpoint.PortState;
import grondag.hard_science.simulator.transport.routing.Leg;

public class ConnectionManager
{

    /**
     * Should be used for all network topology updates and topology - dependent tasks.
     * TODO: based on profiling results, consider separate threads for items, power, etc.
     */
    public static final PrivilegedExecutor EXECUTOR = new PrivilegedExecutor("Hard Science Network Topology Thread");

    /**
     * Check in logic that should only run on {@link #EXECUTOR}
     * for network topology.
     */
    public static boolean confirmNetworkThread()
    {
        return Thread.currentThread().getName().startsWith(EXECUTOR.threadName);
    }

    /**
     * Attempts to connect two ports.  Asynchronous. <p>
     * 
     * Assumes (does not verify) ports are physically adjacent or within wireless range.
     */
    public static void connect(@Nonnull PortState first, @Nonnull PortState second)
    {
        EXECUTOR.execute( new Runnable()
        {
            @Override
            public void run()
            {
                if(Configurator.logTransportNetwork) 
                    Log.info("ConnectionManager.connect: CONNECT STARTED for %s to %s", first.portName(), second.portName());

                ConnectionResult result = connectionResult(first, second);
                
                switch(result)
                {
                case CARRIER_CARRIER:
                case BRIDGE_CARRIER:
                case CARRIER_BRIDGE:
                case BRIDGE_DIRECT:
                case CARRIER_DIRECT:
                case DIRECT_BRIDGE:
                case DIRECT_CARRIER:
                    connectPorts(first, second, result);
                    break;
                    
                case FAIL_CHANNEL_MISMATCH:
                    if(Configurator.logTransportNetwork) 
                        Log.info("ConnectionManager.connect: attempt abandoned - channel mismatch.");
                    break;
                    
                case FAIL_INCOMPATIBLE:
                    if(Configurator.logTransportNetwork) 
                        Log.info("ConnectionManager.connect: attempt abandoned - incompatible port types.");
                    break;

                case FAIL_LEVEL_GAP:
                    if(Configurator.logTransportNetwork) 
                        Log.info("ConnectionManager.connect: attempt abandoned - carrier level mismatch.");
                    break;

                case FAIL_STORAGE_TYPE:
                    if(Configurator.logTransportNetwork) 
                        Log.info("ConnectionManager.connect: attempt abandoned - incompatible storage types.");
                    break;
                    
                default:
                    assert false : "ConnectionManager.connect: Unhandled ConnectionResult enum";
                    if(Configurator.logTransportNetwork) 
                        Log.info("ConnectionManager.connect: attempt abandoned - unhandled result. This is a bug.");
                    break;
                
                }
            }
        }, false);
    }

    /**
     * Handles case when both ports are known to be carrier ports
     */
    private static void connectPorts(PortState first, PortState second, ConnectionResult result)
    {
        if(Configurator.logTransportNetwork) 
            Log.info("ConnectionManager.connectPorts: start");

        // now have to decide which circuit to use, or create a new circuit
        // if neither port already has one
        // We reference the internal circuit for ports in carrier mode because
        // external must be the same as internal for carrier ports, but external
        // carrier value may not yet be set. For all port modes, the external
        // carrier is separate from internal, and is what the port will connect with.
        CarrierCircuit firstCircuit = result.left == PortMode.CARRIER ? first.internalCircuit() : first.externalCircuit();
        CarrierCircuit secondCircuit = result.right == PortMode.CARRIER ? second.internalCircuit() : second.externalCircuit();
        if(firstCircuit == null)
        {
            CarrierCircuit newCircuit;
            if(secondCircuit == null)
            {
                if(Configurator.logTransportNetwork) 
                    Log.info("ConnectionManager.connectPorts: Neither port has circuit - creating new circuit");

                newCircuit = new CarrierCircuit(first.port().externalCarrier(result.left), first.getConfiguredChannel());
            }
            else
            {
                if(Configurator.logTransportNetwork) 
                    Log.info("ConnectionManager.connectPorts: First port has null circuit - using circuit from second port");

                newCircuit = secondCircuit;
            }

            attachBothPorts(first, second, newCircuit, result);
        }
        else if(secondCircuit == null || secondCircuit == firstCircuit)
        {
            if(Configurator.logTransportNetwork) 
                Log.info("ConnectionManager.connectCarrierPorts: Second port has null circuit - using circuit from start port");

            attachBothPorts(first, second, firstCircuit, result);
        }
        else // ports already have two different, non-null circuits
        {
            if(firstCircuit.portCount() > secondCircuit.portCount())
            {
                if(result.allowMerge)
                {
                    if(Configurator.logTransportNetwork) 
                        Log.info("ConnectionManager.connectCarrierPorts: Ports have different circuits. Keeping start port circuit & merging second port circuit into start");
    
                    secondCircuit.mergeInto(firstCircuit);
                    if(!attachBothPorts(first, second, firstCircuit, result))
                    {
                        Log.warn("Unable to connect transport ports after circuit merge. This is a bug, and strange (probably bad) things may happen now.");
                    }
                }
                else
                    Log.warn("Mismatched circuits for port connect but merge not allowed. This is a bug, and strange (probably bad) things may happen now.");

            }
            else
            {
                if(result.allowMerge)
                {
                    if(Configurator.logTransportNetwork) 
                        Log.info("ConnectionManager.connectCarrierPorts: Ports have different circuits. Keeping second port circuit & merging start port circuit into second");
                    
                    firstCircuit.mergeInto(secondCircuit);
                    if(!attachBothPorts(first, second, secondCircuit, result))
                    {
                        Log.warn("Unable to connect transport ports after circuit merge. This is a bug, and strange (probably bad) things may happen now.");
                    }
                }
                else
                    Log.warn("Mismatched circuits for port connect but merge not allowed. This is a bug, and strange (probably bad) things may happen now.");

            }
        }
    }

    private static boolean attachBothPorts(PortState first, PortState second, CarrierCircuit toCircuit, ConnectionResult result)
    {
        if(first.attach(toCircuit, result.left, second))
        {
            if(second.attach(toCircuit, result.right, first))
            {
                return true;
            }
            else
            {
                first.detach();
            }
        }
        return false;
    }

    /**
     * Disconnects two ports if they are connected,
     * and splits affected circuits if necessary.
     * Should be called from the port that is being
     * removed if device removal is the cause.
     */
    public static void disconnect(@Nonnull PortState leaving)
    {
        EXECUTOR.execute(new Runnable()
        {
            @Override
            public void run()
            {
                if(Configurator.logTransportNetwork) 
                    Log.info("ConnectionManager.disconnect: DISCONNECT STARTED for %s", leaving.portName());

                if(!leaving.isAttached())
                {
                    if(Configurator.logTransportNetwork) 
                        Log.info("ConnectionManager.disconnect: Disconnect abandoned - port not attached");
                    return;
                }

                PortState mate = leaving.mate();

                assert mate != null : "Missing mate on port disconnect.";

                if(Configurator.logTransportNetwork) 
                    Log.info("ConnectionManager.disconnect: Port mate is %s", mate.portName());


                // split isn't possible unless both ports are carrier ports
                if(leaving.portMode() == PortMode.CARRIER && mate.portMode() == PortMode.CARRIER)
                {
                    if(Configurator.logTransportNetwork) 
                        Log.info("ConnectionManager.disconnect: Checking for possible split");

                    // Carrier split will be necessary UNLESS the mate is navigable via
                    // an alternate route on the same carrier circuit. 
                    // Split also not necessary if no connected ports beyond the leaving port.
                    Set<PortState> reachableFromLeaving = findNavigableCarrierPorts(leaving, leaving.mate());
                    if(reachableFromLeaving.size() > 1 && !reachableFromLeaving.contains(leaving.mate()))
                    {
                        CarrierCircuit existingCircuit = leaving.internalCircuit();

                        CarrierCircuit newCircuit = 
                                new CarrierCircuit(existingCircuit.carrier, existingCircuit.channel);

                        // no alternate route, so must split
                        if(reachableFromLeaving.size() >= existingCircuit.portCount() / 2)
                        {
                            if(Configurator.logTransportNetwork) 
                                Log.info("ConnectionManager.disconnect: Split needed, moving mate-side ports to new circuit %d", newCircuit.carrierAddress());

                            // Reachable ports are at least half of total. 
                            // Swap mate and unreachable ports to a new circuit
                            existingCircuit.movePorts(newCircuit, new Predicate<PortState>()
                            {
                                @Override
                                public boolean test(PortState t)
                                {
                                    return !reachableFromLeaving.contains(t);
                                }
                            });
                        }
                        else
                        {
                            if(Configurator.logTransportNetwork) 
                                Log.info("ConnectionManager.disconnect: Split needed, moving leave-side ports to new circuit %d", newCircuit.carrierAddress());

                            // Reachable ports are less than half of total.
                            // Swap leaving port and reachable ports to a new circuit
                            existingCircuit.movePorts(newCircuit, new Predicate<PortState>()
                            {
                                @Override
                                public boolean test(PortState t)
                                {
                                    return reachableFromLeaving.contains(t);
                                }
                            });
                        }
                    }
                    else if(Configurator.logTransportNetwork) 
                    {
                        Log.info("ConnectionManager.disconnect: Split not needed for %d ports. (If > 1 ports are still reachable)",
                                reachableFromLeaving.size());
                    }
                }
                leaving.detach();
                mate.detach();
            }
        }, false);
    }

    /**
     * Returns all ports that are reachable via peers of the given port
     * and which reference the given ports's internal carrier circuit,
     * either internally or externally.<p>
     * 
     * Will include those peers, mates of those peers, peers of those mates, etc.
     * WILL also include the starting port that is provided.<p>
     * 
     * If stop port is provided, will return as soon as stop node is found.
     * In that case, stop node will be in the result set, but other nodes
     * that are navigable may be left out. Useful as a performance optimization
     * when the results will not be needed if the stop node is found.
     */
    private static HashSet<PortState> findNavigableCarrierPorts(PortState startingFrom, @Nullable PortState stopAt)
    {
        assert startingFrom.portMode() == PortMode.CARRIER
                : "transport topology search with non-carrier starting port";

        CarrierCircuit onCircuit = startingFrom.internalCircuit();

        /**
         * Ports known to reference the internal carrier of the starting port.
         */
        HashSet<PortState> results = new HashSet<PortState>();
        results.add(startingFrom);

        /**
         * Ports that should be checked for carrier mates.
         * Any port in this list should already have been added
         * to results.
         */
        ArrayDeque<PortState> workList = new ArrayDeque<PortState>();
        workList.add(startingFrom);

        while(!workList.isEmpty())
        {
            PortState port = workList.poll();
            for(PortState peer : port.carrierMates())
            {
                if(peer.internalCircuit() == onCircuit && peer.isAttached())
                {
                    if(results.add(peer))
                    {
                        if(peer == stopAt) return results;

                        // note that we check external circuit here. Will pick up
                        // bridge or direct port using the circuit that will need to be
                        // swapped if there is a split and it is attached
                        if(peer.isAttached())
                        {
                            PortState mate = peer.mate();
                            if(mate.externalCircuit() == onCircuit && results.add(mate))
                            {
                                if(mate == stopAt) return results;

                                // Don't need to check peers of mate unless the
                                // carrier passes through into the device. This
                                // will only be true for Carrier ports, so could
                                // have instead checked port type instead of internal carrier.
                                if(mate.internalCircuit() == onCircuit)
                                {
                                    workList.add(mate);
                                }
                            }
                        }
                    }
                }
            }
        }

        return results;
    }

    /**
     * Convenient version of {@link #connectionResult(Port, int, Port, int)}
     */
    @Nonnull
    public static ConnectionResult connectionResult(
            PortState port1,
            PortState port2)
    {
        return connectionResult(
                port1.port(), 
                port1.getConfiguredChannel(), 
                port2.port(), 
                port2.getConfiguredChannel());
    }

    /**
     * Returns the effective port types for the two ports
     * to be mated with the given given channels.
     * Implements all the rules described in PortType.<p>
     */
    @Nonnull
    public static ConnectionResult connectionResult(
            Port port1,
            int channel1,
            Port port2,
            int channel2)
    {
        if(port1.storageType != port2.storageType) 
            return FAIL_STORAGE_TYPE;

        boolean channelMatch = channel1 == channel2;

        // logic relies on enum ordering: CARRIER / DIRECT / BRIDGE
        // and that ports 1 and 2 are always sorted by that order
        boolean swapOrder = port1.portType.ordinal() > port2.portType.ordinal();
        if(swapOrder)
        {
            Port swapPort = port1;
            port1 = port2;
            port2 = swapPort;
        }

        switch(port1.portType)
        {
        case CARRIER:
        {
            switch(port2.portType)
            {
            case CARRIER:
            {
                if(port1.level == port2.level)
                {
                    // Two parents of same level and storage type, so just 
                    // need to check for channel match.  Top level
                    // parents ignore channel.
                    return port1.level.isTop() || channelMatch
                            ? CARRIER_CARRIER
                                    : FAIL_CHANNEL_MISMATCH;
                }
                // non-top carrier-to-carrier must be same level
                else return FAIL_LEVEL_GAP;
            }

            case DIRECT:
            {
                if(port1.level == port2.level)
                {
                    // direct ports can only join with parents at same level
                    return swapOrder ? DIRECT_CARRIER : CARRIER_DIRECT; 
                }
                else
                    return FAIL_LEVEL_GAP;
            }

            case BRIDGE:
            {
                if(port1.level == port2.level)
                {
                    // if bridge at same level as carrier
                    // then acts exactly like another carrier port
                    return port1.level.isTop() || channelMatch
                            ? CARRIER_CARRIER
                                    : FAIL_CHANNEL_MISMATCH;
                }
                else if(port1.level.above() == port2.level)
                {
                    // if bridge is one level above carrier then it acts
                    // as a proper bridge port
                    return swapOrder ? BRIDGE_CARRIER : CARRIER_BRIDGE;
                }
                else return FAIL_LEVEL_GAP;
            }

            default:
                assert false : "Port.connectionResult(): Unhandled PortType enum";
            return FAIL_INCOMPATIBLE;
            }
        }

        case DIRECT:
        {
            switch(port2.portType)
            {
            case CARRIER:
            {
                // port type ordering should make this case impossible
                assert false : "Port.connectionResult(): Incorrect port ordering";
            return FAIL_INCOMPATIBLE;
            }

            case DIRECT:
            {
                // direct ports can't form circuits on their own
                return FAIL_INCOMPATIBLE;
            }

            case BRIDGE:
            {
                // direct ports always act as direct ports
                // so bridge port mode really only a question of level
                if(port1.level == port2.level)
                {       
                    return swapOrder ? CARRIER_DIRECT : DIRECT_CARRIER; 
                }
                else if(port1.level.above() == port2.level)
                {
                    return swapOrder ? BRIDGE_DIRECT : DIRECT_BRIDGE;
                }
                else return FAIL_LEVEL_GAP;
            }

            default:
                assert false : "Port.connectionResult(): Unhandled PortType enum";
            return FAIL_INCOMPATIBLE;
            }
        }

        case BRIDGE:
        {
            switch(port2.portType)
            {

            case CARRIER:
            case DIRECT:
            {
                // port type ordering should make this case impossible
                assert false : "Port.connectionResult(): Incorrect port ordering";
            return FAIL_INCOMPATIBLE;
            }

            case BRIDGE:
            {
                if(port1.level == port2.level)
                {
                    // two bridge ports of same level behave like carrier ports
                    return port1.level.isTop() || channelMatch
                            ? CARRIER_CARRIER
                                    : FAIL_CHANNEL_MISMATCH;
                }
                else if(port1.level == port2.level.below())
                    // if this port is 1 lower, then it will act as carrier port
                    // and other as passive bridge
                    return swapOrder ? BRIDGE_CARRIER : CARRIER_BRIDGE;

                else if(port1.level == port2.level.above())
                    // if this port is 1 higher, then it will act as bridge port
                    // and other as carrier
                    return swapOrder ? CARRIER_BRIDGE : BRIDGE_CARRIER;

                else 
                    return FAIL_LEVEL_GAP;
            }

            default:
                assert false : "Port.connectionResult(): Unhandled PortType enum";
            return FAIL_INCOMPATIBLE;
            }
        }

        default:
            assert false : "Port.connectionResult(): Unhandled PortType enum";
        return FAIL_INCOMPATIBLE;
        }
    }
    
    public static ImmutableList<Leg> legs(CarrierCircuit startingWith)
    {
        ImmutableList.Builder<Leg> builder = ImmutableList.builder();
        Leg firstLeg = Leg.create(startingWith);
        builder.add(firstLeg);
        if(!firstLeg.end().parents().isEmpty())
        {
            LinkedList<Leg> workList = new LinkedList<Leg>();
            workList.add(firstLeg);
            while(!workList.isEmpty())
            {
                Leg workLeg = workList.poll();
                for(CarrierCircuit c : workLeg.end().parents())
                {
                    // don't extend legs with circuits we can directly access
                    // but can't appy this check to the starting node
                    // because then we wouldn't get the first tier of legs
                    if(workLeg != firstLeg && startingWith.parents().contains(c)) continue;
                    
                    Leg newLeg = workLeg.append(c);
                    builder.add(newLeg);
                    if(!newLeg.end().parents().isEmpty()) workList.add(newLeg);
                }
            }
            
        }
        
        return builder.build();
    }
}
