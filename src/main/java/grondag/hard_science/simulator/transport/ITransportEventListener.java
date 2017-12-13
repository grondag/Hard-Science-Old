package grondag.hard_science.simulator.transport;


import grondag.hard_science.simulator.domain.DomainManager;
import grondag.hard_science.simulator.resource.StorageType;

/**
 * Implement and override the events of interest
 * after registering with the domain transport event bus.
 */
public interface ITransportEventListener<T extends StorageType<T>>
{
    /**
     * Called after a node is added to the domain.
     * IS called during deserialization. Check
     * {@link DomainManager#isDeserializationInProgress()}
     * if you need to filter out additions during reload.
     */
    public default void onNodeAdded(ITransportNode<T> node) {};
    
    /**
     * Called just before a node is removed from the domain.
     * In-game events that cause this are breaking blocks,
     * explosions, and changing ownership of blocks. 
     * Is NOT called when a connection is broken, because the
     * node is still a domain member. 
     */
    public default void onNodeRemoved(ITransportNode<T> node) {};
}
