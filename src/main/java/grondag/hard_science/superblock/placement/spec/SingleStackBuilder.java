package grondag.hard_science.superblock.placement.spec;

import grondag.exotic_matter.model.ISuperModelState;
import grondag.hard_science.superblock.block.PlacementPosition;
import grondag.hard_science.superblock.blockmovetest.PlacementItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public abstract class SingleStackBuilder extends PlacementSpecBuilder
{
    /**
     * Stack that should be placed in the world.
     * Populated during {@link #doValidate()}
     * Default is AIR (for excavations) if not set.
     */
    protected ItemStack outputStack = Items.AIR.getDefaultInstance();
    
    protected SingleStackBuilder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
    {
        super(placedStack, player, pPos);
    }
    
    @Override
    protected ISuperModelState previewModelState()
    {
        return this.outputStack == null ? super.previewModelState() : PlacementItem.getStackModelState(this.outputStack);
    }
}