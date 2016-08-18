package grondag.adversity.niceblock.modelstate;

import grondag.adversity.library.IBlockTest;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModelStateSet
{
    private final int[] typeIndexes = new int[ModelStateComponentType.values().length];
    private final int[] shiftBits = new int[ModelStateComponentType.values().length];
    final int typeCount;
    public static final int NOT_PRESENT = -1;
    
    public ModelStateSet(ModelStateComponentType... components)
    {
        typeCount = components.length;
        
        for(int i = 0; i < ModelStateComponentType.values().length; i++)
        {
            typeIndexes[i] = NOT_PRESENT;
        }
        
        int counter = 0;
        int shift = 0;
        for(ModelStateComponentType c : components)
        {
            typeIndexes[c.ordinal()] = counter++;

            shiftBits[c.ordinal()] = shift;
            shift += c.getBitLength();
        }
    }
    
    public int getTypeCount()
    {
        return typeCount;
    }
    
    public int getIndexForType(ModelStateComponentType type)
    {
        return typeIndexes[type.ordinal()];
    }
    
    public long computeKey(IModelStateComponent<?>... components)
    {
        long key = 0L;
        for(IModelStateComponent<?> c : components)
        {
            int typeIndex = getIndexForType(c.getComponentType());
            if(typeIndex != NOT_PRESENT)
            {
                key |= (c.getBits() << shiftBits[typeIndex]);
            }
        }
        return key;
    }
    
    public ModelStateSetValue getSetValue(IModelStateComponent<?>... components)
    {
        return new ModelStateSetValue(components);
    }
    
    public ModelStateSetValue getSetValueFromWorld(NiceBlock block, IBlockTest test, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        //TODO
        return null;
        //return new ModelStateSetValue(components);
    }

    public ModelStateSetValue getSetValueFromBits(long bits)
    {
        //TODO
        return null;
        //return new ModelStateSetValue(components);
    }

    public class ModelStateSetValue
    {
      //  private final ModelStateSet stateSet;
        private final Object[] values;
        private final long key;
        
        private ModelStateSetValue(IModelStateComponent<?>... components)
        {
   //         this.stateSet = stateSet;
            values = new Object[typeCount];
            for(IModelStateComponent<?> c : components)
            {
                int index = getIndexForType(c.getComponentType());
                values[index] = c;
            }
            key = computeKey(components);
        }
        
        public IModelStateComponent<?> getValue(ModelStateComponentType type)
        {
            int index = getIndexForType(type);
            if(index == ModelStateSet.NOT_PRESENT) return null;
            return type.getAdapter().getType().cast(values[index]);
        }
        
        public long getKey()
        {
            return key;
        }
    }
}