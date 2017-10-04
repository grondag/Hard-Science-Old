package grondag.hard_science.simulator.persistence;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.WorldSavedData;

public class PersistenceDelegate extends WorldSavedData
{
    /** saves initial nbt load until we know our node reference */
    private NBTTagCompound nbtCache;
    
    private IPersistenceNode pnode = null;
    
    public PersistenceDelegate(IPersistenceNode pnode) 
    {
        super(pnode.tagName());
        this.pnode = pnode;
    }
    
    /**
     * Needed by WorldSavedData load constructor.
     * PersistenceManager will call {@link #setNode(IPersistenceNode)} after.
     */
    public PersistenceDelegate(String tagName) 
    {
        super(tagName);
    }
    
    /**
     * Used on load to set node after instantiation.
     * Necessary because WorldSavedData thingymabobber creates the instance
     * and doesn't know / isn't able to do this.
     */
    public void setNode(IPersistenceNode pnode)
    {
        this.pnode = pnode;
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        if(pnode == null)
        {
            nbtCache = nbt;
        }
        else 
        {
            pnode.deserializeNBT(nbt); 
        }
    }
    
    public void readCachedNBT()
    {
        if(pnode != null && nbtCache != null)
        {
            pnode.deserializeNBT(this.nbtCache);
            nbtCache = null;
        }
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
    { 
        if(pnode != null) pnode.serializeNBT(nbt);
        return nbt;
    }

    @Override
    public boolean isDirty() { return pnode == null ? false : pnode.isSaveDirty(); }

    @Override
    public void setDirty(boolean isDirty) 
    { 
        if(pnode != null) pnode.setSaveDirty(isDirty); 
    }
}