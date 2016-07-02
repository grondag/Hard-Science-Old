package grondag.adversity.niceblock;

import grondag.adversity.library.NeighborBlocks.HorizontalCorner;
import grondag.adversity.library.NeighborBlocks.HorizontalFace;
import grondag.adversity.niceblock.FlowController.FlowHeightState;
import grondag.adversity.niceblock.base.BlockModelHelper;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.ModelState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class FlowBlockHelper extends BlockModelHelper
{

  //  public final int levelCount;
    
    protected FlowBlockHelper(ModelDispatcher dispatcher, int levelCount)
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
        
        FlowHeightState flowState = new FlowHeightState(0);
        
        flowState.setCenterHeight(level);
        
        flowState.setCornerHeight(HorizontalCorner.NORTH_EAST, level);
        flowState.setCornerHeight(HorizontalCorner.NORTH_WEST, level);
        flowState.setCornerHeight(HorizontalCorner.SOUTH_EAST, level);
        flowState.setCornerHeight(HorizontalCorner.SOUTH_WEST, level);
        
        flowState.setSideHeight(HorizontalFace.EAST, level);
        flowState.setSideHeight(HorizontalFace.WEST, level);
        flowState.setSideHeight(HorizontalFace.NORTH, level);
        flowState.setSideHeight(HorizontalFace.SOUTH, level);
        
        return new ModelState(0).setShapeIndex(flowState.getStateKey(), BlockRenderLayer.SOLID);
    }

}
