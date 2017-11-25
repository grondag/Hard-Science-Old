package grondag.hard_science.superblock.placement.spec;

import grondag.hard_science.superblock.placement.PlacementPosition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public abstract class SingleStackBuilder extends PlacementSpecBuilder
{
    protected SingleStackBuilder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
    {
        super(placedStack, player, pPos);
    }
}