package grondag.adversity;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.logging.log4j.Logger;

@Mod(modid = Adversity.MODID, name = Adversity.MODNAME, version = Adversity.VERSION)
public class Adversity {
	public static final String MODID = "adversity";
	public static final String VERSION = "@VERSION@";
	public static final String MODNAME = "Adversity";

	public static Logger log;

	public static WorldType adversityWorld;

	public static CreativeTabs tabAdversity = new CreativeTabs("Adversity") {
		@Override
		@SideOnly(Side.CLIENT)
		public Item getTabIconItem() {
			//TODO need a real Icon
			return Items.baked_potato;
		}
	};

	@Instance
	public static Adversity instance = new Adversity();

	@SidedProxy(clientSide = "grondag.adversity.ClientProxy", serverSide = "grondag.adversity.ServerProxy")
	public static CommonProxy proxy;

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
	public void serverLoad(FMLServerStartingEvent event) {

	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		System.out.println("Config changed!");
		if (event.modID.equals(MODID)) {
			Config.load();
		}
	}

}