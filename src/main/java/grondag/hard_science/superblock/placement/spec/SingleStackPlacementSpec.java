package grondag.hard_science.superblock.placement.spec;

import grondag.exotic_matter.block.SuperBlockStackHelper;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.placement.PlacementPosition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public abstract class SingleStackPlacementSpec extends AbstractPlacementSpec
{
    /**
     * Stack that should be placed in the world.
     * Populated during {@link #doValidate()}
     * Default is AIR (for excavations) if not set.
     */
    protected ItemStack outputStack = Items.AIR.getDefaultInstance();
    
    protected SingleStackPlacementSpec(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
    {
        super(placedStack, player, pPos);
    }
    
    @Override
    protected ISuperModelState previewModelState()
    {
        return this.outputStack == null ? super.previewModelState() : SuperBlockStackHelper.getStackModelState(this.outputStack);
    }
}