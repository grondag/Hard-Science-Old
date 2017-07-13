package grondag.hard_science;


import grondag.hard_science.external.WailaDataProvider;
import grondag.hard_science.init.ModBlocks;
import grondag.hard_science.init.ModEntities;
import grondag.hard_science.init.ModRecipes;
import grondag.hard_science.init.ModTileEntities;
import grondag.hard_science.network.ModMessages;
import grondag.hard_science.simulator.Simulator;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

public class CommonProxy 
{
	public void preInit(FMLPreInitializationEvent event) 
	{
		Log.setLog(event.getModLog());
		
        ModMessages.registerNetworkMessages();

		ModTileEntities.preInit(event);
		ModEntities.preInit(event);
		
        if (Loader.isModLoaded("Waila"))
        {
            WailaDataProvider.register();
        }
        
        ForgeChunkManager.setForcedChunkLoadingCallback(HardScience.INSTANCE, Simulator.INSTANCE);
	}

	public void init(FMLInitializationEvent event) 
	{
	    Configurator.recalcDerived();
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