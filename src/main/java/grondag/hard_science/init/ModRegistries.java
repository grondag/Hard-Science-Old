package grondag.hard_science.init;

import grondag.hard_science.HardScience;
import grondag.hard_science.simulator.resource.BulkResource;
import grondag.hard_science.simulator.transport.endpoint.Port;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@Mod.EventBusSubscriber
@ObjectHolder(HardScience.MODID)
public class ModRegistries
{
//    public static ForgeRegistry<Connection> connectionRegistry;
//    public static ForgeRegistry<Connector> connectorRegistry;
    public static ForgeRegistry<Port> portRegistry;
    public static ForgeRegistry<BulkResource> bulkResourceRegistry;
    
    @SubscribeEvent
    public static void newRegistries(RegistryEvent.NewRegistry event) 
    {
//        connectionRegistry = (ForgeRegistry<Connection>) new RegistryBuilder<Connection>()
//                .setName(new ResourceLocation(HardScience.MODID, "connections"))
//                .setIDRange(0, Short.MAX_VALUE)
//                .setType(Connection.class)
//                .create();
        
//        connectorRegistry = (ForgeRegistry<Connector>) new RegistryBuilder<Connector>()
//                .setName(new ResourceLocation(HardScience.MODID, "connectors"))
//                .setIDRange(0, Short.MAX_VALUE)
//                .setType(Connector.class)
//                .create();
        
        portRegistry = (ForgeRegistry<Port>) new RegistryBuilder<Port>()
                .setName(new ResourceLocation(HardScience.MODID, "ports"))
                .setIDRange(0, Short.MAX_VALUE)
                .setType(Port.class)
                .create();
        
        bulkResourceRegistry = (ForgeRegistry<BulkResource>) new RegistryBuilder<BulkResource>()
                .setName(new ResourceLocation(HardScience.MODID, "bulk_resources"))
                .setIDRange(0, Short.MAX_VALUE)
                .setType(BulkResource.class)
                .setDefaultKey(new ResourceLocation(ModBulkResources.EMPTY_BULK_RESOURCE))
                .create();
    }
}
