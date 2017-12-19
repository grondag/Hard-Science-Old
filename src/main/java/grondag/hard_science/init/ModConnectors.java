package grondag.hard_science.init;

import grondag.hard_science.simulator.transport.endpoint.Connector;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ModConnectors
{
    // dedicated power connectors
    public static final Connector power_low = new Connector("power_low");
    public static final Connector power_medium = new Connector("power_medium");
    public static final Connector power_high = new Connector("power_high");
    
    // dedicated vacuum tube connectors
    public static final Connector item_low = new Connector("item_low");
    public static final Connector item_medium = new Connector("item_medium");
    public static final Connector item_high = new Connector("item_high");
    
    // dedicated fluid pipes
    public static final Connector fluid_low = new Connector("fluid_low");
    public static final Connector fluid_medium = new Connector("fluid_medium");
    public static final Connector fluid_high = new Connector("fluid_high");
    
    // combined item/power connectors
    public static final Connector powered_item_low = new Connector("powered_item_low");
    public static final Connector powered_item_medium = new Connector("powered_item_medium");
    public static final Connector powered_item_high = new Connector("powered_item_high");
    
    // combined fluid/power connectors
    public static final Connector powered_fluid_low = new Connector("powered_fluid_low");
    public static final Connector powered_fluid_medium = new Connector("powered_fluid_medium");
    public static final Connector powered_fluid_high = new Connector("powered_fluid_high");
    
    @SubscribeEvent
    public static void registerPorts(RegistryEvent.Register<Connector> event) 
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
        
        event.getRegistry().register(powered_item_low);
        event.getRegistry().register(powered_item_medium);
        event.getRegistry().register(powered_item_high);
        
        event.getRegistry().register(powered_fluid_low);
        event.getRegistry().register(powered_fluid_medium);
        event.getRegistry().register(powered_fluid_high);
    }

}
