package grondag.hard_science.init;

import javax.annotation.Nonnull;

import grondag.hard_science.simulator.resource.EnumStorageType;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.Port;
import grondag.hard_science.simulator.transport.endpoint.PortType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ModPorts
{
    private static final Port[][][]lookup 
        = new Port[EnumStorageType.values().length][CarrierLevel.values().length][PortType.values().length];
    
    public static Port find(@Nonnull StorageType<?> storageType, @Nonnull CarrierLevel level, @Nonnull PortType portType)
    {
        return lookup[storageType.enumType.ordinal()][level.ordinal()][portType.ordinal()];
    }
    
    public static final Port power_base_carrier = new Port("power_base_carrier", PortType.CARRIER, CarrierLevel.BOTTOM, StorageType.POWER);
    public static final Port power_inter_carrier = new Port("power_inter_carrier", PortType.CARRIER, CarrierLevel.MIDDLE, StorageType.POWER);
    public static final Port power_inter_bridge = new Port("power_inter_bridge", PortType.BRIDGE, CarrierLevel.MIDDLE, StorageType.POWER);
    public static final Port power_inter_direct = new Port("power_inter_direct", PortType.DIRECT, CarrierLevel.MIDDLE, StorageType.POWER);
    public static final Port power_top_carrier = new Port("power_top_carrier", PortType.CARRIER, CarrierLevel.TOP, StorageType.POWER);
    public static final Port power_top_bridge = new Port("power_top_bridge", PortType.BRIDGE, CarrierLevel.TOP, StorageType.POWER);
    public static final Port power_top_direct = new Port("power_top_direct", PortType.DIRECT, CarrierLevel.TOP, StorageType.POWER);
    
    public static final Port item_base_carrier = new Port("item_base_carrier", PortType.CARRIER, CarrierLevel.BOTTOM, StorageType.ITEM);
    public static final Port item_inter_carrier = new Port("item_inter_carrier", PortType.CARRIER, CarrierLevel.MIDDLE, StorageType.ITEM);
    public static final Port item_inter_bridge = new Port("item_inter_bridge", PortType.BRIDGE, CarrierLevel.MIDDLE, StorageType.ITEM);
    public static final Port item_inter_direct = new Port("item_inter_direct", PortType.DIRECT, CarrierLevel.MIDDLE, StorageType.ITEM);
    public static final Port item_top_carrier = new Port("item_top_carrier", PortType.CARRIER, CarrierLevel.TOP, StorageType.ITEM);
    public static final Port item_top_bridge = new Port("item_top_bridge", PortType.BRIDGE, CarrierLevel.TOP, StorageType.ITEM);
    public static final Port item_top_direct = new Port("item_top_direct", PortType.DIRECT, CarrierLevel.TOP, StorageType.ITEM);
    
    
    @SubscribeEvent
    public static void registerPorts(RegistryEvent.Register<Port> event) 
    {
        registerPort(event, power_base_carrier);
        registerPort(event, power_inter_carrier);
        registerPort(event, power_inter_bridge);
        registerPort(event, power_inter_direct);
        registerPort(event, power_top_carrier);
        registerPort(event, power_top_bridge);
        registerPort(event, power_top_direct);
        
        registerPort(event, item_base_carrier);
        registerPort(event, item_inter_carrier);
        registerPort(event, item_inter_bridge);
        registerPort(event, item_inter_direct);
        registerPort(event, item_top_carrier);
        registerPort(event, item_top_bridge);
        registerPort(event, item_top_direct);
    }
    
    public static void registerPort(RegistryEvent.Register<Port> event, Port port)
    {
        event.getRegistry().register(port);
        lookup[port.storageType.enumType.ordinal()][port.level.ordinal()][port.portType.ordinal()] = port;
    }
}
