package grondag.hard_science.simulator.transport.endpoint;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.Carrier;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Represents a single, resource-specific port
 * within a connnector.
 */
public class Port extends IForgeRegistryEntry.Impl<Port>
{
    public final StorageType<?> storageType;
    public final PortType portType;
    public final CarrierLevel level;
    /**
     * For Carrier and Bridge ports, the carrier
     * type within the device. <p>
     * 
     * For Carrier ports, will be same as externalCarrier.<p>
     * Will be null for Direct ports.  
     */
    @Nullable
    public final Carrier internalCarrier;
    
    /**
     * The carrier type offers to or expected of potential mate ports.
     * Will be same as internalCarrier for Carrier ports. For bridge
     * ports will be one level lower than internal.
     */
    @Nonnull
    private final Carrier externalCarrier;
    
    private final String label;
    
    public Port(String name, PortType portType, CarrierLevel level, StorageType<?> storageType)
    {
        this.setRegistryName(name);
        this.portType = portType;
        this.level = level;
        this.storageType = storageType;
        this.label = storageType.enumType.toString() + "/" + portType.toString() + "-" + level.toString();
        switch(portType)
        {
        case BRIDGE:
            this.internalCarrier = Carrier.findByTypeAndLevel(storageType, level);
            this.externalCarrier = Carrier.findByTypeAndLevel(storageType, level.below());
            break;

        case DIRECT:
            this.internalCarrier = null;
            this.externalCarrier = Carrier.findByTypeAndLevel(storageType, level);
            break;
        
        default:
            assert false : "Unhandled PortType enum";
        
        case CARRIER:
            this.internalCarrier = Carrier.findByTypeAndLevel(storageType, level);
            this.externalCarrier = internalCarrier;
            break;
        }
    }
    
    @Override
    public String toString()
    {
        return this.label;
    }
    
    /**
     * The carrier type offers to or expected of potential mate ports.
     * For bridge ports in bridge mode will be one level lower than internal.
     * Will be same as internalCarrier for Carrier ports and for bridge
     * ports operating in carrier mode.
     */
    @Nonnull
    public Carrier externalCarrier(PortMode mode)
    {
        return mode == PortMode.CARRIER
                ? this.internalCarrier
                : this.externalCarrier;
    }
}
