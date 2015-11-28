package grondag.adversity.niceblocks.client;

import java.util.Map;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Quat4f;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblocks.NiceBlockStyle;
import grondag.adversity.niceblocks.NiceSubstance;
import grondag.adversity.niceblocks.NiceBlock.TestForCompleteMatch;
import grondag.adversity.niceblocks.NiceBlock.TestForSubstance;
import grondag.adversity.niceblocks.NiceBlock.TestForStyle;
import grondag.adversity.niceblocks.client.NiceCookbook.Ingredients;
import grondag.adversity.niceblocks.client.NiceCookbook.Rotation;
import grondag.adversity.niceblocks.client.NiceCookbook.TextureOffset;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;

public class NiceCookbookColumn extends NiceCookbook{

	private final  Integer[][][][][][] RECIPE_LOOKUP = new Integer[2][2][2][2][2][2];
	private final Quat4f[] ROTATION_LOOKUP;
	private final Vec3[] ROTATION_LOOKUP_Y = {
			new Vec3(0.0, 90.0, 0.0 ), new Vec3(0.0, 270.0, 0.0 ), new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0, 180.0, 0.0 ), 
			new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0, 270.0, 0.0 ), new Vec3(0.0, 90.0, 0.0 ), new Vec3(0.0, 180.0, 0.0 ), 
			new Vec3(0.0,0.0, 0.0 ), new Vec3(0.0, 90.0, 0.0 ), new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0, 180.0, 0.0 ), 
			new Vec3(0.0, 90.0, 0.0 ), new Vec3(0.0, 270.0, 0.0 ), new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0, 0.0, 0.0 ), 
			new Vec3(0.0, 90.0, 0.0 ), new Vec3(0.0, 270.0, 0.0 ), new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0, 180.0, 0.0 ), 
			new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0, 270.0, 0.0), new Vec3(0.0, 90.0, 0.0 ), new Vec3(0.0, 180.0, 0.0 ), 
			new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0, 90.0, 0.0 ), new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0, 180.0, 0.0 ), 
			new Vec3(0.0, 90.0, 0.0 ), new Vec3(0.0, 270.0, 0.0 ), new Vec3(0.0,0.0, 0.0 ), new Vec3(0.0, 0.0, 0.0 ), 
			new Vec3(180.0, 270.0, 0.0 ), new Vec3(180.0, 90.0, 0.0 ), new Vec3(180.0, 180.0,0.0 ), new Vec3(180.0, 0.0, 0.0 ), 
			new Vec3(180.0, 270.0, 0.0 ), new Vec3(180.0, 180.0, 0.0 ), new Vec3(180.0, 0.0, 0.0), new Vec3(180.0, 90.0, 0.0 ), 
			new Vec3(180.0, 0.0, 0.0 ), new Vec3(180.0, 90.0, 0.0 ), new Vec3(180.0, 180.0, 0.0 ),new Vec3(180.0, 0.0, 0.0 ), 
			new Vec3(180.0, 270.0, 0.0 ), new Vec3(180.0, 90.0, 0.0 ), new Vec3(180.0, 0.0, 0.0 ), new Vec3(180.0, 0.0, 0.0 ), 
			new Vec3(0.0, 90.0, 0.0 ), new Vec3(0.0, 270.0, 0.0 ), new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0,180.0, 0.0 ), 
			new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0, 270.0, 0.0 ), new Vec3(0.0, 90.0, 0.0 ), new Vec3(0.0, 180.0, 0.0), 
			new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0, 90.0, 0.0 ), new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0, 180.0, 0.0 ), 
			new Vec3(0.0, 90.0, 0.0 ), new Vec3(0.0, 270.0, 0.0 ), new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0, 0.0, 0.0 )
	};

	private final  String[] MODEL_LOOKUP = {
			"adversity:block/column_single_face", "adversity:block/column_single_face", "adversity:block/column_single_face", "adversity:block/column_single_face", "adversity:block/column_adjacent_faces", "adversity:block/column_adjacent_faces", "adversity:block/column_adjacent_faces", "adversity:block/column_adjacent_faces",
			"adversity:block/column_opposite_faces", "adversity:block/column_opposite_faces", "adversity:block/column_three_faces", "adversity:block/column_three_faces", "adversity:block/column_three_faces", "adversity:block/column_three_faces", "adversity:block/column_four_faces", "adversity:block/column_no_faces",
			"adversity:block/column_single_face_half", "adversity:block/column_single_face_half", "adversity:block/column_single_face_half", "adversity:block/column_single_face_half", "adversity:block/column_adjacent_faces_half", "adversity:block/column_adjacent_faces_half", "adversity:block/column_adjacent_faces_half", "adversity:block/column_adjacent_faces_half",
			"adversity:block/column_opposite_faces_half", "adversity:block/column_opposite_faces_half", "adversity:block/column_three_faces_half", "adversity:block/column_three_faces_half", "adversity:block/column_three_faces_half", "adversity:block/column_three_faces_half", "adversity:block/column_four_faces_half", "adversity:block/column_no_faces_half",
			"adversity:block/column_single_face_half", "adversity:block/column_single_face_half", "adversity:block/column_single_face_half", "adversity:block/column_single_face_half", "adversity:block/column_adjacent_faces_half", "adversity:block/column_adjacent_faces_half", "adversity:block/column_adjacent_faces_half", "adversity:block/column_adjacent_faces_half",
			"adversity:block/column_opposite_faces_half", "adversity:block/column_opposite_faces_half", "adversity:block/column_three_faces_half", "adversity:block/column_three_faces_half", "adversity:block/column_three_faces_half", "adversity:block/column_three_faces_half", "adversity:block/column_four_faces_half", "adversity:block/column_no_faces_half",
			"adversity:block/column_single_face_full", "adversity:block/column_single_face_full", "adversity:block/column_single_face_full", "adversity:block/column_single_face_full", "adversity:block/column_adjacent_faces_full", "adversity:block/column_adjacent_faces_full", "adversity:block/column_adjacent_faces_full", "adversity:block/column_adjacent_faces_full",
			"adversity:block/column_opposite_faces_full", "adversity:block/column_opposite_faces_full", "adversity:block/column_three_faces_full", "adversity:block/column_three_faces_full", "adversity:block/column_three_faces_full", "adversity:block/column_three_faces_full", "adversity:block/column_four_faces_full", "adversity:block/column_no_faces_full"
	};
	
	
	@Override
	public int getRecipeCount() {
		// TODO Auto-generated method stub
		return 64;
	}


	@Override
	public Ingredients getIngredients(NiceSubstance substance, int recipe, int alternate) {

		String modelName = MODEL_LOOKUP[recipe];
		
		int baseOffset = (style.textureCount * alternate) + style.textureIndex;
		Map<String, String> textures = Maps.newHashMap();

//        "cap_inner_side": "adversity:blocks/basalt/basalt_0_4",
//        "cap_no_neighbors": "adversity:blocks/basalt/basalt_0_5",

		textures.put("inner", style.buildTextureName(substance, baseOffset + 0));
		textures.put("outer", style.buildTextureName(substance, baseOffset - 1));
		textures.put("column_face", style.buildTextureName(substance, baseOffset + 7));
		textures.put("cap_opposite_neighbors", style.buildTextureName(substance, baseOffset + 7));
		textures.put("cap_three_neighbors", style.buildTextureName(substance, baseOffset + 6));
		textures.put("cap_adjacent_neighbors", style.buildTextureName(substance, baseOffset + 2));
		textures.put("cap_one_neighbor", style.buildTextureName(substance, baseOffset + 3));
		textures.put("cap_four_neighbors", style.buildTextureName(substance, baseOffset + 1));
		textures.put("cap_no_neighbors", style.buildTextureName(substance, baseOffset + 5));
		textures.put("cap_inner_side", style.buildTextureName(substance, baseOffset + 4));
		
		return new Ingredients(modelName, textures, new TRSRTransformation(null, ROTATION_LOOKUP[recipe], null, null));
	}

	


	
	@Override
	public int getModelIndex(IExtendedBlockState state, IBlockAccess worldIn, BlockPos pos) {
		
		NeighborTestResults tests = new NeighborBlocks(worldIn, pos).getNeighborTestResults(new TestForStyle(state));
		
		return  RECIPE_LOOKUP[tests.up?1:0][tests.down?1:0][tests.east?1:0][tests.west?1:0][tests.north?1:0][tests.south?1:0];

	}

	
	public NiceCookbookColumn (EnumFacing.Axis axis){
		super();
		
		switch (axis){
		case X:
			ROTATION_LOOKUP = new Quat4f[64];
			for(int i=0; i < 64; i++){
				Quat4f rotation = new Quat4f(0, 0, 0, 1);
				rotation.mul(rotationForAxis(Axis.X, ROTATION_LOOKUP_Y[i].yCoord));
				rotation.mul(rotationForAxis(Axis.Y, ROTATION_LOOKUP_Y[i].xCoord));
				rotation.mul(rotationForAxis(Axis.Z, 90.0));
				ROTATION_LOOKUP[i] = rotation;
			}
			
			RECIPE_LOOKUP[0][1][1][1][1][1]=0;
			RECIPE_LOOKUP[1][0][1][1][1][1]=1;
			RECIPE_LOOKUP[1][1][1][1][0][1]=2;
			RECIPE_LOOKUP[1][1][1][1][1][0]=3;
			RECIPE_LOOKUP[0][1][1][1][0][1]=4;
			RECIPE_LOOKUP[1][0][1][1][0][1]=5;
			RECIPE_LOOKUP[0][1][1][1][1][0]=6;
			RECIPE_LOOKUP[1][0][1][1][1][0]=7;
			RECIPE_LOOKUP[0][0][1][1][1][1]=8;
			RECIPE_LOOKUP[1][1][1][1][0][0]=9;
			RECIPE_LOOKUP[0][0][1][1][0][1]=10;
			RECIPE_LOOKUP[0][0][1][1][1][0]=11;
			RECIPE_LOOKUP[0][1][1][1][0][0]=12;
			RECIPE_LOOKUP[1][0][1][1][0][0]=13;
			RECIPE_LOOKUP[0][0][1][1][0][0]=14;
			RECIPE_LOOKUP[1][1][1][1][1][1]=15;
			RECIPE_LOOKUP[0][1][1][0][1][1]=16;
			RECIPE_LOOKUP[1][0][1][0][1][1]=17;
			RECIPE_LOOKUP[1][1][1][0][0][1]=18;
			RECIPE_LOOKUP[1][1][1][0][1][0]=19;
			RECIPE_LOOKUP[0][1][1][0][0][1]=20;
			RECIPE_LOOKUP[1][0][1][0][0][1]=21;
			RECIPE_LOOKUP[0][1][1][0][1][0]=22;
			RECIPE_LOOKUP[1][0][1][0][1][0]=23;
			RECIPE_LOOKUP[0][0][1][0][1][1]=24;
			RECIPE_LOOKUP[1][1][1][0][0][0]=25;
			RECIPE_LOOKUP[0][0][1][0][0][1]=26;
			RECIPE_LOOKUP[0][0][1][0][1][0]=27;
			RECIPE_LOOKUP[0][1][1][0][0][0]=28;
			RECIPE_LOOKUP[1][0][1][0][0][0]=29;
			RECIPE_LOOKUP[0][0][1][0][0][0]=30;
			RECIPE_LOOKUP[1][1][1][0][1][1]=31;
			RECIPE_LOOKUP[0][1][0][1][1][1]=32;
			RECIPE_LOOKUP[1][0][0][1][1][1]=33;
			RECIPE_LOOKUP[1][1][0][1][0][1]=34;
			RECIPE_LOOKUP[1][1][0][1][1][0]=35;
			RECIPE_LOOKUP[0][1][0][1][0][1]=36;
			RECIPE_LOOKUP[1][0][0][1][0][1]=37;
			RECIPE_LOOKUP[0][1][0][1][1][0]=38;
			RECIPE_LOOKUP[1][0][0][1][1][0]=39;
			RECIPE_LOOKUP[0][0][0][1][1][1]=40;
			RECIPE_LOOKUP[1][1][0][1][0][0]=41;
			RECIPE_LOOKUP[0][0][0][1][0][1]=42;
			RECIPE_LOOKUP[0][0][0][1][1][0]=43;
			RECIPE_LOOKUP[0][1][0][1][0][0]=44;
			RECIPE_LOOKUP[1][0][0][1][0][0]=45;
			RECIPE_LOOKUP[0][0][0][1][0][0]=46;
			RECIPE_LOOKUP[1][1][0][1][1][1]=47;
			RECIPE_LOOKUP[0][1][0][0][1][1]=48;
			RECIPE_LOOKUP[1][0][0][0][1][1]=49;
			RECIPE_LOOKUP[1][1][0][0][0][1]=50;
			RECIPE_LOOKUP[1][1][0][0][1][0]=51;
			RECIPE_LOOKUP[0][1][0][0][0][1]=52;
			RECIPE_LOOKUP[1][0][0][0][0][1]=53;
			RECIPE_LOOKUP[0][1][0][0][1][0]=54;
			RECIPE_LOOKUP[1][0][0][0][1][0]=55;
			RECIPE_LOOKUP[0][0][0][0][1][1]=56;
			RECIPE_LOOKUP[1][1][0][0][0][0]=57;
			RECIPE_LOOKUP[0][0][0][0][0][1]=58;
			RECIPE_LOOKUP[0][0][0][0][1][0]=59;
			RECIPE_LOOKUP[0][1][0][0][0][0]=60;
			RECIPE_LOOKUP[1][0][0][0][0][0]=61;
			RECIPE_LOOKUP[0][0][0][0][0][0]=62;
			RECIPE_LOOKUP[1][1][0][0][1][1]=63;
			break;
			
		case Y:
			ROTATION_LOOKUP = new Quat4f[64];
			for(int i=0; i < 64; i++){
				Quat4f rotation = new Quat4f(0, 0, 0, 1);
				rotation.mul(rotationForAxis(Axis.Y, -ROTATION_LOOKUP_Y[i].yCoord));
				rotation.mul(rotationForAxis(Axis.X, ROTATION_LOOKUP_Y[i].xCoord));
				ROTATION_LOOKUP[i] = rotation;
			}
			
			RECIPE_LOOKUP[1][1][0][1][1][1]=0;
			RECIPE_LOOKUP[1][1][1][0][1][1]=1;
			RECIPE_LOOKUP[1][1][1][1][0][1]=2;
			RECIPE_LOOKUP[1][1][1][1][1][0]=3;
			RECIPE_LOOKUP[1][1][0][1][0][1]=4;
			RECIPE_LOOKUP[1][1][1][0][0][1]=5;
			RECIPE_LOOKUP[1][1][0][1][1][0]=6;
			RECIPE_LOOKUP[1][1][1][0][1][0]=7;
			RECIPE_LOOKUP[1][1][0][0][1][1]=8;
			RECIPE_LOOKUP[1][1][1][1][0][0]=9;
			RECIPE_LOOKUP[1][1][0][0][0][1]=10;
			RECIPE_LOOKUP[1][1][0][0][1][0]=11;
			RECIPE_LOOKUP[1][1][0][1][0][0]=12;
			RECIPE_LOOKUP[1][1][1][0][0][0]=13;
			RECIPE_LOOKUP[1][1][0][0][0][0]=14;
			RECIPE_LOOKUP[1][1][1][1][1][1]=15;
			RECIPE_LOOKUP[0][1][0][1][1][1]=16;
			RECIPE_LOOKUP[0][1][1][0][1][1]=17;
			RECIPE_LOOKUP[0][1][1][1][0][1]=18;
			RECIPE_LOOKUP[0][1][1][1][1][0]=19;
			RECIPE_LOOKUP[0][1][0][1][0][1]=20;
			RECIPE_LOOKUP[0][1][1][0][0][1]=21;
			RECIPE_LOOKUP[0][1][0][1][1][0]=22;
			RECIPE_LOOKUP[0][1][1][0][1][0]=23;
			RECIPE_LOOKUP[0][1][0][0][1][1]=24;
			RECIPE_LOOKUP[0][1][1][1][0][0]=25;
			RECIPE_LOOKUP[0][1][0][0][0][1]=26;
			RECIPE_LOOKUP[0][1][0][0][1][0]=27;
			RECIPE_LOOKUP[0][1][0][1][0][0]=28;
			RECIPE_LOOKUP[0][1][1][0][0][0]=29;
			RECIPE_LOOKUP[0][1][0][0][0][0]=30;
			RECIPE_LOOKUP[0][1][1][1][1][1]=31;
			RECIPE_LOOKUP[1][0][0][1][1][1]=32;
			RECIPE_LOOKUP[1][0][1][0][1][1]=33;
			RECIPE_LOOKUP[1][0][1][1][0][1]=34;
			RECIPE_LOOKUP[1][0][1][1][1][0]=35;
			RECIPE_LOOKUP[1][0][0][1][0][1]=36;
			RECIPE_LOOKUP[1][0][1][0][0][1]=37;
			RECIPE_LOOKUP[1][0][0][1][1][0]=38;
			RECIPE_LOOKUP[1][0][1][0][1][0]=39;
			RECIPE_LOOKUP[1][0][0][0][1][1]=40;
			RECIPE_LOOKUP[1][0][1][1][0][0]=41;
			RECIPE_LOOKUP[1][0][0][0][0][1]=42;
			RECIPE_LOOKUP[1][0][0][0][1][0]=43;
			RECIPE_LOOKUP[1][0][0][1][0][0]=44;
			RECIPE_LOOKUP[1][0][1][0][0][0]=45;
			RECIPE_LOOKUP[1][0][0][0][0][0]=46;
			RECIPE_LOOKUP[1][0][1][1][1][1]=47;
			RECIPE_LOOKUP[0][0][0][1][1][1]=48;
			RECIPE_LOOKUP[0][0][1][0][1][1]=49;
			RECIPE_LOOKUP[0][0][1][1][0][1]=50;
			RECIPE_LOOKUP[0][0][1][1][1][0]=51;
			RECIPE_LOOKUP[0][0][0][1][0][1]=52;
			RECIPE_LOOKUP[0][0][1][0][0][1]=53;
			RECIPE_LOOKUP[0][0][0][1][1][0]=54;
			RECIPE_LOOKUP[0][0][1][0][1][0]=55;
			RECIPE_LOOKUP[0][0][0][0][1][1]=56;
			RECIPE_LOOKUP[0][0][1][1][0][0]=57;
			RECIPE_LOOKUP[0][0][0][0][0][1]=58;
			RECIPE_LOOKUP[0][0][0][0][1][0]=59;
			RECIPE_LOOKUP[0][0][0][1][0][0]=60;
			RECIPE_LOOKUP[0][0][1][0][0][0]=61;
			RECIPE_LOOKUP[0][0][0][0][0][0]=62;
			RECIPE_LOOKUP[0][0][1][1][1][1]=63;

			break;
			
		case Z:
			ROTATION_LOOKUP = new Quat4f[64];
			for(int i=0; i < 64; i++){
				Quat4f rotation = new Quat4f(0, 0, 0, 1);
				rotation.mul(rotationForAxis(Axis.Z, -ROTATION_LOOKUP_Y[i].yCoord));
				rotation.mul(rotationForAxis(Axis.X, ROTATION_LOOKUP_Y[i].xCoord + 90));
				rotation.mul(rotationForAxis(Axis.Y, 180.0));
				ROTATION_LOOKUP[i] = rotation;
			}
			
			RECIPE_LOOKUP[1][1][1][0][1][1]=0;
			RECIPE_LOOKUP[1][1][0][1][1][1]=1;
			RECIPE_LOOKUP[1][0][1][1][1][1]=2;
			RECIPE_LOOKUP[0][1][1][1][1][1]=3;
			RECIPE_LOOKUP[1][0][1][0][1][1]=4;
			RECIPE_LOOKUP[1][0][0][1][1][1]=5;
			RECIPE_LOOKUP[0][1][1][0][1][1]=6;
			RECIPE_LOOKUP[0][1][0][1][1][1]=7;
			RECIPE_LOOKUP[1][1][0][0][1][1]=8;
			RECIPE_LOOKUP[0][0][1][1][1][1]=9;
			RECIPE_LOOKUP[1][0][0][0][1][1]=10;
			RECIPE_LOOKUP[0][1][0][0][1][1]=11;
			RECIPE_LOOKUP[0][0][1][0][1][1]=12;
			RECIPE_LOOKUP[0][0][0][1][1][1]=13;
			RECIPE_LOOKUP[0][0][0][0][1][1]=14;
			RECIPE_LOOKUP[1][1][1][1][1][1]=15;
			RECIPE_LOOKUP[1][1][1][0][1][0]=16;
			RECIPE_LOOKUP[1][1][0][1][1][0]=17;
			RECIPE_LOOKUP[1][0][1][1][1][0]=18;
			RECIPE_LOOKUP[0][1][1][1][1][0]=19;
			RECIPE_LOOKUP[1][0][1][0][1][0]=20;
			RECIPE_LOOKUP[1][0][0][1][1][0]=21;
			RECIPE_LOOKUP[0][1][1][0][1][0]=22;
			RECIPE_LOOKUP[0][1][0][1][1][0]=23;
			RECIPE_LOOKUP[1][1][0][0][1][0]=24;
			RECIPE_LOOKUP[0][0][1][1][1][0]=25;
			RECIPE_LOOKUP[1][0][0][0][1][0]=26;
			RECIPE_LOOKUP[0][1][0][0][1][0]=27;
			RECIPE_LOOKUP[0][0][1][0][1][0]=28;
			RECIPE_LOOKUP[0][0][0][1][1][0]=29;
			RECIPE_LOOKUP[0][0][0][0][1][0]=30;
			RECIPE_LOOKUP[1][1][1][1][1][0]=31;
			RECIPE_LOOKUP[1][1][1][0][0][1]=32;
			RECIPE_LOOKUP[1][1][0][1][0][1]=33;
			RECIPE_LOOKUP[1][0][1][1][0][1]=34;
			RECIPE_LOOKUP[0][1][1][1][0][1]=35;
			RECIPE_LOOKUP[1][0][1][0][0][1]=36;
			RECIPE_LOOKUP[1][0][0][1][0][1]=37;
			RECIPE_LOOKUP[0][1][1][0][0][1]=38;
			RECIPE_LOOKUP[0][1][0][1][0][1]=39;
			RECIPE_LOOKUP[1][1][0][0][0][1]=40;
			RECIPE_LOOKUP[0][0][1][1][0][1]=41;
			RECIPE_LOOKUP[1][0][0][0][0][1]=42;
			RECIPE_LOOKUP[0][1][0][0][0][1]=43;
			RECIPE_LOOKUP[0][0][1][0][0][1]=44;
			RECIPE_LOOKUP[0][0][0][1][0][1]=45;
			RECIPE_LOOKUP[0][0][0][0][0][1]=46;
			RECIPE_LOOKUP[1][1][1][1][0][1]=47;
			RECIPE_LOOKUP[1][1][1][0][0][0]=48;
			RECIPE_LOOKUP[1][1][0][1][0][0]=49;
			RECIPE_LOOKUP[1][0][1][1][0][0]=50;
			RECIPE_LOOKUP[0][1][1][1][0][0]=51;
			RECIPE_LOOKUP[1][0][1][0][0][0]=52;
			RECIPE_LOOKUP[1][0][0][1][0][0]=53;
			RECIPE_LOOKUP[0][1][1][0][0][0]=54;
			RECIPE_LOOKUP[0][1][0][1][0][0]=55;
			RECIPE_LOOKUP[1][1][0][0][0][0]=56;
			RECIPE_LOOKUP[0][0][1][1][0][0]=57;
			RECIPE_LOOKUP[1][0][0][0][0][0]=58;
			RECIPE_LOOKUP[0][1][0][0][0][0]=59;
			RECIPE_LOOKUP[0][0][1][0][0][0]=60;
			RECIPE_LOOKUP[0][0][0][1][0][0]=61;
			RECIPE_LOOKUP[0][0][0][0][0][0]=62;
			RECIPE_LOOKUP[1][1][1][1][0][0]=63;
			break;
		default:

			// Done to suppress compiler warning for uninitialized final.
			// If we actually get here we are f'd.
			ROTATION_LOOKUP = null;
			
				
		}
	}
	
}
