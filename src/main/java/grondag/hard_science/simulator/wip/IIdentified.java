package grondag.hard_science.simulator.wip;

import grondag.hard_science.simulator.Simulator;
import net.minecraft.nbt.NBTTagCompound;

public interface IIdentified
{
    public default int getId()
    {
        int result = this.getIdRaw();
        if(result <= 0)
        {
            result = Simulator.INSTANCE.domainManager().ASSIGNED_NUMBERS_AUTHORITY.newNumber(this.idType());
            this.setId(result);
        }
        return result;
    }
    
    /** implement an int in class, return it here */
    public int getIdRaw();
    public void setId(int id);
    public AssignedNumber idType();
    
    /**
     * Use this in serializeNBT of implementing class.
     */
    public default void serializeID(NBTTagCompound tag)
    {
        tag.setInteger("hs_id", this.getId());
    }
    
    /**
     * Use this in deserializeNBT of implementing class.
     */
    public default void deserializeID(NBTTagCompound tag)
    {
        this.setId(tag.getInteger("hs_id"));
    }
}