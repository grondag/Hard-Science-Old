package grondag.adversity.niceblocks;

import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.library.ShapeValidatorCubic;
import grondag.adversity.niceblocks.NiceBlock.TestForStyleAndSubstance;
import grondag.adversity.niceblocks.NiceBlock.TestForCompleteMatch;
import grondag.adversity.niceblocks.NiceBlock.TestForStyleGroupAndSubstance;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public abstract class NicePlacement {
	
	protected NiceBlock owner;
	
	/** call from Block class after setting up **/
	public abstract IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta,EntityLivingBase placer);
	
	/** call from Block class on initialization **/
	public void setOwner(NiceBlock owner) {
		this.owner = owner;
	}

	/** 
	 * Handles placement of blocks that join together in appearance
	 * and have multiple block instances with the same style.
	 * */
	public static class PlacementBigBlock extends NicePlacement{
		
		/** Blocks that share the same style and substance */
		private NiceBlock[] siblings;
		
		@Override
		public void setOwner(NiceBlock owner) {
			super.setOwner(owner);
			// substances are grouped consistently across blocks, so the first substance
			// is sufficient for finding sibling blocks.
			siblings = NiceBlockRegistrar.getBlocksForStyleAndSubstance(owner.style, owner.substances[0]).toArray(new NiceBlock[0]);
		}
		
		@Override
		public IBlockState onBlockPlaced(World worldIn, BlockPos pos,
				EnumFacing facing, float hitX, float hitY, float hitZ,
				int meta, EntityLivingBase placer) {
			
			ShapeValidatorCubic shape = new ShapeValidatorCubic(4, 4, 4);

			NeighborBlocks neighbors = new NeighborBlocks(worldIn, pos);
			NeighborTestResults results = neighbors.getNeighborTestResults(new NiceBlock.TestForStyleAndSubstance(owner.getStateFromMeta(meta)));

			IBlockState candidate;

			if(results.east){
				candidate = neighbors.east;
				if(shape.isValidShape(worldIn, pos, new TestForStyleAndSubstance(candidate), true)){
					return candidate;
				};
			} 
			if (results.west){
				candidate = neighbors.west;
				if(shape.isValidShape(worldIn, pos, new TestForStyleAndSubstance(candidate), true)){
					return candidate;		  
				}
			}	
			if (results.north){
				candidate = neighbors.north;
				if(shape.isValidShape(worldIn, pos, new TestForStyleAndSubstance(candidate), true)){
					return candidate;		  
				}
			} 
			if (results.south){
				candidate = neighbors.south;
				if(shape.isValidShape(worldIn, pos, new TestForStyleAndSubstance(candidate), true)){
					return candidate;		  
				}
			} 
			if (results.up){
				candidate = neighbors.up;
				if(shape.isValidShape(worldIn, pos, new TestForStyleAndSubstance(candidate), true)){
					return candidate;		  
				}
			}  
			if (results.down){
				candidate = neighbors.down;
				if(shape.isValidShape(worldIn, pos, new TestForStyleAndSubstance(candidate), true)){
					return candidate;		  
				}
			}


			// if no available mates, try to choose a style that will not connect to what is surrounding
			NeighborTestResults tests[] = new NeighborTestResults[siblings.length];
			boolean match[] = new boolean[siblings.length];

			for( int n = 0 ; n < siblings.length; n++){
				tests[n] = neighbors.getNeighborTestResults(new TestForCompleteMatch(siblings[n].getStateFromMeta(meta)));
				match[n] = tests[n].north || tests[n].south || tests[n].east || tests[n].west || tests[n].up || tests[n].down;
			}

			for( int n = 0 ; n < siblings.length - 1; n++){
				if(match[n] && !match[n+1]){
					return siblings[n+1].getStateFromMeta(meta);
				}
			}
			return siblings[0].getStateFromMeta(meta);			
		}
	}
	
	/** 
	 * Handles placement of blocks that join together in appearance
	 * and have multiple block instances with different styles.
	 * */
	public static class PlacementMasonry extends NicePlacement{
		
		/** Blocks that share substance and have one of styles given at instantiation*/
		private final NiceBlockStyle[] styles;
		private NiceBlock[] siblings;
		
		public PlacementMasonry(NiceBlockStyle... styles){
			super();
			this.styles = styles;
		}
		
		@Override
		public void setOwner(NiceBlock owner) {
			super.setOwner(owner);
			// Substances are grouped consistently across blocks, so the first substance
			// is sufficient for finding sibling blocks.
			super.setOwner(owner);
			siblings = new NiceBlock[styles.length];
			for(int i = 0; i < styles.length ; i++){
				siblings[i] = NiceBlockRegistrar.getBlockForStyleAndSubstance(styles[i], owner.substances[0]);
			}
		}
		
		@Override
		public IBlockState onBlockPlaced(World worldIn, BlockPos pos,
				EnumFacing facing, float hitX, float hitY, float hitZ,
				int meta, EntityLivingBase placer) {
			
			ShapeValidatorCubic shape = new ShapeValidatorCubic(2, 1, 1);

			NeighborBlocks neighbors = new NeighborBlocks(worldIn, pos);
			NeighborTestResults results = neighbors.getNeighborTestResults(new TestForStyleGroupAndSubstance(owner.getStateFromMeta(meta), styles));

			IBlockState candidate;

			if(results.east){
				candidate = neighbors.east;
				if(shape.isValidShape(worldIn, pos, new TestForStyleGroupAndSubstance(candidate, styles), true)){
					return candidate;
				};
			} 
			if (results.west){
				candidate = neighbors.west;
				if(shape.isValidShape(worldIn, pos, new TestForStyleGroupAndSubstance(candidate, styles), true)){
					return candidate;		  
				}
			}	
			if (results.north){
				candidate = neighbors.north;
				if(shape.isValidShape(worldIn, pos, new TestForStyleGroupAndSubstance(candidate, styles), true)){
					return candidate;		  
				}
			} 
			if (results.south){
				candidate = neighbors.south;
				if(shape.isValidShape(worldIn, pos, new TestForStyleGroupAndSubstance(candidate, styles), true)){
					return candidate;		  
				}
			} 
			if (results.up){
				candidate = neighbors.up;
				if(shape.isValidShape(worldIn, pos, new TestForStyleGroupAndSubstance(candidate, styles), true)){
					return candidate;		  
				}
			}  
			if (results.down){
				candidate = neighbors.down;
				if(shape.isValidShape(worldIn, pos, new TestForStyleGroupAndSubstance(candidate, styles), true)){
					return candidate;		  
				}
			}

			// if no available mates, try to choose a style that will not connect to what is surrounding
			NeighborTestResults tests[] = new NeighborTestResults[siblings.length];
			boolean match[] = new boolean[siblings.length];

			for( int n = 0 ; n < siblings.length; n++){
				tests[n] = neighbors.getNeighborTestResults(new TestForCompleteMatch(siblings[n].getStateFromMeta(meta)));
				match[n] = tests[n].north || tests[n].south || tests[n].east || tests[n].west || tests[n].up || tests[n].down;
			}

			for( int n = 0 ; n < siblings.length - 1; n++){
				if(match[n] && !match[n+1]){
					return siblings[n+1].getStateFromMeta(meta);
				}
			}
			return siblings[0].getStateFromMeta(meta);			
		}
	}
	
	
	/**
	 * Handles placement of axis-aligned blocks like columns.
	 * Switches to different style of block automatically based on axis
	 * of placement using the styles passed in.
	 */
	public static class PlacementColumn extends NicePlacement{
		
		private final NiceBlockStyle styleX;
		private final NiceBlockStyle styleY;
		private final NiceBlockStyle styleZ;
		
		private NiceBlock blockX;
		private NiceBlock blockY;
		private NiceBlock blockZ;
		
		public PlacementColumn(NiceBlockStyle styleX, NiceBlockStyle styleY, NiceBlockStyle styleZ ){
			super();
			this.styleX = styleX;
			this.styleY = styleY;
			this.styleZ = styleZ;
		}
		

		/** Find blocks for each style for later reuse.
		 * Have to do this now instead of initialization
		 * because some blocks may not yet be created at that time.
		 */
		@Override
		public void setOwner(NiceBlock owner) {
			super.setOwner(owner);
			blockX = NiceBlockRegistrar.getBlockForStyleAndSubstance(styleX, owner.substances[0]);
			blockY = NiceBlockRegistrar.getBlockForStyleAndSubstance(styleY, owner.substances[0]);
			blockZ = NiceBlockRegistrar.getBlockForStyleAndSubstance(styleZ, owner.substances[0]);
		}

		@Override
		public IBlockState onBlockPlaced(World worldIn, BlockPos pos,
				EnumFacing facing, float hitX, float hitY, float hitZ,
				int meta, EntityLivingBase placer) {

			switch(facing.getAxis()){
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
	public static class PlacementSimple extends NicePlacement{

		@Override
		public IBlockState onBlockPlaced(World worldIn, BlockPos pos,
				EnumFacing facing, float hitX, float hitY, float hitZ,
				int meta, EntityLivingBase placer) {
			
			return owner.getStateFromMeta(meta);

		}

		
	}
}
