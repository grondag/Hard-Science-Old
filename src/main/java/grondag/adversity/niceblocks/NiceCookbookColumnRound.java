package grondag.adversity.niceblocks;

import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.library.NeighborBlocks.NeighborSet;
import grondag.adversity.niceblocks.NiceBlock.TestForStyle;
import grondag.adversity.niceblocks.NiceCookbook.Ingredients;
import grondag.adversity.niceblocks.NiceCookbookAxisOriented.AxisAlignedModel;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;

public class NiceCookbookColumnRound extends NiceCookbookColumnSquare {

	public NiceCookbookColumnRound(Axis axis) {
		super(axis);
	}
	
	@Override
	protected void populateModelNames() {
		modelNames[AxisAlignedModel.FOUR_CLOSED.index] = "adversity:block/col_round_four_faces_full.obj";
		modelNames[AxisAlignedModel.FOUR_TOP_CLOSED.index] = "adversity:block/col_round_four_faces_half.obj";
		modelNames[AxisAlignedModel.FOUR_OPEN.index] = "adversity:block/col_round_four_faces.obj";
		
		modelNames[AxisAlignedModel.THREE_CLOSED.index] = "adversity:block/col_round_three_faces_full.obj";
		modelNames[AxisAlignedModel.THREE_TOP_CLOSED.index] = "adversity:block/col_round_three_faces_half.obj";
		modelNames[AxisAlignedModel.THREE_OPEN.index] = "adversity:block/col_round_three_faces.obj";
		
		modelNames[AxisAlignedModel.TWO_ADJACENT_CLOSED.index] = "adversity:block/col_round_adjacent_faces_full.obj";
		modelNames[AxisAlignedModel.TWO_ADJACENT_TOP_CLOSED.index] = "adversity:block/col_round_adjacent_faces_half.obj";
		modelNames[AxisAlignedModel.TWO_ADJACENT_OPEN.index] = "adversity:block/col_round_adjacent_faces.obj";
		
		modelNames[AxisAlignedModel.TWO_OPPOSITE_CLOSED.index] = "adversity:block/col_round_opposite_faces_full.obj";
		modelNames[AxisAlignedModel.TWO_OPPOSITE_TOP_CLOSED.index] = "adversity:block/col_round_opposite_faces_half.obj";
		modelNames[AxisAlignedModel.TWO_OPPOSITE_OPEN.index] = "adversity:block/col_round_opposite_faces.obj";
		
		modelNames[AxisAlignedModel.ONE_CLOSED.index] = "adversity:block/col_round_single_face_full.obj";
		modelNames[AxisAlignedModel.ONE_TOP_CLOSED.index] = "adversity:block/col_round_single_face_half.obj";
		modelNames[AxisAlignedModel.ONE_OPEN.index] = "adversity:block/col_round_single_face.obj";
		
		modelNames[AxisAlignedModel.NONE_CLOSED.index] = "adversity:block/col_round_no_faces_full.obj";
		modelNames[AxisAlignedModel.NONE_TOP_CLOSED.index] = "adversity:block/col_round_no_faces_half.obj";
		modelNames[AxisAlignedModel.NONE_OPEN.index] = "adversity:block/col_round_no_faces.obj";
	}
	
