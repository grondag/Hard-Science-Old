package grondag.hard_science.simulator.wip;

import grondag.hard_science.simulator.Simulator;
import net.minecraft.nbt.NBTTagCompound;

public interface IIdentified
{
    public static final int NO_ID = -1;
    
    /** implement an int in class, return it here */
    public int getIdRaw();
    public void setId(int id);
    public AssignedNumber idType();

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
    
    /**
     * Use this in serializeNBT of implementing class.
     * Will cause ID to be generated if it has not already been.
     */
    public default void serializeID(NBTTagCompound tag)
    {
        int id = this.getId();
        if(id > 0) tag.setInteger("hs_id", id);
    }
    
    /**
     * Use this in deserializeNBT of implementing class.
     */
    public default void deserializeID(NBTTagCompound tag)
    {
        this.setId(tag.hasKey("hs_id") ? tag.getInteger("hs_id") : NO_ID);
    }
}