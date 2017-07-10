package grondag.hard_science;

import grondag.hard_science.init.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldType;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(   modid = HardScience.MODID, 
        name = HardScience.MODNAME,
        version = HardScience.VERSION,
        acceptedMinecraftVersions = "[1.12]",
        dependencies = "after:theoneprobe; after:Waila")

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
		public ItemStack getTabIconItem() 
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
    }
    
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		proxy.preInit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
	}

   @EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
       proxy.serverStarted(event);
    }

   @EventHandler
   public void serverStopping(FMLServerStoppingEvent event) {
       proxy.serverStopping(event);
   }
   

}