package grondag.hard_science.virtualblock;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.persistence.IPersistenceNode;
import net.minecraft.nbt.NBTTagCompound;

public class ConstructionRequestTracker implements IPersistenceNode
{
    private boolean isDirty;
    
    @Override
    public boolean isSaveDirty()
    {
        return this.isDirty;
    }

    @Override
    public void setSaveDirty(boolean isDirty)
    {
        this.isDirty = isDirty;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String tagName()
    {
        return ModNBTTag.PLACEMENT_REQUEST_MANAGER;
    }

}
