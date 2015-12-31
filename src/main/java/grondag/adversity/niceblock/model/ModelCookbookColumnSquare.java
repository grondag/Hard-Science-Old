package grondag.adversity.niceblock.model;

import grondag.adversity.niceblock.NiceBlock;
import grondag.adversity.niceblock.NiceSubstance;

import java.util.Map;

import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.EnumFacing.Axis;

import com.google.common.collect.Maps;

public class ModelCookbookColumnSquare extends ModelCookbookAxisOriented {

	@Override
	public EnumWorldBlockLayer getRenderLayer() {
		return EnumWorldBlockLayer.CUTOUT;
	}

	@Override
	public int getTextureCount() {
		return 9;
	}

	@Override
	public boolean useRotatedTexturesAsAlternates(){
		return false;
	}
	
	public ModelCookbookColumnSquare(int textureIndex, int alternateCount, Axis axis) {
		super(textureIndex, alternateCount, axis);
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

		int baseOffset = getTextureCount() * calcAlternate(alternate) + textureIndex;
		Map<String, String> textures = Maps.newHashMap();

		textures.put("inner", buildTextureName(substance, baseOffset + 0));
		textures.put("outer", buildTextureName(substance, baseOffset - 1));
		textures.put("column_face", buildTextureName(substance, baseOffset + 7));
		textures.put("cap_opposite_neighbors", buildTextureName(substance, baseOffset + 7));
		textures.put("cap_three_neighbors", buildTextureName(substance, baseOffset + 6));
		textures.put("cap_adjacent_neighbors", buildTextureName(substance, baseOffset + 2));
		textures.put("cap_one_neighbor", buildTextureName(substance, baseOffset + 3));
		textures.put("cap_four_neighbors", buildTextureName(substance, baseOffset + 1));
		textures.put("cap_no_neighbors", buildTextureName(substance, baseOffset + 5));
		textures.put("cap_inner_side", buildTextureName(substance, baseOffset + 4));

		return new Ingredients(modelName, textures, ROTATION_LOOKUP[recipe]);

	}

}
