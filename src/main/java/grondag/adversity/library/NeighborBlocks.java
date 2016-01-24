package grondag.adversity.library;

import grondag.adversity.Adversity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Convenient way to gather and test block states
 * for blocks adjacent to a given position.
 * Position is immutable, blockstates are looked up lazily 
 * and values are cached for reuse.
 */
public class NeighborBlocks {
	
	private static final int UP 			= 0b1;
	private static final int DOWN 		= UP << 1;
	private static final int EAST 		= DOWN << 1;
	private static final int WEST 		= EAST << 1;
	private static final int NORTH 		= WEST << 1;
	private static final int SOUTH 		= NORTH << 1;

	private static final int UP_NORTH 	= SOUTH << 1;
	private static final int UP_SOUTH 	= UP_NORTH << 1;
	private static final int UP_EAST 	= UP_SOUTH << 1;
	private static final int UP_WEST     = UP_EAST << 1;

	private static final int DOWN_NORTH 	= UP_WEST << 1;
	private static final int DOWN_SOUTH 	= DOWN_NORTH << 1;
	private static final int DOWN_EAST 	= DOWN_SOUTH << 1;
	private static final int DOWN_WEST   = DOWN_EAST << 1;
	
	private static final int NORTH_EAST 	= DOWN_WEST << 1;
	private static final int NORTH_WEST 	= NORTH_EAST << 1;
	private static final int SOUTH_EAST 	= NORTH_WEST << 1;
	private static final int SOUTH_WEST   = SOUTH_EAST << 1;
	
	// cache block states for the most commonly used neighbors
	private IBlockState east;
	private IBlockState west;
	private IBlockState up;
	private IBlockState down;
	private IBlockState north;
	private IBlockState south;

	private final IBlockAccess world;
	private final BlockPos pos;

	/**
	 * Gathers blockstates for adjacent positions as needed.
	 */
	public NeighborBlocks(IBlockAccess worldIn, BlockPos pos) {
		this.world = worldIn;
		this.pos = pos;
	}

	public IBlockState up(){
		if(up == null){
			up = world.getBlockState(pos.up());
		}
		return up;
	}

	public IBlockState down(){
		if(down == null){
			down = world.getBlockState(pos.down());
		}
		return down;
	}
	
	public IBlockState north(){
		if(north == null){
			north = world.getBlockState(pos.north());
		}
		return north;
	}
	
	public IBlockState south(){
		if(south == null){
			south = world.getBlockState(pos.south());
		}
		return south;
	}
	
	public IBlockState east(){
		if(east == null){
			east = world.getBlockState(pos.east());
		}
		return east;
	}
	
	public IBlockState west(){
		if(west == null){
			west = world.getBlockState(pos.west());
		}
		return west;
	}
	
	// convenience methods, no caching
	public IBlockState upEast(){return world.getBlockState(pos.up().east());};
	public IBlockState upWest(){return world.getBlockState(pos.up().west());};
	public IBlockState downEast(){return world.getBlockState(pos.down().east());};
	public IBlockState downWest(){return world.getBlockState(pos.down().west());};

	public IBlockState northEast(){return world.getBlockState(pos.north().east());};
	public IBlockState northWest(){return world.getBlockState(pos.north().west());};
	public IBlockState southEast(){return world.getBlockState(pos.south().east());};
	public IBlockState southWest(){return world.getBlockState(pos.south().west());};

	public IBlockState upNorth(){return world.getBlockState(pos.up().north());};
	public IBlockState upSouth(){return world.getBlockState(pos.up().south());};
	public IBlockState downNorth(){return world.getBlockState(pos.down().north());};
	public IBlockState downSouth(){return world.getBlockState(pos.down().south());};

	/**
	 * Apply given test to all available block states.
	 * Returns false for positions excluded at instantiation.
	 */
	public NeighborTestResults getNeighborTestResults(IBlockTest test) {
		return new NeighborTestResults(test);
	}

