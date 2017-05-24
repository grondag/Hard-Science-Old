package grondag.adversity.superblock.block;

import java.util.concurrent.ThreadLocalRandom;

import grondag.adversity.library.IBlockTest;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.BlockCorner;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.library.PlacementValidatorCubic;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.base.NiceItemBlock;
import grondag.adversity.niceblock.support.BlockTests;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;


/**
 * Specialized onBlockPlaced event handlers to enable building of
 * decorative multiblocks with connected textures/geometry.
 */
public abstract class SuperPlacement {
    
    public static final int PLACEMENT_2x1x1 = (2 << 16) | (1 << 8) | 1;
    public static final int PLACEMENT_2x2x2 = (2 << 16) | (2 << 8) | 2;
    public static final int PLACEMENT_3x3x3 = (3 << 16) | (3 << 8) | 3;
    public static final int PLACEMENT_4x4x4 = (4 << 16) | (4 << 8) | 4;
    

    public abstract int getSpeciesForPlacedStack(World worldIn, BlockPos pos, EnumFacing facing, ItemStack stack, SuperBlock block);

	/**
	 * Handles placement of blocks that join together in appearance and have
	 * multiple block instances with the same meta.
	 * */
	public static class PlacementBigBlock extends SuperPlacement 
	{

	    private final PlacementValidatorCubic shape;
	    
        public PlacementBigBlock()
        {
            this(new PlacementValidatorCubic(4, 4, 4));
        }

	    public PlacementBigBlock(PlacementValidatorCubic shape)
	    {
	        this.shape = shape;
	    }
	    
	    /**
	     * Use this instead of enum order so that masonry blocks join horizontally before vertically
	     */
	    private static final EnumFacing[] PLACEMENT_ORDER = {EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH, 
                   EnumFacing.DOWN, EnumFacing.UP};
	

        @Override
        public int getSpeciesForPlacedStack(World worldIn, BlockPos pos, EnumFacing facing, ItemStack stack, SuperBlock block)
        {
            int speciesInUseFlags = 0;
            
            NeighborBlocks neighbors = new NeighborBlocks(worldIn, pos, false);
            NeighborTestResults results = neighbors.getNeighborTestResults(new BlockTests.SuperBlockBorderCandidateMatch(block));
            
            for(EnumFacing face : PLACEMENT_ORDER)            
            {
                 if (results.result(face)) 
                 {
                     int species = neighbors.getModelState(face).getSpecies();
                     speciesInUseFlags |= (1 << species);
                     if (shape.isValidShape(worldIn, pos, new BlockTests.SuperBlockBorderMatch(block, species))) 
                     {
                         return species;
                     }
                 }
            }

            for(BlockCorner corner : BlockCorner.values())
            {
                if(results.result(corner))
                {
                    speciesInUseFlags |= (1 << neighbors.getModelState(corner).getSpecies());
                }
            }

            
            // if no available mates, randomly choose a species 
            //that will not connect to what is surrounding
            int salt = pos.hashCode() & 15;;
            for(int i = 0; i < 16; i++)
            {
                int species = (i + salt) % 16;
                if((speciesInUseFlags & (1 << species)) == 0)
                {
                    return species;
                }
            }
            return 0;
        }
	}

