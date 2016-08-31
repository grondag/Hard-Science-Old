package grondag.adversity.niceblock.modelstate;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import grondag.adversity.niceblock.base.NiceBlock2;
import grondag.adversity.niceblock.modelstate.ModelStateComponent.WorldRefreshType;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModelStateSet
{
    private static HashMap<BitSet, ModelStateSet> stateSets = new HashMap<BitSet, ModelStateSet>();

    private static final int NOT_PRESENT = -1;

    private final int[] typeIndexes = new int[ModelStateComponents.getCount()];
    private final int[] shiftBits = new int[ModelStateComponents.getCount()];
    private final ModelStateComponent<?,?>[] includedTypes;
    private final int typeCount;
//    private final ModelStateGroup[] groups;
//    private final int[] groupIndexes;
    private final boolean usesWorldState;
    private final ModelColorMapComponent firstColorMapComponent;
    private final ModelSpeciesComponent firstSpeciesComponent;
    
    public static ModelStateSet find(ModelStateGroup... groups)
    {
        BitSet key = new BitSet();
        for(ModelStateGroup g : groups)
        {
            key.set(g.getOrdinal());
        }
        
        ModelStateSet result; 
        synchronized(stateSets)
        {
            result = stateSets.get(key);
            if(result == null)
            {
                result = new ModelStateSet(groups);
                stateSets.put(key, result);
            }
        }
        return result;
    }
    
    
    private LoadingCache<Long, ModelStateSetValue> valueCache = CacheBuilder.newBuilder().maximumSize(0xFFFF).build(new CacheLoader<Long, ModelStateSetValue>()
    {
        @Override
        public ModelStateSetValue load(Long key)
        {
            ArrayList<ModelStateValue<?,?>> parts = new ArrayList<>(typeCount);
            for(ModelStateComponent<?,?> c : includedTypes)
            {
                parts.add(c.createValueFromBits((key >> shiftBits[c.getOrdinal()]) & c.getBitMask())); 
            }
            return new ModelStateSetValue(key, parts); 
        }
    });
    
    private ModelStateSet(ModelStateGroup... groups)
    {
        // groups and group lookups used by SetValue to compute keys for groups
//        this.groups = groups;

        // NB: getGroupCount() *may* change if groups are added after this, but all groups
        // that are part of this set must, by definition, have been created before this call
        // and their ordinals will all be above the current max and should never be used
        // as parameters to this set.
//        this.groupIndexes = new int[ModelStateGroup.getGroupCount()];

//        for(int i = 0; i < groupIndexes.length; i++)
//        {
//            groupIndexes[i] = NOT_PRESENT;
//        }
//        int groupCounter = 0;
//        for(ModelStateGroup g : groups)
//        {
//            if(groupIndexes[g.getOrdinal()] == NOT_PRESENT)
//            {
//                groupIndexes[g.getOrdinal()] = groupCounter++;
//            }
//        }
        
        //initialize lookup array for all component types to default that none are present
        for(int i = 0; i < ModelStateComponents.getCount(); i++)
        {
            typeIndexes[i] = NOT_PRESENT;
        }

        //for each group, update lookup array for all included components to show position within this set
        int componentCounter = 0;
        int shift = 0;
        ModelColorMapComponent colorMap = null;
        ModelSpeciesComponent species = null;
        boolean canRefresh = false;
        for(ModelStateGroup g : groups)
        {
            for(ModelStateComponent<?,?> c : g.getComponents())
            {
                if(typeIndexes[c.getOrdinal()] == NOT_PRESENT)
                {
                    typeIndexes[c.getOrdinal()] = componentCounter++;
                    shiftBits[c.getOrdinal()] = shift;
                    shift += c.getBitLength();
                    canRefresh = canRefresh || c.getRefreshType() != WorldRefreshType.NEVER;
                    
                    if(colorMap == null && c instanceof ModelColorMapComponent)
                    {
                        colorMap = (ModelColorMapComponent) c;
                    }
                    if(species == null && c instanceof ModelSpeciesComponent)
                    {
                        species = (ModelSpeciesComponent) c;
                    }
                }
            }
        }
        this.firstColorMapComponent = colorMap;
        this.firstSpeciesComponent = species;
        this.usesWorldState = canRefresh;
        
        //initialize smaller array to include only types that are part of one or more groups
        typeCount = componentCounter;
        this.includedTypes = new ModelStateComponent<?,?>[typeCount];
        for(int i = 0; i < ModelStateComponents.getCount(); i++)
        {
            if(typeIndexes[i] != NOT_PRESENT)
            {
                includedTypes[typeIndexes[i]] = ModelStateComponents.get(i);
            }
        }
    }
    
//    private int getIndexForType(ModelStateComponent<?,?> type)
//    {
//        return typeIndexes[type.getOrdinal()];
//    }
    
    public long computeKey(ModelStateValue<?,?>... components)
    {
        long key = 0L;
        for(ModelStateValue<?,?> c : components)
        {
            int i = c.getComponent().getOrdinal();
            if(typeIndexes[i] != NOT_PRESENT)
            {
                key |= (c.getBits() << shiftBits[i]);
            }
        }
        return key;
    }
    
    public long computeMask(ModelStateValue<?,?>... components)
    {
        long mask = 0L;
        for(ModelStateValue<?,?> c : components)
        {
            int i = c.getComponent().getOrdinal();
            if(typeIndexes[i] != NOT_PRESENT)
            {
                mask |= (c.getComponent().getBitMask() << shiftBits[i]);
            }
        }
        return mask;
    }
    
    /** used for fast color matching */
    public ModelColorMapComponent getFirstColorMapComponent() { return this.firstColorMapComponent; }
    public ModelSpeciesComponent getFirstSpeciesComponent() { return this.firstSpeciesComponent; }

    
    public ModelStateSetValue getValue(ModelStateValue<?,?>... components)
    {
        return valueCache.getUnchecked(computeKey(components));
    }
    
    public ModelStateSetValue getValueWithUpdates(ModelStateSetValue valueIn, ModelStateValue<?,?>... components)
    {
        long mask = computeMask(components);
        long key = (valueIn.key & mask) | computeKey(components);
        return valueCache.getUnchecked(key);
    }
    
    public boolean canRefreshFromWorld() { return this.usesWorldState; }
    
    public long getRefreshedKeyFromWorld(long startingKey, boolean refreshCache, NiceBlock2 block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        if(!this.usesWorldState) return startingKey;
        
        int refreshCutoffOrdinal = refreshCache ? WorldRefreshType.CACHED.ordinal() : WorldRefreshType.ALWAYS.ordinal(); 
        
        long bits = 0L;
        for(ModelStateComponent<?,?> c : includedTypes)
        {
            if(c.getRefreshType().ordinal() >= refreshCutoffOrdinal)
                bits |= (c.getBitsFromWorld(block, state, world, pos) << shiftBits[c.getOrdinal()]);
            else
                bits |= (startingKey & (c.getBitMask() << shiftBits[c.getOrdinal()]));
        }
        return bits;
    }

    /** provides fast match testing without instantiating two value objectes */
    public boolean doComponentValuesMatch(ModelStateComponent<?,?> c, long key1, long key2)
    {
        long mask = c.getBitMask() << shiftBits[c.getOrdinal()];
        return (key1 & mask) == (key2 & mask);
    }
    
    public ModelStateSetValue getSetValueFromBits(long bits)
    {
        return valueCache.getUnchecked(bits);
    }
    
    public class ModelStateSetValue
    {
        private final ModelStateValue<?,?>[] values;
        private final long key;
//        private final long[]groupKeys;
        
        private ModelStateSetValue(Long key, ArrayList<ModelStateValue<?,?>> valuesIn)
        {
            this.key = key;
            values = new ModelStateValue<?,?>[typeCount];
            for(ModelStateValue<?,?> v : valuesIn)
            {
                int index = typeIndexes[v.getComponent().getOrdinal()];
                values[index] = v;
            }
            
//            //pre-compute group keys
//            groupKeys = new long[groups.length];
//            for(int i = 0; i < groups.length; i++)
//            {
//                ModelStateValue<?,?>[] subValues = new ModelStateValue<?,?>[groups[i].getComponents().length];
//                int valueCounter = 0;
//                for(ModelStateComponent<?,?> c : groups[i].getComponents())
//                {
//                    subValues[valueCounter++] = values[getIndexForType(c)];
//                }
//                groupKeys[i] = computeKey(subValues);
//            }
        }
        
        public <T extends ModelStateValue<T, V>, V> V getValue(ModelStateComponent<T, V> type)
        {
            int index = typeIndexes[type.getOrdinal()];
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
        
//        /** 
//         * Unique ID for a subset of components (group).
//         * Used to lookup baked models that rely on a subset of entire state.
//         */
//        public long getGroupKey(ModelStateGroup group)
//        {
//            return groupKeys[groupIndexes[group.getOrdinal()]];
//        }
    }
}