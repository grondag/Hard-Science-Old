package grondag.adversity.niceblock.modelstate;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

import grondag.adversity.Adversity;
import grondag.adversity.library.cache.ILoadingCache;
import grondag.adversity.library.cache.ManagedLoadingCache;
import grondag.adversity.library.cache.SimpleCacheLoader;
import grondag.adversity.niceblock.base.NiceBlock;
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
    private final boolean usesWorldState;
    private final boolean noAlwaysRefresh;
    private final long persistenceMask;
    public final int bitLength;
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

    private final ILoadingCache<ModelStateSetValue> valueCache = new ManagedLoadingCache<ModelStateSetValue>(new StateCacheLoader(), 1024, 0xFFFF);
    
    private class StateCacheLoader implements SimpleCacheLoader<ModelStateSetValue>
    {
        @Override
        public ModelStateSetValue load(long key)
        {
            ArrayList<ModelStateValue<?,?>> parts = new ArrayList<>(typeCount);
            for(ModelStateComponent<?,?> c : includedTypes)
            {
                parts.add(c.createValueFromBits((key >> shiftBits[c.getOrdinal()]) & c.getBitMask())); 
            }
            return new ModelStateSetValue(key, parts); 
        }
    }
    
    private ModelStateSet(ModelStateGroup... groups)
    {

        
        //initialize lookup array for all component types to default that none are present
        for(int i = 0; i < ModelStateComponents.getCount(); i++)
        {
            typeIndexes[i] = NOT_PRESENT;
        }

        //for each group, update lookup array for all included components to show position within this set
        int componentCounter = 0;
        int shift = 0;
        long persistenceMask = 0;
        ModelColorMapComponent colorMap = null;
        ModelSpeciesComponent species = null;
        boolean canRefresh = false;
        boolean noAlwaysRefresh = true;

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
                    noAlwaysRefresh = noAlwaysRefresh && c.getRefreshType() != WorldRefreshType.ALWAYS;
                    
                    if(c.getRefreshType() == WorldRefreshType.NEVER)
                    {
                        persistenceMask |= c.getBitMask() << shiftBits[c.getOrdinal()];
                    }
                    
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
        this.noAlwaysRefresh = noAlwaysRefresh;
        this.persistenceMask = persistenceMask;
        this.bitLength = shift;
        
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

    public long getPersistenceMask() { return this.persistenceMask; }
    
    public ModelStateSetValue getValue(ModelStateValue<?,?>... components)
    {
        return valueCache.get(computeKey(components));
    }
    
    public ModelStateSetValue getValueWithUpdates(ModelStateSetValue valueIn, ModelStateValue<?,?>... components)
    {
        long mask = computeMask(components);
        long key = (valueIn.key & ~mask) | computeKey(components);
        return valueCache.get(key);
    }
    
    public boolean canRefreshFromWorld() { return this.usesWorldState; }
    
    public long getRefreshedKeyFromWorld(long startingKey, boolean refreshCache, NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
//        Adversity.log.info("getRefreshedKeyFromWorld ENTRY thread=" + Thread.currentThread().getName() + " pos=" + pos.toString());
 
        if(!this.usesWorldState || (!refreshCache && noAlwaysRefresh)) return startingKey;
        
//        Adversity.log.info("getRefreshedKeyFromWorld CONTINUE1 thread=" + Thread.currentThread().getName() + " pos=" + pos.toString());
        
        int refreshCutoffOrdinal = refreshCache ? WorldRefreshType.CACHED.ordinal() : WorldRefreshType.ALWAYS.ordinal(); 
        
//        Adversity.log.info("getRefreshedKeyFromWorld CONTINUE2 thread=" + Thread.currentThread().getName() + " pos=" + pos.toString());
        
        long bits = 0L;
        for(ModelStateComponent<?,?> c : includedTypes)
        {
            if(c.getRefreshType().ordinal() >= refreshCutoffOrdinal)
            {
//                Adversity.log.info("getRefreshedKeyFromWorld REFRESH component=" + c.getClass().getName() + " thread=" + Thread.currentThread().getName() + " pos=" + pos.toString());
                bits |= (c.getBitsFromWorld(block, state, world, pos) << shiftBits[c.getOrdinal()]);
            }
            else
                bits |= (startingKey & (c.getBitMask() << shiftBits[c.getOrdinal()]));
        }
        
//        Adversity.log.info("getRefreshedKeyFromWorld END before/after=" + startingKey + "/" + bits + " thread=" + Thread.currentThread().getName() + " pos=" + pos.toString());
        
        return bits;
    }

    /** provides fast match testing without instantiating two value objectes */
    public boolean doComponentValuesMatch(ModelStateComponent<?,?> c, long key1, long key2)
    {
        long mask = c.getBitMask() << shiftBits[c.getOrdinal()];
        return (key1 & mask) == (key2 & mask);
    }
    
    public ModelStateSetValue getSetValueFromKey(long bits)
    {
        ModelStateSetValue result = valueCache.get(bits);
        if(Adversity.DEBUG_MODE)
        {
            Adversity.LOG.info("Unable to retrieve model state set value. Should never happen.");
        }
        return result;
    }
    
    public class ModelStateSetValue
    {
        private final ModelStateValue<?,?>[] values;
        private final long key;
        
        private ModelStateSetValue(Long key, ArrayList<ModelStateValue<?,?>> valuesIn)
        {
            this.key = key;
            values = new ModelStateValue<?,?>[typeCount];
            for(ModelStateValue<?,?> v : valuesIn)
            {
                int index = typeIndexes[v.getComponent().getOrdinal()];
                values[index] = v;
            }
        }
        
        public <T extends ModelStateValue<T, V>, V> V getValue(ModelStateComponent<T, V> type)
        {
            int index = typeIndexes[type.getOrdinal()];
            if(index == ModelStateSet.NOT_PRESENT) return null;
            return type.getValueType().cast(values[index].getValue());
        }
        
        public <T extends ModelStateValue<T, V>, V> T getWrappedValue(ModelStateComponent<T, V> type)
        {
            int index = typeIndexes[type.getOrdinal()];
            if(index == ModelStateSet.NOT_PRESENT) return null;
            return type.getStateType().cast(values[index]);
        }
        
        /**
         * Unique ID encapsulating entire state.
         */
        public long getKey()
        {
            return key;
        }
    }
}