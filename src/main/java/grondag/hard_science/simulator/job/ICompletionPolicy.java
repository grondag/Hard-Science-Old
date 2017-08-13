package grondag.hard_science.simulator.job;


public interface ICompletionPolicy
{
    public void scheduleContinuationIfNeeded(IJob job, IExecutionManager manager);
}
