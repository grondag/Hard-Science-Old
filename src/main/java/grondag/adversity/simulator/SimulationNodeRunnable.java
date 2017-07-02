package grondag.adversity.simulator;


public abstract class SimulationNodeRunnable extends SimulationNode
{
    protected SimulationNodeRunnable(int nodeID)
    {
        super(nodeID);
    }
    protected void doOnTick() {};
    protected void doOffTick() {};
 }
