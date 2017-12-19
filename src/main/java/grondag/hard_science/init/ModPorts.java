package grondag.hard_science.init;

import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.endpoint.Port;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ModPorts
{
    public static final Port power_low = new Port("power_low", StorageType.POWER);
    public static final Port power_medium = new Port("power_medium", StorageType.POWER);
    public static final Port power_high = new Port("power_high", StorageType.POWER);
    
    public static final Port item_low = new Port("item_low", StorageType.ITEM);
    public static final Port item_medium = new Port("item_medium", StorageType.ITEM);
    public static final Port item_high = new Port("item_high", StorageType.ITEM);
    
    public static final Port fluid_low = new Port("fluid_low", StorageType.FLUID);
    public static final Port fluid_medium = new Port("fluid_medium", StorageType.FLUID);
    public static final Port fluid_high = new Port("fluid_high", StorageType.FLUID);
    
    @SubscribeEvent
    public static void registerPorts(RegistryEvent.Register<Port> event) 
    {
        event.getRegistry().register(power_low);
        event.getRegistry().register(power_medium);
        event.getRegistry().register(power_high);
        
        event.getRegistry().register(item_low);
        event.getRegistry().register(item_medium);
        event.getRegistry().register(item_high);
        
        event.getRegistry().register(fluid_low);
        event.getRegistry().register(fluid_medium);
        event.getRegistry().register(fluid_high);
    }

}
