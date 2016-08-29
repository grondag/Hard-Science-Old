package grondag.adversity.niceblock.modelstate;

import grondag.adversity.library.IBlockTestFactory;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.library.joinstate.SimpleJoin;
import grondag.adversity.niceblock.base.NiceBlock2;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModelSimpleJoinComponent extends ModelStateComponent<ModelSimpleJoinComponent.ModelSimpleJoin, SimpleJoin>
{
    private final IBlockTestFactory blockTestFactory;
    
    public ModelSimpleJoinComponent(int ordinal, IBlockTestFactory blockTestFactory)
    {
        super(ordinal, WorldRefreshType.CACHED, SimpleJoin.STATE_COUNT);
        this.blockTestFactory = blockTestFactory;
    }

    @Override
    public ModelSimpleJoinComponent.ModelSimpleJoin createValueFromBits(long bits)
    {
        return new ModelSimpleJoinComponent.ModelSimpleJoin(new SimpleJoin((int) bits));
    }

    @Override
    public Class<ModelSimpleJoinComponent.ModelSimpleJoin> getStateType()
    {
        return ModelSimpleJoinComponent.ModelSimpleJoin.class;
    }

    @Override
    public Class<SimpleJoin> getValueType()
    {
        return SimpleJoin.class;
    }
    
    @Override
    public long getBitsFromWorld(NiceBlock2 block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        NeighborTestResults tests = new NeighborBlocks(world, pos).getNeighborTestResults(blockTestFactory.makeTest(world, state, pos));
        return SimpleJoin.getIndex(tests);
    }
    
    public class ModelSimpleJoin extends ModelStateValue<ModelSimpleJoinComponent.ModelSimpleJoin, SimpleJoin>
    {
        private ModelSimpleJoin(SimpleJoin valueIn)
        {
            super(valueIn);
        }

        @Override
        public ModelStateComponent<ModelSimpleJoinComponent.ModelSimpleJoin, SimpleJoin> getComponent()
        {
            return ModelSimpleJoinComponent.this;
        }
    
        @Override
        public long getBits()
        {
            return this.value.getIndex();
        }
    }
}