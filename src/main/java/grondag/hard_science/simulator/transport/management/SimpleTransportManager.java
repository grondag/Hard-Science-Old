package grondag.hard_science.simulator.transport.management;

import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.device.blocks.IDeviceBlockManager;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ITypedStorage;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.CarrierCircuit;
import grondag.hard_science.simulator.transport.endpoint.PortState;
import grondag.hard_science.simulator.transport.routing.IItinerary;
import grondag.hard_science.simulator.transport.routing.Legs;

/**
 * Transport manager for single carrier
 */
public class SimpleTransportManager<T extends StorageType<T>> implements ITransportManager<T>, ITypedStorage<T>
{
    private final IDevice owner;
    private final T storageType;
    
    /** 
     * Used to cache legs information if this device is
     * attached to more than one circuit.  Otherwise will
     * rely on cache in the single circuit and will always be null.
     */
    private Legs legs = null;
    
    /**
     * Carriers that can be used for transport in/out of this device.
     * SHOULD ONLY BE CHANGED FROM CONNECTION MANAGER THREAD
     */
    private SimpleUnorderedArrayList<CarrierCircuit> circuits = 
            new SimpleUnorderedArrayList<CarrierCircuit>();
    
    public SimpleTransportManager(IDevice owner, T storageType)
    {
        this.owner = owner;
        this.storageType = storageType;
    }
    
    public IItinerary<T> send(IResource<T> resource, long quantity, IDevice recipient, boolean connectedOnly, boolean simulate)
    {
        assert ConnectionManager.confirmNetworkThread() : "Transport logic running outside transport thread";
        
        //TODO: implement
        return null;
    }

    @Override
    public IDevice device()
    {
        return this.owner;
    }

    @Override
    public void refreshTransport()
    {
        assert ConnectionManager.confirmNetworkThread() : "Transport logic running outside transport thread";
        this.circuits.clear();
        this.legs = null;
        
        // on disconnect we'll get a null block manager
        IDeviceBlockManager blockMgr = this.device().blockManager();
        if(blockMgr == null) return;
        
        for(PortState port : blockMgr.getPorts(this.storageType, true))
        {
            switch(port.portMode())
            {
            case CARRIER:
            case DIRECT:
                // device is navigable via the external circuit for direct ports 
                // and for carrier ports external/internal always the same
                // so can always use external circuit
                this.circuits.addIfNotPresent(port.externalCircuit());
                break;
                
            // bridge devices never enable transport for this device
            // (purpose is to enable transport for device on other side of bridge)
            // but we want to track them for debug/display purposes
            case BRIDGE_ACTIVE:
            case BRIDGE_PASSIVE:
                this.circuits.addIfNotPresent(port.internalCircuit());
                break;
                
            case DISCONNECTED:
            case NO_CONNECTION_CHANNEL_MISMATCH:
            case NO_CONNECTION_INCOMPATIBLE:
            case NO_CONNECTION_LEVEL_GAP:
            case NO_CONNECTION_STORAGE_TYPE:
                break;

            default:
                assert false : "missing enum mapping";
                break;
            
            }
        }
    }

    @Override
    public T storageType()
    {
        return this.storageType;
    }

    @Override
    public Legs legs()
    {
        if(this.circuits.isEmpty()) return Legs.EMPTY_LEGS;
        
        if(this.circuits.size() == 1) return this.circuits.get(0).legs();
        
        // more than one circuit, so check cache
        if(this.legs == null || !this.legs.isCurrent())
        {
            this.legs = new Legs(this.circuits);
        }
        return this.legs;
    }
}
