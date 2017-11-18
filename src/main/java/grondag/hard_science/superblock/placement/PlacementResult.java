package grondag.hard_science.superblock.placement;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import grondag.hard_science.library.world.IntegerAABB;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlacementResult
{
    public static final PlacementResult EMPTY_RESULT_STOP = new PlacementResult(null, null, null, null, PlacementEvent.NO_OPERATION_CONTINUE, null);
    public static final PlacementResult EMPTY_RESULT_CONTINUE = new PlacementResult(null, null, null, null, PlacementEvent.NO_OPERATION_CONTINUE, null);

    private final PlacementEvent event;
    private final BlockPos blockPos;
    private final IPlacementSpecBuilder builder;
    
    //TODO: remove when retired
    @Deprecated
    private final List<Pair<BlockPos, ItemStack>> placements;
    @Deprecated
    private final Set<BlockPos> exclusions;
    @Deprecated
    private final IntegerAABB placementAABB;
    
    /**
     * @param event Identifies state changes and subsequent event processing that should occur with this result.
     * @param blockPos If the event associated with this result requires a BlockPos for state changes,
     * the BlockPos that should be used. Null otherwise.
     */
    public PlacementResult(
            @Nullable IntegerAABB placementAABB, 
            @Nullable List<Pair<BlockPos, ItemStack>> placements, 
            @Nullable Set<BlockPos> exclusions, 
            @Nullable BlockPos blockPos,
            @Nonnull  PlacementEvent event, 
            @Nullable IPlacementSpecBuilder builder)
    {
        this.placementAABB = placementAABB;
        this.placements = placements;
        this.exclusions = exclusions;
        this.blockPos = blockPos;
        this.event = event;
        this.builder = builder;
    }
    
    public IPlacementSpecBuilder builder()
    {
        return this.builder;
    }
    
    /**
     * Blocks that should be placed.
     */
    @Nonnull
    @Deprecated
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
    @Deprecated
    public Set<BlockPos> exclusions()
    {
        return this.exclusions == null
                ? Collections.emptySet()
                : this.exclusions;
    }

    @Nullable
    @Deprecated
    public IntegerAABB placementAABB()
    {
        return this.placementAABB;
    }
    
    @Deprecated
    public boolean hasPlacementAABB()
    {
        return this.placementAABB != null;
    }
    
    @Deprecated
    public boolean hasPlacementList()
    {
        return this.placements != null && !this.placements.isEmpty();
    }
    
    @Deprecated
    public boolean hasExclusionList()
    {
        return this.exclusions != null && !this.exclusions.isEmpty();
    }

    
    
    /**
     * If true, the user input event (mouse click, usually) that caused this result
     * should continue to be processed by other event handlers. True also implies
     * that {@link #apply(ItemStack, EntityPlayer)} will have no effect.
     */
    public boolean shouldInputEventsContinue()
    {
        return this.event == PlacementEvent.NO_OPERATION_CONTINUE;
    }

    /**
     * True if all block changes in this result are for block removal and there are no block placements.
     */
    public boolean isExcavationOnly()
    {
        return this.event.isExcavation;
    }

    private void applyToWorld(ItemStack stackIn, EntityPlayer player, World worldIn)
    {
        this.builder.build();
    }

    public void apply(ItemStack stackIn, EntityPlayer player)
    {
        if(!PlacementItem.isPlacementItem(stackIn)) return;
        
        PlacementItem item = (PlacementItem)stackIn.getItem();
        
        switch(this.event)
        {
            
        case START_PLACEMENT_REGION:
            item.fixedRegionStart(stackIn, blockPos, false);
            break;
            
        case CANCEL_PLACEMENT_REGION:
            item.fixedRegionCancel(stackIn);
            break;
            
        case SET_PLACEMENT_REGION:
            item.fixedRegionFinish(stackIn, player, blockPos, false);
            break;
    
        case PLACE:
        case EXCAVATE:
            this.applyToWorld(stackIn, player, player.world);
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
