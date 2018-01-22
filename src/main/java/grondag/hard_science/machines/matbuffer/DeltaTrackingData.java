package grondag.hard_science.machines.matbuffer;

class DeltaTrackingData
{
    /**
     * Circular buffer with additions made in last 16 sample periods 
     */
    final long deltaIn[] = new long[16];
    
    /**
     * Circular buffer with removals made in last 16 sample periods.
     * Always positive values.
     */
    final long deltaOut[] = new long[16];
    
    /**
     * Total of all samples in {@link #deltaIn}.  Maintained incrementally.
     */
    long deltaTotalIn;
    
    /**
     * Total of all samples in {@link #deltaOut}.  Maintained incrementally.
     */
    long deltaTotalOut;
    
    /**
     * Exponentially smoothed average {@link #deltaTotalIn}.
     */
    float deltaAvgIn;
    
    /**
     * Exponentially smoothed average {@link #deltaTotalOut}.
     */
    float deltaAvgOut;
}