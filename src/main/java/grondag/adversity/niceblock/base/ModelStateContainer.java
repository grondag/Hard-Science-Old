package grondag.adversity.niceblock.base;

import java.util.EnumMap;
import java.util.EnumSet;

import grondag.adversity.library.IBlockTest;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.library.joinstate.CornerJoinBlockState;
import grondag.adversity.library.joinstate.CornerJoinBlockStateSelector;
import grondag.adversity.niceblock.support.BlockTests;
import grondag.adversity.niceblock.support.BlockTests.TestForBigBlockMatch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * axis
 * alternate texture index - could be different for diff layers
 * corner join state
 * simple join state
 * flow join state
 * height state
 * big texture model index
 * simple alternate index (hot basalt, for example)
 * base color
 * glow color
 * border color
 * highlight color
 * primitive offset

 * underlying primitive (probably determined by the block)
 * 
 * 
 * views of model state
 * controller - will use a subset, but can have access to whole state
 * dispatcher - needs a cache key based on visual appearance - get from state or controller?
 * niceblock/plus - needs to persist the parts of the state that should be persisted
 *         - can vary based on type of block with same controller (flowing vs. static lava)
 *         - persistence options are meta, world-derived, NBT, cached)
 * multipart/CSG - will have need to persist full state to NBT even if originally not
 * StateProvider - obtains state instance from a key or from appropriate persistence locale
 * 
 * column - axis (meta), color, altTex, cornerJoin
 * bigTex - color, metaVariant (optional), bigTex index
 * border - color, altTex*2, cornerJoin, (meta used to derive cornerjoin but not part of state)
 * color - color, altTex
 * flow - altTex, flowState (meta is used/implied by flowState), color?
 * height - altTex, height (meta), color?
 * masonry - color, altTex*2, simpleJoin
 * cylinder - color, offset, radius, length, cornerOrCenter
 * 
 * some blocks could have different alternate textures per controller
 */
public class ModelStateContainer
{

    public class ModelStateProvider
    {
        
    }
    
//    public enum ModelStateType
//    {
//        THING1
//        {
//            {
//                this.wut();
//            }
//        },
//        THING2
//        {
//            
//        };
//        
//        public void wut()
//        {
//            
//        }
//    }
    
    public abstract static class ModelStateComponent<T>
    {
        protected final T value;
        protected final ModelStateComponentType componentType;

        
        public ModelStateComponent(ModelStateComponentType type, T valueIn)
        {
            this.value = valueIn;
            this.componentType = type;
        }
        

        public T getValue()
        {
            return this.value;
        }
                
        abstract protected Class<T> getType();
        abstract public long toBits();
        public ModelStateComponentType getComponentType() { return this.componentType; }
    }
    
    public abstract static class ModelStateComponentFactory<T>
    {
        abstract public ModelStateComponent<T> getStateFromWorld(NiceBlock block, IBlockTest test, IBlockState state, IBlockAccess world, BlockPos pos);
        abstract public ModelStateComponent<T> getStateFromBits(long bits);
    }
    
    public enum ModelStateComponentType
    {
        AXIS(ModelAxis.KEY, new ModelAxisFactory(), 2),
        CORNER_JOIN(ModelCornerJoinState.KEY, new ModelCornerJoinStateFactory(),
                Integer.SIZE - Integer.numberOfLeadingZeros(CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT));
        
        private final ModelStateComponent<?> instance;
        private final ModelStateComponentFactory<?> factory;
        protected final int bitLength;
        protected final long bitMask;
        
        private ModelStateComponentType(ModelStateComponent<?> instance, ModelStateComponentFactory<?> factory, int bitLength)
        {
            this.instance = instance;
            this.factory = factory;
            this.bitLength = bitLength;
            long mask = 0L;
            for(int i = 0; i < bitLength; i++)
            {
                mask |= (1L << i);
            }
            this.bitMask = mask;
        }
        
        public ModelStateComponent<?> getInstance() { return this.instance; }
        public ModelStateComponentFactory<?> getFactory() { return this.factory; }
        public int getBitLength() { return bitLength; }
        public long getBitMask() { return bitMask; }
    }
    
