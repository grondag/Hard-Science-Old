package grondag.adversity.niceblocks.client;

import grondag.adversity.library.IBlockTest;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;

import grondag.adversity.niceblocks.NiceBlock;
import grondag.adversity.niceblocks.NiceBlock.TestForCompleteMatch;
import grondag.adversity.niceblocks.NiceBlock.TestForStyle;
import grondag.adversity.niceblocks.NiceBlock.TestForSubstance;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;

public class NiceCookbooks {

	public final static INiceCookbook simple = new Simple();
	public final static INiceCookbook bigBlocks = new ComplexConnected();	
	public final static INiceCookbook masonry = new Masonry();	
	
	private static class Simple implements INiceCookbook{

		@Override
		public  int getRecipeCount() {
			return 1;
		}

		@Override
		public IExtendedBlockState getExtendedState(IExtendedBlockState state, IBlockAccess worldIn, BlockPos pos) {
			// TODO Make it do stuff
			return null;
		}
		
	}
	
	private static class ComplexConnected implements INiceCookbook{
		
		@Override
		public  int getRecipeCount() {
			return 386;
		}

		@Override
		public IExtendedBlockState getExtendedState(IExtendedBlockState state, IBlockAccess worldIn, BlockPos pos) {
			
			NiceBlock.TestForCompleteMatch test = new TestForCompleteMatch(state);

			NeighborBlocks neighbors = new NeighborBlocks(worldIn, pos);
			NeighborTestResults mates = neighbors.getNeighborTestResults(test);

			NiceBlockData.CornerJoin join = NiceBlockData.CORNER_JOIN_LOOKUP[mates.up?1:0][mates.down?1:0]
					[mates.east?1:0][mates.west?1:0] 
							[mates.north?1:0][mates.south?1:0];
			
			int alt = NiceBlockData.textureMix12[pos.getX() & 15][pos.getY() & 15][pos.getZ() & 15];
			
			if(join.hasTests){
				return state.withProperty(NiceBlock.PROP_RECIPE, join.getOffsetID(test, worldIn, pos)).withProperty(NiceBlock.PROP_ALTERNATE, alt);
			} else {
				return state.withProperty(NiceBlock.PROP_RECIPE, join.ID).withProperty(NiceBlock.PROP_ALTERNATE, alt);
			}
		}
	}
	
	private static class Masonry implements INiceCookbook{
		
		@Override
		public  int getRecipeCount() {
			return 64;
		}

		@Override
		public IExtendedBlockState getExtendedState(IExtendedBlockState state, IBlockAccess worldIn, BlockPos pos) {

			NeighborBlocks neighbors = new NeighborBlocks(worldIn, pos);
			NeighborTestResults mates = neighbors.getNeighborTestResults(new TestForCompleteMatch(state));
			NeighborTestResults masonry = neighbors.getNeighborTestResults(new TestForStyle(state));
			NeighborTestResults thisSubstance = neighbors.getNeighborTestResults(new TestForSubstance(state));
	
			int scenario = NiceBlockData.SIMPLE_JOIN_LOOKUP[0][thisSubstance.down && !mates.down?1:0]  					// UP DOWN
					[(thisSubstance.east && !masonry.east) || (masonry.east && !mates.east)?1:0] 		// EAST
					[thisSubstance.west && !masonry.west?1:0]  											// WEST
					[(thisSubstance.north && !masonry.north) || (masonry.north && !mates.north)?1:0]	// NORTH								// NORTH
					[thisSubstance.south && !masonry.south?1:0]; 											// SOUTH
	
			return state.withProperty(NiceBlock.PROP_RECIPE, scenario);		
		}
	}
	
}
