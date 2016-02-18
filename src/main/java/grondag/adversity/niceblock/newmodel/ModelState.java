package grondag.adversity.niceblock.newmodel;

import grondag.adversity.niceblock.newmodel.color.NiceColor;

import java.math.BigInteger;
import java.util.function.Function;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class ModelState
{
    static final String TAG_NAME = "adMdlSt";
    
    public static final int MAX_COLOR_INDEX = 0x03FF;
//    public static final int MAX_SPECIES = 0xF;
    protected static final int COLOR_INDEX_BITLENGTH = BigInteger.valueOf(MAX_COLOR_INDEX).bitLength();

    protected int colorIndex;
    protected int shapeIndex;
//    protected int species;

    public void writeToNBT(NBTTagCompound tag) 
    {
        tag.setInteger(TAG_NAME, (shapeIndex << COLOR_INDEX_BITLENGTH) | (colorIndex & MAX_COLOR_INDEX));
    }
    
    public void readFromNBT(NBTTagCompound tag) 
    {
        int tagValue = tag.getInteger(TAG_NAME);
        colorIndex = tagValue & MAX_COLOR_INDEX;
        shapeIndex = tagValue << COLOR_INDEX_BITLENGTH;
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
        this.shapeIndex = shapeIndex;
        this.colorIndex = colorIndex;
//        this.species = species;
    }
    
     public void setColorIndex(int colorIndex)
    {
        this.colorIndex = colorIndex;
    }
    
    public int getColorIndex()
    {
        return colorIndex;
    }
    
    public void setShapeIndex(int shapeIndex)
    {
        this.shapeIndex = shapeIndex;
    }
 
    public int getShapeIndex()
    {
        return shapeIndex;
    }
    
//    public int getSpecies()
//    {
//        return species;
//    }
//    
//    public void setSpecies(int species)
//    {
//        this.species = species;
//    }
}
