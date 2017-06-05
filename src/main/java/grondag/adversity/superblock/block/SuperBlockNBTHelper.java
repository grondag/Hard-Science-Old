package grondag.adversity.superblock.block;

import grondag.adversity.niceblock.support.BlockSubstance;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;

public class SuperBlockNBTHelper
{

    private static final String PLACEMENT_SHAPE_TAG = "APS";
    private static final String MODEL_STATE_TAG = "AMK";
    private static final String LIGHT_VALUE_TAG = "ALV";
    private static final String SUBSTANCE_TAG = "ASB";

    public static interface NBTReadHandler
    {
        public void handleNBTRead(ModelState modelState);
    }
    
    public static interface SuperModelNBTReadHandler
    {
        public void handleNBTRead(int placementShape, byte lightValue, BlockSubstance substance);
    }

    public static void writeModelState(NBTTagCompound compound, ModelState modelState)
    {
        if(modelState == null) 
            compound.removeTag(MODEL_STATE_TAG);
        else
            compound.setIntArray(MODEL_STATE_TAG, modelState.getBitsIntArray());
    }
    
    public static ModelState readModelState(NBTTagCompound compound)
    {
        int[] stateBits = compound.getIntArray(MODEL_STATE_TAG);
        return (stateBits == null || stateBits.length != 8)
                ? null
                : new ModelState(stateBits); 
    }
    
    public static void writePlacementShape(NBTTagCompound compound, int placementShape)
    {
        if(placementShape == 0)
            compound.removeTag(PLACEMENT_SHAPE_TAG);
        else
            compound.setInteger(PLACEMENT_SHAPE_TAG, placementShape);
    }
    
    public static int readPlacementShape(NBTTagCompound compound)
    {
        return compound.getInteger(PLACEMENT_SHAPE_TAG);
    }
    
    /** Inputs are masked to 0-15 */
    public static void writeLightValue(NBTTagCompound compound, int lightValue)
    {
        if(lightValue == 0)
            compound.removeTag(LIGHT_VALUE_TAG);
        else
            compound.setByte(LIGHT_VALUE_TAG, (byte)(lightValue & 0xF));
    }
    
    public static byte readLightValue(NBTTagCompound compound)
    {
        return compound.getByte(LIGHT_VALUE_TAG);
    }
    
    public static void writeSubstance(NBTTagCompound compound, BlockSubstance substance)
    {
        compound.setByte(SUBSTANCE_TAG, substance == null ? 0 : (byte)(substance.ordinal()));
    }
    
    public static BlockSubstance readSubstance(NBTTagCompound compound)
    {
        return BlockSubstance.values()[MathHelper.clamp(compound.getByte(SUBSTANCE_TAG), 0, BlockSubstance.values().length)];
    }
    
    /** extra attributes for supermodel blocks */
    public static NBTTagCompound writeToNBT(NBTTagCompound compound, int placementShape, byte lightValue, BlockSubstance substance)
    {
        writePlacementShape(compound, placementShape);
        writeLightValue(compound, lightValue);
        writeSubstance(compound, substance);
        return compound;
    }
    
    public static NBTTagCompound writeToNBT(NBTTagCompound compound, ModelState modelState)
    {
        writeModelState(compound, modelState);
        return compound;
    }
    
    public static void readFromNBT(NBTTagCompound compound, NBTReadHandler target)
    {
        target.handleNBTRead(
                readModelState(compound));
    }
    
    public static void superModelReadFromNBT(NBTTagCompound compound, SuperModelNBTReadHandler target)
    {
        target.handleNBTRead(
                compound.getInteger(PLACEMENT_SHAPE_TAG), 
                compound.getByte(LIGHT_VALUE_TAG),
                readSubstance(compound));
    }
    
}
