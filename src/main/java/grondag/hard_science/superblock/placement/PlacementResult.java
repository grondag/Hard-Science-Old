package grondag.hard_science.superblock.placement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.simulator.storage.jobs.WorldTaskManager;
import grondag.hard_science.superblock.placement.spec.IPlacementSpecBuilder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class PlacementResult
{
    public static final PlacementResult EMPTY_RESULT_STOP = new PlacementResult(null, PlacementEvent.NO_OPERATION_CONTINUE, null);
    public static final PlacementResult EMPTY_RESULT_CONTINUE = new PlacementResult(null, PlacementEvent.NO_OPERATION_CONTINUE, null);

    private final PlacementEvent event;
    private final BlockPos blockPos;
    private final IPlacementSpecBuilder builder;
    
    /**
     * @param event Identifies state changes and subsequent event processing that should occur with this result.
     * @param blockPos If the event associated with this result requires a BlockPos for state changes,
     * the BlockPos that should be used. Null otherwise.
     */
    public PlacementResult(
            @Nullable BlockPos blockPos,
            @Nonnull  PlacementEvent event, 
            @Nullable IPlacementSpecBuilder builder)
    {
        this.blockPos = blockPos;
        this.event = event;
        this.builder = builder;
    }
    
    public IPlacementSpecBuilder builder()
    {
        return this.builder;
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
    
        case PLACE:
        case EXCAVATE:
            if(!player.world.isRemote)
            {
                if(this.builder.validate())
                {
                    // Turn off fixed region when completing a successful fixed region
                    // Did this because did not want to require two clicks to place a fixed region
                    // and no point in leaving the region there once placed.
                    if(item.isFixedRegionEnabled(stackIn)) item.setFixedRegionEnabled(stackIn, false);
                    
                    if(item.isVirtual(stackIn))
                    {
                        WorldTaskManager.enqueue(this.builder.worldTask((EntityPlayerMP)player));
                    }
                    else
                    {
                        // non-virtual placement operations happen immediately
                        // such actions are typically single blocks
                        this.builder.worldTask((EntityPlayerMP)player).runInServerTick(Integer.MAX_VALUE);
                    }
                }
            }
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
