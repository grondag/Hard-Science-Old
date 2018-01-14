package grondag.hard_science.simulator.transport.management;

import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.device.IDeviceComponent;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.CarrierCircuit;
import grondag.hard_science.simulator.transport.routing.Legs;

/**
 *  Contains and manages the transport components of a device. 
 */
public interface ITransportManager<T extends StorageType<T>> extends IDeviceComponent
{

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
     * True if this device is attached to any circuit.
     * If false, implies any off-device transport request will fail.
     */
    public boolean hasAnyCircuit();
    
    /**
     * All legs accessible from circuits on which this device is connected.
     * @return
     */
    public Legs legs();
    
    /**
     * True if can send/receive on the given circuit. Used to validate
     * transport routes.
     */
    public boolean isConnectedTo(CarrierCircuit circuit);
}
