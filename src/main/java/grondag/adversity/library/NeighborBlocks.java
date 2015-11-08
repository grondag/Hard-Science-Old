package grondag.adversity.library;

import grondag.adversity.feature.volcano.BlockBasalt.EnumStyle;
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
	
	public interface INeighborTest{
		public boolean TestNeighbor(IBlockState ibs);
	}
	
	public NeighborTestResults getNeighborTestResults(INeighborTest test){
		return new NeighborTestResults(
				test.TestNeighbor(this.up), test.TestNeighbor(this.down), 
				test.TestNeighbor(this.east), test.TestNeighbor(this.west),
				test.TestNeighbor(this.north), test.TestNeighbor(this.south)
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
