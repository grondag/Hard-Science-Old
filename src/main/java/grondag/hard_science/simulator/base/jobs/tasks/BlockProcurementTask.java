package grondag.hard_science.simulator.base.jobs.tasks;

import grondag.hard_science.simulator.base.jobs.BuildingTask;
import grondag.hard_science.simulator.base.jobs.TaskType;
import grondag.hard_science.superblock.placement.spec.AbstractPlacementSpec.PlacementSpecEntry;

public class BlockProcurementTask extends BuildingTask
{
    /**
     * Use for new instances.
     */
    public BlockProcurementTask(PlacementSpecEntry entry)
    {
        super(entry);
        entry.procurementTaskID = this.getId();
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
