package grondag.hard_science.simulator.base.jobs.tasks;

import grondag.hard_science.simulator.base.jobs.BuildingTask;
import grondag.hard_science.simulator.base.jobs.RequestStatus;
import grondag.hard_science.simulator.base.jobs.TaskType;
import grondag.hard_science.superblock.placement.spec.PlacementSpecEntry;

public class ExcavationTask extends BuildingTask
{
    /**
     * Use for new instances.
     */
    public ExcavationTask(PlacementSpecEntry entry)
    {
        super(entry);
        entry.excavationTaskID = this.getId();
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
        
        // prevent orphaned active excavation tasks
        // must be reclaimed at startup
        if(this.getStatus() == RequestStatus.ACTIVE) this.abandon();
    }

    @Override
    protected synchronized void onTerminated()
    {
        super.onTerminated();
    }

    @Override
    public TaskType requestType()
    {
        return TaskType.EXCAVATION;
    }
}
