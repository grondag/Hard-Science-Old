package grondag.adversity.niceblock;

import grondag.adversity.niceblock.base.BlockModelHelper;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.modelstate.ModelState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class FlowHeightHelper extends BlockModelHelper
{

  //  public final int levelCount;
    
    protected FlowHeightHelper(ModelDispatcher dispatcher, int levelCount)
    {
        super(dispatcher);
    //    this.levelCount = levelCount;
    }

    @Override
    public ModelState getModelStateForBlock(IBlockState state, IBlockAccess world, BlockPos pos, boolean doClientStateRefresh)
    {
        ModelState retVal = new ModelState(0);
        dispatcher.refreshClientShapeIndex(block, state, world, pos, retVal, true);
        return retVal;
    }

    @Override
    public int getItemModelCount()
    {
       // return levelCount;
        return 16;
    }

    @Override
    public ModelState getModelStateForItemModel(int itemIndex)
    {
        int level = 16 - itemIndex;
        int [] quadrants = new int[] {level, level, level, level};
        long key = FlowHeightState.computeStateKey(level, quadrants, quadrants, 0);
        return new ModelState(0).setShapeIndex(key, BlockRenderLayer.SOLID);
    }

}
