package grondag.hard_science.library.world;

import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
/**
 * Validates building of simple cubic multi-block structures
 * of arbitrary dimensions up to 255 x 255 x 255
 * with homogeneous composition. (All blocks must pass a given test.)
 * 
 * Placement of a new block is valid if all contiguous blocks passing the test
 * fit within any orientation of the specified volume.  If any
 * contiguous block would fall outside all orientations of the volume,
 * the structure is invalid.
 * 
 * Dimensions are immutable after instance creation,
 * but an instance can be reused for subsequent validations.
 * 
 * Not thread-safe.
 */
public class PlacementValidatorCubic {

	/** 
	 * Records all blocks that have already been visited.
	 * Tried using an array of BitSets to save space but 
	 * this is cleaner and performant enough.  
	 */
	private final TIntHashSet visited;

	/**
	 * Dimensions of a valid shape.
	 * Members are sorted such that x >= y >= z;
	 */
	private final BlockPos validShape;

	/**
	 * Magnitude of longest dimension.
	 * Redundant of validShape.getX()
	 * but use for brevity and clarity.
	 */
	private final int maxOffset;

	/** 
	 * Used during calculation. 
	 * The position from which validation starts
	 * in world coordinates.
	 */
	private BlockPos origin;
	
	/**
	 * Least-valued corner of the volume in 
	 * which validation can occur.  Used to compute a 
	 * positive hash value for each position tested.
	 */
	private BlockPos bottomCorner;

	/** Highest-valued corner of the axis-aligned bounding box
	 * containing all visited blocks that are valid for this structure.
	 */
	private BlockPos maxPos;
	
	/** Least-valued corner of the axis-aligned bounding box
	 * containing all visited blocks that are valid for this structure.
	 */
	private BlockPos minPos;

	/** 
	 * Creates new placement validator with specified
	 * dimensions.  See class declaration for more info.
	 */
	public PlacementValidatorCubic(int i, int j, int k) {

		validShape = WorldHelper.sortedBlockPos(new BlockPos(Math.min(i, 255), Math.min(j, 255), Math.min(k, 255)));

		// Relabeling the longest dimension to prevent confusion 
		// because it may not actually be X.
		maxOffset = validShape.getX();

		visited = new TIntHashSet((i + 2) * (j + 2) * (k + 2));
	}

	/**
	 * Returns true if all blocks contiguous with origin and passing the test
	 * fit within any orientation of volume specified at instantiation.
	 * 
	 * The block at origin is where you are placing a new block
	 * and will be assumed valid.
	 * 
	 * Uses recursion and stores state in class instance to avoid passing a bunch of
	 * crap up and down the call stack even though that would probably be better.
	 */
	public boolean isValidShape(IBlockAccess worldIn, BlockPos origin, IBlockTest test) {

		maxPos = origin;
		minPos = origin;

		this.origin = origin;

		// Used to ensure positive numbers for PosHash function
		bottomCorner = origin.subtract(new BlockPos(256, 256, 256));

		visited.clear();

		setVisited(origin);

		updateMeasurements(origin);

		visit(worldIn, origin.up(), test);
		visit(worldIn, origin.down(), test);
		visit(worldIn, origin.east(), test);
		visit(worldIn, origin.west(), test);
		visit(worldIn, origin.north(), test);
		visit(worldIn, origin.south(), test);

		return isValid();
	}

	private void visit(IBlockAccess worldIn, BlockPos pos, IBlockTest test) {

		if (!isReachable(pos) || !setVisited(pos)) {
			return;
		}

		if (test.testBlock(worldIn, worldIn.getBlockState(pos), pos)) {
			updateMeasurements(pos);

			visit(worldIn, pos.up(), test);
			visit(worldIn, pos.down(), test);
			visit(worldIn, pos.east(), test);
			visit(worldIn, pos.west(), test);
			visit(worldIn, pos.north(), test);
			visit(worldIn, pos.south(), test);
		}
	}

	private void updateMeasurements(BlockPos pos) {
		minPos = new BlockPos(Math.min(minPos.getX(), pos.getX()), Math.min(minPos.getY(), pos.getY()), Math.min(minPos.getZ(), pos.getZ()));
		maxPos = new BlockPos(Math.max(maxPos.getX(), pos.getX()), Math.max(maxPos.getY(), pos.getY()), Math.max(maxPos.getZ(), pos.getZ()));
	}

	private boolean isReachable(BlockPos pos) {
		BlockPos dist = pos.subtract(origin);
		return !(Math.abs(dist.getX()) > maxOffset || Math.abs(dist.getY()) > maxOffset || Math.abs(dist.getZ()) > maxOffset);
	}

	private boolean isValid() {
		BlockPos clearances = validShape.subtract(WorldHelper.sortedBlockPos(maxPos.subtract(minPos).add(1, 1, 1)));
		return clearances.getX() >= 0 && clearances.getY() >= 0 && clearances.getZ() >= 0;
	}

	/** 
	 * Returns true if key was not already in the set.
	 */
	private boolean setVisited(BlockPos pos) {
		// subtracting bottomCorner ensures positive values for hash function
		return visited.add(getPosHash(pos.subtract(bottomCorner)));
	}

	private int getPosHash(BlockPos pos) {
		// volume could be 255 in any direction for origin,
		// so need more than 8 bits per axis. 10 is plenty. 
		return pos.getX() & 0x3FF | (pos.getY() & 0x3FF) << 10 | (pos.getZ() & 0x3FF) << 20;
	}
}
