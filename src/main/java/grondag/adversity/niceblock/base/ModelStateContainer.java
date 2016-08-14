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
//        protected final T value;
//        
//        public ModelStateComponent(T valueIn)
//        {
//            this.value = valueIn;
//        }
//        
//
//        public T getValue()
//        {
//            return this.value;
//        }
        protected final ModelStateComponentType componentType;
        
        protected ModelStateComponent(ModelStateComponentType type)
        {
            this.componentType = type;
        }
        
        abstract protected Class<T> getType();
        abstract public int getBitLength();
        abstract public long toBits(T value);
        abstract protected T getValueFromWorld(NiceBlock block, IBlockTest test, IBlockState state, IBlockAccess world, BlockPos pos);
        abstract protected T getValueFromBits(long bits);
        public ModelStateComponentType getComponentType() { return this.componentType; }
    }
    
    public enum ModelStateComponentType
    {
        AXIS(ModelAxis.INSTANCE),
        CORNER_JOIN(ModelCornerJoinState.INSTANCE);
        
        private final ModelStateComponent<?> instance;

        private ModelStateComponentType(ModelStateComponent<?> instance)
        {
            this.instance = instance;
        }
        
        public ModelStateComponent<?> getInstance()
        {
            return this.instance;
        }
    }
    
    public static class ModelAxis extends ModelStateComponent<EnumFacing.Axis>
    {
        public static final ModelAxis INSTANCE = new ModelAxis(ModelStateComponentType.AXIS);

        private ModelAxis(ModelStateComponentType type)
        {
            super(type);
        }
        
        @Override
        public ModelStateComponentType getComponentType()
        {
            return ModelStateComponentType.AXIS;
        }
        
//        public ModelAxis(Axis valueIn)
//        {
//            super(valueIn);
//        }

        @Override
        public int getBitLength()
        {
            return 2;
        }

        @Override
        public long toBits(EnumFacing.Axis value)
        {
            return value.ordinal();
        }

        @Override
        protected Axis getValueFromWorld(NiceBlock block, IBlockTest test, IBlockState state, IBlockAccess world, BlockPos pos)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        protected Axis getValueFromBits(long bits)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        protected Class<Axis> getType()
        {
            return EnumFacing.Axis.class;
        }


    }
    

    public static class ModelCornerJoinState extends ModelStateComponent<CornerJoinBlockState>
    {
        public static final ModelCornerJoinState INSTANCE = new ModelCornerJoinState(ModelStateComponentType.CORNER_JOIN);
        
        private static final int CORNER_JOIN_BIT_LENGTH = Integer.SIZE - Integer.numberOfLeadingZeros(CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT);

        private ModelCornerJoinState(ModelStateComponentType type)
        {
            super(type);
        }

        @Override
        public ModelStateComponentType getComponentType()
        {
            return ModelStateComponentType.CORNER_JOIN;
        }
        
//        public ModelCornerJoinState(CornerJoinBlockState valueIn)
//        {
//            super(valueIn);
//            // TODO Auto-generated constructor stub
//        }

        @Override
        public int getBitLength()
        {
            return CORNER_JOIN_BIT_LENGTH;
        }

        @Override
        public long toBits(CornerJoinBlockState value)
        {
            return value.getIndex();
        }

        @Override
        protected CornerJoinBlockState getValueFromWorld(NiceBlock block, IBlockTest test, IBlockState state, IBlockAccess world, BlockPos pos)
        {
            NeighborTestResults tests = new NeighborBlocks(world, pos).getNeighborTestResults(test);
            return CornerJoinBlockStateSelector.getJoinState(CornerJoinBlockStateSelector.findIndex(tests));
        }

        @Override
        protected CornerJoinBlockState getValueFromBits(long bits)
        {
            return CornerJoinBlockStateSelector.getJoinState((int) bits);
        }

        @Override
        protected Class<CornerJoinBlockState> getType()
        {
            return CornerJoinBlockState.class;
        }
        
    }
    
    public static class ModelStateSet
    {
        private final int[] typeIndexes = new int[ModelStateComponentType.values().length];
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
            for(ModelStateComponentType c : components)
            {
                typeIndexes[c.ordinal()] = counter++;
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
    }
    
    public static class ModelStateSetValue
    {
        private final ModelStateSet stateSet;
        private final Object[] values;
        
        public ModelStateSetValue(ModelStateSet stateSet)
        {
            this.stateSet = stateSet;
            values = new Object[stateSet.typeCount];
        }
        
        public <V>V getValue(ModelStateComponent<V> type)
        {
            int index = stateSet.getIndexForType(type.getComponentType());
            if(index == ModelStateSet.NOT_PRESENT) return null;
            return type.getType().cast(values[index]);
        }
        
        public long getKey()
        {
            return 0L;
        }
        
        public long getSubKey(ModelStateSet SubSet)
        {
            return 0L;
        }
    }
    
    public static void testDongle()
    {
        ModelStateSet set = new ModelStateSet(ModelStateComponentType.AXIS, ModelStateComponentType.CORNER_JOIN);
        ModelStateSetValue value = new ModelStateSetValue(set);
        EnumFacing.Axis axis = value.getValue(ModelAxis.INSTANCE);
    }
}
