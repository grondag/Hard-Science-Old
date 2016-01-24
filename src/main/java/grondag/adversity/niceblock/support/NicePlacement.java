package grondag.adversity.niceblock.support;

import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.library.PlacementValidatorCubic;
import grondag.adversity.niceblock.NiceBlock;
import grondag.adversity.niceblock.NiceBlock.TestForCompleteMatch;
import grondag.adversity.niceblock.NiceBlock.TestForStyleGroupAndSubstance;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.NiceStyle;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;


/**
 * Specialized onBlockPlaced event handlers to enable building of
 * decorative multiblocks with connected textures/geometry.
 */
public abstract class NicePlacement {

	protected NiceBlock owner;

	/** call from Block class on initialization **/
	public void setOwner(NiceBlock owner) {
		this.owner = owner;
	}

	/** call from Block class after setting up **/
	public abstract IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer);

	/** convenience factory method */
	public static NicePlacement makeMasonryPlacer() {
		return new PlacementMasonry(NiceStyle.MASONRY_A, NiceStyle.MASONRY_B,
				NiceStyle.MASONRY_C, NiceStyle.MASONRY_D, NiceStyle.MASONRY_E);
	}

	/** convenience factory method */
	public static NicePlacement makeColumnPlacerRound() {
		return new PlacementColumn(NiceStyle.COLUMN_ROUND_X, NiceStyle.COLUMN_ROUND_Y,
				NiceStyle.COLUMN_ROUND_Z);
	}

	/** convenience factory method */
	public static NicePlacement makeColumnPlacerSquare() {
		return new PlacementColumn(NiceStyle.COLUMN_SQUARE_X, NiceStyle.COLUMN_SQUARE_Y,
				NiceStyle.COLUMN_SQUARE_Z);
	}

	/**
	 * Handles placement of blocks that join together in appearance and have
	 * multiple block instances with the same style.
	 * */
	public static class PlacementBigBlock extends NicePlacement {

		/** Blocks that share the same style and substance */
		private NiceBlock[] siblingsCache;
		private boolean isSiblingsCacheDone = false;

		private NiceBlock[] getSiblings() {
			// substances are grouped consistently across blocks, so the first substance
			// is sufficient for finding sibling blocks.
			if (!isSiblingsCacheDone) {
				siblingsCache = NiceBlockRegistrar.getBlocksForStyleAndSubstance(owner.style, owner.substances[0])
						.toArray(new NiceBlock[0]);
				isSiblingsCacheDone = true;
			}
			return siblingsCache;
		}

		@Override
		public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
				float hitZ, int meta, EntityLivingBase placer) {

			PlacementValidatorCubic shape = new PlacementValidatorCubic(4, 4, 4);

			NeighborBlocks neighbors = new NeighborBlocks(worldIn, pos);
			NeighborTestResults results = neighbors.getNeighborTestResults(new NiceBlock.TestForStyleAndSubstance(owner
					.getStateFromMeta(meta)));

			IBlockState candidate;

			if (results.east()) {
				candidate = neighbors.east();
				if (shape.isValidShape(worldIn, pos, new TestForCompleteMatch(candidate))) {
					return candidate;
				}
				;
			}
			if (results.west()) {
				candidate = neighbors.west();
				if (shape.isValidShape(worldIn, pos, new TestForCompleteMatch(candidate))) {
					return candidate;
				}
			}
			if (results.north()) {
				candidate = neighbors.north();
				if (shape.isValidShape(worldIn, pos, new TestForCompleteMatch(candidate))) {
					return candidate;
				}
			}
			if (results.south()) {
				candidate = neighbors.south();
				if (shape.isValidShape(worldIn, pos, new TestForCompleteMatch(candidate))) {
					return candidate;
				}
			}
			if (results.up()) {
				candidate = neighbors.up();
				if (shape.isValidShape(worldIn, pos, new TestForCompleteMatch(candidate))) {
					return candidate;
				}
			}
			if (results.down()) {
				candidate = neighbors.down();
				if (shape.isValidShape(worldIn, pos, new TestForCompleteMatch(candidate))) {
					return candidate;
				}
			}

			// if no available mates, try to choose a style that will not
			// connect to what is surrounding
			NeighborTestResults tests[] = new NeighborTestResults[getSiblings().length];
			boolean match[] = new boolean[getSiblings().length];

			for (int n = 0; n < getSiblings().length; n++) {
				tests[n] = neighbors.getNeighborTestResults(new TestForCompleteMatch(getSiblings()[n]
						.getStateFromMeta(meta)));
				match[n] = tests[n].north() || tests[n].south() || tests[n].east() || tests[n].west() || tests[n].up()
						|| tests[n].down();
			}

			for (int n = 0; n < getSiblings().length - 1; n++) {
				if (match[n] && !match[n + 1]) {
					return getSiblings()[n + 1].getStateFromMeta(meta);
				}
			}
			return getSiblings()[0].getStateFromMeta(meta);
		}
	}

	/**
	 * Handles placement of blocks that join together in appearance and have
	 * multiple block instances with different styles.
	 * */
	public static class PlacementMasonry extends NicePlacement {

		/**
		 * Blocks that share substance and have one of styles given at
		 * instantiation
		 */
		private final NiceStyle[] styles;

		/** Blocks that share the same style and substance */
		private NiceBlock[] siblingsCache;
		private boolean isSiblingsCacheDone = false;

		public PlacementMasonry(NiceStyle... styles) {
			super();
			this.styles = styles;
		}

		private NiceBlock[] getSiblings() {
			// substances are grouped consistently across blocks, so the first substance
			// is sufficient for finding sibling blocks.
			if (!isSiblingsCacheDone) {
				siblingsCache = new NiceBlock[styles.length];
				for (int i = 0; i < styles.length; i++) {
					siblingsCache[i] = NiceBlockRegistrar.getBlockForStyleAndSubstance(styles[i], owner.substances[0]);
				}
				isSiblingsCacheDone = true;
			}
			return siblingsCache;
		}

		@Override
		public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
				float hitZ, int meta, EntityLivingBase placer) {

			PlacementValidatorCubic shape = new PlacementValidatorCubic(2, 1, 1);

			NeighborBlocks neighbors = new NeighborBlocks(worldIn, pos);
			NeighborTestResults results = neighbors.getNeighborTestResults(
					new TestForStyleGroupAndSubstance(worldIn, owner.getStateFromMeta(meta), pos, styles));

			IBlockState candidate;

			if (results.east()) {
				candidate = neighbors.east();
				if (shape.isValidShape(worldIn, pos, new TestForCompleteMatch(candidate))) {
					return candidate;
				}
				;
			}
			if (results.west()) {
				candidate = neighbors.west();
				if (shape.isValidShape(worldIn, pos, new TestForCompleteMatch(candidate))) {
					return candidate;
				}
			}
			if (results.north()) {
				candidate = neighbors.north();
				if (shape.isValidShape(worldIn, pos, new TestForCompleteMatch(candidate))) {
					return candidate;
				}
			}
			if (results.south()) {
				candidate = neighbors.south();
				if (shape.isValidShape(worldIn, pos, new TestForCompleteMatch(candidate))) {
					return candidate;
				}
			}
			if (results.up()) {
				candidate = neighbors.up();
				if (shape.isValidShape(worldIn, pos, new TestForCompleteMatch(candidate))) {
					return candidate;
				}
			}
			if (results.down()) {
				candidate = neighbors.down();
				if (shape.isValidShape(worldIn, pos, new TestForCompleteMatch(candidate))) {
					return candidate;
				}
			}

			// if no available mates, try to choose a style that will not
			// connect to what is surrounding
			NeighborTestResults tests[] = new NeighborTestResults[getSiblings().length];
			boolean match[] = new boolean[getSiblings().length];

			for (int n = 0; n < getSiblings().length; n++) {
				tests[n] = neighbors.getNeighborTestResults(new TestForCompleteMatch(getSiblings()[n]
						.getStateFromMeta(meta)));
				match[n] = tests[n].north() || tests[n].south() || tests[n].east() || tests[n].west() || tests[n].up()
						|| tests[n].down();
			}

			for (int n = 0; n < getSiblings().length - 1; n++) {
				if (match[n] && !match[n + 1]) {
					return getSiblings()[n + 1].getStateFromMeta(meta);
				}
			}
			return getSiblings()[0].getStateFromMeta(meta);
		}
	}

	/**
	 * Handles placement of axis-aligned blocks like columns. Switches to
	 * different style of block automatically based on axis of placement using
	 * the styles passed in.
	 */
	public static class PlacementColumn extends NicePlacement {

		private final NiceStyle styleX;
		private final NiceStyle styleY;
		private final NiceStyle styleZ;

		private NiceBlock blockX;
		private NiceBlock blockY;
		private NiceBlock blockZ;
		private boolean areSiblingBlocksFound = false;

		public PlacementColumn(NiceStyle styleX, NiceStyle styleY, NiceStyle styleZ) {
			super();
			this.styleX = styleX;
			this.styleY = styleY;
			this.styleZ = styleZ;
		}

		@Override
		public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
				float hitZ, int meta, EntityLivingBase placer) {

			/**
			 * Have to do this now instead of during set owner because some
			 * blocks may not yet be created at that time.
			 */
			if (!areSiblingBlocksFound) {
				blockX = NiceBlockRegistrar.getBlockForStyleAndSubstance(styleX, owner.substances[0]);
				blockY = NiceBlockRegistrar.getBlockForStyleAndSubstance(styleY, owner.substances[0]);
				blockZ = NiceBlockRegistrar.getBlockForStyleAndSubstance(styleZ, owner.substances[0]);
				areSiblingBlocksFound = true;
			}

			switch (facing.getAxis()) {
			case X:
				return blockX.getStateFromMeta(meta);

			case Y:
				return blockY.getStateFromMeta(meta);

			case Z:
				return blockZ.getStateFromMeta(meta);

			default:
				// should be impossible to get here, but silences eclipse warning
				return owner.getStateFromMeta(meta);
			}
		}
	}

	/**
	 * For blocks that require no special handling.
	 */
	public static class PlacementSimple extends NicePlacement {

		@Override
		public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
				float hitZ, int meta, EntityLivingBase placer) {

			return owner.getStateFromMeta(meta);
		}
	}
}
