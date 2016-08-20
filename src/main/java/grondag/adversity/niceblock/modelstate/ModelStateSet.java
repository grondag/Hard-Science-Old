package grondag.adversity.niceblock.modelstate;

import java.util.ArrayList;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import grondag.adversity.library.IBlockTest;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public enum ModelStateSet
{
    TEST(ModelStateGroup.INNER_SHAREDCOLOR_TEX4_ROTATE, ModelStateGroup.OUTER_CJ_SHAREDCOLOR_TEX2);
    public static final int NOT_PRESENT = -1;

    private final int[] typeIndexes = new int[ModelStateComponents.MODEL_STATE_COMPONENTS.length];
    private final int[] shiftBits = new int[ModelStateComponents.MODEL_STATE_COMPONENTS.length];
    private final ModelStateComponent<?,?>[] includedTypes;
    private final int typeCount;
    private final ModelStateGroup[] groups;
    private final int groupIndexes[] = new int[ModelStateGroup.values().length];
    
    private LoadingCache<Long, ModelStateSetValue> valueCache = CacheBuilder.newBuilder().maximumSize(0xFFFF).build(new CacheLoader<Long, ModelStateSetValue>()
    {
        @Override
        public ModelStateSetValue load(Long key)
        {
            ArrayList<IModelStateValue<?,?>> parts = new ArrayList<>(typeCount);
            for(ModelStateComponent<?,?> c : includedTypes)
            {
                parts.add(c.createValueFromBits((key >> shiftBits[c.getOrdinal()]) & c.getBitMask())); 
            }
            return new ModelStateSetValue(parts.toArray(null)); 
        }
    });
    
    private ModelStateSet(ModelStateGroup... groups)
    {
        // groups and group lookups used by SetValue to compute keys for groups
        this.groups = groups;
        for(int i = 0; i < ModelStateGroup.values().length; i++)
        {
            groupIndexes[i] = NOT_PRESENT;
        }
        int groupCounter = 0;
        for(ModelStateGroup g : groups)
        {
            if(groupIndexes[g.ordinal()] == NOT_PRESENT)
            {
                groupIndexes[g.ordinal()] = groupCounter++;
            }
        }
        
        //initialize lookup array for all component types to default that none are present
        for(int i = 0; i < ModelStateComponents.MODEL_STATE_COMPONENTS.length; i++)
        {
            typeIndexes[i] = NOT_PRESENT;
        }

        //for each group, update lookup array for all included components to show position within this set
        int componentCounter = 0;
        int shift = 0;
        for(ModelStateGroup g : groups)
        {
            for(ModelStateComponent<?,?> c : g.getComponents())
            {
                if(typeIndexes[c.getOrdinal()] == NOT_PRESENT)
                {
                    typeIndexes[c.getOrdinal()] = componentCounter++;
                    shiftBits[c.getOrdinal()] = shift;
                    shift += c.getBitLength();
                }
            }
        }
        
        //initialize smaller array to include only types that are part of one or more groups
        typeCount = componentCounter;
        this.includedTypes = new ModelStateComponent<?,?>[typeCount];
        for(int i = 0; i < ModelStateComponents.MODEL_STATE_COMPONENTS.length; i++)
        {
            if(typeIndexes[i] != NOT_PRESENT)
            {
                includedTypes[typeIndexes[i]] = ModelStateComponents.MODEL_STATE_COMPONENTS[i];
            }
        }
    }
    
    private int getIndexForType(ModelStateComponent<?,?> type)
    {
        return typeIndexes[type.getOrdinal()];
    }
    
    public long computeKey(IModelStateValue<?,?>... components)
    {
        long key = 0L;
        for(IModelStateValue<?,?> c : components)
        {
            int typeIndex = getIndexForType(c.getComponentType());
            if(typeIndex != NOT_PRESENT)
            {
                key |= (c.getBits() << shiftBits[typeIndex]);
            }
        }
        return key;
    }
    
    public ModelStateSetValue getSetValue(IModelStateValue<?,?>... components)
    {
        return valueCache.getUnchecked(computeKey(components));
    }
    
    public ModelStateSetValue getSetValueFromWorld(NiceBlock block, IBlockTest test, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        long bits = 0L;
        for(ModelStateComponent<?,?> c : includedTypes)
        {
            bits |= (c.getBitsFromWorld(block, test, state, world, pos) << shiftBits[c.getOrdinal()]);
        }
        return valueCache.getUnchecked(bits);
    }

    public ModelStateSetValue getSetValueFromBits(long bits)
    {
        return valueCache.getUnchecked(bits);
    }
    
    public class ModelStateSetValue
    {
        private final IModelStateValue<?,?>[] values;
        private final long key;
        private final long[]groupKeys;
        
        private ModelStateSetValue(IModelStateValue<?,?>... valuesIn)
        {
            values = new IModelStateValue<?,?>[typeCount];
            for(IModelStateValue<?,?> v : valuesIn)
            {
                int index = getIndexForType(v.getComponentType());
                values[index] = v;
            }
            key = computeKey(valuesIn);
            
            //pre-compute group keys
            groupKeys = new long[groups.length];
            for(int i = 0; i < groups.length; i++)
            {
                IModelStateValue<?,?>[] subValues = new IModelStateValue<?,?>[groups[i].getComponents().length];
                int valueCounter = 0;
                for(ModelStateComponent<?,?> c : groups[i].getComponents())
                {
                    subValues[valueCounter++] = values[getIndexForType(c)];
                }
                groupKeys[i] = computeKey(subValues);
            }
        }
        
        public <T extends IModelStateValue<T, V>, V> V getValue(ModelStateComponent<T, V> type)
        {
            int index = getIndexForType(type);
            if(index == ModelStateSet.NOT_PRESENT) return null;
            return type.getValueType().cast(values[index].getValue());
        }
        
        /**
         * Unique ID encapsulating entire state.
         */
        public long getKey()
        {
            return key;
        }
        
        /** 
         * Unique ID for a subset of components (group).
         * Used to lookup baked models that rely on a subset of entire state.
         */
        public long getGroupKey(ModelStateGroup group)
        {
            return groupKeys[groupIndexes[group.ordinal()]];
        }
    }
}