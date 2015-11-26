package grondag.adversity.niceblocks.client;

import java.util.Map;

import com.google.common.collect.Maps;

import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblocks.NiceBlock;
import grondag.adversity.niceblocks.NiceBlock.TestForCompleteMatch;
import grondag.adversity.niceblocks.NiceBlock.TestForStyle;
import grondag.adversity.niceblocks.NiceBlock.TestForSubstance;
import grondag.adversity.niceblocks.NiceBlockStyle;
import grondag.adversity.niceblocks.NiceSubstance;
import grondag.adversity.niceblocks.client.NiceCookbook.CornerRecipeFinder;
import grondag.adversity.niceblocks.client.NiceCookbook.Ingredients;
import grondag.adversity.niceblocks.client.NiceCookbook.Rotation;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;

public class NiceCookbookMasonry extends NiceCookbook{

	public NiceCookbookMasonry(NiceBlockStyle style) {
		super(style);
	}

	@Override
	public  int getRecipeCount() {
		return 64;
	}

	@Override
	public Ingredients getIngredients(NiceSubstance substance, int recipe, int alternate) {
		
		Rotation rotation = calcRotation(alternate);
		String modelName = "adversity:block/cube_rotate_" + calcRotation(alternate).degrees;
		int baseOffset = (style.textureCount * alternate) + style.textureIndex;
		Map<String, String> textures = Maps.newHashMap();

		NiceCookbook.TextureOffset offset = SIMPLE_JOIN_MASONRY_OFFSETS[rotation.index][recipe];

		textures.put("up", style.buildTextureName(substance, baseOffset + offset.up));
		textures.put("down", style.buildTextureName(substance, baseOffset + offset.down));
		textures.put("east", style.buildTextureName(substance, baseOffset + offset.east));
		textures.put("west", style.buildTextureName(substance, baseOffset + offset.west));
		textures.put("north", style.buildTextureName(substance, baseOffset + offset.north));
		textures.put("south", style.buildTextureName(substance, baseOffset + offset.south));
		
		return new Ingredients(modelName, textures, ModelRotation.X0_Y0);
	}

	@Override
	public int getModelIndex(IExtendedBlockState state, IBlockAccess worldIn, BlockPos pos) {
		NeighborBlocks neighbors = new NeighborBlocks(worldIn, pos);
		NeighborTestResults mates = neighbors.getNeighborTestResults(new TestForCompleteMatch(state));
		NeighborTestResults masonry = neighbors.getNeighborTestResults(new TestForStyle(state));
		NeighborTestResults thisSubstance = neighbors.getNeighborTestResults(new TestForSubstance(state));

		return SIMPLE_JOIN_RECIPE_LOOKUP[0][thisSubstance.down && !mates.down?1:0]  					// UP DOWN
				[(thisSubstance.east && !masonry.east) || (masonry.east && !mates.east)?1:0] 		// EAST
				[thisSubstance.west && !masonry.west?1:0]  											// WEST
				[(thisSubstance.north && !masonry.north) || (masonry.north && !mates.north)?1:0]	// NORTH								// NORTH
				[thisSubstance.south && !masonry.south?1:0]; 	
	}
	
}
