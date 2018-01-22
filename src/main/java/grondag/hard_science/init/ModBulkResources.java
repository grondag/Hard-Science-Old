package grondag.hard_science.init;

import grondag.hard_science.HardScience;
import grondag.hard_science.simulator.resource.BulkResource;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@Mod.EventBusSubscriber
@ObjectHolder(HardScience.MODID)
public class ModBulkResources
{
    
    public static final BulkResource empty = null;
    
    @SubscribeEvent
    public static void registerResources(RegistryEvent.Register<BulkResource> event) 
    {
        event.getRegistry().register(new BulkResource(ModRegistries.EMPTY_KEY));
    }
}
