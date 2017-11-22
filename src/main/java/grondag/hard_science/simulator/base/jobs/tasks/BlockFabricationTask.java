package grondag.hard_science.simulator.base.jobs.tasks;

import grondag.hard_science.simulator.base.jobs.BuildingTask;
import grondag.hard_science.simulator.base.jobs.TaskType;
import grondag.hard_science.superblock.placement.AbstractPlacementSpec.PlacementSpecEntry;

public class BlockFabricationTask extends BuildingTask
{
    /**
     * Use for new instances.
     */
    public BlockFabricationTask(PlacementSpecEntry entry)
    {
        super(entry);
    }
    
    /** Use for deserialization */
    public BlockFabricationTask()
    {
        super();
    }
    @Override
    public TaskType requestType()
    {
        return TaskType.BLOCK_FABRICATION;
    }
}
