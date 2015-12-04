package grondag.adversity.library;

import net.minecraft.block.state.IBlockState;

/** 
 * Used to implement visitor pattern for block-state dependent conditional logic. 
 * See NeighborBlocks for example of usage.
 */
public interface IBlockTest{
	public boolean testBlock(IBlockState ibs);
}