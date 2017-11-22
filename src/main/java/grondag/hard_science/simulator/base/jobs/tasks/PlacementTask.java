package grondag.hard_science.simulator.base.jobs.tasks;

import grondag.hard_science.simulator.base.jobs.BuildingTask;
import grondag.hard_science.simulator.base.jobs.TaskType;
import grondag.hard_science.superblock.items.SuperItemBlock;
import grondag.hard_science.superblock.placement.AbstractPlacementSpec.PlacementSpecEntry;
import grondag.hard_science.superblock.placement.PlacementItem;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlacementTask extends BuildingTask
{
    /**
     * Use for new instances.
     */
    public PlacementTask(PlacementSpecEntry entry)
    {
        super(entry);
        entry.constructionTaskID = this.getId();
    }
    
    /** Use for deserialization */
    public PlacementTask()
    {
        super();
    }
    
    @Override
    public TaskType requestType()
    {
        return TaskType.PLACEMENT;
    }

    /**
     * Places a virtual block for this placement.
     * Called by build planner when the placement is scheduled
     * if the space is unoccupied, or by excavation task
     * after excavation task if the space is occupied.
     * <p>
     * This method expects the item stack for 
     * any placement task to contain a virtual block.
     * So it does not do any translation to the physical block.
     */
    public void placeVirtualBlock(World world)
    {
        PlacementSpecEntry entry = this.entry();
        
        if(entry == null) return;
        
        ItemStack placedStack = entry.placement();
        
        if(!(placedStack.getItem() instanceof SuperItemBlock)) return;
        
        SuperItemBlock itemBlock = (SuperItemBlock)placedStack.getItem();
        
        BlockPos placedPos = entry.pos();
            
        IBlockState placedState = itemBlock.getPlacementBlockStateFromStack(placedStack);
        
        if (itemBlock.placeBlockAt(placedStack, null, world, placedPos, null, 0, 0, 0, placedState))
        {
            SoundType sound = PlacementItem.getStackSubstance(placedStack).soundType;
            
            world.playSound(
                    null,
                    placedPos, 
                    sound.getPlaceSound(), 
                    SoundCategory.BLOCKS, 
                    (sound.getVolume() + 1.0F) / 2.0F, 
                    sound.getPitch() * 0.8F);
        }
        
    }
}
