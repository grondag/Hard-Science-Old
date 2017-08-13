package grondag.hard_science.simulator.network;

/**
 * Used to notify scheduled transport requesters when transport is ready.
 */
public interface IScheduleCallback
{
    public abstract void updateStatus(AbstractScheduledTransportResult result);
}
