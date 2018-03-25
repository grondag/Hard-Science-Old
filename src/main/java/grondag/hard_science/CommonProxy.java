package grondag.hard_science;

import grondag.hard_science.init.ModBlocks;
import grondag.hard_science.init.ModComponents;
import grondag.hard_science.init.ModDevices;
import grondag.hard_science.init.ModEntities;
import grondag.hard_science.init.ModRecipes;
import grondag.hard_science.init.ModTileEntities;
import grondag.hard_science.superblock.virtual.ExcavationRenderTracker;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

public class CommonProxy 
{
	public void preInit(FMLPreInitializationEvent event) 
	{
		HardScience.setLog(event.getModLog());
		Configurator.recalcDerived();

		ModTileEntities.preInit(event);
		ModEntities.preInit(event);
		ModComponents.preInit(event);
	    ModDevices.preInit(event);
	}

	public void init(FMLInitializationEvent event) 
	{
		ModRecipes.init(event);
        ModBlocks.init(event);
	}

	public void postInit(FMLPostInitializationEvent event) 
	{
	    
	}

    public void serverStarting(FMLServerStartingEvent event)
    {
        ExcavationRenderTracker.INSTANCE.clear();
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