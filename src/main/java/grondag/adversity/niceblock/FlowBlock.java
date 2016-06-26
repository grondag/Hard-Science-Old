package grondag.adversity.niceblock;

import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class FlowBlock extends NiceBlock implements IFlowBlock
{
    private final int levelCount;
    
    public FlowBlock(FlowBlockHelper blockModelHelper, BaseMaterial material, String styleName)
    {
        super(blockModelHelper, material, styleName);
        this.levelCount = blockModelHelper.levelCount;
    }

    /**
     * Force this to a minimum of 1 because we can only handle 15 values in model construction.
     * (The 0 value is reserved for non flow blocks.)
     * In practice, this makes one meta value (15) redundant.
     */
    @Override
    public int getRenderHeightFromState(IBlockState state)
    {
        return Math.max(1, ((levelCount - state.getValue(META)) * 16 / levelCount) - 1);
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        // TODO actually implement this
        return true;
    }

//    @Override
//    public boolean isFullBlock(IBlockState state)
//    {
//        // TODO Auto-generated method stub
//        return super.isFullBlock(state);
//    }
//
//    @Override
//    public boolean isNormalCube(IBlockState state)
//    {
//        // TODO Auto-generated method stub
//        return super.isNormalCube(state);
//    }
//
    
    // setting to false drops AO light value
    @Override
    public boolean isFullCube(IBlockState state)
    {
        return state.getValue(META) == 0;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
        //return state.getValue(META) == 0;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return false;
        //return state.getValue(META) == 0;
    }

    @Override
    public boolean needsCustomHighlight()
    {
        return true;
    }

    
}
