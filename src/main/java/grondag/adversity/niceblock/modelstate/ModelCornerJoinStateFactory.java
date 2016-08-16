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

public class ModelCornerJoinStateFactory extends AbstractModelStateComponentFactory<CornerJoinBlockState>
{
    //public static final ModelCornerJoinState KEY = new ModelCornerJoinState(null);
    public static final ModelCornerJoinStateFactory INSTANCE = new ModelCornerJoinStateFactory();
    
    private ModelCornerJoinStateFactory()
    {
        super(ModelStateComponentType.CORNER_JOIN);
    }

    @Override
    public ModelCornerJoinState getStateFromWorld(NiceBlock block, IBlockTest test, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        NeighborTestResults tests = new NeighborBlocks(world, pos).getNeighborTestResults(test);
        return new ModelCornerJoinState(CornerJoinBlockStateSelector.getJoinState(CornerJoinBlockStateSelector.findIndex(tests)));
    }
    
    @Override
    protected Class<CornerJoinBlockState> getType()
    {
        return CornerJoinBlockState.class;
    }

    @Override
    public ModelCornerJoinState getStateFromBits(long bits)
    {
        return new ModelCornerJoinState(CornerJoinBlockStateSelector.getJoinState((int) bits));
    }
    
    public class ModelCornerJoinState extends ModelCornerJoinStateFactory.ModelStateComponent
    {
        
        public ModelCornerJoinState(CornerJoinBlockState valueIn)
        {
            super(valueIn);
        }

        @Override
        public long toBits()
        {
            return this.value.getIndex();
        }
        
    }
}