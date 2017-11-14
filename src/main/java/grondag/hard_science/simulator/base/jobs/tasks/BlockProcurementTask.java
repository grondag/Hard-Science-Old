package grondag.hard_science.simulator.base.jobs.tasks;

import grondag.hard_science.simulator.base.jobs.AbstractTask;
import grondag.hard_science.simulator.base.jobs.BuildingTask;
import grondag.hard_science.simulator.base.jobs.TaskType;
import grondag.hard_science.superblock.placement.AbstractPlacementSpec.PlacementSpecEntry;

public class BlockProcurementTask extends BuildingTask
{
    /**
     * Use for new instances.
     */
    public BlockProcurementTask(BuildPlanningTask planningTask, PlacementSpecEntry entry)
    {
        super(planningTask, entry);
    }
    
    /** Use for deserialization */
    public BlockProcurementTask()
    {
        super();
    }
    
    @Override
    public TaskType requestType()
    {
        return TaskType.BLOCK_PROCUREMENT;
    }
}
