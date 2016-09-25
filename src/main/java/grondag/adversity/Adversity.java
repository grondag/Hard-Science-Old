package grondag.adversity;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.WorldType;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.logging.log4j.Logger;

import grondag.adversity.config.Config;


@Mod(modid = Adversity.MODID, name = Adversity.MODNAME, version = Adversity.VERSION)
public class Adversity {
	public static final String MODID = "adversity";
	public static final String VERSION = "@VERSION@";
	public static final String MODNAME = "Adversity";

	public static Logger log;

	public static WorldType adversityWorld;
	
	public WorldSavedData thing;

	public static CreativeTabs tabAdversity = new CreativeTabs("Adversity") 
	{
		@Override
		@SideOnly(Side.CLIENT)
		public Item getTabIconItem() 
		{
			//TODO need a real Icon
			return Items.BAKED_POTATO;
		}
	};

	@Instance
	public static Adversity instance = new Adversity();

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
   
	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(MODID)) {
			Config.load();
		}
	}

}