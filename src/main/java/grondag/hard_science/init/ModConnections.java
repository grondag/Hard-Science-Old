package grondag.hard_science.init;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.simulator.transport.endpoint.Connection;
import grondag.hard_science.simulator.transport.endpoint.Connector;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ModConnections
{
    private static final Int2ObjectOpenHashMap<Connection> connectionMap
        = new Int2ObjectOpenHashMap<Connection>();
    
    // dedicated power Connections
    public static final Connection power_low = new Connection("power_low", ModPorts.power_low);
    public static final Connection power_medium = new Connection("power_medium", ModPorts.power_medium);
    public static final Connection power_high = new Connection("power_high", ModPorts.power_high);
    
    // dedicated vacuum tube Connections
    public static final Connection item_low = new Connection("item_low", ModPorts.item_low);
    public static final Connection item_medium = new Connection("item_medium", ModPorts.item_medium);
    public static final Connection item_high = new Connection("item_high", ModPorts.item_high);
    
    // dedicated fluid pipes
    public static final Connection fluid_low = new Connection("fluid_low", ModPorts.fluid_low);
    public static final Connection fluid_medium = new Connection("fluid_medium", ModPorts.fluid_medium);
    public static final Connection fluid_high = new Connection("fluid_high", ModPorts.fluid_high);
    
    // combined item/power Connections
    public static final Connection powered_item_low = new Connection("powered_item_low", ModPorts.item_low, ModPorts.power_low);
    public static final Connection powered_item_medium = new Connection("powered_item_medium", ModPorts.item_medium, ModPorts.power_medium);
    public static final Connection powered_item_high = new Connection("powered_item_high", ModPorts.item_high, ModPorts.power_high);
    
    // combined fluid/power Connections
    public static final Connection powered_fluid_low = new Connection("powered_fluid_low", ModPorts.fluid_low, ModPorts.power_low);
    public static final Connection powered_fluid_medium = new Connection("powered_fluid_medium", ModPorts.fluid_medium, ModPorts.power_medium);
    public static final Connection powered_fluid_high = new Connection("powered_fluid_high", ModPorts.fluid_high, ModPorts.power_high);
    
    @SubscribeEvent
    public static void registerConnections(RegistryEvent.Register<Connection> event) 
    {
        connectionMap.clear();
        
        event.getRegistry().register(power_low);
        registerConnectionPairing(ModConnectors.power_low, ModConnectors.power_low, power_low);
        registerConnectionPairing(ModConnectors.power_low, ModConnectors.powered_item_low, power_low);
        registerConnectionPairing(ModConnectors.power_low, ModConnectors.powered_fluid_low, power_low);
        
        event.getRegistry().register(power_medium);
        registerConnectionPairing(ModConnectors.power_medium, ModConnectors.power_medium, power_medium);
        registerConnectionPairing(ModConnectors.power_medium, ModConnectors.powered_item_medium, power_medium);
        registerConnectionPairing(ModConnectors.power_medium, ModConnectors.powered_fluid_medium, power_medium);
        
        event.getRegistry().register(power_high);
        registerConnectionPairing(ModConnectors.power_high, ModConnectors.power_high, power_high);
        registerConnectionPairing(ModConnectors.power_high, ModConnectors.powered_item_high, power_high);
        registerConnectionPairing(ModConnectors.power_high, ModConnectors.powered_fluid_high, power_high);
        
        event.getRegistry().register(item_low);
        registerConnectionPairing(ModConnectors.item_low, ModConnectors.item_low, item_low);
        registerConnectionPairing(ModConnectors.item_low, ModConnectors.powered_item_low, item_low);
        
        event.getRegistry().register(item_medium);
        registerConnectionPairing(ModConnectors.item_medium, ModConnectors.item_medium, item_medium);
        registerConnectionPairing(ModConnectors.item_medium, ModConnectors.powered_item_medium, item_medium);
        
        event.getRegistry().register(item_high);
        registerConnectionPairing(ModConnectors.item_high, ModConnectors.item_high, item_high);
        registerConnectionPairing(ModConnectors.item_high, ModConnectors.powered_item_high, item_high);
        
        event.getRegistry().register(fluid_low);
        registerConnectionPairing(ModConnectors.fluid_low, ModConnectors.fluid_low, fluid_low);
        registerConnectionPairing(ModConnectors.fluid_low, ModConnectors.powered_fluid_low, fluid_low);
        
        event.getRegistry().register(fluid_medium);
        registerConnectionPairing(ModConnectors.fluid_medium, ModConnectors.fluid_medium, fluid_medium);
        registerConnectionPairing(ModConnectors.fluid_medium, ModConnectors.powered_fluid_medium, fluid_medium);

        event.getRegistry().register(fluid_high);
        registerConnectionPairing(ModConnectors.fluid_high, ModConnectors.fluid_high, fluid_high);
        registerConnectionPairing(ModConnectors.fluid_high, ModConnectors.powered_fluid_high, fluid_high);

        event.getRegistry().register(powered_item_low);
        registerConnectionPairing(ModConnectors.powered_item_low, ModConnectors.powered_item_low, powered_item_low);
        
        event.getRegistry().register(powered_item_medium);
        registerConnectionPairing(ModConnectors.powered_item_medium, ModConnectors.powered_item_medium, powered_item_medium);

        event.getRegistry().register(powered_item_high);
        registerConnectionPairing(ModConnectors.powered_item_high, ModConnectors.powered_item_high, powered_item_high);

        event.getRegistry().register(powered_fluid_low);
        registerConnectionPairing(ModConnectors.powered_fluid_low, ModConnectors.powered_fluid_low, powered_fluid_low);
        
        event.getRegistry().register(powered_fluid_medium);
        registerConnectionPairing(ModConnectors.powered_fluid_medium, ModConnectors.powered_fluid_medium, powered_fluid_medium);

        event.getRegistry().register(powered_fluid_high);
        registerConnectionPairing(ModConnectors.powered_fluid_high, ModConnectors.powered_fluid_high, powered_fluid_high);

    }

    public static void registerConnectionPairing(@Nonnull Connector first, @Nonnull Connector second, @Nonnull Connection result)
    {
        connectionMap.put(getKeyForPairing(first, second), result);
    }
    
    @Nullable
    public static Connection getConnectionPairing(@Nonnull Connector first, @Nonnull Connector second, @Nonnull Connection result)
    {
        return connectionMap.get(getKeyForPairing(first, second));
    }
    
    private static int getKeyForPairing(@Nonnull Connector first, @Nonnull Connector second)
    {
        int id1 = ModRegistries.connectorRegistry.getID(first);
        int id2 = ModRegistries.connectorRegistry.getID(second);
        return id1 < id2 ? id2 << 16 | id1 : id1 << 16 | id2;
    }
    
    @Nullable
    public static Connection getConnectionPairing(Connector first, Connector second)
    {
        return null;
    }
}
