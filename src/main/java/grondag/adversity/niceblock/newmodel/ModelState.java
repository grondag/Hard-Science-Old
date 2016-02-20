package grondag.adversity.niceblock.newmodel;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.newmodel.color.NiceColor;

import java.math.BigInteger;
import java.util.function.Function;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class ModelState
{
    static final String TAG_NAME = "adMdlSt";
    
//    public static final int MAX_COLOR_INDEX = 0x03FF;
//    protected static final int COLOR_INDEX_BITLENGTH = BigInteger.valueOf(MAX_COLOR_INDEX).bitLength();

//    protected short shapeIndex;
//    protected short colorIndex;
    protected int[] clientState = new int[1];
    protected int[] serverState = new int[1];
    
    protected final int SS_COLOR_INDEX = 0;
    protected final int CS_SHAPE_INDEX = 0;
    
    
    public void writeToNBT(NBTTagCompound tag) 
    {
 //       tag.setInteger(TAG_NAME, (shapeIndex << COLOR_INDEX_BITLENGTH) | (colorIndex & MAX_COLOR_INDEX));
        tag.setIntArray(TAG_NAME, serverState);
    }
    
    public void readFromNBT(NBTTagCompound tag) 
    {
        serverState = tag.getIntArray(TAG_NAME);
//        int tagValue = tag.getInteger(TAG_NAME);
//        colorIndex = tagValue & MAX_COLOR_INDEX;
//        shapeIndex = tagValue << COLOR_INDEX_BITLENGTH;
    }
    
    /** useful for creating item stacks */
    public NBTTagCompound getNBT()
    {
        NBTTagCompound tag = new NBTTagCompound();
        this.writeToNBT(tag);
        return tag;
    }
    
    public ModelState(NBTTagCompound tag)
    {
        this();
        readFromNBT(tag);
    }
    
    public ModelState()
    {
        this(0, 0, 0);
    }
     
    public ModelState(int shapeIndex, int colorIndex)
    {
        this(shapeIndex, colorIndex, 0);
    }
    
    public ModelState(int shapeIndex, int colorIndex, int species)
    {
        clientState[CS_SHAPE_INDEX] = shapeIndex;
        serverState[SS_COLOR_INDEX] = colorIndex;
    }
    
     public void setColorIndex(int colorIndex)
    {
         serverState[SS_COLOR_INDEX] = colorIndex;
    }
    
    public int getColorIndex()
    {
        return serverState[SS_COLOR_INDEX];
    }
    
    public void setClientShapeIndex(int shapeIndex)
    {
        clientState[CS_SHAPE_INDEX] = shapeIndex;
    }
 
    public int getClientShapeIndex()
    {
        return clientState[CS_SHAPE_INDEX];
    }
}
