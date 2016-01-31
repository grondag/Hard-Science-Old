package grondag.adversity.niceblock.newmodel;

import java.util.function.Function;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public abstract class ModelState
{
    static final String TAG_NAME = "mdlIdx";
    
    public void writeToNBT(NBTTagCompound tag)
    {
        // default implementation does nothing
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
    
    public NiceColor getColor()
    {
        return NiceColor.values()[0];
    }
    
    public int getShapeIndex()
    {
        return 0;
    }
    
    public int getSpecies()
    {
        return 0;
    }
  
    
    public static class Color extends ModelState
    {
        protected final int modelIndex;
        
        /** use this when creating from block state */
        public Color(int shapeIndex, NiceColor color)
        {
            this.modelIndex =  (shapeIndex * NiceColor.values().length) + color.ordinal();
        }
        
        @Override
        public int getModelIndex()
        {
            return modelIndex;
        }
        
        @Override
        public NiceColor getColor()
        {
            return NiceColor.values()[modelIndex - (modelIndex / NiceColor.values().length)];
        }
        
        @Override
        public int getShapeIndex()
        {
            return modelIndex / NiceColor.values().length;
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
        
        public ColorSpecies(int shapeIndex, NiceColor color, int species)
        {
            super(shapeIndex, color);
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
