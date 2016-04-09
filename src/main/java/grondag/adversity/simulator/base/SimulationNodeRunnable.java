package grondag.adversity.simulator.base;

import grondag.adversity.simulator.TaskCounter;

public abstract class SimulationNodeRunnable extends SimulationNode implements Runnable
{
    private final TaskCounter taskCounter;
    
    protected SimulationNodeRunnable(int nodeID, TaskCounter taskCounter)
    {
        super(nodeID);
        this.taskCounter = taskCounter;
    }
    
    @Override
    public final void run()
    {
        taskCounter.incrementActiveTasks();
        this.doStuff();
        taskCounter.decrementActiveTasks();
    }
    
    protected abstract void doStuff();
}
