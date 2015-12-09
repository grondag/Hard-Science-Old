package grondag.adversity.niceblocks.client;

import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblocks.NiceSubstance;
import grondag.adversity.niceblocks.NiceBlock.TestForStyle;
import grondag.adversity.niceblocks.client.NiceCookbook.Ingredients;

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
	
	private static final String[] MODEL_LOOKUP = {
		"adversity:block/col_round_single_face.obj", "adversity:block/col_round_single_face.obj", "adversity:block/col_round_single_face.obj", "adversity:block/col_round_single_face.obj", "adversity:block/col_round_adjacent_faces.obj", "adversity:block/col_round_adjacent_faces.obj", "adversity:block/col_round_adjacent_faces.obj", "adversity:block/col_round_adjacent_faces.obj",
		"adversity:block/col_round_opposite_faces.obj", "adversity:block/col_round_opposite_faces.obj", "adversity:block/col_round_three_faces.obj", "adversity:block/col_round_three_faces.obj", "adversity:block/col_round_three_faces.obj", "adversity:block/col_round_three_faces.obj", "adversity:block/col_round_four_faces.obj", "adversity:block/col_round_no_faces.obj",
		"adversity:block/col_round_single_face_half.obj", "adversity:block/col_round_single_face_half.obj", "adversity:block/col_round_single_face_half.obj", "adversity:block/col_round_single_face_half.obj", "adversity:block/col_round_adjacent_faces_half.obj", "adversity:block/col_round_adjacent_faces_half.obj", "adversity:block/col_round_adjacent_faces_half.obj", "adversity:block/col_round_adjacent_faces_half.obj",
		"adversity:block/col_round_opposite_faces_half.obj", "adversity:block/col_round_opposite_faces_half.obj", "adversity:block/col_round_three_faces_half.obj", "adversity:block/col_round_three_faces_half.obj", "adversity:block/col_round_three_faces_half.obj", "adversity:block/col_round_three_faces_half.obj", "adversity:block/col_round_four_faces_half.obj", "adversity:block/col_round_no_faces_half.obj",
		"adversity:block/col_round_single_face_half.obj", "adversity:block/col_round_single_face_half.obj", "adversity:block/col_round_single_face_half.obj", "adversity:block/col_round_single_face_half.obj", "adversity:block/col_round_adjacent_faces_half.obj", "adversity:block/col_round_adjacent_faces_half.obj", "adversity:block/col_round_adjacent_faces_half.obj", "adversity:block/col_round_adjacent_faces_half.obj",
		"adversity:block/col_round_opposite_faces_half.obj", "adversity:block/col_round_opposite_faces_half.obj", "adversity:block/col_round_three_faces_half.obj", "adversity:block/col_round_three_faces_half.obj", "adversity:block/col_round_three_faces_half.obj", "adversity:block/col_round_three_faces_half.obj", "adversity:block/col_round_four_faces_half.obj", "adversity:block/col_round_no_faces_half.obj",
		"adversity:block/col_round_single_face_full.obj", "adversity:block/col_round_single_face_full.obj", "adversity:block/col_round_single_face_full.obj", "adversity:block/col_round_single_face_full.obj", "adversity:block/col_round_adjacent_faces_full.obj", "adversity:block/col_round_adjacent_faces_full.obj", "adversity:block/col_round_adjacent_faces_full.obj", "adversity:block/col_round_adjacent_faces_full.obj",
		"adversity:block/col_round_opposite_faces_full.obj", "adversity:block/col_round_opposite_faces_full.obj", "adversity:block/col_round_three_faces_full.obj", "adversity:block/col_round_three_faces_full.obj", "adversity:block/col_round_three_faces_full.obj", "adversity:block/col_round_three_faces_full.obj", "adversity:block/col_round_four_faces_full.obj", "adversity:block/col_round_no_faces_full.obj"
	};
	
	
	@Override
	public Ingredients getIngredients(NiceSubstance substance, int recipe, int alternate) {

		String modelName = MODEL_LOOKUP[recipe];
		
		int baseOffset = (style.textureCount * calcAlternate(alternate)) + style.textureIndex;
		Map<String, String> textures = Maps.newHashMap();

		textures.put("#all", style.buildTextureName(substance, baseOffset));
		
		return new Ingredients(modelName, textures, 
				 TRSRTransformation.blockCenterToCorner(new TRSRTransformation(null, ROTATION_LOOKUP[recipe], null, null)));
	}
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public int getModelIndex(IExtendedBlockState state, IBlockAccess worldIn, BlockPos pos) {
		
		NeighborBlocks neighbors = new NeighborBlocks(worldIn, pos);
		
		NeighborTestResults tests = neighbors.getNeighborTestResults(new TestForStyle(state));
	
		int up = tests.up ? 1 : 0;
		int down = tests.down ? 1 : 0;
		int east = tests.east ? 1 : 0;
		int west = tests.west ? 1 : 0;
		int north = tests.north ? 1 : 0;
		int south = tests.south ? 1 : 0;
		
		switch (style){
		case COLUMN_ROUND_X:
			
		case COLUMN_ROUND_Y:
			if(!tests.up){
				if(neighbors.up.getBlock().isOpaqueCube()){
					up = 1;
				}
			}
			
			if(!tests.down){
				if(neighbors.down.getBlock().isOpaqueCube()){
					down = 1;
				}
			}
			
		case COLUMN_ROUND_Z:
			
		}
		
		
		return  RECIPE_LOOKUP[up][down][east][west][north][south];

	}

}
