package grondag.hard_science.simulator.base;

import grondag.hard_science.library.serialization.ModNBTTag;
import net.minecraft.nbt.NBTTagCompound;

public interface IIdentified
{
    /**
     * Initialize new objects with this value to cause them to be assigned a new ID.
     * Can also be used in a reference to an ID to mean "null" because this ID will never be assigned.
     */
    public static final int UNASSIGNED_ID = 0;
    
    /**
     * Use this to represent a "default" reference.  All other system IDs should be > this value.
     */
    public static final int DEFAULT_ID = 1;

    
    /** implement an int in class, return it here */
    public int getIdRaw();
    public void setId(int id);
    public AssignedNumber idType();

    public default int getId()
    {
        int result = this.getIdRaw();
        if(result == UNASSIGNED_ID)
        {
            result = DomainManager.INSTANCE.assignedNumbersAuthority().newNumber(this.idType());
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
        if(id > 0) tag.setInteger(ModNBTTag.ASSIGNED_IDENTIFER, id);
    }
    
    /**
     * Use this in deserializeNBT of implementing class.
     */
    public default void deserializeID(NBTTagCompound tag)
    {
        this.setId(tag.hasKey(ModNBTTag.ASSIGNED_IDENTIFER) ? tag.getInteger(ModNBTTag.ASSIGNED_IDENTIFER) : UNASSIGNED_ID);
    }
}