package grondag.hard_science.simulator.storage.jobs;

import grondag.hard_science.simulator.domain.Domain;

/**
 * Exists to allow interfaces that subclass tasks
 */
public interface ITask
{

    /**
     * Moves status from READY to ACTIVE.  
     * Called by job manager when assigning work.
     */
    void claim();

    /**
     * Moves status from ACTIVE back to READY.  
     * Called by worker when task must be abandoned.
     */
    void abandon();

    /**
     * Called when an antecedent that previously declared itself
     * ready via {@link #onAntecedentTerminated(AbstractTask)} becomes
     * unready again for any reason.<p>
     * 
     * Will add called as an antecedent for this task and if status 
     * of this task is something other than WAITING, will attempt to
     * make status WAITING. <p>
     * 
     * If this task has consequents, and this task was previously 
     * COMPLETE, then will cascade the backtrack to the consequent tasks.
     */
    void backTrack(AbstractTask antecedent);
    
    TaskType requestType();

    Job job();

    RequestStatus getStatus();

    /** 
     * Convenient shorthand for getStatus().isTerminated 
     */
    boolean isTerminated();

    void cancel();
    

    /**
     * Should be called on a claimed, active task to move it to completion.
     */
    void complete();

    void addListener(ITaskListener listener);

    void removeListener(ITaskListener listener);

    Domain getDomain();
    
    public int getId();
    
    public void onAntecedentTerminated(AbstractTask antecedent);
    
    /**
     * Called on all tasks after deserialization is complete.  
     * Override to handle actions that may require other objects to be deserialized first.
     */
    public default void afterDeserialization() {};
}