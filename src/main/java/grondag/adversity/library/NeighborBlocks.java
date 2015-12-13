package grondag.adversity.library;

import grondag.adversity.Adversity;
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
	public final NeighborSet set;
	
	public NeighborBlocks(IBlockAccess worldIn, BlockPos pos){
	  up = worldIn.getBlockState(pos.up()); 
	  down = worldIn.getBlockState(pos.down()); 
	  east = worldIn.getBlockState(pos.east()); 
	  west = worldIn.getBlockState(pos.west()); 
	  north = worldIn.getBlockState(pos.north()); 
	  south = worldIn.getBlockState(pos.south()); 
	  set = NeighborSet.ALL;
	}

	public NeighborBlocks(IBlockAccess worldIn, BlockPos pos, NeighborSet set){
		switch(set){
		case X_NORMALS:
			  up = worldIn.getBlockState(pos.up()); 
			  down = worldIn.getBlockState(pos.down()); 
			  east = null; 
			  west = null; 
			  north = worldIn.getBlockState(pos.north()); 
			  south = worldIn.getBlockState(pos.south()); 
			  this.set = NeighborSet.X_NORMALS;
			  break;
		case Y_NORMALS:
			  up = null; 
			  down = null; 
			  east = worldIn.getBlockState(pos.east()); 
			  west = worldIn.getBlockState(pos.west()); 
			  north = worldIn.getBlockState(pos.north()); 
			  south = worldIn.getBlockState(pos.south()); 
			  this.set = NeighborSet.Y_NORMALS;
			  break;
		case Z_NORMALS:
			  up = worldIn.getBlockState(pos.up()); 
			  down = worldIn.getBlockState(pos.down()); 
			  east = worldIn.getBlockState(pos.east()); 
			  west = worldIn.getBlockState(pos.west()); 
			  north = null; 
			  south = null; 
			  this.set = NeighborSet.Z_NORMALS;
			  break;
		 default:
			  up = worldIn.getBlockState(pos.up()); 
			  down = worldIn.getBlockState(pos.down()); 
			  east = worldIn.getBlockState(pos.east()); 
			  west = worldIn.getBlockState(pos.west()); 
			  north = worldIn.getBlockState(pos.north()); 
			  south = worldIn.getBlockState(pos.south()); 
			  this.set = NeighborSet.ALL;
			  break;
		}

	}
	
	public NeighborTestResults getNeighborTestResults(IBlockTest test){
		
		switch(set){
		case ALL:
			 return new NeighborTestResults(
						test.testBlock(this.up), test.testBlock(this.down), 
						test.testBlock(this.east), test.testBlock(this.west),
						test.testBlock(this.north), test.testBlock(this.south)
				);

		case X_NORMALS:
			  return new NeighborTestResults(
						test.testBlock(this.up), test.testBlock(this.down), 
						false, false,
						test.testBlock(this.north), test.testBlock(this.south)
				);

		case Y_NORMALS:

			  return new NeighborTestResults(
						false, false, 
						test.testBlock(this.east), test.testBlock(this.west),
						test.testBlock(this.north), test.testBlock(this.south)
				);

		case Z_NORMALS:
			  return new NeighborTestResults(
						test.testBlock(this.up), test.testBlock(this.down), 
						test.testBlock(this.east), test.testBlock(this.west),
						false, false
				);

		}
		
		// will never get here - just to make eclipse stop complaining at me about no return value
		Adversity.log.warn("Unrecognized neighbor test enumerator. This should be impossible and there has probably been a huge derp.");
		return new NeighborTestResults(false, false, false, false, false, false);

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
	
	public enum NeighborSet{
		ALL,
		X_NORMALS,
		Y_NORMALS,
		Z_NORMALS
	}
	
}
