package grondag.adversity;

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
import grondag.adversity.init.ModItems;

@Mod(   modid = Adversity.MODID, 
        name = Adversity.MODNAME,
        version = Adversity.VERSION,
        acceptedMinecraftVersions = "[1.11.2]",
        dependencies = "after:theoneprobe; after:Waila")

public class Adversity 
{
	public static final String MODID = "adversity";
	public static final String MODNAME = "Adversity";
	public static final String VERSION = "0.0.1";
	public static WorldType adversityWorld;
	
	public static CreativeTabs tabAdversity = new CreativeTabs("Adversity") 
	{
		@Override
		@SideOnly(Side.CLIENT)
		public ItemStack getTabIconItem() 
		{
			return ModItems.basalt_cobble.getDefaultInstance();
		}
	};

	@Instance
	public static Adversity INSTANCE = new Adversity();

	@SidedProxy(clientSide = "grondag.adversity.ClientProxy", serverSide = "grondag.adversity.ServerProxy")
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