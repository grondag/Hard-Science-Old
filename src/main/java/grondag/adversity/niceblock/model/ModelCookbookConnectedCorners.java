package grondag.adversity.niceblock.model;

import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblock.NiceBlock;
import grondag.adversity.niceblock.NiceBlock.TestForCompleteMatch;
import grondag.adversity.niceblock.NiceSubstance;

import java.util.Map;

import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;

import com.google.common.collect.Maps;

public class ModelCookbookConnectedCorners extends ModelCookbook {

	public ModelCookbookConnectedCorners(int textureIndex, int alternateCount) {
		super(textureIndex, alternateCount);
	}

	@Override
	public int getRecipeCount() {
		return 386;
	}

	@Override
	public int getTextureCount() {
		return 48;
	}

	@Override
	public boolean useRotatedTexturesAsAlternates(){
		return false;
	}
	
	@Override
	public Ingredients getIngredients(NiceSubstance substance, int recipe, int alternate) {

		Rotation rotation = calcRotation(alternate);
		String modelName = "adversity:block/cube_rotate_" + calcRotation(alternate).degrees;
		int baseOffset = getTextureCount() * calcAlternate(alternate) + textureIndex;
		Map<String, String> textures = Maps.newHashMap();

		ModelCookbook.TextureOffset offset = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][recipe];

		textures.put("up", getTextureName(substance, baseOffset + offset.up));
		textures.put("down", getTextureName(substance, baseOffset + offset.down));
		textures.put("east", getTextureName(substance, baseOffset + offset.east));
		textures.put("west", getTextureName(substance, baseOffset + offset.west));
		textures.put("north", getTextureName(substance, baseOffset + offset.north));
		textures.put("south", getTextureName(substance, baseOffset + offset.south));

		return new Ingredients(modelName, textures, TRSRTransformation.identity());

	}

	@Override
	public int getModelRecipeID(IExtendedBlockState state, IBlockAccess worldIn, BlockPos pos) {
		NiceBlock.TestForCompleteMatch test = new TestForCompleteMatch(state);
		NeighborBlocks neighbors = new NeighborBlocks(worldIn, pos);
		NeighborTestResults mates = neighbors.getNeighborTestResults(test);

		CornerRecipeFinder finder = CONNECTED_CORNER_RECIPE_LOOKUP[mates.up ? 1 : 0][mates.down ? 1 : 0]
				[mates.east ? 1 : 0][mates.west ? 1 : 0]
				[mates.north ? 1 : 0][mates.south ? 1 : 0];

		return finder.getRecipe(test, worldIn, pos);
	}

}
