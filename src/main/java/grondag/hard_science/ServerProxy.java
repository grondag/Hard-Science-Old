package grondag.hard_science;

import grondag.hard_science.virtualblock.VirtualBlockTracker;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
public class ServerProxy extends CommonProxy
{

	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		super.preInit(event);
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

	@Override
    public void serverStarted(FMLServerStartedEvent event)
	{
        super.serverStarted(event);
    }

	@Override
    public void serverStopping(FMLServerStoppingEvent event)
	{
        super.serverStopping(event);
    }

    @Override
    public void serverAboutToStart(FMLServerAboutToStartEvent event)
    {
        super.serverAboutToStart(event);
        VirtualBlockTracker.INSTANCE.clear();
    }
	
	
}
