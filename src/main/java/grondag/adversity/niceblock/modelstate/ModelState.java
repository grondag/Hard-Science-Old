package grondag.adversity.niceblock.modelstate;

import java.math.BigInteger;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;

public class ModelState
{
    static final String TAG_NAME = "adMdlSt";
    public static final int MAX_COLOR_INDEX = 0x03FF;
    private static final int COLOR_INDEX_BITLENGTH = BigInteger.valueOf(MAX_COLOR_INDEX).bitLength();
   
    private int colorIndex = 0;
    private long[] shapeIndex = new long[BlockRenderLayer.values().length];
    private int persistenceIndex = -1;
    
    public void writeToNBT(NBTTagCompound tag) 
    {
        if(persistenceIndex == -1)
        {
            tag.setLong(TAG_NAME, (colorIndex & MAX_COLOR_INDEX));
        }
        else
        {
            tag.setLong(TAG_NAME, (shapeIndex[persistenceIndex] << COLOR_INDEX_BITLENGTH) | (colorIndex & MAX_COLOR_INDEX));
        }
    }
    
    public ModelState readFromNBT(NBTTagCompound tag) 
    {
        long tagValue = tag.getLong(TAG_NAME);
        colorIndex = (int) (tagValue & MAX_COLOR_INDEX);
        if(persistenceIndex >= 0)
        {
            shapeIndex[persistenceIndex] = tagValue << COLOR_INDEX_BITLENGTH;
        }
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
        this(0);
    }

    public ModelState(NBTTagCompound tag)
    {
        readFromNBT(tag);
    }
    
    public ModelState(int colorIndex)
    {
        this.colorIndex = colorIndex;
//        this.serverShapeIndex = serverShapeIndex;
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
    
//    public ModelState setServerShapeIndex(long serverShapeIndex)
//    {
//         this.serverShapeIndex = serverShapeIndex;
//         return this;
//    }
//    
//    public long getServerShapeIndex()
//    {
//        return serverShapeIndex;
//    }
    
    public ModelState setShapeIndex(long shapeIndex, BlockRenderLayer layer)
    {
        this.shapeIndex[layer.ordinal()] = shapeIndex;
        return this;
    }
 
    public long getShapeIndex(BlockRenderLayer layer)
    {
        return shapeIndex[layer.ordinal()];
    }
}
