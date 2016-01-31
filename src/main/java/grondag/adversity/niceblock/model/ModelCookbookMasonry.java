package grondag.adversity.niceblock.model;

import grondag.adversity.library.IBlockTest;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblock.newmodel.NiceBlock;
import grondag.adversity.niceblock.newmodel.NiceBlock.TestForCompleteMatch;
import grondag.adversity.niceblock.newmodel.NiceBlock.TestForSubstance;

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

	public ModelCookbookMasonry(String textureName, int alternateCount) {
		super(textureName, alternateCount);
	}
	
	@Override
	public int getRecipeCount() {
		return 64;
	}

	@Override
	public int getTextureCount() {
		return 16;
	}
	
	@Override
	public boolean useRotatedTexturesAsAlternates(){
		return false;
	}
	
	@Override
	public Ingredients getIngredients(int meta, int recipe, int alternate) {

		Rotation rotation = calcRotation(alternate);
		String modelName = "adversity:block/cube_rotate_" + calcRotation(alternate).degrees;
		int baseOffset = getTextureCount() * calcAlternate(alternate);
		Map<String, String> textures = Maps.newHashMap();

		ModelCookbook.TextureOffset offset = SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][recipe];

		textures.put("up", getTextureName(meta, baseOffset + offset.up));
		textures.put("down", getTextureName(meta, baseOffset + offset.down));
		textures.put("east", getTextureName(meta, baseOffset + offset.east));
		textures.put("west", getTextureName(meta, baseOffset + offset.west));
		textures.put("north", getTextureName(meta, baseOffset + offset.north));
		textures.put("south", getTextureName(meta, baseOffset + offset.south));

		return new Ingredients(modelName, textures, TRSRTransformation.identity());
	}

	@Override
	public int getModelRecipeID(IExtendedBlockState state, IBlockAccess worldIn, BlockPos pos) {
		NeighborBlocks neighbors = new NeighborBlocks(worldIn, pos);
		NeighborTestResults mates = neighbors.getNeighborTestResults(new TestForCompleteMatch(state));
		NeighborTestResults needsMortar = neighbors.getNeighborTestResults(
				new IBlockTest() {
					@Override
					public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos) {
						return ibs.getBlock() instanceof NiceBlock && ibs.getBlock().isFullCube();
					}
				});
		NeighborTestResults thisSubstance = neighbors.getNeighborTestResults(new TestForSubstance(state));

		return SIMPLE_JOIN_RECIPE_LOOKUP[0][thisSubstance.down() && !mates.down() ? 1 : 0] // UP & DOWN
		[thisSubstance.east() && !needsMortar.east() || needsMortar.east() && !mates.east() ? 1 : 0] // EAST
		[thisSubstance.west() && !needsMortar.west() ? 1 : 0] // WEST
		[thisSubstance.north() && !needsMortar.north() || needsMortar.north() && !mates.north() ? 1 : 0] // NORTH
		[thisSubstance.south() && !needsMortar.south() ? 1 : 0]; // SOUTH
	}
}
