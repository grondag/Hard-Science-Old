package grondag.hard_science.superblock.placement.spec;

import java.util.HashSet;

import grondag.exotic_matter.world.CubicBlockRegion;
import grondag.hard_science.Configurator;
import grondag.hard_science.superblock.placement.PlacementItem;
import grondag.hard_science.superblock.placement.PlacementPosition;
import grondag.hard_science.superblock.placement.RegionOrientation;
import grondag.hard_science.superblock.placement.TargetMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

abstract class VolumetricBuilder extends SingleStackBuilder
{
    protected final boolean isHollow;
    protected final BlockPos offsetPos;
    /**
     * Reference this instead of {@link PlacementItem#isFixedRegionEnabled(ItemStack)}
     * because stack property is typically reset after builder is instantiated.
     */
    protected final boolean isFixedRegion;
    protected final boolean isAdjustmentEnabled;

    protected VolumetricBuilder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
    {
        super(placedStack, player, pPos);
        this.isHollow = this.selectionMode == TargetMode.HOLLOW_REGION;
        this.offsetPos = this.placementItem.getRegionSize(placedStack, true);
        this.isFixedRegion = this.placementItem.isFixedRegionEnabled(placedStack);
        this.isAdjustmentEnabled = !this.isFixedRegion 
                && !this.isExcavation 
                && !this.isSelectionInProgress
                && this.placementItem.getRegionOrientation(placedStack) == RegionOrientation.AUTOMATIC;
    }

    /**
     * Clears the exclusion list in the given block region and
     * adds obstacles checked within the region to the exclusion list. 
     * Does not fully validate region - is intended for preview use only.
     * <p>
     * Stops checking after finding 16 obstacles.  
     * Checks are only  performed if the selection mode is <code>COMPLETE_REGION</code>
     * because otherwise the placement cannot be prevented by obstructions.
     * 
     * @param region
     */
    protected void excludeObstaclesInRegion(CubicBlockRegion region)
    {
        region.clearExclusions();

        if(this.selectionMode != TargetMode.COMPLETE_REGION) return;

        HashSet<BlockPos> set = new HashSet<BlockPos>();

        World world = this.player.world;

        int checkCount = 0, foundCount = 0;

        if(this.isExcavation)
        {
            for(BlockPos.MutableBlockPos pos : region.positions())
            {
                if(world.isOutsideBuildHeight(pos))
                {
                    set.add(pos.toImmutable());
                    if(foundCount++ == 16) break;
                }

                IBlockState blockState = world.getBlockState(pos);
                if(blockState.getBlock().isAir(blockState, world, pos) 
                        || !this.effectiveFilterMode.shouldAffectBlock(blockState, world, pos, this.placedStack(), this.isVirtual))
                {
                    set.add(pos.toImmutable());
                    if(foundCount++ == 16) break;
                }
                if(checkCount++ >= Configurator.BLOCKS.maxPlacementCheckCount) break;
            }
        }
        else
        {
            for(BlockPos.MutableBlockPos pos : region.includedPositions())
            {
                if(world.isOutsideBuildHeight(pos))
                {
                    set.add(pos.toImmutable());
                    if(foundCount++ == 16) break;
                }

                IBlockState blockState = world.getBlockState(pos);
                if(!this.effectiveFilterMode.shouldAffectBlock(blockState, world, pos, this.placedStack(), this.isVirtual))
                {
                    set.add(pos.toImmutable());
                    if(foundCount++ == 16) break;
                }
                if(checkCount++ >= Configurator.BLOCKS.maxPlacementCheckCount) break;
            }
        }
        region.exclude(set);
    }

    /**
     * Returns true if the region has no obstacles or
     * obstacles do not matter. Must call AFTER {@link #excludeObstaclesInRegion(CubicBlockRegion)}
     * or result will be meaningless. 
     */
    protected boolean canPlaceRegion(CubicBlockRegion region)
    {
        return region.exclusions().isEmpty() || this.selectionMode != TargetMode.COMPLETE_REGION;
    }
}