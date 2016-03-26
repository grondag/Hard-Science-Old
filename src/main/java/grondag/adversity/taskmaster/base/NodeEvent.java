package grondag.adversity.taskmaster.base;

public abstract class NodeEvent
{
	private final long eventTime;
	
	protected NodeEvent(long eventTime)
	{
		this.eventTime = eventTime;
	}
	
	public long getEventTime() { return eventTime; }
}
