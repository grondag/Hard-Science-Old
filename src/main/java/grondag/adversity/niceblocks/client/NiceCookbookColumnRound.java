package grondag.adversity.niceblocks.client;

import grondag.adversity.niceblocks.NiceSubstance;
import grondag.adversity.niceblocks.client.NiceCookbook.Ingredients;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.util.EnumFacing.Axis;
import net.minecraftforge.client.model.TRSRTransformation;

public class NiceCookbookColumnRound extends NiceCookbookColumnSquare {

	public NiceCookbookColumnRound(Axis axis) {
		super(axis);
	}
	
	@Override
	public Ingredients getIngredients(NiceSubstance substance, int recipe, int alternate) {

		//String modelName = MODEL_LOOKUP[recipe];
		String modelName = "adversity:block/cylinder.obj";
		
		int baseOffset = (style.textureCount * calcAlternate(alternate)) + style.textureIndex;
		Map<String, String> textures = Maps.newHashMap();

		textures.put("#all", style.buildTextureName(substance, baseOffset));
		
		return new Ingredients(modelName, textures, 
				 TRSRTransformation.blockCenterToCorner(new TRSRTransformation(null, ROTATION_LOOKUP[recipe], null, null)));
	}

}
