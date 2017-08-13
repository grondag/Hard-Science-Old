package grondag.hard_science.simulator.scratch;

import java.util.Arrays;

import grondag.hard_science.simulator.persistence.IReadWriteNBT;
import jline.internal.Log;
import net.minecraft.nbt.NBTTagCompound;

public class AssignedNumbersAuthority implements IReadWriteNBT
{
    public static final AssignedNumbersAuthority INSTANCE = new AssignedNumbersAuthority();
    
    private static final String NBT_TAG_NAME = "HSANA";
    
    private int[] lastID = new int[AssignedNumber.values().length];
    
    private boolean isDirty = false;
    
    /** 
     * First ID returned for each type is 1 to distinguish between missing/unset ID of 0.
     */
    public synchronized int generateNewNumber(AssignedNumber numberType)
    {
        this.isDirty = true;
        return ++this.lastID[numberType.ordinal()];
    }
    
    @Override
    public synchronized void readFromNBT(NBTTagCompound tag)
    {
        int input[] = tag.getIntArray(NBT_TAG_NAME);
        if(input == null)
        {
            lastID = new int[AssignedNumber.values().length];
        }
        {
            if(input.length == lastID.length)
            {
                lastID = Arrays.copyOf(input, input.length);
            }
            else
            {
                Log.warn("Simulation assigned numbers save data corrupt.  World may be borked.");
                lastID = new int[AssignedNumber.values().length];
            }
        }
        this.isDirty = false;
    }
    
    @Override
    public synchronized void writeToNBT(NBTTagCompound tag)
    {
        tag.setIntArray(NBT_TAG_NAME, Arrays.copyOf(lastID, lastID.length));
        this.isDirty = false;
    }
    
    public boolean isDirty() { return this.isDirty || AssignedNumbersAuthority.INSTANCE.isDirty(); }
    
}
