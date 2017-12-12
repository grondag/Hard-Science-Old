package grondag.hard_science.simulator.transport;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

import grondag.hard_science.simulator.domain.IDomainMember;
import grondag.hard_science.simulator.resource.StorageType;

/**
 * Characteristics of a physical network node.
 * Every continuously connected network
 */
public interface ITransportNode extends IDomainMember
{
    /**
     * Use this to generate values for {@link #transportAddress()}
     */
    public static AtomicInteger NEXT_ADDRESS = new AtomicInteger(1);
    
    /**
     * Physical network address - not persisted but
     * unique and immutable within a game session.
     * Implementation should always generate via {@link #NEXT_ADDRESS}.
     */
    public int transportAddress();
    
    /**
     * Return true if this node interacts with the given type of 
     * transport (power, fluid, items, etc.)
     */
    public default boolean isTransportTypeSupported(StorageType<?> storageType) { return false; }
    
    public int getLocalTransportSubNet();
    
    public void setTransportSubNet(@Nonnull TransportSubNet net);
    
    
}
