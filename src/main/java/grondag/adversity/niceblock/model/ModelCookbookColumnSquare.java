package grondag.adversity.niceblock.model;

import java.util.Map;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblock.NiceSubstance;
import grondag.adversity.niceblock.NiceBlock.TestForCompleteMatch;
import grondag.adversity.niceblock.NiceBlock.TestForStyle;
import grondag.adversity.niceblock.model.ModelCookbook.Ingredients;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;

public class ModelCookbookColumnSquare extends ModelCookbookAxisOriented{
	
	public ModelCookbookColumnSquare(Axis axis) {
		super(axis);
	}

	@Override
	protected void populateModelNames() {
		modelNames[AxisAlignedModel.FOUR_CLOSED.index] = "adversity:block/column_four_faces_full";
		modelNames[AxisAlignedModel.FOUR_TOP_CLOSED.index] = "adversity:block/column_four_faces_half";
		modelNames[AxisAlignedModel.FOUR_OPEN.index] = "adversity:block/column_four_faces";
		
		modelNames[AxisAlignedModel.THREE_CLOSED.index] = "adversity:block/column_three_faces_full";
		modelNames[AxisAlignedModel.THREE_TOP_CLOSED.index] = "adversity:block/column_three_faces_half";
		modelNames[AxisAlignedModel.THREE_OPEN.index] = "adversity:block/column_three_faces";
		
		modelNames[AxisAlignedModel.TWO_ADJACENT_CLOSED.index] = "adversity:block/column_adjacent_faces_full";
		modelNames[AxisAlignedModel.TWO_ADJACENT_TOP_CLOSED.index] = "adversity:block/column_adjacent_faces_half";
		modelNames[AxisAlignedModel.TWO_ADJACENT_OPEN.index] = "adversity:block/column_adjacent_faces";
		
		modelNames[AxisAlignedModel.TWO_OPPOSITE_CLOSED.index] = "adversity:block/column_opposite_faces_full";
		modelNames[AxisAlignedModel.TWO_OPPOSITE_TOP_CLOSED.index] = "adversity:block/column_opposite_faces_half";
		modelNames[AxisAlignedModel.TWO_OPPOSITE_OPEN.index] = "adversity:block/column_opposite_faces";
		
		modelNames[AxisAlignedModel.ONE_CLOSED.index] = "adversity:block/column_single_face_full";
		modelNames[AxisAlignedModel.ONE_TOP_CLOSED.index] = "adversity:block/column_single_face_half";
		modelNames[AxisAlignedModel.ONE_OPEN.index] = "adversity:block/column_single_face";
		
		modelNames[AxisAlignedModel.NONE_CLOSED.index] = "adversity:block/column_no_faces_full";
		modelNames[AxisAlignedModel.NONE_TOP_CLOSED.index] = "adversity:block/column_no_faces_half";
		modelNames[AxisAlignedModel.NONE_OPEN.index] = "adversity:block/column_no_faces";
	}

	@Override
	public Ingredients getIngredients(NiceSubstance substance, int recipe, int alternate) {

		String modelName = modelNames[MODEL_FOR_RECIPE[recipe].index];
		
		int baseOffset = (style.textureCount * calcAlternate(alternate)) + style.textureIndex;
		Map<String, String> textures = Maps.newHashMap();

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
		
		return new Ingredients(modelName, textures, ROTATION_LOOKUP[recipe]);

	}

	
	
}
