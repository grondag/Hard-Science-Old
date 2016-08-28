package grondag.adversity.library;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface IBlockTestFactory
{
    public IBlockTest makeTest(IBlockAccess world, IBlockState ibs, BlockPos pos);
}
