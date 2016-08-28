package grondag.adversity.niceblock.modelstate;

import grondag.adversity.library.IBlockTest;
import grondag.adversity.library.IBlockTestFactory;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.library.joinstate.CornerJoinBlockState;
import grondag.adversity.library.joinstate.CornerJoinBlockStateSelector;
import grondag.adversity.niceblock.base.NiceBlock2;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModelCornerJoinComponent extends ModelStateComponent<ModelCornerJoinComponent.ModelCornerJoin, CornerJoinBlockState>
{
    private final IBlockTestFactory blockTestFactory;
    
    public ModelCornerJoinComponent(int ordinal, IBlockTestFactory blockTestFactory)
    {
        super(ordinal, WorldRefreshType.SOMETIMES, CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT);
        this.blockTestFactory = blockTestFactory;
    }

    @Override
    public long getBitsFromWorld(NiceBlock2 block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        NeighborTestResults tests = new NeighborBlocks(world, pos).getNeighborTestResults(blockTestFactory.makeTest(world, state, pos));
        return CornerJoinBlockStateSelector.findIndex(tests);
    }

    @Override
    public ModelCornerJoin createValueFromBits(long bits)
    {
        return new ModelCornerJoin(CornerJoinBlockStateSelector.getJoinState((int) bits));
    }

    @Override
    public Class<ModelCornerJoinComponent.ModelCornerJoin> getStateType()
    {
        return ModelCornerJoinComponent.ModelCornerJoin.class;
    }

    @Override
    public Class<CornerJoinBlockState> getValueType()
    {
        return CornerJoinBlockState.class;
    }
    
    public class ModelCornerJoin extends ModelStateValue<ModelCornerJoin, CornerJoinBlockState>
    {
        private ModelCornerJoin(CornerJoinBlockState value)
        {
            super(value);
        }
        
        @Override
        public ModelStateComponent<ModelCornerJoin, CornerJoinBlockState> getComponent()
        {
            return ModelCornerJoinComponent.this;
        }

        @Override
        public long getBits()
        {
            return value.getIndex();
        }
    }
}