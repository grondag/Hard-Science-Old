package grondag.hard_science.simulator.transport.management;

import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.device.IDeviceComponent;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.endpoint.TransportNode;
import grondag.hard_science.simulator.transport.routing.IItinerary;

/**
 *  Contains and manages the transport components of a device. 
 */
public interface ITransportManager<T extends StorageType<T>> extends IDeviceComponent
{

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
    public IItinerary<T> send(
            IResource<T> resource, 
            long quantity, 
            IDevice recipient, 
            boolean connectedOnly, 
            boolean simulate);

    /**
     * Called by transport circuits after ports for this device are detached from
     * a circuit and a previously added node has become disconnected as a result.
     */
    public void removeTransportNode(TransportNode node);
    
    /**
     * Called by transport circuits after ports for this device have attached to
     * a circuit and a new transport node has formed as a result.
     */
    public void addTransportNode(TransportNode node);

    /**
     * True if has at least one node.
     */
    boolean hasNodes();
}
