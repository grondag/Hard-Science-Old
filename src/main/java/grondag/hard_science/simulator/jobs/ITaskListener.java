package grondag.hard_science.simulator.jobs;

/**
 * Callback interface for task status changes.
 */
public interface ITaskListener
{
    public void onTaskComplete(AbstractTask task);
    
    /** 
     * Called when active task is cancelled. 
     * Default implementation calls {@link #onTaskComplete(AbstractTask)}
     */
    public default void onTaskCancelled(AbstractTask task) { this.onTaskComplete(task); }
    
    /** 
     * Called when task that is started or complete has to 
     * backtrack due to antecedent change. Default implementation
     * calls {@link #onTaskCancelled(AbstractTask)}
     */
    public default void onTaskBackTracked(AbstractTask task) { this.onTaskCancelled(task); }
}
