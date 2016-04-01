package grondag.adversity.simulator;

import grondag.adversity.simulator.base.INode;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;

public class PersistenceDelegate extends WorldSavedData
{
    private INode node;
    /** saves initial nbt load until we know our node reference */
    private NBTTagCompound nbtCache;
    
    public PersistenceDelegate(String name) {
        super(name);
    }
    
    public void setNode(INode node) { this.node = node; }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        if(node == null)
        {
            nbtCache = nbt;
        }
        else 
        {
            node.readFromNBT(nbt); 
        }
    }
    
    public void readCachedNBT()
    {
        if(node != null && nbtCache != null)
        {
            node.readFromNBT(this.nbtCache);
            nbtCache = null;
        }
    }
    
    @Override
    public void writeToNBT(NBTTagCompound nbt) { if(node != null) node.writeToNBT(nbt); }

    @Override
    public boolean isDirty() { return node == null ? false : node.isSaveDirty(); }

    @Override
    public void setDirty(boolean isDirty) { if(node != null) node.setSaveDirty(isDirty); }
}