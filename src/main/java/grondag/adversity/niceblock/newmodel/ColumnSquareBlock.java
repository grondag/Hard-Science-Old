package grondag.adversity.niceblock.newmodel;

import net.minecraft.util.math.BlockPos;
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
