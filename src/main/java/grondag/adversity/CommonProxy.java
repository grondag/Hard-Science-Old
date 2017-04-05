package grondag.adversity;


import grondag.adversity.config.Config;
import grondag.adversity.feature.volcano.Volcano;
import grondag.adversity.gui.AdversityGuiHandler;
import grondag.adversity.network.AdversityMessages;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.simulator.Simulator;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class CommonProxy {

	public void preInit(FMLPreInitializationEvent event) {

		Adversity.log = event.getModLog();
		Config.init(event.getSuggestedConfigurationFile());
		
        AdversityMessages.registerNetworkMessages();
		
		Volcano.preInit(event);
        NiceBlockRegistrar.preInit(event);
		
		// TODO: reenable Waila when it is ported
        if (Loader.isModLoaded("Waila")){
       //     WailaDataProvider.register();
        }
        
        ForgeChunkManager.setForcedChunkLoadingCallback(Adversity.instance, Simulator.instance);
        MinecraftForge.EVENT_BUS.register(Simulator.instance);
        MinecraftForge.EVENT_BUS.register(CommonEventHandler.INSTANCE);
        
	}

	public void init(FMLInitializationEvent event) {
      //  Simulator.instance.init(event);
	    NetworkRegistry.INSTANCE.registerGuiHandler(Adversity.instance, new AdversityGuiHandler());
		Volcano.init(event);
        NiceBlockRegistrar.init(event);
	}

	public void postInit(FMLPostInitializationEvent event) 
	{
	    //NOOP
	}

    public void serverStarted(FMLServerStartedEvent event) {
        Simulator.instance.start();
    }

    public void serverStopping(FMLServerStoppingEvent event) {
        Simulator.instance.stop();
    }


}