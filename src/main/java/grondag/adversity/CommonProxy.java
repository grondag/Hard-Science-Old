package grondag.adversity;


import grondag.adversity.feature.volcano.Volcano;
import grondag.adversity.niceblock.newmodel.NiceBlockRegistrar;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {

	public void preInit(FMLPreInitializationEvent event) {

		Adversity.log = event.getModLog();
		Config.init(event.getSuggestedConfigurationFile());
		
		Volcano.preInit(event);
		NiceBlockRegistrar.preInit(event);
		// TODO: reenable Waila when it is ported
        if (Loader.isModLoaded("Waila")){
       //     WailaDataProvider.register();
        }
	}

	public void init(FMLInitializationEvent event) {
		Volcano.init(event);
		NiceBlockRegistrar.init(event);
	}

	public void postInit(FMLPostInitializationEvent event) {
		Volcano.postInit(event);
	}

	public void serverLoad(FMLServerStartingEvent event) {

	}

}