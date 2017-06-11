package grondag.adversity;


import grondag.adversity.external.WailaDataProvider;
import grondag.adversity.gui.AdversityGuiHandler;
import grondag.adversity.init.ModBlocks;
import grondag.adversity.init.ModEntities;
import grondag.adversity.init.ModItems;
import grondag.adversity.init.ModRecipes;
import grondag.adversity.init.ModTileEntities;
import grondag.adversity.network.AdversityMessages;
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

public class CommonProxy 
{
	public void preInit(FMLPreInitializationEvent event) 
	{
		Log.setLog(event.getModLog());
		
        AdversityMessages.registerNetworkMessages();

        ModBlocks.preInit(event);
		ModItems.preInit(event);
		ModTileEntities.preInit(event);
		ModEntities.preInit(event);
		
        if (Loader.isModLoaded("Waila"))
        {
            WailaDataProvider.register();
        }
        
        ForgeChunkManager.setForcedChunkLoadingCallback(Adversity.INSTANCE, Simulator.INSTANCE);
        MinecraftForge.EVENT_BUS.register(CommonEventHandler.INSTANCE);
	}

	public void init(FMLInitializationEvent event) 
	{
	    Configurator.recalcDerived();
	    NetworkRegistry.INSTANCE.registerGuiHandler(Adversity.INSTANCE, new AdversityGuiHandler());
		ModRecipes.init(event);
		ModBlocks.init(event);
	}

	public void postInit(FMLPostInitializationEvent event) 
	{
	    //NOOP
	}

    public void serverStarted(FMLServerStartedEvent event)
    {
        Simulator.INSTANCE.start();
    }

    public void serverStopping(FMLServerStoppingEvent event)
    {
        Simulator.INSTANCE.stop();
    }


}