package grondag.hard_science.simulator.base.jobs.tasks;

import grondag.hard_science.simulator.base.jobs.BuildingTask;
import grondag.hard_science.simulator.base.jobs.TaskType;
import grondag.hard_science.superblock.placement.AbstractPlacementSpec.PlacementSpecEntry;

public class ExcavationTask extends BuildingTask
{
    /**
     * Use for new instances.
     */
    public ExcavationTask(BuildPlanningTask planningTask, PlacementSpecEntry entry)
    {
        super(planningTask, entry);
    }
    
    /** Use for deserialization */
    public ExcavationTask()
    {
        super();
    }
    
    @Override
    protected synchronized void onLoaded()
    {
        super.onLoaded();
        //TODO: add excavation rendering
    }

    @Override
    protected synchronized void onTerminated()
    {
        super.onTerminated();
        // TODO: Remove excavation rendering
    }

    @Override
    public TaskType requestType()
    {
        return TaskType.EXCAVATION;
    }
}
