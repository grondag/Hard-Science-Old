package grondag.hard_science;

import grondag.hard_science.gui.ModGuiHandler;
import grondag.hard_science.init.ModKeys;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{

    @Override
    public void init(FMLInitializationEvent event) 
    {
        super.init(event);
        ModKeys.init(event);
        NetworkRegistry.INSTANCE.registerGuiHandler(HardScience.INSTANCE, new ModGuiHandler());
    }

    @Override
    public boolean allowCollisionWithVirtualBlocks(World world)
    {
        // uses client proxy when running local so still have to check world for side
        if(world == null)
        {
            return FMLCommonHandler.instance().getEffectiveSide() ==  Side.CLIENT;
        }
        else
        {
            return world.isRemote;
        }
    }
}
