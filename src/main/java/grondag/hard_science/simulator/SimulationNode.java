package grondag.hard_science.simulator;

import net.minecraft.nbt.NBTTagCompound;

public abstract class SimulationNode
{
    protected volatile boolean isDirty = false;
    protected final int nodeID;

    protected SimulationNode(int nodeID)
    {
        this.nodeID = nodeID;
    }
    
    public static int getNodeIdFromTagKey(String tagKey) { return Integer.parseInt(tagKey); }
    public String getTagKey() { return "" + this.getID(); }
    public boolean isSaveDirty() { return isDirty; }
    public void setSaveDirty(boolean isDirty) { this.isDirty = isDirty; }
    
    public int getID() { return nodeID; };
    public abstract void readFromNBT(NBTTagCompound nbt);
    public abstract void writeToNBT(NBTTagCompound nbt);    
}
