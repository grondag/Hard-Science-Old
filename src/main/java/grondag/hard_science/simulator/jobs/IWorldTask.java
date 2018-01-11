package grondag.hard_science.simulator.jobs;

/**
 *  Interface to be supported by all job tasks that require world access.
 */
public interface IWorldTask
{
    /**
     * Does work that requires world access.  
     * Called during server tick.<p>
     * 
     * @param maxOperations  How much work is permitted.  
     * Arbitrary, but getBlockState counts as 1 operation 
     * and setBlockState counts as 4. Use these as approx benchmarks.
     * @return Number of operations performed.
     */
    public int runInServerTick(int maxOperations);
    
    /**
     * True if task should be removed from queue. Will be
     * checked after each invocation of {@link #runInServerTick(int)}.
     */
    public boolean isDone();
}
