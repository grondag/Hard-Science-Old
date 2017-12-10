package grondag.hard_science.simulator.transport;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Defines a physically-connected collection of nodes within a domain.
 * Transit within a subnet is considered to be virtually instant,
 * due to time acceleration within the game and use of high-speed
 * vacuum transport. <p>
 * 
 * Each member of the subnet must implement ITransportNode.
 *
 */
public class TransportSubNet
{
    private static final AtomicInteger nextSubnetID = new AtomicInteger(0);
    
    public static final TransportSubNet NONE = new TransportSubNet();

    //TODO this class is a stub
    public final long subnetID = nextSubnetID.getAndIncrement();
}
