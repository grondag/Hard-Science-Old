package grondag.adversity.library;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/** 
 * Used to implement visitor pattern for block-state dependent conditional logic. 
 * See NeighborBlocks for example of usage.
 */
public interface IBlockTest{
	public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos);
}