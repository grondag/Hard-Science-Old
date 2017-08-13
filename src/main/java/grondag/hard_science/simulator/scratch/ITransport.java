package grondag.hard_science.simulator.scratch;

import java.util.concurrent.atomic.AtomicInteger;

public interface ITransport<T extends IPacket>
{
    /** use this to generate IDs for all implementing classes */
    public static final AtomicInteger NEXT_TRANSPORT_ID = new AtomicInteger(1);
    
    public abstract int getTransportID();
    public abstract boolean requiresScheduling();
    public abstract 

}
