package grondag.adversity.niceblock.newmodel;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.newmodel.color.NiceColor;

import java.math.BigInteger;
import java.util.function.Function;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;

public class ModelState
{
    static final String TAG_NAME = "adMdlSt";
    public static final int MAX_COLOR_INDEX = 0x03FF;
    private static final int COLOR_INDEX_BITLENGTH = BigInteger.valueOf(MAX_COLOR_INDEX).bitLength();
   
    protected int colorIndex = 0;
    protected int[] clientShapeIndex = new int[BlockRenderLayer.values().length];
    protected int serverShapeIndex = 0;
    
    public void writeToNBT(NBTTagCompound tag) 
    {
        tag.setInteger(TAG_NAME, (serverShapeIndex << COLOR_INDEX_BITLENGTH) | (colorIndex & MAX_COLOR_INDEX));
    }
    
    public ModelState readFromNBT(NBTTagCompound tag) 
    {
        int tagValue = tag.getInteger(TAG_NAME);
        colorIndex = tagValue & MAX_COLOR_INDEX;
        serverShapeIndex = tagValue << COLOR_INDEX_BITLENGTH;
        return this;
    }
    
    /** useful for creating item stacks */
    public NBTTagCompound getNBT()
    {
        NBTTagCompound tag = new NBTTagCompound();
        this.writeToNBT(tag);
        return tag;
    }
    
    public ModelState()
    {
        this(0, 0);
    }

    public ModelState(NBTTagCompound tag)
    {
        readFromNBT(tag);
    }
    
    public ModelState(int serverShapeIndex, int colorIndex)
    {
        this.colorIndex = colorIndex;
        this.serverShapeIndex = serverShapeIndex;
    }
    
    public ModelState setColorIndex(int colorIndex)
    {
         this.colorIndex = colorIndex;
         return this;
    }
    
    public int getColorIndex()
    {
        return colorIndex;
    }
    
    public ModelState setServerShapeIndex(int serverShapeIndex)
    {
         this.serverShapeIndex = serverShapeIndex;
         return this;
    }
    
    public int getServerShapeIndex()
    {
        return serverShapeIndex;
    }
    
    public ModelState setClientShapeIndex(int shapeIndex, int layer)
    {
        clientShapeIndex[layer] = shapeIndex;
        return this;
    }
 
    public int getClientShapeIndex(int layer)
    {
        return clientShapeIndex[layer];
    }
}
