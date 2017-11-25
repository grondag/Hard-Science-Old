package grondag.hard_science.superblock.placement.spec;

import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.placement.PlacementItem;
import grondag.hard_science.superblock.placement.PlacementPosition;
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
    
    protected ModelState previewModelState()
    {
        return this.outputStack == null ? super.previewModelState() : PlacementItem.getStackModelState(this.outputStack);
    }
}