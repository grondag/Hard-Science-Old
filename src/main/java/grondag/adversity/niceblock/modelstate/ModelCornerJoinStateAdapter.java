package grondag.adversity.niceblock.modelstate;

import grondag.adversity.library.IBlockTest;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.library.joinstate.CornerJoinBlockState;
import grondag.adversity.library.joinstate.CornerJoinBlockStateSelector;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModelCornerJoinStateAdapter extends AbstractModelStateComponentAdapter<CornerJoinBlockState>
{
    public static final ModelCornerJoinStateAdapter INSTANCE = new ModelCornerJoinStateAdapter();
    
    @Override
    public Class<CornerJoinBlockState> getType()
    {
        return CornerJoinBlockState.class;
    }

    @Override
    public long getValueCount()
    {
        return CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT;
    }

    @Override
    public long getBitsFromWorld(NiceBlock block, IBlockTest test, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        NeighborTestResults tests = new NeighborBlocks(world, pos).getNeighborTestResults(test);
        return CornerJoinBlockStateSelector.findIndex(tests);
    }

    @Override
    public CornerJoinBlockState createValueFromBits(long bits)
    {
        return CornerJoinBlockStateSelector.getJoinState((int) bits);
    }
}