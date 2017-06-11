package grondag.adversity.library.world;

import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/** 
 * Used to implement visitor pattern for block-state dependent conditional logic. 
 * See NeighborBlocks for example of usage.
 */
public interface IBlockTest
{
	public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos);
	
	public default boolean wantsModelState() { return false; }
	
	public default boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos, ModelState modelState)
	{
	    return testBlock(world, ibs, pos);
	}
}