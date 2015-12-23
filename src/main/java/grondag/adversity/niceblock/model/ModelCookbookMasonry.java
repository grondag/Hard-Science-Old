package grondag.adversity.niceblock.model;

import grondag.adversity.library.IBlockTest;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblock.NiceBlock;
import grondag.adversity.niceblock.NiceBlock.TestForCompleteMatch;
import grondag.adversity.niceblock.NiceBlock.TestForSubstance;
import grondag.adversity.niceblock.NiceSubstance;

import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;

import com.google.common.collect.Maps;

/**
 * Masonry blocks emulate the appearance of real-world large-block masonry construction.
 * Very similar to regular connected texture handling except for deciding when to display a border.
 */
public class ModelCookbookMasonry extends ModelCookbook {

	@Override
	public int getRecipeCount() {
		return 64;
	}

	@Override
	public Ingredients getIngredients(NiceSubstance substance, int recipe, int alternate) {

		Rotation rotation = calcRotation(alternate);
		String modelName = "adversity:block/cube_rotate_" + calcRotation(alternate).degrees;
		int baseOffset = style.textureCount * calcAlternate(alternate) + style.textureIndex;
		Map<String, String> textures = Maps.newHashMap();

		ModelCookbook.TextureOffset offset = SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][recipe];

		textures.put("up", style.buildTextureName(substance, baseOffset + offset.up));
		textures.put("down", style.buildTextureName(substance, baseOffset + offset.down));
		textures.put("east", style.buildTextureName(substance, baseOffset + offset.east));
		textures.put("west", style.buildTextureName(substance, baseOffset + offset.west));
		textures.put("north", style.buildTextureName(substance, baseOffset + offset.north));
		textures.put("south", style.buildTextureName(substance, baseOffset + offset.south));

		return new Ingredients(modelName, textures, TRSRTransformation.identity());
	}

	@Override
	public int getRecipeIndex(IExtendedBlockState state, IBlockAccess worldIn, BlockPos pos) {
		NeighborBlocks neighbors = new NeighborBlocks(worldIn, pos);
		NeighborTestResults mates = neighbors.getNeighborTestResults(new TestForCompleteMatch(state));
		NeighborTestResults needsMortar = neighbors.getNeighborTestResults(
				new IBlockTest() {
					@Override
					public boolean testBlock(IBlockState ibs) {
						return ibs.getBlock() instanceof NiceBlock && ibs.getBlock().isFullCube();
					}
				});
		NeighborTestResults thisSubstance = neighbors.getNeighborTestResults(new TestForSubstance(state));

		return SIMPLE_JOIN_RECIPE_LOOKUP[0][thisSubstance.down && !mates.down ? 1 : 0] // UP & DOWN
		[thisSubstance.east && !needsMortar.east || needsMortar.east && !mates.east ? 1 : 0] // EAST
		[thisSubstance.west && !needsMortar.west ? 1 : 0] // WEST
		[thisSubstance.north && !needsMortar.north || needsMortar.north && !mates.north ? 1 : 0] // NORTH
		[thisSubstance.south && !needsMortar.south ? 1 : 0]; // SOUTH
	}
}