    public static class ModelAxis extends ModelStateComponent<EnumFacing.Axis>
    {
        public static final ModelAxis KEY = new ModelAxis(null);

        public ModelAxis(Axis valueIn)
        {
            super(ModelStateComponentType.AXIS, valueIn);
        }

        @Override
        public long toBits()
        {
            return this.value.ordinal();
        }

        @Override
        protected Class<Axis> getType()
        {
            return null;
        }

    }
    
    public static class ModelAxisFactory extends ModelStateComponentFactory<EnumFacing.Axis>
    {

        @Override
        public ModelAxis getStateFromWorld(NiceBlock block, IBlockTest test, IBlockState state, IBlockAccess world, BlockPos pos)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ModelAxis getStateFromBits(long bits)
        {
            // TODO Auto-generated method stub
            return null;
        }
        
    }
    
    public static class ModelCornerJoinState extends ModelStateComponent<CornerJoinBlockState>
    {
        public static final ModelCornerJoinState KEY = new ModelCornerJoinState(null);
        
        public ModelCornerJoinState(CornerJoinBlockState valueIn)
        {
            super(ModelStateComponentType.CORNER_JOIN, valueIn);
        }

        @Override
        public long toBits()
        {
            return this.value.getIndex();
        }

        @Override
        protected Class<CornerJoinBlockState> getType()
        {
            return CornerJoinBlockState.class;
        }
        
    }
    
    public static class ModelCornerJoinStateFactory extends ModelStateComponentFactory<CornerJoinBlockState>
    {
        @Override
        public ModelCornerJoinState getStateFromWorld(NiceBlock block, IBlockTest test, IBlockState state, IBlockAccess world, BlockPos pos)
        {
            NeighborTestResults tests = new NeighborBlocks(world, pos).getNeighborTestResults(test);
            return new ModelCornerJoinState(CornerJoinBlockStateSelector.getJoinState(CornerJoinBlockStateSelector.findIndex(tests)));
        }

        @Override
        public ModelCornerJoinState getStateFromBits(long bits)
        {
            return new ModelCornerJoinState(CornerJoinBlockStateSelector.getJoinState((int) bits));
        }
    }

    public static class ModelStateSet
    {
        private final int[] typeIndexes = new int[ModelStateComponentType.values().length];
        private final int[] shiftBits = new int[ModelStateComponentType.values().length];
        private final int typeCount;
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
        
        public int getBitShiftForType(ModelStateComponentType type)
        {
            return shiftBits[typeIndexes[type.ordinal()]];
        }
        
        public long computeKey(ModelStateComponent<?>... components)
        {
            long key = 0L;
            for(ModelStateComponent<?> c : components)
            {
                if(getIndexForType(c.getComponentType()) != NOT_PRESENT)
                {
                    key |= (c.toBits() << getBitShiftForType(c.getComponentType()));
                }
            }
            return key;
        }
    }
    
    public static class ModelStateSetValue
    {
        private final ModelStateSet stateSet;
        private final Object[] values;
        private final long key;
        
        public ModelStateSetValue(ModelStateSet stateSet, ModelStateComponent<?>... components)
        {
            this.stateSet = stateSet;
            values = new Object[stateSet.typeCount];
            for(ModelStateComponent<?> c : components)
            {
                int index = stateSet.getIndexForType(c.getComponentType());
                values[index] = c;
            }
            key = stateSet.computeKey(components);
        }
        
        public <V>V getValue(ModelStateComponent<V> type)
        {
            int index = stateSet.getIndexForType(type.getComponentType());
            if(index == ModelStateSet.NOT_PRESENT) return null;
            return type.getType().cast(values[index]);
        }

        public long getKey()
        {
            return key;
        }
    }
    
    public static void testDongle()
    {
        ModelStateSet set = new ModelStateSet(ModelStateComponentType.AXIS, ModelStateComponentType.CORNER_JOIN);
        ModelStateSetValue value = new ModelStateSetValue(set);
        EnumFacing.Axis axis = value.getValue(ModelAxis.KEY);
    }
}
