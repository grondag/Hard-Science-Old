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
    private final Set<BlockPos> deletions;
    private final AxisAlignedBB placementAABB;
    private final AxisAlignedBB deletionAABB;
    private final BlockPos blockPos;
    private final PlacementEvent event;
    
    public static final PlacementResult EMPTY_RESULT_CONTINUE = new PlacementResult(null, null, null, null, null, PlacementEvent.NO_OPERATION_CONTINUE);
    public static final PlacementResult EMPTY_RESULT_STOP = new PlacementResult(null, null, null, null, null, PlacementEvent.NO_OPERATION_CONTINUE);
    
    public PlacementResult(
            @Nullable AxisAlignedBB placementAABB, 
            @Nullable List<Pair<BlockPos, ItemStack>> placements, 
            @Nullable AxisAlignedBB deletionAABB, 
            @Nullable Set<BlockPos> deletions,
            @Nullable BlockPos blockPos,
            @Nonnull  PlacementEvent event)
    {
        this.placementAABB = placementAABB;
        this.placements = placements;
        this.deletionAABB = deletionAABB;
        this.deletions = deletions;
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
     * For deletion actions, blocks to be deleted.
     * For placement actions, blocks that were obstacles to placement.
     */
    @Nonnull
    public Set<BlockPos> deletions()
    {
        return this.deletions == null
                ? Collections.emptySet()
                : this.deletions;
    }

    @Nullable
    public AxisAlignedBB placementAABB()
    {
        return this.placementAABB;
    }
    
    @Nullable
    public AxisAlignedBB deletionAABB()
    {
        return this.deletionAABB;
    }
    
    public boolean hasPlacementAABB()
    {
        return this.placementAABB != null;
    }
    
    public boolean hasDeletiopnAABB()
    {
        return this.deletionAABB != null;
    }
    
    public boolean hasPlacementList()
    {
        return this.placements != null && !this.placements.isEmpty();
    }
    
    public boolean hasDeletionList()
    {
        return this.deletions != null && !this.deletions.isEmpty();
    }

    public void applyToStack(ItemStack stackIn, EntityPlayer player)
    {
        switch(this.event())
        {
        case CANCEL_EXCAVATION_REGION:
            PlacementItem.selectDeletionRegionCancel(stackIn);
            break;
            
        case CANCEL_PLACEMENT_REGION:
            PlacementItem.selectPlacementRegionCancel(stackIn);
            break;
            
        case PLACE_AND_SET_REGION:
        case SET_PLACEMENT_REGION:
            PlacementItem.selectPlacementRegionFinish(stackIn, player, blockPos, false);
            break;

        case EXCAVATE_AND_SET_REGION:
        case SET_EXCAVATION_REGION:
            PlacementItem.selectDeletionRegionFinish(stackIn, player, blockPos, false);
            break;
            
        case PLACE:
            // TODO: record for undo?
            break;

        case EXCAVATE:
            // TODO: record for undo?
            break;
            
        case START_EXCAVATION_REGION:
            PlacementItem.selectDeletionRegionStart(stackIn, blockPos, false);
            break;
            
        case START_PLACEMENT_REGION:
            PlacementItem.selectPlacementRegionStart(stackIn, blockPos, false);
            break;
            
        case UNDO_EXCAVATION:
            //TODO
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
