package grondag.adversity.gui;

import grondag.adversity.niceblock.base.NiceItemBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class AdversityGuiHandler implements IGuiHandler
{

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) 
    {
        if (id == 0) 
        {
            EnumHand hand = EnumHand.values()[x];
            ItemStack held = player.getHeldItem(hand);
            if (held != null && held.getItem() instanceof NiceItemBlock) 
            {
                    return new NiceBlockContainer(player.inventory, hand);
            }
            
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) 
    {
        if (id == 0) 
        {
            EnumHand hand = EnumHand.values()[x];
            ItemStack held = player.getHeldItem(hand);
            if (held != null && held.getItem() instanceof NiceItemBlock) 
            {
                    return new NiceBlockGui(player.inventory, hand);
            }
            
        }
        return null;
    }

}
