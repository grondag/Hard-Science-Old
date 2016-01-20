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
	
	private IBlockState up;
	private IBlockState down;
	private IBlockState east;
	private IBlockState west;
	private IBlockState north;
	private IBlockState south;
	
	private final IBlockAccess world;
	private final BlockPos pos;

	/**
	 * Gathers blockstates for all six adjacent positions.
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
				if(test.testBlock(NeighborBlocks.this.up())) resultFlags |= UP;
				completionFlags |= UP;
			}
			return (resultFlags & UP) == UP;
		}
		
		public int upBit(){
			return up() ? 1 : 0;
		}
		
		public boolean down(){
			if((completionFlags & DOWN) != DOWN) {
				if(test.testBlock(NeighborBlocks.this.down())) resultFlags |= DOWN;
				completionFlags |= DOWN;
			}
			return (resultFlags & DOWN) == DOWN;
		}
		
		public int downBit(){
			return down() ? 1 : 0;
		}
		
		public boolean north(){
			if((completionFlags & NORTH) != NORTH) {
				if(test.testBlock(NeighborBlocks.this.north())) resultFlags |= NORTH;
				completionFlags |= NORTH;
			}
			return (resultFlags & NORTH) == NORTH;
		}
		
		public int northBit(){
			return north() ? 1 : 0;
		}
		
		public boolean south(){
			if((completionFlags & SOUTH) != SOUTH) {
				if(test.testBlock(NeighborBlocks.this.south())) resultFlags |= SOUTH;
				completionFlags |= SOUTH;
			}
			return (resultFlags & SOUTH) == SOUTH;
		}
		
		public int southBit(){
			return south() ? 1 : 0;
		}
		
		public boolean east(){
			if((completionFlags & EAST) != EAST) {
				if(test.testBlock(NeighborBlocks.this.east())) resultFlags |= EAST;
				completionFlags |= EAST;
			}
			return (resultFlags & EAST) == EAST;
		}
		
		public int eastBit(){
			return east() ? 1 : 0;
		}
		
		public boolean west(){
			if((completionFlags & WEST) != WEST) {
				if(test.testBlock(NeighborBlocks.this.west())) resultFlags |= WEST;
				completionFlags |= WEST;
			}
			return (resultFlags & WEST) == WEST;
		}
		
		public int westBit(){
			return west() ? 1 : 0;
		}
	}

}
