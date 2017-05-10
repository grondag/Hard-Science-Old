package grondag.adversity.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class AdversityGuiHandler implements IGuiHandler
{
    public static final int GUI_NICE_BLOCK_ITEM = 0;
    public static final int GUI_SUPERMODEL_ITEM = 1;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) 
    {
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) 
    {
        switch(id)
        {
        case GUI_NICE_BLOCK_ITEM:
            return new NiceGuiScreen();
            
        case GUI_SUPERMODEL_ITEM:
            return new SuperGuiScreen();
            
        default:
            return null;
            
        }
    }

}
