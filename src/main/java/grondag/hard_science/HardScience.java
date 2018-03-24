package grondag.hard_science;

import javax.annotation.Nonnull;

import grondag.exotic_matter.network.PacketHandler;
import grondag.exotic_matter.simulator.Simulator;
import grondag.exotic_matter.simulator.domain.Domain;
import grondag.exotic_matter.simulator.domain.DomainManager;
import grondag.exotic_matter.simulator.domain.DomainUser;
import grondag.hard_science.init.ModItems;
import grondag.hard_science.network.client_to_server.PacketConfigurePlacementItem;
import grondag.hard_science.network.client_to_server.PacketDestroyVirtualBlock;
import grondag.hard_science.network.client_to_server.PacketMachineInteraction;
import grondag.hard_science.network.client_to_server.PacketMachineStatusAddListener;
import grondag.hard_science.network.client_to_server.PacketOpenContainerStorageInteraction;
import grondag.hard_science.network.client_to_server.PacketSimpleAction;
import grondag.hard_science.network.client_to_server.PacketUpdateModifierKeys;
import grondag.hard_science.network.server_to_client.PacketExcavationRenderRefresh;
import grondag.hard_science.network.server_to_client.PacketExcavationRenderUpdate;
import grondag.hard_science.network.server_to_client.PacketMachineStatusUpdateListener;
import grondag.hard_science.network.server_to_client.PacketOpenContainerItemStorageRefresh;
import grondag.hard_science.simulator.device.DeviceManager;
import grondag.hard_science.simulator.domain.ProcessManager;
import grondag.hard_science.simulator.fobs.TransientTaskContainer;
import grondag.hard_science.simulator.jobs.JobManager;
import grondag.hard_science.simulator.storage.FluidStorageManager;
import grondag.hard_science.simulator.storage.ItemStorageManager;
import grondag.hard_science.simulator.storage.PowerStorageManager;
import grondag.hard_science.superblock.placement.BuildCapability;
import grondag.hard_science.superblock.placement.BuildManager;
import grondag.hard_science.volcano.lava.simulator.LavaSimulator;
import grondag.hard_science.volcano.lava.simulator.VolcanoManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldType;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(   modid = HardScience.MODID, 
        name = HardScience.MODNAME,
        version = HardScience.VERSION,
        acceptedMinecraftVersions = "[1.12]",
        dependencies = "after:theoneprobe")

public class HardScience 
{
	public static final String MODID = "hard_science";
	public static final String MODNAME = "Hard Science";
	public static final String VERSION = "0.0.1";
	public static WorldType adversityWorld;
	
	public static CreativeTabs tabMod = new CreativeTabs("Hard Science") 
	{
		@Override
		@SideOnly(Side.CLIENT)
		public @Nonnull ItemStack getTabIconItem() 
		{
			return ModItems.basalt_cobble.getDefaultInstance();
		}
	};

	@Instance
	public static HardScience INSTANCE = new HardScience();

	@SidedProxy(clientSide = "grondag.hard_science.ClientProxy", serverSide = "grondag.hard_science.ServerProxy")
	public static CommonProxy proxy;

    static
    {
        FluidRegistry.enableUniversalBucket();
        
        //TODO move these where they should go
        Simulator.register(DeviceManager.class);
        Simulator.register(DomainManager.class);
        if(Configurator.VOLCANO.enableVolcano)
        {
            Simulator.register(VolcanoManager.class);
            Simulator.register(LavaSimulator.class);
        }
        
        DomainUser.registerCapability(BuildCapability.class);
        
        Domain.registerCapability(ItemStorageManager.class);
        Domain.registerCapability(FluidStorageManager.class);
        Domain.registerCapability(PowerStorageManager.class);
        Domain.registerCapability(BuildManager.class);
        Domain.registerCapability(JobManager.class);
        Domain.registerCapability(TransientTaskContainer.class);
        Domain.registerCapability(ProcessManager.class);
        
        // Packets handled on Server side, sent from Client
        PacketHandler.registerMessage(PacketConfigurePlacementItem.class, PacketConfigurePlacementItem.class, Side.SERVER);
        PacketHandler.registerMessage(PacketUpdateModifierKeys.class, PacketUpdateModifierKeys.class, Side.SERVER);
        PacketHandler.registerMessage(PacketDestroyVirtualBlock.class, PacketDestroyVirtualBlock.class, Side.SERVER);
        PacketHandler.registerMessage(PacketOpenContainerStorageInteraction.class, PacketOpenContainerStorageInteraction.class, Side.SERVER);
        PacketHandler.registerMessage(PacketMachineStatusAddListener.class, PacketMachineStatusAddListener.class, Side.SERVER);
        PacketHandler.registerMessage(PacketMachineInteraction.class, PacketMachineInteraction.class, Side.SERVER);
        PacketHandler.registerMessage(PacketSimpleAction.class, PacketSimpleAction.class, Side.SERVER);
        
        // Packets handled on Client side, sent from Server        
        PacketHandler.registerMessage(PacketOpenContainerItemStorageRefresh.class, PacketOpenContainerItemStorageRefresh.class, Side.CLIENT);
        PacketHandler.registerMessage(PacketMachineStatusUpdateListener.class, PacketMachineStatusUpdateListener.class, Side.CLIENT);
        PacketHandler.registerMessage(PacketExcavationRenderUpdate.class, PacketExcavationRenderUpdate.class, Side.CLIENT);
        PacketHandler.registerMessage(PacketExcavationRenderRefresh.class, PacketExcavationRenderRefresh.class, Side.CLIENT);
        
    }
    
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		proxy.preInit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		proxy.init(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		proxy.postInit(event);
	}
	
	
   @EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event) 
   {
       proxy.serverAboutToStart(event);
    }
   
	@EventHandler
    public void serverStarting(FMLServerStartingEvent event) 
	{
       proxy.serverStarting(event);
    }

   @EventHandler
   public void serverStopping(FMLServerStoppingEvent event)
   {
       proxy.serverStopping(event);
   }
   
   /**
    * Puts mod ID and . in front of whatever is passed in
    */
   public static String prefixName(String name)
   {
       return String.format("%s.%s", MODID, name.toLowerCase());
   }
   
   public static String prefixResource(String name)
   {
       return String.format("%s:%s", MODID, name.toLowerCase());
   }
   
   public static ResourceLocation resource(String name)
   {
       return new ResourceLocation(prefixResource(name));
   }
}