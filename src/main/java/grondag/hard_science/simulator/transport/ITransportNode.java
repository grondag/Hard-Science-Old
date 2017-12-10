package grondag.hard_science.simulator.transport;

import javax.annotation.Nonnull;

/**
 * Characteristics of a physical network.
 * Every continuously connected network
 * @author grondag
 *
 */
public interface ITransportNode
{
    public TransportSubNet getTransportSubNet();
    
    public void setTransportSubNet(@Nonnull TransportSubNet net);
}
