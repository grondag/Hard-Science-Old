package grondag.hard_science.simulator.transport.management;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.library.concurrency.PrivilegedExecutor;
import grondag.hard_science.simulator.transport.carrier.CarrierCircuit;
import grondag.hard_science.simulator.transport.endpoint.PortState;
import grondag.hard_science.simulator.transport.endpoint.PortType;

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
     * True if ports <em>may</em> connect.  Does not guarantee they will.
     */
    public static boolean isConnectionPossible(@Nonnull PortState first, @Nonnull PortState second)
    {
        // ports can only attach 1x
        if(first.isAttached() || second.isAttached()) return false;
        
        // ports must be same storage type
        if(first.port().storageType != second.port().storageType) return false;
        
        return true;
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

                if(!isConnectionPossible(first, second))
                {
                    if(Configurator.logTransportNetwork) 
                        Log.info("ConnectionManager.connect: attempt abandoned - connection not possible");
                    return;
                }
                
                // logic depends on port types
                switch(first.port().portType)
                {
                case BRIDGE:
                    connectBridgePort(first, second);
                    
                case DIRECT:
                    connectDirectPort(first, second);
                    
                case CARRIER:
                    switch(first.port().portType)
                    {
                    case BRIDGE:
                        connectBridgePort(second, first);
                        
                    case DIRECT:
                        connectDirectPort(second, first);
                        
                    case CARRIER:
                        connectCarrierPorts(first, second);
                        
                    default:
                        return;
                    }
                    
                default:
                    return;
                    
                }
                
            }}, false);
    }

    /**
     * Handles case when both ports are known to be carrier ports
     */
    private static void connectCarrierPorts(PortState first, PortState second)
    {
        if(Configurator.logTransportNetwork) 
            Log.info("ConnectionManager.connectCarrierPorts: start (using carrier port connection logic)");

        // carrier ports must be of same type
        if(first.port().internalCarrier != second.port().internalCarrier) 
        {
            if(Configurator.logTransportNetwork) 
                Log.info("ConnectionManager.connectCarrierPorts: ABANDONED due to mismatched internal carriers");
            return;
        }
        
        // non-top carrier ports must have same channel to connect
        if(!first.port().level.isTop() && first.getConfiguredChannel() != second.getConfiguredChannel()) 
        {
            if(Configurator.logTransportNetwork) 
                Log.info("ConnectionManager.connectCarrierPorts: ABANDONED due to mismatched channels");
            return;
        }
        
        // now have to decide which circuit to use, or create a new circuit
        // if neither port already has one
        CarrierCircuit firstCircuit = first.internalCircuit();
        CarrierCircuit secondCircuit = second.internalCircuit();
        if(firstCircuit == null)
        {
            CarrierCircuit newCircuit;
            if(secondCircuit == null)
            {
                if(Configurator.logTransportNetwork) 
                    Log.info("ConnectionManager.connectCarrierPorts: Neither port has circuit - creating new circuit");

                newCircuit = new CarrierCircuit(first.port().internalCarrier, first.getConfiguredChannel());
            }
            else
            {
                if(Configurator.logTransportNetwork) 
                    Log.info("ConnectionManager.connectCarrierPorts: First port has null circuit - using circuit from second port");

                newCircuit = secondCircuit;
            }
                    
            attachBothPorts(first, second, newCircuit);
        }
        else if(secondCircuit == null || secondCircuit == firstCircuit)
        {
            if(Configurator.logTransportNetwork) 
                Log.info("ConnectionManager.connectCarrierPorts: Second port has null circuit - using circuit from first port");

            attachBothPorts(first, second, firstCircuit);
        }
        else // ports already have two different, non-null circuits
        {
            if(firstCircuit.portCount() > secondCircuit.portCount())
            {
                if(Configurator.logTransportNetwork) 
                    Log.info("ConnectionManager.connectCarrierPorts: Ports have different circuits. Keeping first port circuit & merging second port circuit into first");

                secondCircuit.mergeInto(firstCircuit);
                if(!attachBothPorts(first, second, firstCircuit))
                {
                    Log.warn("Unable to connect transport ports after circuit merge. This is a bug, and strange (probably bad) things may happen now.");
                }
            }
            else
            {
                if(Configurator.logTransportNetwork) 
                    Log.info("ConnectionManager.connectCarrierPorts: Ports have different circuits. Keeping second port circuit & merging first port circuit into second");

                firstCircuit.mergeInto(secondCircuit);
                if(!attachBothPorts(first, second, secondCircuit))
                {
                    Log.warn("Unable to connect transport ports after circuit merge. This is a bug, and strange (probably bad) things may happen now.");
                }

            }
        }
    }

    private static boolean attachBothPorts(PortState first, PortState second, CarrierCircuit toCircuit)
    {
        if(first.attach(toCircuit, second))
        {
            if(second.attach(toCircuit, first))
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
     * Handles case when one port is known to be a direct port
     */
    private static void connectDirectPort(PortState direct, PortState other)
    {
        if(Configurator.logTransportNetwork) 
            Log.info("ConnectionManager.connectDirectPort: start (using direct port connection logic)");

    }

    /**
     * Handles case when one port is known to be a bridge port
     */
    private static void connectBridgePort(PortState bridge, PortState other)
    {
        if(Configurator.logTransportNetwork) 
            Log.info("ConnectionManager.connectBridgePort: start (using bridge port connection logic)");

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
                if(leaving.port().portType == PortType.CARRIER && mate.port().portType == PortType.CARRIER)
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
        assert startingFrom.port().portType == PortType.CARRIER
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
}
