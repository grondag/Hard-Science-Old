package grondag.hard_science.simulator.job;

public enum RequestStatus
{
    /** Job is waiting for planned external dependencies (schedule, resource availability, target capacity) to be met */
    WAITING,
    
    /** 
     * Job dependencies have been met but something broke (link down, etc).
     * Job will not complete unless block is cleared.
     */
    BLOCKED,
    
    /**
     * Job is doing stuff!
     */
    ACTIVE,
    
    /**
     * Job is done.  Ain't gonna do any more stuff.  Move along now.
     */
    COMPLETE,
    
    /**
     * Job was cancelled and clean up completed.  No more activity will happen.
     */
    CANCELLED,
    
    /**
     * Job cancel has been requested, cleaning up.
     */
    CANCEL_IN_PROGRESS,
    
    /**
     * Sometimes bad things happen to good jobs...
     */
    ABEND
    
}
