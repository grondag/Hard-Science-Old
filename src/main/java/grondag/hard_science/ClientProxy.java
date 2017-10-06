package grondag.hard_science;

import grondag.hard_science.gui.ModGuiHandler;
import grondag.hard_science.init.ModKeys;
import grondag.hard_science.init.ModModels;
import grondag.hard_science.superblock.block.SuperTileEntity;
import grondag.hard_science.superblock.color.BlockColorMapProvider;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    
	private static boolean isVirtualBlockRenderingEnabled = false;

    @Override
	public void preInit(FMLPreInitializationEvent event) 
	{
		super.preInit(event);
		ModModels.preInit(event);
		
		if(Configurator.RENDER.debugOutputColorAtlas)
	    {
		    BlockColorMapProvider.writeColorAtlas(event.getModConfigurationDirectory());
		}
	}
	
	@Override
	public void init(FMLInitializationEvent event) 
	{
		super.init(event);
		ModKeys.init(event);
	    ModModels.init(event);
		NetworkRegistry.INSTANCE.registerGuiHandler(HardScience.INSTANCE, new ModGuiHandler());
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) 
	{
		super.postInit(event);
		SuperTileEntity.updateRenderDistance();
	}
	
	@Override
    public boolean allowCollisionWithVirtualBlocks(World world)
    {
        // uses client proxy when running local so still have to check world for side
	    if(world == null)
	    {
	        return isVirtualBlockRenderingEnabled && FMLCommonHandler.instance().getEffectiveSide() ==  Side.CLIENT;
	    }
	    else
	    {
	        return world.isRemote && isVirtualBlockRenderingEnabled;
	    }
    }

    public static void setVirtualBlockRenderingEnabled(boolean isEnabled)
    {
        isVirtualBlockRenderingEnabled = isEnabled;
    }

    public static boolean isVirtualBlockRenderingEnabled()
    {
        return isVirtualBlockRenderingEnabled;
    }
}
