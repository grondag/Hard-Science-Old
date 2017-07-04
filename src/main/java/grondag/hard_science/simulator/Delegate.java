package grondag.hard_science.simulator;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Provides thread-safe access to a simulation node.
 * Mainly used for sending and receiving notifications.
 */
public abstract class Delegate
{	
	private NBTTagCompound state;
	private boolean hasUpdate = false;
	
	public abstract void postEvent(NodeEvent event);
	public abstract NodeEvent[] pullEvents();
	
	public boolean hasUpdate() { return hasUpdate; }
	
	public NBTTagCompound getState() { return state; }
}
