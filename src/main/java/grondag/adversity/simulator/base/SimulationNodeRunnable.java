package grondag.adversity.simulator.base;


public abstract class SimulationNodeRunnable extends SimulationNode
{
    protected SimulationNodeRunnable(int nodeID)
    {
        super(nodeID);
    }
    protected void doOnTick() {};
    protected void doOffTick() {};
 }
