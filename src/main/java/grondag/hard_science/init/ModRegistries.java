package grondag.hard_science.init;

import grondag.hard_science.HardScience;
import grondag.hard_science.simulator.transport.endpoint.PortLayout;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@Mod.EventBusSubscriber
public class ModRegistries
{
//    public static ForgeRegistry<BulkResource> bulkResourceRegistry;

    public static ForgeRegistry<PortLayout> portLayoutRegistry;

    public static final String EMPTY_KEY = HardScience.prefixResource("empty");

    @SubscribeEvent
    public static void newRegistries(RegistryEvent.NewRegistry event) 
    {
//        bulkResourceRegistry = (ForgeRegistry<BulkResource>) new RegistryBuilder<BulkResource>()
//                .setName(new ResourceLocation(HardScience.MODID, "bulk_resources"))
//                .setIDRange(0, Short.MAX_VALUE)
//                .setType(BulkResource.class)
//                .setDefaultKey(new ResourceLocation(EMPTY_KEY))
//                .create();
        
        portLayoutRegistry = (ForgeRegistry<PortLayout>) new RegistryBuilder<PortLayout>()
                .setName(new ResourceLocation(HardScience.MODID, "port_layouts"))
                .setIDRange(0, Short.MAX_VALUE)
                .setType(PortLayout.class)
                .setDefaultKey(new ResourceLocation(EMPTY_KEY))
                .create();
    }
}
