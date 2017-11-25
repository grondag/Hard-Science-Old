package grondag.hard_science.superblock.placement.spec;

import grondag.hard_science.simulator.base.IIdentified;
import grondag.hard_science.superblock.placement.PlacementItem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

/**
 * Holds individual blocks to be placed.
 */
public abstract class PlacementSpecEntry
{
    private int index;
    private BlockPos pos;

    /**
     * ID of associated excavation task.
     * Will be unassigned if no task created.
     */
    public int excavationTaskID = IIdentified.UNASSIGNED_ID;
    
    /**
     * ID of associated placement task.
     * Will be unassigned if no task created.
     */
    public int placementTaskID = IIdentified.UNASSIGNED_ID;
    
    /**
     * ID of associated procurement task - set by build planning task.
     * Fabrication task (if applies) can be obtained from the procurement task.
     * Will be unassigned if no task created.
     */
    public int procurementTaskID = IIdentified.UNASSIGNED_ID;
    
    protected PlacementSpecEntry(int index, BlockPos pos)
    {
        this.index = index;
        this.pos = pos;
    }

    /** 0-based position within this spec - must never change because used externally to identify */
    public int index()
    {
        return this.index;
    }

    public BlockPos pos()
    {
        return this.pos;
    }
    
    /** Will be air if is excavation */
    public abstract ItemStack placement();
    
    public abstract PlacementItem placementItem();
    
    /** Same result as checking placement() stack is air */
    public abstract boolean isExcavation();
    
    /** 
     * Block state that should be placed.
     */
    public IBlockState blockState()
    {
        return placementItem().getPlacementBlockStateFromStack(this.placement());
    }

}