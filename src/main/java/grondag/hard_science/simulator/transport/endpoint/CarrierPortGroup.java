package grondag.hard_science.simulator.transport.endpoint;

import java.util.Collections;
import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.AbstractIterator;

import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.init.ModPorts;
import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.CarrierCircuit;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.management.LogisticsService;
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

    /**
     * Counts how many ports in this group are attached to the internal
     * carrier.  If drops to zero, then void our reference to the
     * internal carrier so that it doesn't get used/retained improperly.
     * Will include both bridge and carrier ports.
     */
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
        @Nullable
        public CarrierCircuit internalCircuit()
        {
            return internalCircuit;
        }

        /**
         * For carrier ports and bridge ports in carrier mode, 
         * will *always* return internal carrier if it exists,
         * even if this specific port is not attached.<p>
         * 
         * {@inheritDoc}
         */
        @Override
        @Nullable
        public CarrierCircuit externalCircuit()
        {
            switch(this.getMode())
            {
            case BRIDGE:
                // note we are checking raw value for external here due to override logic
                assert (externalCircuit == null && internalCircuit == null)
                || externalCircuit != internalCircuit
                    : "External circuit matches internal circuit on bridge port";
                return externalCircuit;
                
            case CARRIER:
             // note we are checking raw value for external here due to override logic
                assert externalCircuit == null 
                    || externalCircuit == internalCircuit
                        : "Mismatched external circuit on carrier port";
                return internalCircuit;
                
            case DIRECT:
                assert false : "Unsupported port mode for carrier group port";
                return null;
                
            case DISCONNECTED:
            case NO_CONNECTION_CHANNEL_MISMATCH:
            case NO_CONNECTION_INCOMPATIBLE:
            case NO_CONNECTION_LEVEL_GAP:
            case NO_CONNECTION_STORAGE_TYPE:
                assert externalCircuit == null
                    : "Non-null external circuit on disconnected port";
                return null;
                
            default:
                assert false: "Missing enum mappig";
                return null;
            }
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
        public void attach(
                @Nonnull CarrierCircuit externalCircuit, 
                @Nonnull PortState mate)
        {
            assert LogisticsService.serviceFor(storageType).confirmServiceThread() 
                : "Transport logic running outside transport thread";
            
            assert internalCircuit == null ? carrierPortCount == 0 : carrierPortCount > 0
                    : "Inconsistent carrier count on carrier port pre-attach";
            
            switch(this.mode)
            {
            case BRIDGE:
                
                if(Configurator.logTransportNetwork) 
                    Log.info("CarrierPortGroup.attach %s: using bridge mode logic",
                            this.device().machineName());
                
                // If internal carrier not already set up then 
                // need to create it and attach.  Will happen only
                // if the start port to connect connects in bridge mode.
                // Any previous carrier-mode attachments will have
                // established a carrier shared with another device.
                if(internalCircuit == null)
                {
                    internalCircuit = new CarrierCircuit(this.port().internalCarrier, this.getConfiguredChannel());
                    
                    if(Configurator.logTransportNetwork) 
                        Log.info("CarrierPortGroup.attach %s: created new internal circuit %d",
                                this.device().machineName(),
                                internalCircuit.carrierAddress());
                }

                // need to explicitly attach to internal carrier for bridge
                // ports because super.attach will handle external circuit
                internalCircuit.attach(this, true);
                break;
                
            case CARRIER:
                
                if(Configurator.logTransportNetwork) 
                    Log.info("CarrierPortGroup.attach %s: using carrier mode logic",
                            this.device().machineName());
                
                // These checks should have been done already but doesn't hurt to check again
                if(internalCircuit == null)
                {
                    if(externalCircuit.channel != this.getConfiguredChannel()) 
                    {
                        throw new UnsupportedOperationException("CarrierPortGroup request for mismatched channel attach");
                    }
                    
                    // note there is no need to attach to internal carrier 
                    // for carrier ports because internal and external circuits are same
                    internalCircuit = externalCircuit;
                 
                }
                else if(externalCircuit != internalCircuit)
                {
                    // already have an internal circuit so any new carrier ports
                    // must be on the same circuit
                    throw new UnsupportedOperationException("CarrierPortGroup request for mismatched circuit attach");
                }
                break;
          
            default:
                throw new UnsupportedOperationException("Incorrect/Unsupported Port Mode");
            }
            
            super.attach(externalCircuit, mate);
            carrierPortCount++;

            assert internalCircuit != null
                    : "Missing internal carrier on carrier group post-attach.";
            
            assert carrierPortCount > 0
                : "Inconsistent carrier count (zero) on carrier group post-attach";
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
            assert LogisticsService.serviceFor(storageType).confirmServiceThread() : "Transport logic running outside transport thread";

            assert internalCircuit != null
                : "Missing internal carrier on carrier port pre-detach.";
            
            assert carrierPortCount > 0
                : "Inconsistent carrier count on carrier port pre-detach.";

            // need to explicitly detach to internal carrier for bridge
            // ports because super.detach will only handle external circuit.
            // NB: must do comparison on mode before super method, which
            // will set mode to disconnected.
            if(this.mode.isBridge()) internalCircuit.detach(this);

            super.detach();
            
            if(--carrierPortCount == 0) internalCircuit = null;
        }

        /**
         * This will be called multiple times for the same port group
         * if multiple ports are attached to the same carrier.  However
         * because all the ports are (in concept) attached to the same
         * physical carrier this can create a temporary situation
         * where carrier ports external carrier are different from the 
         * internal carrier.<p>
         * 
         * Normally this is prevented by only allowing carrier ports to 
         * connect, one-at-a-time, if and only of the external carrier 
         * matches the internal carrier. <p>
         * 
         * We could have prevented this by forcing ports to disconnect
         * when there is a merge or split event, but such events are 
         * frequent, especially on world load, and it could be a performance
         * risk for large, complex networks. And it wouldn't necessarily be simple,
         * because we'd have to disconnect and reconnect all involved
         * <em>device blocks</em> (not ports) in a specific sequence that
         * would resolve the merge/split event that is currently handled by a swap.<p>
         * 
         * So to prevent inconsistency within this device, we update the internal carrier 
         * and <em>all ports</em> on the device.  If method is called multiple times
         * because there are multiple ports, subsequent calls will be ignored because
         * the swapped circuit will no longer be present in the device. <p>
         * 
         * This does not prevent temporary inconsistency of carrier circuits
         * with port <em>mates</em> but there is nothing we can do about that here.<p>
         * 
         * {@inheritDoc}
         */
        @Override
        public void swapCircuit(@Nonnull CarrierCircuit oldCircuit, @Nonnull CarrierCircuit newCircuit)
        {
            assert LogisticsService.serviceFor(storageType).confirmServiceThread() : "Transport logic running outside transport thread";
            
            // see notes in header
            if(internalCircuit == oldCircuit)
            {
                if(Configurator.logTransportNetwork) 
                    Log.info("CarrierPortGroup.swapCircuit %s: replacing internal circuit %d with new circuit %d",
                            this.device().machineName(),
                            oldCircuit.carrierAddress(),
                            newCircuit.carrierAddress());
                
                internalCircuit = newCircuit;
                
                for(PortState port : CarrierPortGroup.this.ports)
                {
                    // reproducing per-port logic here because
                    // super method would call device refresh before
                    // all ports have been updated
                    if(port.externalCircuit == oldCircuit)
                    {
                        if(Configurator.logTransportNetwork) 
                            Log.info("CarrierPortGroup.swapCircuit %s: replacing external circuit %d with new circuit %d",
                                    this.device().machineName(),
                                    oldCircuit.carrierAddress(),
                                    newCircuit.carrierAddress());

                        port.externalCircuit = newCircuit;
                    }
                }
                
                this.device().refreshTransport(storageType);
            }
            // if internal carrier not involved, then normal logic applies
            else
            {
                super.swapCircuit(oldCircuit, newCircuit);
            }
        }

        @Override
        @Nonnull
        public Iterable<PortState> carrierMates()
        {
            if(this.mode != PortMode.CARRIER) return Collections.emptyList();
            
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