	/**
	 * Handles placement of blocks that join together in appearance and have
	 * multiple block instances with different styles.
	 * */
//	public static class PlacementMasonry extends NicePlacement {
//
//		/**
//		 * Blocks that share substance and have one of styles given at
//		 * instantiation
//		 */
//		private final NiceStyle[] styles;
//
//		/** Blocks that share the same style and substance */
//		private NiceBlock[] siblingsCache;
//		private boolean isSiblingsCacheDone = false;
//
//		public PlacementMasonry(NiceStyle... styles) {
//			super();
//			this.styles = styles;
//		}
//
//		private NiceBlock[] getSiblings() {
//			// substances are grouped consistently across blocks, so the first substance
//			// is sufficient for finding sibling blocks.
//			if (!isSiblingsCacheDone) {
//				siblingsCache = new NiceBlock[styles.length];
//				for (int i = 0; i < styles.length; i++) {
//					siblingsCache[i] = NiceBlockRegistrar.getBlockForStyleAndMeta(styles[i], 0);
//				}
//				isSiblingsCacheDone = true;
//			}
//			return siblingsCache;
//		}
//
//		@Override
//		public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
//				float hitZ, int meta, EntityLivingBase placer) {
//
//			PlacementValidatorCubic shape = new PlacementValidatorCubic(2, 1, 1);
//
//			NeighborBlocks neighbors = new NeighborBlocks(worldIn, pos);
//			NeighborTestResults results = neighbors.getNeighborTestResults(
//					new TestForStyleGroupAndSubstance(worldIn, owner.getStateFromMeta(meta), pos, styles));
//
//			IBlockState candidate;
//
//			if (results.east()) {
//				candidate = neighbors.east();
//				if (shape.isValidShape(worldIn, pos, new TestForCompleteMatch(candidate))) {
//					return candidate;
//				}
//				;
//			}
//			if (results.west()) {
//				candidate = neighbors.west();
//				if (shape.isValidShape(worldIn, pos, new TestForCompleteMatch(candidate))) {
//					return candidate;
//				}
//			}
//			if (results.north()) {
//				candidate = neighbors.north();
//				if (shape.isValidShape(worldIn, pos, new TestForCompleteMatch(candidate))) {
//					return candidate;
//				}
//			}
//			if (results.south()) {
//				candidate = neighbors.south();
//				if (shape.isValidShape(worldIn, pos, new TestForCompleteMatch(candidate))) {
//					return candidate;
//				}
//			}
//			if (results.up()) {
//				candidate = neighbors.up();
//				if (shape.isValidShape(worldIn, pos, new TestForCompleteMatch(candidate))) {
//					return candidate;
//				}
//			}
//			if (results.down()) {
//				candidate = neighbors.down();
//				if (shape.isValidShape(worldIn, pos, new TestForCompleteMatch(candidate))) {
//					return candidate;
//				}
//			}
//
//			// if no available mates, try to choose a style that will not
//			// connect to what is surrounding
//			NeighborTestResults tests[] = new NeighborTestResults[getSiblings().length];
//			boolean match[] = new boolean[getSiblings().length];
//
//			for (int n = 0; n < getSiblings().length; n++) {
//				tests[n] = neighbors.getNeighborTestResults(new TestForCompleteMatch(getSiblings()[n]
//						.getStateFromMeta(meta)));
//				match[n] = tests[n].north() || tests[n].south() || tests[n].east() || tests[n].west() || tests[n].up()
//						|| tests[n].down();
//			}
//
//			for (int n = 0; n < getSiblings().length - 1; n++) {
//				if (match[n] && !match[n + 1]) {)
//					return getSiblings()[n + 1].getStateFromMeta(meta);
//				}
//			}
//			return getSiblings()[0].getStateFromMeta(meta);
//		}
//	}

//	/**
//	 * Handles placement of axis-aligned blocks like columns. Switches to
//	 * different style of block automatically based on axis of placement using
//	 * the styles passed in.
//	 */
//	public static class PlacementColumn extends NicePlacement {
//
//		private final NiceStyle styleX;
//		private final NiceStyle styleY;
//		private final NiceStyle styleZ;
//
//		private NiceBlock blockX;
//		private NiceBlock blockY;
//		private NiceBlock blockZ;
//		private boolean areSiblingBlocksFound = false;
//
//		public PlacementColumn(NiceStyle styleX, NiceStyle styleY, NiceStyle styleZ) {
//			super();
//			this.styleX = styleX;
//			this.styleY = styleY;
//			this.styleZ = styleZ;
//		}
//
//		@Override
//		public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
//				float hitZ, int meta, EntityLivingBase placer) {
//
//			/**
//			 * Have to do this now instead of during set owner because some
//			 * blocks may not yet be created at that time.
//			 */
//			if (!areSiblingBlocksFound) {
//				blockX = NiceBlockRegistrar.getBlockForStyleAndMeta(styleX, 0);
//				blockY = NiceBlockRegistrar.getBlockForStyleAndMeta(styleY, 0);
//				blockZ = NiceBlockRegistrar.getBlockForStyleAndMeta(styleZ, 0);
//				areSiblingBlocksFound = true;
//			}
//
//			switch (facing.getAxis()) {
//			case X:
//				return blockX.getStateFromMeta(meta);
//
//			case Y:
//				return blockY.getStateFromMeta(meta);
//
//			case Z:
//				return blockZ.getStateFromMeta(meta);
//
//			default:
//				// should be impossible to get here, but silences eclipse warning
//				return owner.getStateFromMeta(meta);
//			}
//		}
//	}
//
//	/**
//	 * For blocks that require no special handling.
//	 */
//	public static class PlacementSimple extends NicePlacement {
//
//		@Override
//		public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
//				float hitZ, int meta, EntityLivingBase placer) {
//
//			return owner.getStateFromMeta(meta);
//		}
//	}
}
