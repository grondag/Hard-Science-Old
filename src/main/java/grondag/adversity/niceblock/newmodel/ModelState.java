package grondag.adversity.niceblock.newmodel;

import grondag.adversity.niceblock.newmodel.color.NiceColor;

import java.math.BigInteger;
import java.util.function.Function;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public abstract class ModelState
{
    static final String TAG_NAME = "mdlIdx";
    
    public static final int MAX_COLOR_INDEX = 0x03FF;
    protected static final int COLOR_INDEX_BITLENGTH = BigInteger.valueOf(MAX_COLOR_INDEX).bitLength();
    
    public void writeToNBT(NBTTagCompound tag)
    {
        //default implementation does nothing
    };
    
    public  int getMeta()
    {
        return 0;
    }
    
    public ModelState(NBTTagCompound tag, int meta)
    {
    }
    
    public ModelState()
    {
    }
    
    public int getModelIndex()
    {
        return 0;
    }
    
    public int getColorIndex()
    {
        return 0;
    }
    
    public abstract int getShapeIndex();
    
    public int getSpecies()
    {
        return 0;
    }
  
    
    public static class Color extends ModelState
    {
        protected final int modelIndex;
        
        /** use this when creating from block state */
        public Color(int shapeIndex, int colorIndex)
        {
            this.modelIndex =  (shapeIndex << COLOR_INDEX_BITLENGTH) | (colorIndex & MAX_COLOR_INDEX);
        }
        
        @Override
        public int getModelIndex()
        {
            return modelIndex;
        }
        
        @Override
        public int getColorIndex()
        {
            return modelIndex & MAX_COLOR_INDEX;
        }
        
        @Override
        public int getShapeIndex()
        {
            return modelIndex >> COLOR_INDEX_BITLENGTH;
        }
        
        @Override
        public void writeToNBT(NBTTagCompound tag) 
        {
            tag.setInteger(TAG_NAME, modelIndex);
        }
        
        /** meta not used because color is always stored in modelIndex */
        public Color(NBTTagCompound tag, int meta) 
        {
            this.modelIndex = tag.getInteger(TAG_NAME);
        }
    }
    
    public static class ColorSpecies extends Color
    {
        protected final int species;
        
        public ColorSpecies(int shapeIndex, int colorIndex, int species)
        {
            super(shapeIndex, colorIndex);
            this.species = species;
        }
        
        @Override
        public int getSpecies(){
            return species;
        }
        
        public ColorSpecies(NBTTagCompound tag, int meta) 
        {
            super(tag, meta);
            this.species = meta;   
        }
        
    }
}
