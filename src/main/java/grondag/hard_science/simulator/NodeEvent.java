package grondag.hard_science.simulator;

public abstract class NodeEvent implements Runnable
{
	private int eventTick;
	
	protected NodeEvent()
	{
	}
	
	public int getEventTick() { return eventTick; }
    public void setEventTick(int eventTick) { this.eventTick = eventTick; }
}
