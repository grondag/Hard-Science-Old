package grondag.hard_science.simulator.storage.jobs;

/**
 * Callback interface for task status changes.
 */
public interface ITaskListener
{
    public void onTaskComplete(AbstractTask task);
}
