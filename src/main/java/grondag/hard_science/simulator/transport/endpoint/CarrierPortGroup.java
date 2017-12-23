package grondag.hard_science.simulator.transport.endpoint;

import java.util.Iterator;

import javax.annotation.Nonnull;

import grondag.hard_science.init.ModPorts;
import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.CarrierCircuit;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;

/**
 * Collection of ports all on same device sharing same internal carrier.
 */
public class CarrierPortGroup implements Iterable<PortState>
{
    private final StorageType<?> storageType;
    
    private final CarrierLevel carrierLevel;
    
    /**
     * Physical device on which this port is present.
     */
    private final IDevice device;

    private CarrierCircuit internalCircuit;
    
    private SimpleUnorderedArrayList<PortState> ports
        = new SimpleUnorderedArrayList<PortState>();

    /**
     * See {@link PortState#getConfiguredChannel()}
     */
    private byte channel = 0;

    private int carrierPortCount = 0;
    
    public CarrierPortGroup(IDevice device, StorageType<?> storageType, CarrierLevel carrierLevel)
    {
        this.device = device;
        this.storageType = storageType;
        this.carrierLevel = carrierLevel;
    }

    /**
     * See {@link PortState#getConfiguredChannel()} <p>
     * 
     * Device must be disconnected (no internal channel) when changing.
     */
    public void setConfiguredChannel(int channel)
    {
        assert this.internalCircuit == null
                : "Attempt to configure channel with live transport circuit";

        assert !this.carrierLevel.isTop()
        : "Attempt to configure channel on top-level transport device";

        this.channel = (byte) (channel & 0xF);
    }
    
    /**
     * @param isDirect  Returns a bridge-type port if true, carrier type otherwise.
     */
    public PortState createPort(boolean isBridge)
    {
        synchronized(this)
        {
            Port port = ModPorts.find(
                    this.storageType, 
                    this.carrierLevel, 
                    isBridge ? PortType.BRIDGE : PortType.CARRIER);
            
            CarrierPortState result = new CarrierPortState(port);
            this.ports.add(result);
            return result;
        }
    }
    
    /**
     * The number of carrier ports that are attached.
     * If zero, then {@link #internalCircuit} will be null.
     */
    public int carrierPortCount()
    {
        return this.carrierPortCount();
    }

    /**
     * Will call detach if connected.
     */
    public void removePort(PortState port)
    {
        synchronized(this)
        {
            assert this.ports.contains(port)
                : "Removal request for non-existant port";
            
            if(port.isAttached()) port.detach();
            this.ports.removeIfPresent(port);
        }
    }
    
    @Override
    public Iterator<PortState> iterator()
    {
        return this.ports.iterator();
    }
    
    private class CarrierPortState extends PortState
    {
        private CarrierPortState(Port port)
        {
            super(port);
        }

        @Override
        public CarrierCircuit internalCircuit()
        {
            return internalCircuit;
        }

        @Override
        public IDevice device()
        {
            return device;
        }

        @Override
        public int getConfiguredChannel()
        { 
            return channel; 
        }

        /**
         * 
         * For carrier ports, externalCircuit must match existing internal
         * circuit if internal circuit is non-null.
         * 
         * For bridge ports, behaves same as direct port and also creates or joins
         * and internal carrier. (But no conflict with internal is possible because
         * external does not have to match.) <p>
         * 
         * {@inheritDoc}
         */
        @Override
        public boolean attach(@Nonnull CarrierCircuit externalCircuit, PortState mate)
        {
            synchronized(CarrierPortGroup.this)
            {
                assert this.externalCircuit == null
                        : "PortState attach request when already attached.";
                
                switch(this.port().portType)
                {
                case BRIDGE:
                    
                    // bridge ports require a carrier port as mate
                    if(mate.port().portType != PortType.CARRIER) return false;
                    
                    // bridge ports shouldn't be connected until
                    // after internal circuit set up
                    if(internalCircuit == null)
                    {
                        assert false : "Bridge port group missing internal circuit during attach";
                        return false;
                    }
                    
                    if(super.attach(externalCircuit, mate))
                    {
                        
                        if(!internalCircuit.attach(this, true))
                            assert false : "Bridge port unable to attach to internal circuit";
                        
                        carrierPortCount++;
                        return true;
                    }
                    break;
                
                case CARRIER:
                    
                    if(internalCircuit == null)
                    {
                        // we are joining the external circuit so just need to 
                        // confirm compatible channels
                        if(externalCircuit.channel != this.getConfiguredChannel()) return false;
                    }
                    else
                    {
                        // already have an internal circuit so any new carrier ports
                        // must be on the same circuit
                        if(externalCircuit != internalCircuit) return false;
                    }
                    
                    if(super.attach(externalCircuit, mate))
                    {
                        // note there is no need to attach to internal carrier 
                        // for carrier ports because internal and external circuits are same
                        if(internalCircuit == null)
                        {
                            internalCircuit = externalCircuit;
                            
                            assert carrierPortCount == 0
                                    : "Inconsitent carrier count on carrier port attach";
                        }
                        carrierPortCount++;
                        return true;
                    }
                    break;
    
                default:
                    assert false : "Unsupported Port Type in Carrier Port Group";
                
                }
                return false;
            }
        }

        /**
         * For carrier ports, will drop reference to internal circuit
         * whan last carrier port is detached.<p>
         * 
         * {@inheritDoc}
         */
        @Override
        public void detach()
        {
            synchronized(CarrierPortGroup.this)
            {
                if(this.port().portType == PortType.CARRIER)
                {
                    assert internalCircuit != null
                            : "Missing internal carrier on carrier port detach.";
                    
                    assert carrierPortCount > 0
                            : "Inconsistent carrier count on carrier port detach.";
    
                    if(--carrierPortCount == 0)
                    {
                        internalCircuit = null;
                    }
                }
                super.detach();
            }
        }
    }
}

