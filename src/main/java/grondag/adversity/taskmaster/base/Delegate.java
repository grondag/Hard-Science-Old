package grondag.adversity.taskmaster.base;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Provides thread-safe access to a simulation node.
 * Mainly used for sending and receiving notifications.
 */
public abstract class Delegate
{	
	private NBTTagCompound state;
	private boolean hasUpdate = false;
	
	public void postEvent(NodeEvent event);
	public NodeEvent[] pullEvents()
	
	public boolean hasUpdate() { return state != null
	
	public NBTTagCompound getState() { return state; }
}
