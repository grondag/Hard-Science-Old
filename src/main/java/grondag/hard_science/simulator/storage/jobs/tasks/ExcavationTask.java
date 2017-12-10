package grondag.hard_science.simulator.storage.jobs.tasks;

import grondag.hard_science.simulator.storage.jobs.AbstractPositionedTask;
import grondag.hard_science.simulator.storage.jobs.RequestStatus;
import grondag.hard_science.simulator.storage.jobs.TaskType;
import net.minecraft.util.math.BlockPos;

public class ExcavationTask extends AbstractPositionedTask
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
    public TaskType requestType()
    {
        return TaskType.EXCAVATION;
    }
}
