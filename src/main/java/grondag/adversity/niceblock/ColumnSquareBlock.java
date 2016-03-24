package grondag.adversity.niceblock;

import net.minecraft.util.math.BlockPos;
import grondag.adversity.niceblock.base.BlockModelHelper;
import grondag.adversity.niceblock.base.NiceBlockPlus;
import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

public class ColumnSquareBlock extends NiceBlockPlus
{

    public ColumnSquareBlock(BlockModelHelper blockModelHelper, BaseMaterial material, String styleName)
    {
        super(blockModelHelper, material, styleName);
    }
 
    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess worldIn, BlockPos pos, EnumFacing side)
    {
        return true;
    }

}
