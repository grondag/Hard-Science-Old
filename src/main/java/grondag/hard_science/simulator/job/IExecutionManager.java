package grondag.hard_science.simulator.job;


import grondag.hard_science.simulator.domain.Domain;


public interface IExecutionManager
{
    public Domain getDomain();
    
    public void schedule(IJob job); 
}
