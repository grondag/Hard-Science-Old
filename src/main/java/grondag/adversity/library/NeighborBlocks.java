package grondag.adversity.library;

import grondag.adversity.Adversity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Convenient way to gather and test block states
 * for blocks adjacent to a given position.
 * Stateful but immutable, blockstates are looked up at instantiation
 * and retained to allow for multiple references / tests.
 */
public class NeighborBlocks {

	public final IBlockState up;
	public final IBlockState down;
	public final IBlockState east;
	public final IBlockState west;
	public final IBlockState north;
	public final IBlockState south;
	public final NeighborSet set;

	/**
	 * Gathers blockstates for all six adjacent positions.
	 */
	public NeighborBlocks(IBlockAccess worldIn, BlockPos pos) {
		up = worldIn.getBlockState(pos.up());
		down = worldIn.getBlockState(pos.down());
		east = worldIn.getBlockState(pos.east());
		west = worldIn.getBlockState(pos.west());
		north = worldIn.getBlockState(pos.north());
		south = worldIn.getBlockState(pos.south());
		set = NeighborSet.ALL;
	}

	/**
	 * Gathers blockstates for the positions included by the given set.
	 * Positions not in the set will have null values and tests on them will return false.
	 * Use this when you don't need all six positions to avoid performance overhead.
	 */
	public NeighborBlocks(IBlockAccess worldIn, BlockPos pos, NeighborSet set) {
		switch (set) {
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

	/**
	 * Apply given test to all available block states.
	 * Returns false for positions excluded at instantiation.
	 */
	public NeighborTestResults getNeighborTestResults(IBlockTest test) {

		switch (set) {
		case ALL:
			return new NeighborTestResults(
					test.testBlock(up), test.testBlock(down),
					test.testBlock(east), test.testBlock(west),
					test.testBlock(north), test.testBlock(south));

		case X_NORMALS:
			return new NeighborTestResults(
					test.testBlock(up), test.testBlock(down),
					false, false,
					test.testBlock(north), test.testBlock(south));

		case Y_NORMALS:

			return new NeighborTestResults(
					false, false,
					test.testBlock(east), test.testBlock(west),
					test.testBlock(north), test.testBlock(south));

		case Z_NORMALS:
			return new NeighborTestResults(
					test.testBlock(up), test.testBlock(down),
					test.testBlock(east), test.testBlock(west),
					false, false);

		}

		// will never get here - just to make eclipse stop complaining at me
		// about no return value
		Adversity.log.warn("Unrecognized neighbor test enumerator. This should be impossible and there has probably been a huge derp.");
		return new NeighborTestResults(false, false, false, false, false, false);

	}

	/**
	 * Convenient data structure for returning test results.
	 */
	public class NeighborTestResults {

		public final boolean up;
		public final boolean down;
		public final boolean east;
		public final boolean west;
		public final boolean north;
		public final boolean south;

		public NeighborTestResults(boolean up, boolean down, boolean east, boolean west, boolean north, boolean south) {
			this.up = up;
			this.down = down;
			this.east = east;
			this.west = west;
			this.north = north;
			this.south = south;
		}
	}

	public enum NeighborSet {
		/** include all six adjacent positions */
		ALL,
		/** exclude east and west */
		X_NORMALS,
		/** exclude up and down */
		Y_NORMALS,
		/** exclude north and south */
		Z_NORMALS
	}

}
