package grondag.adversity.library;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

public class NeighborBlocks {

	public final IBlockState up;
	public final IBlockState down;
	public final IBlockState east;
	public final IBlockState west;
	public final IBlockState north;
	public final IBlockState south;
	
	public NeighborBlocks(IBlockAccess worldIn, BlockPos pos){
	  up = worldIn.getBlockState(pos.up()); 
	  down = worldIn.getBlockState(pos.down()); 
	  east = worldIn.getBlockState(pos.east()); 
	  west = worldIn.getBlockState(pos.west()); 
	  north = worldIn.getBlockState(pos.north()); 
	  south = worldIn.getBlockState(pos.south()); 
	}

	
	public NeighborTestResults getNeighborTestResults(IBlockTest test){
		return new NeighborTestResults(
				test.testBlock(this.up), test.testBlock(this.down), 
				test.testBlock(this.east), test.testBlock(this.west),
				test.testBlock(this.north), test.testBlock(this.south)
		);
	}
	
	public class NeighborTestResults{
		
		public final boolean up;
		public final boolean down;
		public final boolean east;
		public final boolean west;
		public final boolean north;
		public final boolean south;
		
		public NeighborTestResults(boolean up, boolean down, boolean east, boolean west, boolean north, boolean south){
			this.up = up;
			this.down = down;
			this.east = east;
			this.west = west;
			this.north = north;
			this.south = south;
		}
	}
	
}