	/**
	 * Convenient data structure for returning test results.
	 */
	public class NeighborTestResults {

		private int completionFlags = 0;
		private int resultFlags = 0;
		private final IBlockTest test;

		protected NeighborTestResults(IBlockTest test) {
			this.test = test;
		}
		
		public boolean up(){
			if((completionFlags & UP) != UP) {
				if(test.testBlock(world, NeighborBlocks.this.up(), pos.up())) resultFlags |= UP;
				completionFlags |= UP;
			}
			return (resultFlags & UP) == UP;
		}
		
		public int upBit(){
			return up() ? 1 : 0;
		}
		
		public boolean down(){
			if((completionFlags & DOWN) != DOWN) {
				if(test.testBlock(world, NeighborBlocks.this.down(), pos.down())) resultFlags |= DOWN;
				completionFlags |= DOWN;
			}
			return (resultFlags & DOWN) == DOWN;
		}
		
		public int downBit(){
			return down() ? 1 : 0;
		}
		
		public boolean north(){
			if((completionFlags & NORTH) != NORTH) {
				if(test.testBlock(world, NeighborBlocks.this.north(), pos.north())) resultFlags |= NORTH;
				completionFlags |= NORTH;
			}
			return (resultFlags & NORTH) == NORTH;
		}
		
		public int northBit(){
			return north() ? 1 : 0;
		}
		
		public boolean south(){
			if((completionFlags & SOUTH) != SOUTH) {
				if(test.testBlock(world, NeighborBlocks.this.south(), pos.south())) resultFlags |= SOUTH;
				completionFlags |= SOUTH;
			}
			return (resultFlags & SOUTH) == SOUTH;
		}
		
		public int southBit(){
			return south() ? 1 : 0;
		}
		
		public boolean east(){
			if((completionFlags & EAST) != EAST) {
				if(test.testBlock(world, NeighborBlocks.this.east(), pos.east())) resultFlags |= EAST;
				completionFlags |= EAST;
			}
			return (resultFlags & EAST) == EAST;
		}
		
		public int eastBit(){
			return east() ? 1 : 0;
		}
		
		public boolean west(){
			if((completionFlags & WEST) != WEST) {
				if(test.testBlock(world, NeighborBlocks.this.west(), pos.west())) resultFlags |= WEST;
				completionFlags |= WEST;
			}
			return (resultFlags & WEST) == WEST;
		}
		
		public int westBit(){
			return west() ? 1 : 0;
		}
		
		public boolean upNorth(){
			if((completionFlags & UP_NORTH) != UP_NORTH) {
				if(test.testBlock(world, NeighborBlocks.this.upNorth(), pos.up().north())) resultFlags |= UP_NORTH;
				completionFlags |= UP_NORTH;
			}
			return (resultFlags & UP_NORTH) == UP_NORTH;
		}
		
		public int upNorthBit(){
			return upNorth() ? 1 : 0;
		}

		public boolean upSouth(){
			if((completionFlags & UP_SOUTH) != UP_SOUTH) {
				if(test.testBlock(world, NeighborBlocks.this.upSouth(), pos.up().south())) resultFlags |= UP_SOUTH;
				completionFlags |= UP_SOUTH;
			}
			return (resultFlags & UP_SOUTH) == UP_SOUTH;
		}
		
		public int upSouthBit(){
			return upSouth() ? 1 : 0;
		}

		public boolean upEast(){
			if((completionFlags & UP_EAST) != UP_EAST) {
				if(test.testBlock(world, NeighborBlocks.this.upEast(), pos.up().east())) resultFlags |= UP_EAST;
				completionFlags |= UP_EAST;
			}
			return (resultFlags & UP_EAST) == UP_EAST;
		}
		
		public int upEastBit(){
			return upEast() ? 1 : 0;
		}

		public boolean upWest(){
			if((completionFlags & UP_WEST) != UP_WEST) {
				if(test.testBlock(world, NeighborBlocks.this.upWest(), pos.up().west())) resultFlags |= UP_WEST;
				completionFlags |= UP_WEST;
			}
			return (resultFlags & UP_WEST) == UP_WEST;
		}
		
