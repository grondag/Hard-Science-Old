package grondag.hard_science.simulator.base.jobs;

/**
 * Callback interface for task status changes.
 */
public interface ITaskListener
{
    public void onTaskComplete(AbstractTask task);
}
