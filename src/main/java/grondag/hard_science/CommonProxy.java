package grondag.hard_science;

import grondag.hard_science.init.ModBlocks;
import grondag.hard_science.init.ModComponents;
import grondag.hard_science.init.ModDevices;
import grondag.hard_science.init.ModEntities;
import grondag.hard_science.init.ModRecipes;
import grondag.hard_science.init.ModTileEntities;
import grondag.hard_science.library.varia.Base32Namer;
import grondag.hard_science.network.ModMessages;
import grondag.hard_science.simulator.Simulator;
import grondag.hard_science.superblock.virtual.ExcavationRenderTracker;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

public class CommonProxy 
{
	public void preInit(FMLPreInitializationEvent event) 
	{
		Log.setLog(event.getModLog());
		Configurator.recalcDerived();
	      
        ModMessages.registerNetworkMessages();

		ModTileEntities.preInit(event);
		ModEntities.preInit(event);
		ModComponents.preInit(event);
	    ModDevices.preInit(event);
		
        ForgeChunkManager.setForcedChunkLoadingCallback(HardScience.INSTANCE, Simulator.RAW_INSTANCE_DO_NOT_USE);
	}

	public void init(FMLInitializationEvent event) 
	{
		ModRecipes.init(event);
        ModBlocks.init(event);
        
        Base32Namer.loadBadNames(I18n.translateToLocal("misc.offensive"));
	}

	public void postInit(FMLPostInitializationEvent event) 
	{
//    	    new ItemStorageTest().test();
//    	    new ItemResourceTest().test();
//    	    new SystemTests().test();
//        Log.info("In-Game System Tests Complete");
	}

    public void serverStarting(FMLServerStartingEvent event)
    {
        ExcavationRenderTracker.INSTANCE.clear();
        Simulator.loadSimulatorIfNotLoaded();
    }

    public void serverStopping(FMLServerStoppingEvent event)
    {
        Simulator.instance().stop();
    }
    
    public void serverAboutToStart(FMLServerAboutToStartEvent event)
    {
      
    }

    /**
     * Can ray trace collide with virtual blocks?
     * Used for block placement, one probe info, etc. 
     * Always false on server 
     */
    public boolean allowCollisionWithVirtualBlocks(World world)
    {
        return false;
    }
    
}