		public int upWestBit(){
			return upWest() ? 1 : 0;
		}

		public boolean downNorth(){
			if((completionFlags & DOWN_NORTH) != DOWN_NORTH) {
				if(test.testBlock(world, NeighborBlocks.this.downNorth(), pos.down().north())) resultFlags |= DOWN_NORTH;
				completionFlags |= DOWN_NORTH;
			}
			return (resultFlags & DOWN_NORTH) == DOWN_NORTH;
		}
		
		public int downNorthBit(){
			return downNorth() ? 1 : 0;
		}

		public boolean downSouth(){
			if((completionFlags & DOWN_SOUTH) != DOWN_SOUTH) {
				if(test.testBlock(world, NeighborBlocks.this.downSouth(), pos.down().south())) resultFlags |= DOWN_SOUTH;
				completionFlags |= DOWN_SOUTH;
			}
			return (resultFlags & DOWN_SOUTH) == DOWN_SOUTH;
		}
		
		public int downSouthBit(){
			return downSouth() ? 1 : 0;
		}

		public boolean downEast(){
			if((completionFlags & DOWN_EAST) != DOWN_EAST) {
				if(test.testBlock(world, NeighborBlocks.this.downEast(), pos.down().east())) resultFlags |= DOWN_EAST;
				completionFlags |= DOWN_EAST;
			}
			return (resultFlags & DOWN_EAST) == DOWN_EAST;
		}
		
		public int downEastBit(){
			return downEast() ? 1 : 0;
		}

		public boolean downWest(){
			if((completionFlags & DOWN_WEST) != DOWN_WEST) {
				if(test.testBlock(world, NeighborBlocks.this.downWest(), pos.down().west())) resultFlags |= DOWN_WEST;
				completionFlags |= DOWN_WEST;
			}
			return (resultFlags & DOWN_WEST) == DOWN_WEST;
		}
		
		public int downWestBit(){
			return downWest() ? 1 : 0;
		}
		
		public boolean northEast(){
			if((completionFlags & NORTH_EAST) != NORTH_EAST) {
				if(test.testBlock(world, NeighborBlocks.this.northEast(), pos.north().east())) resultFlags |= NORTH_EAST;
				completionFlags |= NORTH_EAST;
			}
			return (resultFlags & NORTH_EAST) == NORTH_EAST;
		}
		
		public int northEastBit(){
			return northEast() ? 1 : 0;
		}

		public boolean northWest(){
			if((completionFlags & NORTH_WEST) != NORTH_WEST) {
				if(test.testBlock(world, NeighborBlocks.this.northWest(), pos.north().west())) resultFlags |= NORTH_WEST;
				completionFlags |= NORTH_WEST;
			}
			return (resultFlags & NORTH_WEST) == NORTH_WEST;
		}
		
		public int northWestBit(){
			return northWest() ? 1 : 0;
		}

		public boolean southEast(){
			if((completionFlags & SOUTH_EAST) != SOUTH_EAST) {
				if(test.testBlock(world, NeighborBlocks.this.southEast(), pos.south().east())) resultFlags |= SOUTH_EAST;
				completionFlags |= SOUTH_EAST;
			}
			return (resultFlags & SOUTH_EAST) == SOUTH_EAST;
		}
		
		public int southEastBit(){
			return southEast() ? 1 : 0;
		}

		public boolean southWest(){
			if((completionFlags & SOUTH_WEST) != SOUTH_WEST) {
				if(test.testBlock(world, NeighborBlocks.this.southWest(), pos.south().west())) resultFlags |= SOUTH_WEST;
				completionFlags |= SOUTH_WEST;
			}
			return (resultFlags & SOUTH_WEST) == SOUTH_WEST;
		}
		
		public int southWestBit(){
			return southWest() ? 1 : 0;
		}

	}
}
