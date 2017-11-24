package grondag.hard_science;

import grondag.hard_science.init.ModBlocks;
import grondag.hard_science.init.ModEntities;
import grondag.hard_science.init.ModRecipes;
import grondag.hard_science.init.ModTileEntities;
import grondag.hard_science.library.varia.Base32Namer;
import grondag.hard_science.network.ModMessages;
import grondag.hard_science.simulator.Simulator;
import grondag.hard_science.virtualblock.ExcavationRenderTracker;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

public class CommonProxy 
{
    /**
     * Updated by client and server tick events to avoid having calls 
     * to System.currentTimeMillis() littered around the code
     * and calling it more frequently than needed.
     * 
     * MC Server does same thing, but is not exposed on client side, plus
     * we need current time in some client-side methods.
     */
    private static long currentTimeMillis;
    
    /**
     * Current system time, as of the most recent client or server tick.
     */
    public static long currentTimeMillis() { return currentTimeMillis; }
    
    public static void updateCurrentTime() { currentTimeMillis = System.currentTimeMillis(); }
    
	public void preInit(FMLPreInitializationEvent event) 
	{
		Log.setLog(event.getModLog());
		Configurator.recalcDerived();
	      
        ModMessages.registerNetworkMessages();

		ModTileEntities.preInit(event);
		ModEntities.preInit(event);
		
        ForgeChunkManager.setForcedChunkLoadingCallback(HardScience.INSTANCE, Simulator.INSTANCE);
	}

	public void init(FMLInitializationEvent event) 
	{
		ModRecipes.init(event);
        ModBlocks.init(event);
        
        Base32Namer.loadBadNames(I18n.translateToLocal("misc.offensive"));
	}

	public void postInit(FMLPostInitializationEvent event) 
	{
	    //NOOP
	}

    public void serverStarted(FMLServerStartedEvent event)
    {
        ExcavationRenderTracker.INSTANCE.clear();
        Simulator.INSTANCE.start();
    }

    public void serverStopping(FMLServerStoppingEvent event)
    {
        Simulator.INSTANCE.stop();
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