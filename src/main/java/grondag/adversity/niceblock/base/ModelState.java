package grondag.adversity.niceblock.base;

import java.math.BigInteger;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;

public class ModelState
{
    static final String TAG_NAME = "adMdlSt";
    public static final int MAX_COLOR_INDEX = 0x03FF;
    private static final int COLOR_INDEX_BITLENGTH = BigInteger.valueOf(MAX_COLOR_INDEX).bitLength();
   
    private int colorIndex = 0;
    private long[] clientShapeIndex = new long[BlockRenderLayer.values().length];
    private long serverShapeIndex = 0;
    
    public void writeToNBT(NBTTagCompound tag) 
    {
        tag.setLong(TAG_NAME, (serverShapeIndex << COLOR_INDEX_BITLENGTH) | (colorIndex & MAX_COLOR_INDEX));
    }
    
    public ModelState readFromNBT(NBTTagCompound tag) 
    {
        long tagValue = tag.getLong(TAG_NAME);
        colorIndex = (int) (tagValue & MAX_COLOR_INDEX);
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
    
    public ModelState(long serverShapeIndex, int colorIndex)
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
    
    public ModelState setServerShapeIndex(long serverShapeIndex)
    {
         this.serverShapeIndex = serverShapeIndex;
         return this;
    }
    
    public long getServerShapeIndex()
    {
        return serverShapeIndex;
    }
    
    public ModelState setClientShapeIndex(long shapeIndex, int layer)
    {
        clientShapeIndex[layer] = shapeIndex;
        return this;
    }
 
    public long getClientShapeIndex(int layer)
    {
        return clientShapeIndex[layer];
    }
}
