package grondag.adversity.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class AdversityGuiHandler implements IGuiHandler
{
    private static int guiIndex = 0;
    
    public static final int GUI_NICE_BLOCK_ITEM = guiIndex++;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) 
    {
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) 
    {
        return new NiceGuiScreen();
    }

}
