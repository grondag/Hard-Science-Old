package grondag.adversity.niceblock.support;

import grondag.adversity.Adversity;
import grondag.adversity.library.IBlockTest;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Dynamically determines the correct recipe to use when corner tests
 * matter. Used specifically in the case of connected textures with outside
 * borders.
 */
public class CornerRecipeFinder {

	public final int baseRecipe;
	private BlockPos[] offsets;
	public final boolean hasTests;

	/**
	 * This class assumes your recipes are ordered a particular way without
	 * gaps. The first recipe (identified in the first parameter) is used
	 * when all corner blocks are present Each missing corner block toggles
	 * a bit that is added to the offset. So, for example, one corner gives
	 * two potential outputs, two gives four, etc.
	 *
	 * Corners should be provided in LSB-first order. Corner directions
	 * should be provided in pairs ordered so that U or D < N or S < E or W.
	 *
	 * You can use this class without any corner tests. Do this for
	 * consistency of types in lookup arrays when some cases have no corner
	 * tests.
	 *
	 * @param baseRecipeID
	 *            Recipe to use if all corners are present.
	 * @param corners
	 *            Corners that modify this recipe.
	 */

	public CornerRecipeFinder(int baseRecipe, String... corners) {
		this.baseRecipe = baseRecipe;
		hasTests = corners.length > 0;

		offsets = new BlockPos[corners.length];
		for (int i = 0; i < corners.length; i++) {

			if (corners[i] == "NE") {
				offsets[i] = new BlockPos(0, 0, 0).north().east();
			} else if (corners[i] == "NW") {
				offsets[i] = new BlockPos(0, 0, 0).north().west();
			} else if (corners[i] == "SE") {
				offsets[i] = new BlockPos(0, 0, 0).south().east();
			} else if (corners[i] == "SW") {
				offsets[i] = new BlockPos(0, 0, 0).south().west();
			} else if (corners[i] == "UE") {
				offsets[i] = new BlockPos(0, 0, 0).up().east();
			} else if (corners[i] == "UW") {
				offsets[i] = new BlockPos(0, 0, 0).up().west();
			} else if (corners[i] == "UN") {
				offsets[i] = new BlockPos(0, 0, 0).up().north();
			} else if (corners[i] == "US") {
				offsets[i] = new BlockPos(0, 0, 0).up().south();
			} else if (corners[i] == "DE") {
				offsets[i] = new BlockPos(0, 0, 0).down().east();
			} else if (corners[i] == "DW") {
				offsets[i] = new BlockPos(0, 0, 0).down().west();
			} else if (corners[i] == "DN") {
				offsets[i] = new BlockPos(0, 0, 0).down().north();
			} else if (corners[i] == "DS") {
				offsets[i] = new BlockPos(0, 0, 0).down().south();
			} else {
				offsets[i] = new BlockPos(0, 0, 0);
				Adversity.log.warn("Unrecognized corner ID string when setting up corner lookups. This should never happen.");
			}
		}
	}

	public int getRecipe(IBlockTest test, IBlockAccess worldIn, BlockPos pos) {

		int result = baseRecipe;

		if (hasTests) {
			for (int i = 0; i < offsets.length; i++) {
				// corner block not found means we need to show corner
				// texture
				if (!test.testBlock(worldIn.getBlockState(pos.add(offsets[i])))) {
					result += 1 << i;
				}
			}
		}

		return result;
	}
}