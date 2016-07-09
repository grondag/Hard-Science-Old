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
   // private final int levelCount;
    
    protected int tickRate = 20;
    
    public FlowBlock(FlowBlockHelper blockModelHelper, BaseMaterial material, String styleName)
    {
        super(blockModelHelper, material, styleName);
    //    this.levelCount = blockModelHelper.levelCount;
    }

    /**
     * (The 0 value is reserved for non flow blocks.)
     * In practice, this makes one meta value (15) redundant.
     */
    @Override
    public int getRenderHeightFromState(IBlockState state)
    {
        return 16 - state.getValue(META);
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        if(blockAccess.getBlockState(pos.up()).getBlock() instanceof IFlowBlock)
        {
            return false;
        }
        else
        {
            return true;
        }
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
        return false;//state.getValue(META) == 0;
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

    @Override
    public int getPackedLightmapCoords(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        int lightThis     = world.getCombinedLight(pos, 0);
        int lightUp       = world.getCombinedLight(pos.up(), 0);
        int lightThisBase = lightThis & 255;
        int lightUpBase   = lightUp & 255;
        int lightThisExt  = lightThis >> 16 & 255;
        int lightUpExt    = lightUp >> 16 & 255;
        return (lightThisBase > lightUpBase ? lightThisBase : lightUpBase) |
               ((lightThisExt > lightUpExt ? lightThisExt : lightUpExt) << 16);
    }
 
    
}