	@Override
	public Ingredients getIngredients(NiceSubstance substance, int recipe, int alternate) {

		String modelName = modelNames[MODEL_INDEX[recipe]];
		
		int baseOffset = (style.textureCount * calcAlternate(alternate)) + style.textureIndex;
		Map<String, String> textures = Maps.newHashMap();

		textures.put("#all", style.buildTextureName(substance, baseOffset));
		
		return new Ingredients(modelName, textures, ROTATION_LOOKUP[recipe]);
	}
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public int getModelIndex(IExtendedBlockState state, IBlockAccess worldIn, BlockPos pos) {
		
		NeighborBlocks neighbors = new NeighborBlocks(worldIn, pos);
		TestForStyle styleTest = new TestForStyle(state);
		
		NeighborTestResults tests = neighbors.getNeighborTestResults(styleTest);
	
		int up = tests.up ? 1 : 0;
		int down = tests.down ? 1 : 0;
		int east = tests.east ? 1 : 0;
		int west = tests.west ? 1 : 0;
		int north = tests.north ? 1 : 0;
		int south = tests.south ? 1 : 0;
		
		/**
		 * The column caps have a large poly count and I don't think MC can do occlusion culling on them
		 * automatically, so select a model without them if an adjacent block would occlude them.
		 * Because columns are commonly stacked along an axis, we test for them specifically in addition to
		 * normal cubes, but because model shape varies for columns, we have to test for the neighbors of the 
		 * adjacent column block to know if it would occlude this one fully.
		 */
		switch (style){
		case COLUMN_ROUND_X:
			if(tests.east){
				NeighborTestResults faceTests = (new NeighborBlocks(worldIn, pos.east(), NeighborSet.X_NORMALS)).getNeighborTestResults(styleTest);
				if(!(faceTests.up == tests.up && faceTests.down == tests.down && faceTests.north == tests.north && faceTests.south == tests.south)){
					east = 0;
				}
			} else {
				if(neighbors.east.getBlock().isOpaqueCube()){
					east = 1;
				}
			}
			
			if(tests.west){
				NeighborTestResults faceTests = (new NeighborBlocks(worldIn, pos.west(), NeighborSet.X_NORMALS)).getNeighborTestResults(styleTest);
				if(!(faceTests.up == tests.up && faceTests.down == tests.down && faceTests.north == tests.north && faceTests.south == tests.south)){
					west = 0;
				}
			} else {
				if(neighbors.west.getBlock().isOpaqueCube()){
					west = 1;
				}
			}
			
			break;
			
		case COLUMN_ROUND_Y:
			if(tests.up){
				NeighborTestResults faceTests = (new NeighborBlocks(worldIn, pos.up(), NeighborSet.Y_NORMALS)).getNeighborTestResults(styleTest);
				if(!(faceTests.east == tests.east && faceTests.west == tests.west && faceTests.north == tests.north && faceTests.south == tests.south)){
					up = 0;
				}
			} else {
				if(neighbors.up.getBlock().isOpaqueCube()){
					up = 1;
				}
			}
			
			if(tests.down){
				NeighborTestResults faceTests = (new NeighborBlocks(worldIn, pos.down(), NeighborSet.Y_NORMALS)).getNeighborTestResults(styleTest);
				if(!(faceTests.east == tests.east && faceTests.west == tests.west && faceTests.north == tests.north && faceTests.south == tests.south)){
					down = 0;
				}
			} else {
				if(neighbors.down.getBlock().isOpaqueCube()){
					down = 1;
				}
			}
			
			break;
			
		case COLUMN_ROUND_Z:
			if(tests.north){
				NeighborTestResults faceTests = (new NeighborBlocks(worldIn, pos.north(), NeighborSet.Z_NORMALS)).getNeighborTestResults(styleTest);
				if(!(faceTests.up == tests.up && faceTests.down == tests.down && faceTests.east == tests.east && faceTests.west == tests.west)){
					north = 0;
				}
			} else {
				if(neighbors.north.getBlock().isOpaqueCube()){
					north = 1;
				}
			}
			
			if(tests.south){
				NeighborTestResults faceTests = (new NeighborBlocks(worldIn, pos.south(), NeighborSet.Z_NORMALS)).getNeighborTestResults(styleTest);
				if(!(faceTests.up == tests.up && faceTests.down == tests.down && faceTests.east == tests.east && faceTests.west == tests.west)){
					south = 0;
				}
			} else {
				if(neighbors.south.getBlock().isOpaqueCube()){
					south = 1;
				}
			}
			
			break;
		}
		
		
		return  RECIPE_LOOKUP[up][down][east][west][north][south];

	}

}
