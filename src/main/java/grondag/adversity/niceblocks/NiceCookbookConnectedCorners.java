package grondag.adversity.niceblocks;

import java.util.Map;

import com.google.common.collect.Maps;

import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblocks.NiceBlock.TestForCompleteMatch;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;

public class NiceCookbookConnectedCorners extends NiceCookbook {
	
	@Override
	public int getRecipeCount() {
		return 386;
	}

	@Override
	public Ingredients getIngredients(NiceSubstance substance, int recipe, int alternate) {
		
		Rotation rotation = calcRotation(alternate);
		String modelName = "adversity:block/cube_rotate_" + calcRotation(alternate).degrees;
		int baseOffset = (style.textureCount * calcAlternate(alternate)) + style.textureIndex;
		Map<String, String> textures = Maps.newHashMap();

		NiceCookbook.TextureOffset offset = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][recipe];

		textures.put("up", style.buildTextureName(substance, baseOffset + offset.up));
		textures.put("down", style.buildTextureName(substance, baseOffset + offset.down));
		textures.put("east", style.buildTextureName(substance, baseOffset + offset.east));
		textures.put("west", style.buildTextureName(substance, baseOffset + offset.west));
		textures.put("north", style.buildTextureName(substance, baseOffset + offset.north));
		textures.put("south", style.buildTextureName(substance, baseOffset + offset.south));
		
		return new Ingredients(modelName, textures, ModelRotation.X0_Y0);
		
	}

	@Override
	public int getRecipeIndex(IExtendedBlockState state, IBlockAccess worldIn, BlockPos pos) {
		NiceBlock.TestForCompleteMatch test = new TestForCompleteMatch(state);
		NeighborBlocks neighbors = new NeighborBlocks(worldIn, pos);
		NeighborTestResults mates = neighbors.getNeighborTestResults(test);

		CornerRecipeFinder finder = CONNECTED_CORNER_RECIPE_LOOKUP[mates.up?1:0][mates.down?1:0]
				[mates.east?1:0][mates.west?1:0] 
						[mates.north?1:0][mates.south?1:0];

		return finder.getRecipe(test, worldIn, pos);
	}
	
	

}

