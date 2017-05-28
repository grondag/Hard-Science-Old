package grondag.adversity.superblock.block;

import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.nbt.NBTTagCompound;

public class SuperBlockNBTHelper
{

    private static final String PLACEMENT_SHAPE_TAG = "APS";
    private static final String MODEL_STATE_TAG = "AMK";
    private static final String LIGHT_VALUE_TAG = "ALV";

    public static interface NBTReadHandler
    {
        public void handleNBTRead(ModelState modelState, int placementShape, byte lightValue);
    }
    
    public static void writeModelState(NBTTagCompound compound, ModelState modelState)
    {
        if(modelState == null) 
            compound.removeTag(MODEL_STATE_TAG);
        else
            compound.setIntArray(SuperBlockNBTHelper.MODEL_STATE_TAG, modelState.getBitsIntArray());
    }
    
    public static ModelState readModelState(NBTTagCompound compound)
    {
        int[] stateBits = compound.getIntArray(SuperBlockNBTHelper.MODEL_STATE_TAG);
        return (stateBits == null || stateBits.length != 8)
                ? null
                : new ModelState(stateBits); 
    }
    
    public static void writePlacementShape(NBTTagCompound compound, int placementShape)
    {
        if(placementShape == 0)
            compound.removeTag(SuperBlockNBTHelper.PLACEMENT_SHAPE_TAG);
        else
            compound.setInteger(SuperBlockNBTHelper.PLACEMENT_SHAPE_TAG, placementShape);
    }
    
    public static int readPlacementShape(NBTTagCompound compound)
    {
        return compound.getInteger(SuperBlockNBTHelper.PLACEMENT_SHAPE_TAG);
    }
    
    /** Inputs are masked to 0-15 */
    public static void writeLightValue(NBTTagCompound compound, int lightValue)
    {
        if(lightValue == 0)
            compound.removeTag(LIGHT_VALUE_TAG);
        else
            compound.setByte(SuperBlockNBTHelper.LIGHT_VALUE_TAG, (byte)(lightValue & 0xF));
    }
    
    public static byte readLightValue(NBTTagCompound compound)
    {
        return compound.getByte(SuperBlockNBTHelper.LIGHT_VALUE_TAG);
    }
    
    public static NBTTagCompound writeToNBT(NBTTagCompound compound, ModelState modelState, int placementShape, byte lightValue)
    {
        writeModelState(compound, modelState);
        writePlacementShape(compound, placementShape);
        writeLightValue(compound, lightValue);
        return compound;
    }
    
    public static void readFromNBT(NBTTagCompound compound, NBTReadHandler target)
    {
        target.handleNBTRead(
                readModelState(compound), 
                compound.getInteger(SuperBlockNBTHelper.PLACEMENT_SHAPE_TAG), 
                compound.getByte(SuperBlockNBTHelper.LIGHT_VALUE_TAG));
    }
}
