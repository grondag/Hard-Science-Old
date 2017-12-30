package grondag.hard_science.library.concurrency;

public interface ICountedJobBacker
{
    /** 
     * Returns an array where the contiguous non-null elements 
     * from 0 until the start null elements should be processed
     * by the job.
     */
    public abstract Object[] getOperands();
    
    /**
     * <em>Approximate</em> number of non-null operands in array 
     * returned by {@link #getOperands()}.  Purpose is for
     * determining if a parallel run is appropriate.
     */
    public abstract int size();
    
}