package grondag.hard_science.simulator.base.jobs.tasks;

import grondag.hard_science.simulator.base.jobs.AbstractTask;
import grondag.hard_science.simulator.base.jobs.TaskType;

public class BlockFabricationTask extends AbstractTask
{
    @Override
    public TaskType requestType()
    {
        return TaskType.BLOCK_FABRICATION;
    }
}
