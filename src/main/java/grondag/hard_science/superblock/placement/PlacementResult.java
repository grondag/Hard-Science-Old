package grondag.hard_science.superblock.placement;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class PlacementResult
{
    private final List<Pair<BlockPos, ItemStack>> placements;
    private final Set<BlockPos> exclusions;
    private final AxisAlignedBB placementAABB;
    private final BlockPos blockPos;
    private final PlacementEvent event;
    
    public static final PlacementResult EMPTY_RESULT_CONTINUE = new PlacementResult(null, null, null, null, PlacementEvent.NO_OPERATION_CONTINUE);
    public static final PlacementResult EMPTY_RESULT_STOP = new PlacementResult(null, null, null, null, PlacementEvent.NO_OPERATION_CONTINUE);
    
    public PlacementResult(
            @Nullable AxisAlignedBB placementAABB, 
            @Nullable List<Pair<BlockPos, ItemStack>> placements, 
            @Nullable Set<BlockPos> exclusions, 
            @Nullable BlockPos blockPos,
            @Nonnull  PlacementEvent event)
    {
        this.placementAABB = placementAABB;
        this.placements = placements;
        this.exclusions = exclusions;
        this.blockPos = blockPos;
        this.event = event;
    }
    
    /**
     * Identifies state changes and subsequent event processing that should occur with this result.
     */
    public PlacementEvent event()
    {
        return this.event;
    }
    
    public PlacementResult withEvent(PlacementEvent event)
    {
        return new PlacementResult(this.placementAABB, this.placements, this.exclusions, this.blockPos, event);
    }
    
    /**
     * If the event associated with this result requires a BlockPos for state changes,
     * the BlockPos that should be used. Null otherwise.
     */
    @Nullable
    public BlockPos blockPos()
    {
        return this.blockPos;
    }
    
    /**
     * Blocks that should be placed.
     */
    @Nonnull
    public List<Pair<BlockPos, ItemStack>> placements()
    {
        return this.placements == null
                ? Collections.emptyList()
                : this.placements;
    }
    
    /**
     * Locations of blocks that are obstacles to the placement action.
     */
    @Nonnull
    public Set<BlockPos> exclusions()
    {
        return this.exclusions == null
                ? Collections.emptySet()
                : this.exclusions;
    }

    @Nullable
    public AxisAlignedBB placementAABB()
    {
        return this.placementAABB;
    }
    
    public boolean hasPlacementAABB()
    {
        return this.placementAABB != null;
    }
    
    public boolean hasPlacementList()
    {
        return this.placements != null && !this.placements.isEmpty();
    }
    
    public boolean hasExclusionList()
    {
        return this.exclusions != null && !this.exclusions.isEmpty();
    }

    public void applyToStack(ItemStack stackIn, EntityPlayer player)
    {
        switch(this.event())
        {
        case PLACE_AND_SET_REGION:
        case SET_PLACEMENT_REGION:
            PlacementItem.selectPlacementRegionFinish(stackIn, player, blockPos, false);
            break;

        case PLACE:
            // TODO: record for undo?
            break;

        case START_PLACEMENT_REGION:
            PlacementItem.selectPlacementRegionStart(stackIn, blockPos, false);
            break;
            
        case UNDO_PLACEMENT:
            //TODO
            break;
            
        case NO_OPERATION_STOP:
        case NO_OPERATION_CONTINUE:
        default:
            // NOOP
            break;
        
        }
    }
}
