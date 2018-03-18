package grondag.hard_science.superblock.virtual;

import grondag.hard_science.gui.ModGuiHandler;
import grondag.hard_science.init.ModSuperModelBlocks;
import grondag.hard_science.movetogether.ISuperModelState;
import grondag.hard_science.superblock.items.SuperItemBlock;
import grondag.hard_science.superblock.placement.FilterMode;
import grondag.hard_science.superblock.placement.PlacementItem;
import grondag.hard_science.superblock.placement.PlacementItemFeature;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class VirtualItemBlock extends SuperItemBlock implements PlacementItem
{
    
    public static final int FEATURE_FLAGS = PlacementItem.BENUMSET_FEATURES.getFlagsForIncludedValues(
            PlacementItemFeature.FIXED_REGION,
            PlacementItemFeature.REGION_SIZE,
            PlacementItemFeature.GUI,
            PlacementItemFeature.REGION_ORIENTATION,
            PlacementItemFeature.SELECTION_RANGE,
            PlacementItemFeature.SPECIES_MODE,
            PlacementItemFeature.TARGET_MODE,
            PlacementItemFeature.BLOCK_ORIENTATION);
    
    public VirtualItemBlock(VirtualBlock block)
    {
        super(block);
        this.setMaxDamage(0);
        this.setMaxStackSize(1);
    }
    
    @Override
    public int featureFlags(ItemStack stack)
    {
        return FEATURE_FLAGS;
    }
    
    @Override
    public int guiOrdinal()
    {
        return ModGuiHandler.ModGui.SUPERMODEL_ITEM.ordinal();
    }
    
    @Override
    public boolean isVirtual(ItemStack stack)
    {
        return true;
    }
    
    @Override
    public FilterMode getFilterMode(ItemStack stack)
    {
        return FilterMode.FILL_REPLACEABLE;
    }
    
    /**
     * Gets the appropriate virtual block to place from a given item stack if it is
     * a virtual item stack. Returns block state for AIR otherwise.
     * May be different than the stack block because virtul in-world blocks are dependent 
     * rendering needs.
     */
    @Override
    public IBlockState getPlacementBlockStateFromStack(ItemStack stack)
    {
        Item item = stack.getItem();
        if(item instanceof VirtualItemBlock)
        {
            ISuperModelState modelState = PlacementItem.getStackModelState(stack);
            if(modelState == null) return null;

            VirtualBlock targetBlock = ModSuperModelBlocks.findAppropriateVirtualBlock(modelState);
            
            return targetBlock.getStateFromMeta(stack.getMetadata());
        }
        else
        {
            return Blocks.AIR.getDefaultState();
        }
    }
}
