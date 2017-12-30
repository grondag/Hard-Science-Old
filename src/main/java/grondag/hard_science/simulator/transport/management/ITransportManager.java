package grondag.hard_science.simulator.transport.management;

import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.device.IDeviceComponent;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.routing.IItinerary;
import grondag.hard_science.simulator.transport.routing.Legs;

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
     * Called after transport ports on this device are attached
     * or detached to notify transport manager to update transport
     * addressability for this device. Call happens via
     * {@link IDevice#refreshTransport(StorageType)}.
     * 
     * SHOULD ONLY BE CALLED FROM CONNECTION MANAGER THREAD.
     */
    public void refreshTransport();
    
    /**
     * All legs accessible from circuits on which this device is connected.
     * @return
     */
    public Legs legs();
    
}
