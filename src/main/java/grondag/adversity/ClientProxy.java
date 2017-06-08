package grondag.adversity;

import grondag.adversity.superblock.color.NiceHues;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) 
	{
		super.preInit(event);
		
		if(Output.DEBUG_MODE)
		{
		    NiceHues.INSTANCE.writeColorAtlas(event.getModConfigurationDirectory());
		}
	}

	@Override
	public void init(FMLInitializationEvent event) 
	{
		super.init(event);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) 
	{
		super.postInit(event);
	}
}
