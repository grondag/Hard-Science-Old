package grondag.adversity.niceblock;

import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class HeightBlock extends NiceBlock
{
    
    public HeightBlock(SimpleHelper blockModelHelper, BaseMaterial material, String styleName)
    {
        super(blockModelHelper, material, styleName);
    }

//    @Override
//    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
//    {
//        // TODO actually implement this
//        return true;
//    }

    // setting to false drops AO light value
    @Override
    public boolean isFullCube(IBlockState state)
    {
        return state.getValue(NiceBlock.META) == 15;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return state.getValue(NiceBlock.META) == 15;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return state.getValue(NiceBlock.META) == 15;
    }
    
    @Override
    public boolean needsCustomHighlight()
    {
        return true;
    }
}