package grondag.hard_science.simulator;


public abstract class SimulationNodeRunnable extends SimulationNode
{
    protected SimulationNodeRunnable(int nodeID)
    {
        super(nodeID);
    }
    protected void doOnTick() {};
    protected void doOffTick() {};
 }
