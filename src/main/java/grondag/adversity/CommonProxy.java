package grondag.adversity;


import grondag.adversity.feature.volcano.Volcano;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.NiceBlockRegistrar2;
import grondag.adversity.simulator.Simulator;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

public class CommonProxy {

	public void preInit(FMLPreInitializationEvent event) {

		Adversity.log = event.getModLog();
		Config.init(event.getSuggestedConfigurationFile());
		
		Volcano.preInit(event);
		NiceBlockRegistrar.preInit(event);
        NiceBlockRegistrar2.preInit(event);
		
		// TODO: reenable Waila when it is ported
        if (Loader.isModLoaded("Waila")){
       //     WailaDataProvider.register();
        }
        
        ForgeChunkManager.setForcedChunkLoadingCallback(Adversity.MODID, Simulator.instance);
        MinecraftForge.EVENT_BUS.register(Simulator.instance);
	}

	public void init(FMLInitializationEvent event) {
      //  Simulator.instance.init(event);
		Volcano.init(event);
		NiceBlockRegistrar.init(event);
        NiceBlockRegistrar2.init(event);
	}

	public void postInit(FMLPostInitializationEvent event) {
		Volcano.postInit(event);

	}


    public void serverStarted(FMLServerStartedEvent event) {
        Simulator.instance.start();
    }

    public void serverStopping(FMLServerStoppingEvent event) {
        Simulator.instance.stop();
    }


}