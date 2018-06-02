package grondag.hard_science.superblock.placement.spec;

import grondag.exotic_matter.block.SuperBlockStackHelper;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.model.state.StateFormat;
import grondag.exotic_matter.placement.BlockOrientationHandler;
import grondag.exotic_matter.placement.IPlacementSpec;
import grondag.exotic_matter.placement.IPlacementItem;
import grondag.exotic_matter.placement.PlacementPosition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

/**
 * Knows how to get the appropriate placement builder for a given stack.
 */
public class PlacementSpecHelper
{
    /**
     * Instantiates the appropriate placement builder object if the object in the stack is 
     * placed in world given current player/world context.<br><br>
     * 
     * Assumes determination that block should be placed is already made.
     * (Not clicking in mid air without floating selection, for example.)
     */
    public static IPlacementSpec placementBuilder(EntityPlayer player, PlacementPosition pPos, ItemStack stack)
    {
        ItemStack placedStack = stack.copy();
        
        BlockOrientationHandler.configureStackForPlacement(placedStack, player, pPos);
        
        IPlacementItem item = IPlacementItem.getPlacementItem(stack);
        
        if(item == null) return null;
        
        // non-virtual items should always be single block placements
        if(!item.isVirtual(placedStack))
        {
            return new SingleBlockPlacementSpec(placedStack, player, pPos);
        }
        
        switch(item.getTargetMode(placedStack))
        {
            // meaning of following three types depends... 
            // if we are placing individual blocks in a multiblock (cubic) region
            // then the geometry of the region is defined by the builder.
            // if we are placing a CSG multiblock, then the geometry
            // of the placement is part of the GSC shape itself, as configured in model state.
            case COMPLETE_REGION:
            case FILL_REGION:
            case HOLLOW_REGION:
                ISuperModelState modelState = SuperBlockStackHelper.getStackModelState(placedStack);
                if(modelState != null && modelState.getShape().meshFactory().stateFormat == StateFormat.MULTIBLOCK)
                {
                    return new CSGPlacementSpec(placedStack, player, pPos);
                }
                else
                {
                    if(!item.isFixedRegionEnabled(placedStack) && item.getRegionSize(placedStack, false).equals(new BlockPos(1, 1, 1)))
                    {
                        return new SingleBlockPlacementSpec(placedStack, player, pPos);
                    }
                    else
                    {
                        return new CuboidPlacementSpec(placedStack, player, pPos);
                    }
                }
                
            case MATCH_CLICKED_BLOCK:
                return new PredicatePlacementSpec(placedStack, player, pPos);
                
            case ON_CLICKED_SURFACE:
                return new SurfacePlacementSpec(placedStack, player, pPos);
                
            case ON_CLICKED_FACE:
            default:
                return new SingleBlockPlacementSpec(placedStack, player, pPos);
        }
    }
}
