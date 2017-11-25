package grondag.hard_science.superblock.placement;

import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.placement.spec.CSGPlacementSpec;
import grondag.hard_science.superblock.placement.spec.CuboidPlacementSpec;
import grondag.hard_science.superblock.placement.spec.PredicatePlacementSpec;
import grondag.hard_science.superblock.placement.spec.SinglePlacementSpec;
import grondag.hard_science.superblock.placement.spec.SurfacePlacementSpec;
import grondag.hard_science.superblock.model.state.StateFormat;
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
    public static IPlacementSpecBuilder placementBuilder(EntityPlayer player, PlacementPosition pPos, ItemStack stack)
    {
        ItemStack placedStack = stack.copy();
        
        BlockOrientationHandler.configureStackForPlacement(placedStack, player, pPos);
        
        PlacementItem item = PlacementItem.getPlacementItem(stack);
        
        if(item == null) return null;
        
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
                ModelState modelState = PlacementItem.getStackModelState(placedStack);
                if(modelState != null && modelState.getShape().meshFactory().stateFormat == StateFormat.MULTIBLOCK)
                {
                    return CSGPlacementSpec.builder(placedStack, player, pPos);
                }
                else
                {
                    if(!item.isFixedRegionEnabled(placedStack) && item.getRegionSize(placedStack, false).equals(new BlockPos(1, 1, 1)))
                    {
                        return SinglePlacementSpec.builder(placedStack, player, pPos);
                    }
                    else
                    {
                        return CuboidPlacementSpec.builder(placedStack, player, pPos);
                    }
                }
                
            case MATCH_CLICKED_BLOCK:
                return PredicatePlacementSpec.builder(placedStack, player, pPos);
                
            case ON_CLICKED_SURFACE:
                return SurfacePlacementSpec.builder(placedStack, player, pPos);
                
            case ON_CLICKED_FACE:
            default:
                return SinglePlacementSpec.builder(placedStack, player, pPos);
        }
    }
}
