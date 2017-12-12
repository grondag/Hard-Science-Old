package grondag.hard_science.simulator.transport;

import com.raoulvdberge.refinedstorage.api.network.node.INetworkNode;

import grondag.hard_science.simulator.domain.DomainManager;

/**
 * Implement and override the events of interest
 * after registering with the domain transport event bus.
 */
public interface ITransportEventListener
{
    /**
     * Called after a node is added to the domain.
     * IS called during deserialization. Check
     * {@link DomainManager#isDeserializationInProgress()}
     * if you need to filter out additions during reload.
     */
    public default void onNodeAdded(INetworkNode node) {};
    
    /**
     * Called just before a node is removed from the domain.
     * In-game events that cause this are breaking blocks,
     * explosions, and changing ownership of blocks. 
     * Is NOT called when a connection is broken, because the
     * node is still a domain member. 
     */
    public default void onNodeRemoved(INetworkNode node) {};
}
