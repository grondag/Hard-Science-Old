package grondag.hard_science.simulator.transport.endpoint;

import java.util.Iterator;

import javax.annotation.Nonnull;

import com.google.common.collect.AbstractIterator;

import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.init.ModPorts;
import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.CarrierCircuit;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.management.ConnectionManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * Collection of ports all on same device sharing same internal carrier.<p>
 * 
 * Methods related to network topology should ONLY be
 * called from the connection manager thread and will 
 * throw an exception if called otherwise.  This avoids
 * the need for synchronization of these methods.
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

    public CarrierPortGroup(
            @Nonnull IDevice device, 
            @Nonnull StorageType<?> storageType, 
            @Nonnull CarrierLevel carrierLevel)
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
        if(this.internalCircuit != null)
            throw new UnsupportedOperationException("Attempt to configure channel with live transport circuit");

        assert !this.carrierLevel.isTop()
        : "Attempt to configure channel on top-level transport device";

        this.channel = (byte) (channel & 0xF);
    }

    /**
     * Creates a new port and adds it to this carrier port group.
     * @param isBridge  Returns a bridge-type port if true, carrier type otherwise.
     */
    public PortState createPort(boolean isBridge, BlockPos pos, EnumFacing face)
    {
        synchronized(this)
        {
            if(this.internalCircuit != null)
                throw new UnsupportedOperationException("Attempt to add port with live transport circuit");
            
            Port port = ModPorts.find(
                    this.storageType, 
                    this.carrierLevel, 
                    isBridge ? PortType.BRIDGE : PortType.CARRIER);

            assert port != null : "CarrierPortGroup.createPort: Unable to find configured port type.";
            
            if(port == null) return null;
            
            CarrierPortState result = new CarrierPortState(port, pos, face);
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
     * Device must not be connected.
     */
    public void removePort(PortState port)
    {
        synchronized(this)
        {
            if(this.internalCircuit != null)
                throw new UnsupportedOperationException("Attempt to remove port with live transport circuit");
            
            assert this.ports.contains(port)
            : "Removal request for non-existant port";

            this.ports.removeIfPresent(port);
        }
    }

    @Override
    public Iterator<PortState> iterator()
    {
        return this.ports.iterator();
    }

    public CarrierCircuit internalCircuit()
    {
        return this.internalCircuit;
    }
    
    private class CarrierPortState extends PortState
    {
        private CarrierPortState(@Nonnull Port port, BlockPos pos, EnumFacing face)
        {
            super(port, pos, face);
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
         * For carrier ports, externalCircuit must match existing internal
         * circuit if internal circuit is non-null.
         * 
         * For bridge ports, behaves same as direct port and also creates or joins
         * an internal carrier. (But no conflict with internal is possible because
         * external does not have to match.) <p>
         * 
         * {@inheritDoc}
         */
        @Override
        public boolean attach(
                @Nonnull CarrierCircuit externalCircuit, 
                @Nonnull PortMode mode,
                @Nonnull PortState mate)
        {
            assert ConnectionManager.confirmNetworkThread() : "Transport logic running outside transport thread";
            
            assert this.externalCircuit == null
                    : "PortState attach request when already attached.";

            switch(mode)
            {
            case BRIDGE_ACTIVE:
            case BRIDGE_PASSIVE:
                
                if(Configurator.logTransportNetwork) 
                    Log.info("CarrierPortGroup.attach: using bridge mode logic");
                
                // If internal carrier not already set up then 
                // need to create it and attach.  Will happen only
                // if the start port to connect connects in bridge mode.
                // Any previous carrier-mode attachments will have
                // established a carrier shared with another device.
                if(internalCircuit == null)
                {
                    internalCircuit = new CarrierCircuit(this.port().internalCarrier, this.getConfiguredChannel());
                    
                    if(Configurator.logTransportNetwork) 
                        Log.info("CarrierPortGroup.attach: created new internal circuit %d",
                                internalCircuit.carrierAddress());
                }

                if(super.attach(externalCircuit, mode, mate))
                {

                    if(!internalCircuit.attach(this, true))
                        assert false : "Bridge port unable to attach to internal circuit";

                    carrierPortCount++;
                    return true;
                }
                break;
                
            case CARRIER:
                
                if(Configurator.logTransportNetwork) 
                    Log.info("CarrierPortGroup.attach: using carrier mode logic");
                
                // These checks should have been done already but doesn't hurt to check again
                if(internalCircuit == null)
                {
                    if(externalCircuit.channel != this.getConfiguredChannel()) 
                    {
                        assert false : "CarrierPortGroup request for mismatched channel attach";
                        return false;
                    }
                }
                else
                {
                    // already have an internal circuit so any new carrier ports
                    // must be on the same circuit
                    if(externalCircuit != internalCircuit)
                    {
                        assert false : "CarrierPortGroup request for mismatched circuit attach";
                        return false;
                    }
                }

                if(super.attach(externalCircuit, mode, mate))
                {
                    // note there is no need to attach to internal carrier 
                    // for carrier ports because internal and external circuits are same
                    if(internalCircuit == null)
                    {
                        internalCircuit = externalCircuit;

                        assert carrierPortCount == 0
                                : "Inconsistent carrier count on carrier port attach";
                    }
                    carrierPortCount++;
                    return true;
                }
                break;
          
            default:
                assert false : "Unsupported Port Type in Carrier Port Group";
                break;
            }
            return false;
        }

        /**
         * For carrier ports, will drop reference to internal circuit
         * when last carrier port is detached.<p>
         * 
         * {@inheritDoc}
         */
        @Override
        public void detach()
        {
            assert ConnectionManager.confirmNetworkThread() : "Transport logic running outside transport thread";

            if(this.portMode() == PortMode.CARRIER)
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

        /**
         * This will be called multiple times for the same port group
         * if multiple ports are attached to the same carrier.  This is
         * fine.  Will simply update internal carrier on start call and
         * ignore subsequent calls. <p>
         * 
         * {@inheritDoc}
         */
        @Override
        public void swapCircuit(@Nonnull CarrierCircuit oldCircuit, @Nonnull CarrierCircuit newCircuit)
        {
            assert ConnectionManager.confirmNetworkThread() : "Transport logic running outside transport thread";

            super.swapCircuit(oldCircuit, newCircuit);

            if(internalCircuit == oldCircuit)
            {
                if(Configurator.logTransportNetwork) 
                    Log.info("CarrierPortGroup.swapCircuit: replacing internal circuit %d with new circuit %d",
                            oldCircuit.carrierAddress(),
                            newCircuit.carrierAddress());
                
                internalCircuit = newCircuit;
            }
        }

        @Override
        @Nonnull
        public Iterable<PortState> carrierMates()
        {
            return new Iterable<PortState>() 
            {

                @Override
                public Iterator<PortState> iterator()
                {
                    return new AbstractIterator<PortState>() 
                    {
                        private Iterator<PortState> unfiltered = ports.iterator();

                        @Override
                        protected PortState computeNext()
                        {
                            while (unfiltered.hasNext()) 
                            {
                                PortState element = unfiltered.next();
                                if (element != CarrierPortState.this) 
                                {
                                    return element;
                                }
                            }
                            return endOfData();
                        }
                    };
                }
            };
        }
    }
}
