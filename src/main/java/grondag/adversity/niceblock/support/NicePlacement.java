package grondag.adversity.niceblock.support;

import java.util.Random;

import org.apache.commons.lang3.BitField;

import grondag.adversity.library.IBlockTest;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.library.PlacementValidatorCubic;
import grondag.adversity.niceblock.NiceStyle;
import grondag.adversity.niceblock.newmodel.BlockModelHelper;
import grondag.adversity.niceblock.newmodel.BlockTests;
import grondag.adversity.niceblock.newmodel.ModelReference;
import grondag.adversity.niceblock.newmodel.NiceBlock;
import grondag.adversity.niceblock.newmodel.NiceBlockRegistrar;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;


/**
 * Specialized onBlockPlaced event handlers to enable building of
 * decorative multiblocks with connected textures/geometry.
 */
public abstract class NicePlacement {
    

//	/** call from Block class after setting up **/
//	public abstract IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
//			float hitZ, int meta, EntityLivingBase placer);

	public abstract int getMetaForPlacedStack(World worldIn, BlockPos pos, EnumFacing facing, ItemStack stack, BlockModelHelper helper);
	
//	/** convenience factory method */
//	public static NicePlacement makeMasonryPlacer() {
//		return new PlacementMasonry(NiceStyle.MASONRY_A, NiceStyle.MASONRY_B,
//				NiceStyle.MASONRY_C, NiceStyle.MASONRY_D, NiceStyle.MASONRY_E);
//	}

//	/** convenience factory method */
//	public static NicePlacement makeColumnPlacerRound() {
//		return new PlacementColumn(NiceStyle.COLUMN_ROUND_X, NiceStyle.COLUMN_ROUND_Y,
//				NiceStyle.COLUMN_ROUND_Z);
//	}
//
//	/** convenience factory method */
//	public static NicePlacement makeColumnPlacerSquare() {
//		return new PlacementColumn(NiceStyle.COLUMN_SQUARE_X, NiceStyle.COLUMN_SQUARE_Y,
//				NiceStyle.COLUMN_SQUARE_Z);
//	}


	/**
	 * Handles placement of blocks that join together in appearance and have
	 * multiple block instances with the same meta.
	 * */
	public static class PlacementBigBlock extends NicePlacement {

	    private final PlacementValidatorCubic shape;
	    
        public PlacementBigBlock()
        {
            this(new PlacementValidatorCubic(4, 4, 4));
        }

	    public PlacementBigBlock(PlacementValidatorCubic shape){
	        this.shape = shape;
	    }
	    
		@Override
		public int getMetaForPlacedStack(World worldIn, BlockPos pos, EnumFacing facing, ItemStack stack, BlockModelHelper helper)
		{
		    int colorIndex = helper.getColorIndexFromItemStack(stack);
            int speciesInUseFlags = 0;
            int species;
            NeighborBlocks neighbors = new NeighborBlocks(worldIn, pos);
			NeighborTestResults results = neighbors.getNeighborTestResults(new BlockTests.TestForBlockColorMatch(helper.block, colorIndex));
			
			for(EnumFacing face : EnumFacing.VALUES)		    
			{
		         if (results.result(face)) 
		         {
		             species = neighbors.getByFace(face).getValue(NiceBlock.META);
		             speciesInUseFlags |= (1 << species);
		             if (shape.isValidShape(worldIn, pos, new BlockTests.TestForBigBlockMatch(helper.block, colorIndex, species))) {
		                 return species;
		             }
		         }
			}

            // try to avoid corners also if picking a species that won't connect
			if (results.downEast()) speciesInUseFlags |= (1 << neighbors.downEast().getValue(NiceBlock.META));
            if (results.downNorth()) speciesInUseFlags |= (1 << neighbors.downNorth().getValue(NiceBlock.META));
            if (results.downSouth()) speciesInUseFlags |= (1 << neighbors.downSouth().getValue(NiceBlock.META));
            if (results.downWest()) speciesInUseFlags |= (1 << neighbors.downWest().getValue(NiceBlock.META));

            if (results.upEast()) speciesInUseFlags |= (1 << neighbors.upEast().getValue(NiceBlock.META));
            if (results.upNorth()) speciesInUseFlags |= (1 << neighbors.upNorth().getValue(NiceBlock.META));
            if (results.upSouth()) speciesInUseFlags |= (1 << neighbors.upSouth().getValue(NiceBlock.META));
            if (results.upWest()) speciesInUseFlags |= (1 << neighbors.upWest().getValue(NiceBlock.META));

            if (results.northEast()) speciesInUseFlags |= (1 << neighbors.northEast().getValue(NiceBlock.META));
            if (results.northWest()) speciesInUseFlags |= (1 << neighbors.northWest().getValue(NiceBlock.META));
            if (results.southEast()) speciesInUseFlags |= (1 << neighbors.southEast().getValue(NiceBlock.META));
            if (results.southWest()) speciesInUseFlags |= (1 << neighbors.southWest().getValue(NiceBlock.META));

            
            // if no available mates, randomly choose a species 
			//that will not connect to what is surrounding
			int salt = ModelReference.SALT_SHAKER.nextInt(16);
			for(int i = 0; i < 16; i++)
			{
			    species = (i + salt) % 16;
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
