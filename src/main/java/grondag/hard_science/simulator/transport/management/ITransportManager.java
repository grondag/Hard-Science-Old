package grondag.hard_science.simulator.transport.management;

import java.util.Collection;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.endpoint.TransportNode;
import grondag.hard_science.simulator.transport.routing.IItinerary;

/**
 *  Contains and manages the transport components of a device. 
 */
public interface ITransportManager extends IReadWriteNBT
{
    /**
     * Called when device connects to the network, either when placed
     * or after deserialization.  Must be called only 1X.
     */
    public void connect();
    
    /**
     * Called when device disconnects from the network, either when placed
     * or after deserialization.  Must be called only 1X.
     */
    public void disconnect();

    /**
     * Nodes on this device for the given transport type. If more than one,
     * should be sorted so that preferred nodes are first.
     */
    public <T extends StorageType<T>> Collection<TransportNode> getNodes(T storageType);
    
    /**
     * Attempts to send the given resource and quantityIn from the device that owns
     * this transport manager to the given destination device. <p>
     * 
     * If route will require a packaged resource at the start, then will fail if
     * resource is not already packaged.
     * @param resource
     * @param quantityIn
     * @param recipient
     * @param connectedOnly
     * @param simulate
     * @return
     */
    public <T extends StorageType<T>> IItinerary<T> send(
            IResource<T> resource, 
            long quantity, 
            IDevice recipient, 
            boolean connectedOnly, 
            boolean simulate);
}
