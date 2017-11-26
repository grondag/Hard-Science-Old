package grondag.hard_science.simulator.base.jobs.tasks;

import grondag.hard_science.simulator.base.jobs.BuildingTask;
import grondag.hard_science.simulator.base.jobs.RequestStatus;
import grondag.hard_science.simulator.base.jobs.TaskType;
import net.minecraft.util.math.BlockPos;

public class ExcavationTask extends BuildingTask
{
    /**
     * Use for new instances.
     */
    public ExcavationTask(BlockPos pos)
    {
        super(pos);
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
