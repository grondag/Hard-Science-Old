package grondag.hard_science.superblock.placement;

import grondag.exotic_matter.model.StateFormat;
import grondag.hard_science.movetogether.ISuperModelState;
import grondag.hard_science.superblock.placement.spec.CSGBuilder;
import grondag.hard_science.superblock.placement.spec.CuboidBuilder;
import grondag.hard_science.superblock.placement.spec.IPlacementSpecBuilder;
import grondag.hard_science.superblock.placement.spec.PredicateBuilder;
import grondag.hard_science.superblock.placement.spec.SingleBuilder;
import grondag.hard_science.superblock.placement.spec.SurfaceBuilder;
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
        
        // non-virtual items should always be single block placements
        if(!item.isVirtual(placedStack))
        {
            return new SingleBuilder(placedStack, player, pPos);
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
                ISuperModelState modelState = PlacementItem.getStackModelState(placedStack);
                if(modelState != null && modelState.getShape().meshFactory().stateFormat == StateFormat.MULTIBLOCK)
                {
                    return new CSGBuilder(placedStack, player, pPos);
                }
                else
                {
                    if(!item.isFixedRegionEnabled(placedStack) && item.getRegionSize(placedStack, false).equals(new BlockPos(1, 1, 1)))
                    {
                        return new SingleBuilder(placedStack, player, pPos);
                    }
                    else
                    {
                        return new CuboidBuilder(placedStack, player, pPos);
                    }
                }
                
            case MATCH_CLICKED_BLOCK:
                return new PredicateBuilder(placedStack, player, pPos);
                
            case ON_CLICKED_SURFACE:
                return new SurfaceBuilder(placedStack, player, pPos);
                
            case ON_CLICKED_FACE:
            default:
                return new SingleBuilder(placedStack, player, pPos);
        }
    }
}
