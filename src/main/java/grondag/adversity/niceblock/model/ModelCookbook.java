package grondag.adversity.niceblock.model;

import grondag.adversity.Adversity;
import grondag.adversity.library.IBlockTest;
import grondag.adversity.niceblock.NiceStyle;
import grondag.adversity.niceblock.NiceSubstance;
import grondag.adversity.niceblock.support.ICollisionHandler;

import java.util.Map;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Quat4f;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * Responsible for generating model baking inputs and determining which model to
 * use via extended block state handling. This class, combined with style,
 * substance and model, determine how block appears in game.
 */
public class ModelCookbook {

	/**
	 * Use with NeighborBlocks test for fast lookup of recipe for connected
	 * blocks that depend on adjacent blocks but don't require corner tests. (No
	 * outside border.) Dimensions are UDNSEW. Value 0 means no neighbor, 1
	 * means neighbor present
	 */
	protected static Integer[][][][][][] SIMPLE_JOIN_RECIPE_LOOKUP = new Integer[2][2][2][2][2][2];

	/**
	 * Provides the face texture offsets for a given recipe and rotation.
	 * Assumes textures follow a consistent numbering/naming sequence of offsets
	 * from a base value (to be given later). We don't rotate these textures
	 * dynamically because they may be noisy and that leads to the jarring
	 * effect of blocks changing appearance when blocks are placed next to them.
	 * Array dimensions are rotation and recipe.
	 */
	protected static final TextureOffset[][] SIMPLE_JOIN_TEXTURE_OFFSETS = new TextureOffset[4][64];

	 // Connected corner lookups are defined in base class because 
	// simple join texture lookups are a subset of them.

	
	/**
	 * Use with NeighborBlocks test for lookup of recipe for connected blocks
	 * that depend on adjacent blocks and do require corner tests. (Blocks with
	 * outside border.) Does not return a recipe directly - use the
	 * CornerRecipeFinder to get it. Dimensions are UDNSEW. Value 0 means no
	 * neighbor, 1 means neighbor present. The values in the array are not
	 * continuous - the CornerRecipeFinder adds between 0 and 16 to the base
	 * recipe number depending on the specific scenario and presence of absence
	 * of corner blocks, giving 386 possible recipes..
	 */
	protected static CornerRecipeFinder[][][][][][] CONNECTED_CORNER_RECIPE_LOOKUP = new CornerRecipeFinder[2][2][2][2][2][2];

	/**
	 * Provides the face texture offsets for a given recipe and rotation.
	 * Assumes textures follow a consistent numbering/naming sequence of offsets
	 * from a base value (to be given later). We don't rotate these textures
	 * dynamically because they may be noisy and that leads to the jarring
	 * effect of blocks changing appearance when blocks are placed next to them.
	 * Array dimensions are rotation and recipe.
	 */
	protected static final TextureOffset[][] CONNECTED_CORNER_TEXTURE_OFFSETS = new TextureOffset[4][386];

	/**
	 * Style that holds this cook book instance.
	 */
	protected NiceStyle style;

	public void setStyle(NiceStyle style) {
		if (this.style == null) {
			this.style = style;
		} else {
			Adversity.log.warn("Attempted to set Cookbook style more than once.  This should never happen.");
		}
	}

	public int getRecipeCount() {
		return 1;
	}

	public final int getAlternateCount() {
		return calcExpanded();
	}

	/**
	 * Override if special collision handling is needed due to non-cubic shape.
	 */
	public ICollisionHandler getCollisionHandler() {
		return null;
	}

	public Ingredients getIngredients(NiceSubstance substance, int recipe, int alternate) {

		String modelName = "adversity:block/cube_rotate_all_" + calcRotation(alternate).degrees;

		Map<String, String> textures = Maps.newHashMap();
		textures.put("all", style.buildTextureName(substance, calcAlternate(alternate) + style.textureIndex));

		return new Ingredients(modelName, textures, TRSRTransformation.identity());
	}

	/**
	 * Used by NiceBlock to provide extended state to NiceModel so that it knows
	 * which model to provide to renderer.
	 *
	 */
	public int getRecipeIndex(IExtendedBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return 0;
	}

	/**
	 * Tells NiceModel which recipe to use for generating an item model. Assumes
	 * that items have a static appearance. Will probably need to override other
	 * stuff if dynamic item appearance is needed.
	 */
	public int getItemModelIndex() {
		return 0;
	}

	/**
	 * Tells NiceModel which texture to use to block-breaking particles.
	 */
	public String getParticleTextureName(NiceSubstance substance) {
		return style.buildTextureName(substance, style.textureIndex);
	}

	/**
	 * Creates an expanded alternate count when we use rotations as alternates
	 * pairs with calcRotation and calcAlternate
	 * */
	protected final int calcExpanded() {
		if (style.useRotationsAsAlternates) {
			return style.alternateCount * 4;
		} else {
			return style.alternateCount;
		}
	}

	/**
	 * retrieves alternate index from expanded value see calcExpanded
	 */
	protected final int calcAlternate(int expanded) {
		if (style.useRotationsAsAlternates) {
			return expanded / 4;
		} else {
			return expanded;
		}
	}

	/**
	 * retrieves rotation from expanded value see calcExpanded
	 */
	protected final Rotation calcRotation(int expanded) {
		if (style.useRotationsAsAlternates) {
			return Rotation.values()[expanded & 3];
		} else {
			return Rotation.ROTATE_NONE;
		}
	}

	/**
	 * Builds the appropriate quaternion to rotate around the given axis.
	 */
	protected final static Quat4f rotationForAxis(EnumFacing.Axis axis, double degrees)
	{
		Quat4f retVal = new Quat4f();
		switch (axis) {
		case X:
			retVal.set(new AxisAngle4d(1, 0, 0, Math.toRadians(degrees)));
			break;
		case Y:
			retVal.set(new AxisAngle4d(0, 1, 0, Math.toRadians(degrees)));
			break;
		case Z:
			retVal.set(new AxisAngle4d(0, 0, 1, Math.toRadians(degrees)));
			break;
		}
		return retVal;
	}

	/**
	 * Used to pass all the stuff NiceModel needs to bake a model.
	 *
	 */
	public class Ingredients {
		public final String modelName;
		public final ImmutableMap<String, String> textures;
		public final TRSRTransformation state;

		public Ingredients(String modelName, Map<String, String> textures, TRSRTransformation state) {
			this.modelName = modelName;
			this.textures = ImmutableMap.copyOf(textures);
			this.state = state;
		}
	}

	/**
	 * Lightweight data structure to store relative position of the texture for
	 * each face of a recipe. Positions are relative to the base texture index
	 * (which is not known to this class.)
	 */
	protected static class TextureOffset {

		public final byte up;
		public final byte down;
		public final byte east;
		public final byte west;
		public final byte north;
		public final byte south;

		public TextureOffset(byte up, byte down, byte east, byte west, byte north, byte south) {
			this.up = up;
			this.down = down;
			this.east = east;
			this.west = west;
			this.north = north;
			this.south = south;
		}
	}

	/**
	 * Texture rotations. Used mainly when rotated textures are used as
	 * alternate textures.
	 */
	protected static enum Rotation {
		ROTATE_NONE(0, 0),
		ROTATE_90(1, 90),
		ROTATE_180(2, 180),
		ROTATE_270(3, 270);

		/**
		 * May be useful for dynamic manipulations.
		 */
		public final int index;

		/**
		 * Useful for locating model file names that use degrees as a suffix.
		 */
		public final int degrees;

		Rotation(int index, int degrees) {
			this.index = index;
			this.degrees = degrees;
		}

	}

	/**
	 * Dynamically determines the correct recipe to use when corner tests
	 * matter. Used specifically in the case of connected textures with outside
	 * borders.
	 */
	protected static class CornerRecipeFinder {

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

	static {

		// Could have generated all this programmatically but did it by hand
		// before setting down the path of creating a generic block framework
		// and it wasn't broke and constants are fast and reliable, so kept it.
		
		SIMPLE_JOIN_RECIPE_LOOKUP[0][0][0][0][0][0] = 0;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][0][0][0][0][0] = 1;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][1][0][0][0][0] = 2;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][0][1][0][0][0] = 3;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][0][0][1][0][0] = 4;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][0][0][0][1][0] = 5;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][0][0][0][0][1] = 6;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][1][0][0][0][0] = 7;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][0][1][1][0][0] = 8;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][0][0][0][1][1] = 9;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][0][1][0][0][0] = 10;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][0][0][1][0][0] = 11;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][0][0][0][1][0] = 12;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][0][0][0][0][1] = 13;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][1][1][0][0][0] = 14;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][1][0][1][0][0] = 15;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][1][0][0][1][0] = 16;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][1][0][0][0][1] = 17;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][0][1][0][1][0] = 18;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][0][1][0][0][1] = 19;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][0][0][1][1][0] = 20;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][0][0][1][0][1] = 21;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][0][1][1][0][0] = 22;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][0][0][0][1][1] = 23;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][1][1][1][0][0] = 24;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][1][0][0][1][1] = 25;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][0][1][1][1][0] = 26;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][1][0][0][1][0] = 27;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][0][1][1][0][1] = 28;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][1][0][0][0][1] = 29;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][1][1][0][0][0] = 30;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][0][1][0][1][1] = 31;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][1][0][1][0][0] = 32;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][0][0][1][1][1] = 33;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][0][1][0][1][0] = 34;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][0][1][0][0][1] = 35;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][0][0][1][1][0] = 36;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][0][0][1][0][1] = 37;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][1][1][0][1][0] = 38;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][1][1][0][0][1] = 39;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][1][0][1][1][0] = 40;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][1][0][1][0][1] = 41;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][0][1][1][1][1] = 42;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][1][1][1][0][0] = 43;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][1][0][0][1][1] = 44;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][0][0][1][1][1] = 45;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][0][1][0][1][1] = 46;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][0][1][1][0][1] = 47;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][0][1][1][1][0] = 48;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][1][0][1][1][1] = 49;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][1][1][0][1][1] = 50;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][1][1][1][0][1] = 51;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][1][1][1][1][0] = 52;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][1][1][0][1][0] = 53;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][1][1][0][0][1] = 54;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][1][0][1][1][0] = 55;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][1][0][1][0][1] = 56;
		SIMPLE_JOIN_RECIPE_LOOKUP[0][1][1][1][1][1] = 57;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][0][1][1][1][1] = 58;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][1][0][1][1][1] = 59;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][1][1][0][1][1] = 60;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][1][1][1][0][1] = 61;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][1][1][1][1][0] = 62;
		SIMPLE_JOIN_RECIPE_LOOKUP[1][1][1][1][1][1] = 63;

		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][1][1][1][1] = new CornerRecipeFinder(0);
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][1][1][1][0] = new CornerRecipeFinder(162, "UE", "UW", "DE", "DW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][1][1][0][1] = new CornerRecipeFinder(162, "UE", "UW", "DE", "DW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][1][1][0][0] = new CornerRecipeFinder(162, "UE", "UW", "DE", "DW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][1][0][1][1] = new CornerRecipeFinder(178, "UN", "US", "DN", "DS");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][1][0][1][0] = new CornerRecipeFinder(322, "UN", "UE", "DN", "DE");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][1][0][0][1] = new CornerRecipeFinder(338, "UE", "US", "DE", "DS");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][1][0][0][0] = new CornerRecipeFinder(66, "UE", "DE");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][0][1][1][1] = new CornerRecipeFinder(178, "UN", "US", "DN", "DS");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][0][1][1][0] = new CornerRecipeFinder(354, "UN", "UW", "DN", "DW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][0][1][0][1] = new CornerRecipeFinder(370, "US", "UW", "DS", "DW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][0][1][0][0] = new CornerRecipeFinder(74, "UW", "DW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][0][0][1][1] = new CornerRecipeFinder(178, "UN", "US", "DN", "DS");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][0][0][1][0] = new CornerRecipeFinder(54, "UN", "DN");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][0][0][0][1] = new CornerRecipeFinder(62, "US", "DS");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][0][0][0][0] = new CornerRecipeFinder(7);
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][1][1][1][1] = new CornerRecipeFinder(146, "NE", "SE", "SW", "NW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][1][1][1][0] = new CornerRecipeFinder(242, "UE", "UW", "NE", "NW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][1][1][0][1] = new CornerRecipeFinder(226, "UE", "UW", "SE", "SW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][1][1][0][0] = new CornerRecipeFinder(34, "UE", "UW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][1][0][1][1] = new CornerRecipeFinder(210, "UN", "US", "NE", "SE");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][1][0][1][0] = new CornerRecipeFinder(82, "UN", "UE", "NE");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][1][0][0][1] = new CornerRecipeFinder(90, "UE", "US", "SE");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][1][0][0][0] = new CornerRecipeFinder(10, "UE");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][0][1][1][1] = new CornerRecipeFinder(194, "UN", "US", "SW", "NW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][0][1][1][0] = new CornerRecipeFinder(98, "UN", "UW", "NW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][0][1][0][1] = new CornerRecipeFinder(106, "US", "UW", "SW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][0][1][0][0] = new CornerRecipeFinder(12, "UW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][0][0][1][1] = new CornerRecipeFinder(38, "UN", "US");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][0][0][1][0] = new CornerRecipeFinder(14, "UN");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][0][0][0][1] = new CornerRecipeFinder(16, "US");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][0][0][0][0] = new CornerRecipeFinder(1);
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][1][1][1][1] = new CornerRecipeFinder(146, "NE", "SE", "SW", "NW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][1][1][1][0] = new CornerRecipeFinder(306, "DE", "DW", "NE", "NW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][1][1][0][1] = new CornerRecipeFinder(290, "DE", "DW", "SE", "SW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][1][1][0][0] = new CornerRecipeFinder(42, "DE", "DW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][1][0][1][1] = new CornerRecipeFinder(274, "DN", "DS", "NE", "SE");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][1][0][1][0] = new CornerRecipeFinder(114, "DN", "DE", "NE");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][1][0][0][1] = new CornerRecipeFinder(122, "DE", "DS", "SE");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][1][0][0][0] = new CornerRecipeFinder(18, "DE");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][0][1][1][1] = new CornerRecipeFinder(258, "DN", "DS", "SW", "NW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][0][1][1][0] = new CornerRecipeFinder(130, "DN", "DW", "NW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][0][1][0][1] = new CornerRecipeFinder(138, "DS", "DW", "SW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][0][1][0][0] = new CornerRecipeFinder(20, "DW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][0][0][1][1] = new CornerRecipeFinder(46, "DN", "DS");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][0][0][1][0] = new CornerRecipeFinder(22, "DN");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][0][0][0][1] = new CornerRecipeFinder(24, "DS");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][0][0][0][0] = new CornerRecipeFinder(2);
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][1][1][1][1] = new CornerRecipeFinder(146, "NE", "SE", "SW", "NW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][1][1][1][0] = new CornerRecipeFinder(50, "NE", "NW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][1][1][0][1] = new CornerRecipeFinder(58, "SE", "SW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][1][1][0][0] = new CornerRecipeFinder(8);
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][1][0][1][1] = new CornerRecipeFinder(70, "NE", "SE");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][1][0][1][0] = new CornerRecipeFinder(26, "NE");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][1][0][0][1] = new CornerRecipeFinder(28, "SE");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][1][0][0][0] = new CornerRecipeFinder(3);
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][0][1][1][1] = new CornerRecipeFinder(78, "SW", "NW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][0][1][1][0] = new CornerRecipeFinder(30, "NW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][0][1][0][1] = new CornerRecipeFinder(32, "SW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][0][1][0][0] = new CornerRecipeFinder(4);
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][0][0][1][1] = new CornerRecipeFinder(9);
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][0][0][1][0] = new CornerRecipeFinder(5);
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][0][0][0][1] = new CornerRecipeFinder(6);
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][0][0][0][0] = new CornerRecipeFinder(0);

		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][0] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][1] = new TextureOffset((byte) 0, (byte) 0, (byte) 1, (byte) 1, (byte) 1, (byte) 1);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][2] = new TextureOffset((byte) 0, (byte) 0, (byte) 4, (byte) 4, (byte) 4, (byte) 4);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][3] = new TextureOffset((byte) 2, (byte) 2, (byte) 0, (byte) 0, (byte) 8, (byte) 2);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][4] = new TextureOffset((byte) 8, (byte) 8, (byte) 0, (byte) 0, (byte) 2, (byte) 8);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][5] = new TextureOffset((byte) 1, (byte) 4, (byte) 2, (byte) 8, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][6] = new TextureOffset((byte) 4, (byte) 1, (byte) 8, (byte) 2, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][7] = new TextureOffset((byte) 0, (byte) 0, (byte) 5, (byte) 5, (byte) 5, (byte) 5);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][8] = new TextureOffset((byte) 10, (byte) 10, (byte) 0, (byte) 0, (byte) 10, (byte) 10);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][9] = new TextureOffset((byte) 5, (byte) 5, (byte) 10, (byte) 10, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][10] = new TextureOffset((byte) 0, (byte) 2, (byte) 0, (byte) 1, (byte) 9, (byte) 3);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][11] = new TextureOffset((byte) 0, (byte) 2, (byte) 0, (byte) 1, (byte) 19, (byte) 16);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][12] = new TextureOffset((byte) 0, (byte) 8, (byte) 1, (byte) 0, (byte) 3, (byte) 9);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][13] = new TextureOffset((byte) 0, (byte) 8, (byte) 1, (byte) 0, (byte) 16, (byte) 19);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][14] = new TextureOffset((byte) 0, (byte) 4, (byte) 3, (byte) 9, (byte) 0, (byte) 1);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][15] = new TextureOffset((byte) 0, (byte) 4, (byte) 16, (byte) 19, (byte) 0, (byte) 1);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][16] = new TextureOffset((byte) 0, (byte) 1, (byte) 9, (byte) 3, (byte) 1, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][17] = new TextureOffset((byte) 0, (byte) 1, (byte) 19, (byte) 16, (byte) 1, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][18] = new TextureOffset((byte) 2, (byte) 0, (byte) 0, (byte) 4, (byte) 12, (byte) 6);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][19] = new TextureOffset((byte) 2, (byte) 0, (byte) 0, (byte) 4, (byte) 18, (byte) 17);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][20] = new TextureOffset((byte) 8, (byte) 0, (byte) 4, (byte) 0, (byte) 6, (byte) 12);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][21] = new TextureOffset((byte) 8, (byte) 0, (byte) 4, (byte) 0, (byte) 17, (byte) 18);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][22] = new TextureOffset((byte) 1, (byte) 0, (byte) 6, (byte) 12, (byte) 0, (byte) 4);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][23] = new TextureOffset((byte) 1, (byte) 0, (byte) 17, (byte) 18, (byte) 0, (byte) 4);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][24] = new TextureOffset((byte) 4, (byte) 0, (byte) 12, (byte) 6, (byte) 4, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][25] = new TextureOffset((byte) 4, (byte) 0, (byte) 18, (byte) 17, (byte) 4, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][26] = new TextureOffset((byte) 3, (byte) 6, (byte) 0, (byte) 8, (byte) 0, (byte) 2);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][27] = new TextureOffset((byte) 16, (byte) 17, (byte) 0, (byte) 8, (byte) 0, (byte) 2);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][28] = new TextureOffset((byte) 6, (byte) 3, (byte) 0, (byte) 2, (byte) 8, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][29] = new TextureOffset((byte) 17, (byte) 16, (byte) 0, (byte) 2, (byte) 8, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][30] = new TextureOffset((byte) 9, (byte) 12, (byte) 2, (byte) 0, (byte) 0, (byte) 8);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][31] = new TextureOffset((byte) 19, (byte) 18, (byte) 2, (byte) 0, (byte) 0, (byte) 8);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][32] = new TextureOffset((byte) 12, (byte) 9, (byte) 8, (byte) 0, (byte) 2, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][33] = new TextureOffset((byte) 18, (byte) 19, (byte) 8, (byte) 0, (byte) 2, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][34] = new TextureOffset((byte) 0, (byte) 10, (byte) 0, (byte) 0, (byte) 11, (byte) 11);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][35] = new TextureOffset((byte) 0, (byte) 10, (byte) 0, (byte) 0, (byte) 23, (byte) 24);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][36] = new TextureOffset((byte) 0, (byte) 10, (byte) 0, (byte) 0, (byte) 24, (byte) 23);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][37] = new TextureOffset((byte) 0, (byte) 10, (byte) 0, (byte) 0, (byte) 25, (byte) 25);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][38] = new TextureOffset((byte) 0, (byte) 5, (byte) 11, (byte) 11, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][39] = new TextureOffset((byte) 0, (byte) 5, (byte) 24, (byte) 23, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][40] = new TextureOffset((byte) 0, (byte) 5, (byte) 23, (byte) 24, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][41] = new TextureOffset((byte) 0, (byte) 5, (byte) 25, (byte) 25, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][42] = new TextureOffset((byte) 10, (byte) 0, (byte) 0, (byte) 0, (byte) 14, (byte) 14);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][43] = new TextureOffset((byte) 10, (byte) 0, (byte) 0, (byte) 0, (byte) 30, (byte) 29);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][44] = new TextureOffset((byte) 10, (byte) 0, (byte) 0, (byte) 0, (byte) 29, (byte) 30);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][45] = new TextureOffset((byte) 10, (byte) 0, (byte) 0, (byte) 0, (byte) 31, (byte) 31);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][46] = new TextureOffset((byte) 5, (byte) 0, (byte) 14, (byte) 14, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][47] = new TextureOffset((byte) 5, (byte) 0, (byte) 29, (byte) 30, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][48] = new TextureOffset((byte) 5, (byte) 0, (byte) 30, (byte) 29, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][49] = new TextureOffset((byte) 5, (byte) 0, (byte) 31, (byte) 31, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][50] = new TextureOffset((byte) 11, (byte) 14, (byte) 0, (byte) 0, (byte) 0, (byte) 10);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][51] = new TextureOffset((byte) 24, (byte) 29, (byte) 0, (byte) 0, (byte) 0, (byte) 10);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][52] = new TextureOffset((byte) 23, (byte) 30, (byte) 0, (byte) 0, (byte) 0, (byte) 10);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][53] = new TextureOffset((byte) 25, (byte) 31, (byte) 0, (byte) 0, (byte) 0, (byte) 10);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][54] = new TextureOffset((byte) 0, (byte) 0, (byte) 7, (byte) 13, (byte) 0, (byte) 5);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][55] = new TextureOffset((byte) 0, (byte) 0, (byte) 20, (byte) 27, (byte) 0, (byte) 5);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][56] = new TextureOffset((byte) 0, (byte) 0, (byte) 21, (byte) 26, (byte) 0, (byte) 5);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][57] = new TextureOffset((byte) 0, (byte) 0, (byte) 22, (byte) 28, (byte) 0, (byte) 5);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][58] = new TextureOffset((byte) 14, (byte) 11, (byte) 0, (byte) 0, (byte) 10, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][59] = new TextureOffset((byte) 29, (byte) 24, (byte) 0, (byte) 0, (byte) 10, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][60] = new TextureOffset((byte) 30, (byte) 23, (byte) 0, (byte) 0, (byte) 10, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][61] = new TextureOffset((byte) 31, (byte) 25, (byte) 0, (byte) 0, (byte) 10, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][62] = new TextureOffset((byte) 0, (byte) 0, (byte) 13, (byte) 7, (byte) 5, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][63] = new TextureOffset((byte) 0, (byte) 0, (byte) 27, (byte) 20, (byte) 5, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][64] = new TextureOffset((byte) 0, (byte) 0, (byte) 26, (byte) 21, (byte) 5, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][65] = new TextureOffset((byte) 0, (byte) 0, (byte) 28, (byte) 22, (byte) 5, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][66] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 5, (byte) 13, (byte) 7);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][67] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 5, (byte) 27, (byte) 20);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][68] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 5, (byte) 26, (byte) 21);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][69] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 5, (byte) 28, (byte) 22);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][70] = new TextureOffset((byte) 7, (byte) 7, (byte) 0, (byte) 10, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][71] = new TextureOffset((byte) 20, (byte) 21, (byte) 0, (byte) 10, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][72] = new TextureOffset((byte) 21, (byte) 20, (byte) 0, (byte) 10, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][73] = new TextureOffset((byte) 22, (byte) 22, (byte) 0, (byte) 10, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][74] = new TextureOffset((byte) 0, (byte) 0, (byte) 5, (byte) 0, (byte) 7, (byte) 13);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][75] = new TextureOffset((byte) 0, (byte) 0, (byte) 5, (byte) 0, (byte) 20, (byte) 27);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][76] = new TextureOffset((byte) 0, (byte) 0, (byte) 5, (byte) 0, (byte) 21, (byte) 26);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][77] = new TextureOffset((byte) 0, (byte) 0, (byte) 5, (byte) 0, (byte) 22, (byte) 28);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][78] = new TextureOffset((byte) 13, (byte) 13, (byte) 10, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][79] = new TextureOffset((byte) 26, (byte) 27, (byte) 10, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][80] = new TextureOffset((byte) 27, (byte) 26, (byte) 10, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][81] = new TextureOffset((byte) 28, (byte) 28, (byte) 10, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][82] = new TextureOffset((byte) 0, (byte) 6, (byte) 0, (byte) 9, (byte) 0, (byte) 3);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][83] = new TextureOffset((byte) 0, (byte) 6, (byte) 0, (byte) 19, (byte) 0, (byte) 3);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][84] = new TextureOffset((byte) 0, (byte) 6, (byte) 0, (byte) 9, (byte) 0, (byte) 16);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][85] = new TextureOffset((byte) 0, (byte) 6, (byte) 0, (byte) 19, (byte) 0, (byte) 16);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][86] = new TextureOffset((byte) 0, (byte) 17, (byte) 0, (byte) 9, (byte) 0, (byte) 3);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][87] = new TextureOffset((byte) 0, (byte) 17, (byte) 0, (byte) 19, (byte) 0, (byte) 3);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][88] = new TextureOffset((byte) 0, (byte) 17, (byte) 0, (byte) 9, (byte) 0, (byte) 16);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][89] = new TextureOffset((byte) 0, (byte) 17, (byte) 0, (byte) 19, (byte) 0, (byte) 16);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][90] = new TextureOffset((byte) 0, (byte) 3, (byte) 0, (byte) 3, (byte) 9, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][91] = new TextureOffset((byte) 0, (byte) 3, (byte) 0, (byte) 3, (byte) 19, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][92] = new TextureOffset((byte) 0, (byte) 3, (byte) 0, (byte) 16, (byte) 9, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][93] = new TextureOffset((byte) 0, (byte) 3, (byte) 0, (byte) 16, (byte) 19, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][94] = new TextureOffset((byte) 0, (byte) 16, (byte) 0, (byte) 3, (byte) 9, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][95] = new TextureOffset((byte) 0, (byte) 16, (byte) 0, (byte) 3, (byte) 19, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][96] = new TextureOffset((byte) 0, (byte) 16, (byte) 0, (byte) 16, (byte) 9, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][97] = new TextureOffset((byte) 0, (byte) 16, (byte) 0, (byte) 16, (byte) 19, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][98] = new TextureOffset((byte) 0, (byte) 12, (byte) 3, (byte) 0, (byte) 0, (byte) 9);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][99] = new TextureOffset((byte) 0, (byte) 12, (byte) 16, (byte) 0, (byte) 0, (byte) 9);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][100] = new TextureOffset((byte) 0, (byte) 12, (byte) 3, (byte) 0, (byte) 0, (byte) 19);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][101] = new TextureOffset((byte) 0, (byte) 12, (byte) 16, (byte) 0, (byte) 0, (byte) 19);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][102] = new TextureOffset((byte) 0, (byte) 18, (byte) 3, (byte) 0, (byte) 0, (byte) 9);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][103] = new TextureOffset((byte) 0, (byte) 18, (byte) 16, (byte) 0, (byte) 0, (byte) 9);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][104] = new TextureOffset((byte) 0, (byte) 18, (byte) 3, (byte) 0, (byte) 0, (byte) 19);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][105] = new TextureOffset((byte) 0, (byte) 18, (byte) 16, (byte) 0, (byte) 0, (byte) 19);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][106] = new TextureOffset((byte) 0, (byte) 9, (byte) 9, (byte) 0, (byte) 3, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][107] = new TextureOffset((byte) 0, (byte) 9, (byte) 19, (byte) 0, (byte) 3, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][108] = new TextureOffset((byte) 0, (byte) 9, (byte) 9, (byte) 0, (byte) 16, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][109] = new TextureOffset((byte) 0, (byte) 9, (byte) 19, (byte) 0, (byte) 16, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][110] = new TextureOffset((byte) 0, (byte) 19, (byte) 9, (byte) 0, (byte) 3, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][111] = new TextureOffset((byte) 0, (byte) 19, (byte) 19, (byte) 0, (byte) 3, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][112] = new TextureOffset((byte) 0, (byte) 19, (byte) 9, (byte) 0, (byte) 16, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][113] = new TextureOffset((byte) 0, (byte) 19, (byte) 19, (byte) 0, (byte) 16, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][114] = new TextureOffset((byte) 3, (byte) 0, (byte) 0, (byte) 12, (byte) 0, (byte) 6);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][115] = new TextureOffset((byte) 3, (byte) 0, (byte) 0, (byte) 18, (byte) 0, (byte) 6);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][116] = new TextureOffset((byte) 3, (byte) 0, (byte) 0, (byte) 12, (byte) 0, (byte) 17);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][117] = new TextureOffset((byte) 3, (byte) 0, (byte) 0, (byte) 18, (byte) 0, (byte) 17);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][118] = new TextureOffset((byte) 16, (byte) 0, (byte) 0, (byte) 12, (byte) 0, (byte) 6);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][119] = new TextureOffset((byte) 16, (byte) 0, (byte) 0, (byte) 18, (byte) 0, (byte) 6);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][120] = new TextureOffset((byte) 16, (byte) 0, (byte) 0, (byte) 12, (byte) 0, (byte) 17);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][121] = new TextureOffset((byte) 16, (byte) 0, (byte) 0, (byte) 18, (byte) 0, (byte) 17);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][122] = new TextureOffset((byte) 6, (byte) 0, (byte) 0, (byte) 6, (byte) 12, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][123] = new TextureOffset((byte) 6, (byte) 0, (byte) 0, (byte) 6, (byte) 18, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][124] = new TextureOffset((byte) 6, (byte) 0, (byte) 0, (byte) 17, (byte) 12, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][125] = new TextureOffset((byte) 6, (byte) 0, (byte) 0, (byte) 17, (byte) 18, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][126] = new TextureOffset((byte) 17, (byte) 0, (byte) 0, (byte) 6, (byte) 12, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][127] = new TextureOffset((byte) 17, (byte) 0, (byte) 0, (byte) 6, (byte) 18, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][128] = new TextureOffset((byte) 17, (byte) 0, (byte) 0, (byte) 17, (byte) 12, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][129] = new TextureOffset((byte) 17, (byte) 0, (byte) 0, (byte) 17, (byte) 18, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][130] = new TextureOffset((byte) 9, (byte) 0, (byte) 6, (byte) 0, (byte) 0, (byte) 12);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][131] = new TextureOffset((byte) 9, (byte) 0, (byte) 17, (byte) 0, (byte) 0, (byte) 12);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][132] = new TextureOffset((byte) 9, (byte) 0, (byte) 6, (byte) 0, (byte) 0, (byte) 18);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][133] = new TextureOffset((byte) 9, (byte) 0, (byte) 17, (byte) 0, (byte) 0, (byte) 18);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][134] = new TextureOffset((byte) 19, (byte) 0, (byte) 6, (byte) 0, (byte) 0, (byte) 12);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][135] = new TextureOffset((byte) 19, (byte) 0, (byte) 17, (byte) 0, (byte) 0, (byte) 12);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][136] = new TextureOffset((byte) 19, (byte) 0, (byte) 6, (byte) 0, (byte) 0, (byte) 18);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][137] = new TextureOffset((byte) 19, (byte) 0, (byte) 17, (byte) 0, (byte) 0, (byte) 18);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][138] = new TextureOffset((byte) 12, (byte) 0, (byte) 12, (byte) 0, (byte) 6, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][139] = new TextureOffset((byte) 12, (byte) 0, (byte) 18, (byte) 0, (byte) 6, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][140] = new TextureOffset((byte) 12, (byte) 0, (byte) 12, (byte) 0, (byte) 17, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][141] = new TextureOffset((byte) 12, (byte) 0, (byte) 18, (byte) 0, (byte) 17, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][142] = new TextureOffset((byte) 18, (byte) 0, (byte) 12, (byte) 0, (byte) 6, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][143] = new TextureOffset((byte) 18, (byte) 0, (byte) 18, (byte) 0, (byte) 6, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][144] = new TextureOffset((byte) 18, (byte) 0, (byte) 12, (byte) 0, (byte) 17, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][145] = new TextureOffset((byte) 18, (byte) 0, (byte) 18, (byte) 0, (byte) 17, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][146] = new TextureOffset((byte) 15, (byte) 15, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][147] = new TextureOffset((byte) 34, (byte) 36, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][148] = new TextureOffset((byte) 36, (byte) 34, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][149] = new TextureOffset((byte) 38, (byte) 38, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][150] = new TextureOffset((byte) 40, (byte) 33, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][151] = new TextureOffset((byte) 42, (byte) 37, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][152] = new TextureOffset((byte) 44, (byte) 35, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][153] = new TextureOffset((byte) 46, (byte) 39, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][154] = new TextureOffset((byte) 33, (byte) 40, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][155] = new TextureOffset((byte) 35, (byte) 44, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][156] = new TextureOffset((byte) 37, (byte) 42, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][157] = new TextureOffset((byte) 39, (byte) 46, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][158] = new TextureOffset((byte) 41, (byte) 41, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][159] = new TextureOffset((byte) 43, (byte) 45, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][160] = new TextureOffset((byte) 45, (byte) 43, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][161] = new TextureOffset((byte) 47, (byte) 47, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][162] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 15, (byte) 15);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][163] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 33, (byte) 34);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][164] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 34, (byte) 33);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][165] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 35, (byte) 35);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][166] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 40, (byte) 36);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][167] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 41, (byte) 38);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][168] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 42, (byte) 37);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][169] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 43, (byte) 39);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][170] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 36, (byte) 40);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][171] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 37, (byte) 42);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][172] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 41);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][173] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 39, (byte) 43);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][174] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 44, (byte) 44);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][175] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 45, (byte) 46);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][176] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 46, (byte) 45);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][177] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 47, (byte) 47);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][178] = new TextureOffset((byte) 0, (byte) 0, (byte) 15, (byte) 15, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][179] = new TextureOffset((byte) 0, (byte) 0, (byte) 34, (byte) 33, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][180] = new TextureOffset((byte) 0, (byte) 0, (byte) 33, (byte) 34, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][181] = new TextureOffset((byte) 0, (byte) 0, (byte) 35, (byte) 35, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][182] = new TextureOffset((byte) 0, (byte) 0, (byte) 36, (byte) 40, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][183] = new TextureOffset((byte) 0, (byte) 0, (byte) 38, (byte) 41, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][184] = new TextureOffset((byte) 0, (byte) 0, (byte) 37, (byte) 42, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][185] = new TextureOffset((byte) 0, (byte) 0, (byte) 39, (byte) 43, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][186] = new TextureOffset((byte) 0, (byte) 0, (byte) 40, (byte) 36, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][187] = new TextureOffset((byte) 0, (byte) 0, (byte) 42, (byte) 37, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][188] = new TextureOffset((byte) 0, (byte) 0, (byte) 41, (byte) 38, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][189] = new TextureOffset((byte) 0, (byte) 0, (byte) 43, (byte) 39, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][190] = new TextureOffset((byte) 0, (byte) 0, (byte) 44, (byte) 44, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][191] = new TextureOffset((byte) 0, (byte) 0, (byte) 46, (byte) 45, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][192] = new TextureOffset((byte) 0, (byte) 0, (byte) 45, (byte) 46, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][193] = new TextureOffset((byte) 0, (byte) 0, (byte) 47, (byte) 47, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][194] = new TextureOffset((byte) 0, (byte) 13, (byte) 11, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][195] = new TextureOffset((byte) 0, (byte) 13, (byte) 24, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][196] = new TextureOffset((byte) 0, (byte) 13, (byte) 23, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][197] = new TextureOffset((byte) 0, (byte) 13, (byte) 25, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][198] = new TextureOffset((byte) 0, (byte) 27, (byte) 11, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][199] = new TextureOffset((byte) 0, (byte) 27, (byte) 24, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][200] = new TextureOffset((byte) 0, (byte) 27, (byte) 23, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][201] = new TextureOffset((byte) 0, (byte) 27, (byte) 25, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][202] = new TextureOffset((byte) 0, (byte) 26, (byte) 11, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][203] = new TextureOffset((byte) 0, (byte) 26, (byte) 24, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][204] = new TextureOffset((byte) 0, (byte) 26, (byte) 23, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][205] = new TextureOffset((byte) 0, (byte) 26, (byte) 25, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][206] = new TextureOffset((byte) 0, (byte) 28, (byte) 11, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][207] = new TextureOffset((byte) 0, (byte) 28, (byte) 24, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][208] = new TextureOffset((byte) 0, (byte) 28, (byte) 23, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][209] = new TextureOffset((byte) 0, (byte) 28, (byte) 25, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][210] = new TextureOffset((byte) 0, (byte) 7, (byte) 0, (byte) 11, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][211] = new TextureOffset((byte) 0, (byte) 7, (byte) 0, (byte) 23, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][212] = new TextureOffset((byte) 0, (byte) 7, (byte) 0, (byte) 24, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][213] = new TextureOffset((byte) 0, (byte) 7, (byte) 0, (byte) 25, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][214] = new TextureOffset((byte) 0, (byte) 21, (byte) 0, (byte) 11, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][215] = new TextureOffset((byte) 0, (byte) 21, (byte) 0, (byte) 23, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][216] = new TextureOffset((byte) 0, (byte) 21, (byte) 0, (byte) 24, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][217] = new TextureOffset((byte) 0, (byte) 21, (byte) 0, (byte) 25, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][218] = new TextureOffset((byte) 0, (byte) 20, (byte) 0, (byte) 11, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][219] = new TextureOffset((byte) 0, (byte) 20, (byte) 0, (byte) 23, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][220] = new TextureOffset((byte) 0, (byte) 20, (byte) 0, (byte) 24, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][221] = new TextureOffset((byte) 0, (byte) 20, (byte) 0, (byte) 25, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][222] = new TextureOffset((byte) 0, (byte) 22, (byte) 0, (byte) 11, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][223] = new TextureOffset((byte) 0, (byte) 22, (byte) 0, (byte) 23, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][224] = new TextureOffset((byte) 0, (byte) 22, (byte) 0, (byte) 24, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][225] = new TextureOffset((byte) 0, (byte) 22, (byte) 0, (byte) 25, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][226] = new TextureOffset((byte) 0, (byte) 11, (byte) 0, (byte) 0, (byte) 11, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][227] = new TextureOffset((byte) 0, (byte) 11, (byte) 0, (byte) 0, (byte) 23, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][228] = new TextureOffset((byte) 0, (byte) 11, (byte) 0, (byte) 0, (byte) 24, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][229] = new TextureOffset((byte) 0, (byte) 11, (byte) 0, (byte) 0, (byte) 25, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][230] = new TextureOffset((byte) 0, (byte) 24, (byte) 0, (byte) 0, (byte) 11, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][231] = new TextureOffset((byte) 0, (byte) 24, (byte) 0, (byte) 0, (byte) 23, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][232] = new TextureOffset((byte) 0, (byte) 24, (byte) 0, (byte) 0, (byte) 24, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][233] = new TextureOffset((byte) 0, (byte) 24, (byte) 0, (byte) 0, (byte) 25, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][234] = new TextureOffset((byte) 0, (byte) 23, (byte) 0, (byte) 0, (byte) 11, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][235] = new TextureOffset((byte) 0, (byte) 23, (byte) 0, (byte) 0, (byte) 23, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][236] = new TextureOffset((byte) 0, (byte) 23, (byte) 0, (byte) 0, (byte) 24, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][237] = new TextureOffset((byte) 0, (byte) 23, (byte) 0, (byte) 0, (byte) 25, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][238] = new TextureOffset((byte) 0, (byte) 25, (byte) 0, (byte) 0, (byte) 11, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][239] = new TextureOffset((byte) 0, (byte) 25, (byte) 0, (byte) 0, (byte) 23, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][240] = new TextureOffset((byte) 0, (byte) 25, (byte) 0, (byte) 0, (byte) 24, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][241] = new TextureOffset((byte) 0, (byte) 25, (byte) 0, (byte) 0, (byte) 25, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][242] = new TextureOffset((byte) 0, (byte) 14, (byte) 0, (byte) 0, (byte) 0, (byte) 11);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][243] = new TextureOffset((byte) 0, (byte) 14, (byte) 0, (byte) 0, (byte) 0, (byte) 24);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][244] = new TextureOffset((byte) 0, (byte) 14, (byte) 0, (byte) 0, (byte) 0, (byte) 23);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][245] = new TextureOffset((byte) 0, (byte) 14, (byte) 0, (byte) 0, (byte) 0, (byte) 25);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][246] = new TextureOffset((byte) 0, (byte) 29, (byte) 0, (byte) 0, (byte) 0, (byte) 11);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][247] = new TextureOffset((byte) 0, (byte) 29, (byte) 0, (byte) 0, (byte) 0, (byte) 24);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][248] = new TextureOffset((byte) 0, (byte) 29, (byte) 0, (byte) 0, (byte) 0, (byte) 23);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][249] = new TextureOffset((byte) 0, (byte) 29, (byte) 0, (byte) 0, (byte) 0, (byte) 25);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][250] = new TextureOffset((byte) 0, (byte) 30, (byte) 0, (byte) 0, (byte) 0, (byte) 11);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][251] = new TextureOffset((byte) 0, (byte) 30, (byte) 0, (byte) 0, (byte) 0, (byte) 24);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][252] = new TextureOffset((byte) 0, (byte) 30, (byte) 0, (byte) 0, (byte) 0, (byte) 23);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][253] = new TextureOffset((byte) 0, (byte) 30, (byte) 0, (byte) 0, (byte) 0, (byte) 25);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][254] = new TextureOffset((byte) 0, (byte) 31, (byte) 0, (byte) 0, (byte) 0, (byte) 11);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][255] = new TextureOffset((byte) 0, (byte) 31, (byte) 0, (byte) 0, (byte) 0, (byte) 24);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][256] = new TextureOffset((byte) 0, (byte) 31, (byte) 0, (byte) 0, (byte) 0, (byte) 23);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][257] = new TextureOffset((byte) 0, (byte) 31, (byte) 0, (byte) 0, (byte) 0, (byte) 25);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][258] = new TextureOffset((byte) 13, (byte) 0, (byte) 14, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][259] = new TextureOffset((byte) 13, (byte) 0, (byte) 29, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][260] = new TextureOffset((byte) 13, (byte) 0, (byte) 30, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][261] = new TextureOffset((byte) 13, (byte) 0, (byte) 31, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][262] = new TextureOffset((byte) 26, (byte) 0, (byte) 14, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][263] = new TextureOffset((byte) 26, (byte) 0, (byte) 29, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][264] = new TextureOffset((byte) 26, (byte) 0, (byte) 30, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][265] = new TextureOffset((byte) 26, (byte) 0, (byte) 31, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][266] = new TextureOffset((byte) 27, (byte) 0, (byte) 14, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][267] = new TextureOffset((byte) 27, (byte) 0, (byte) 29, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][268] = new TextureOffset((byte) 27, (byte) 0, (byte) 30, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][269] = new TextureOffset((byte) 27, (byte) 0, (byte) 31, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][270] = new TextureOffset((byte) 28, (byte) 0, (byte) 14, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][271] = new TextureOffset((byte) 28, (byte) 0, (byte) 29, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][272] = new TextureOffset((byte) 28, (byte) 0, (byte) 30, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][273] = new TextureOffset((byte) 28, (byte) 0, (byte) 31, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][274] = new TextureOffset((byte) 7, (byte) 0, (byte) 0, (byte) 14, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][275] = new TextureOffset((byte) 7, (byte) 0, (byte) 0, (byte) 30, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][276] = new TextureOffset((byte) 7, (byte) 0, (byte) 0, (byte) 29, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][277] = new TextureOffset((byte) 7, (byte) 0, (byte) 0, (byte) 31, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][278] = new TextureOffset((byte) 20, (byte) 0, (byte) 0, (byte) 14, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][279] = new TextureOffset((byte) 20, (byte) 0, (byte) 0, (byte) 30, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][280] = new TextureOffset((byte) 20, (byte) 0, (byte) 0, (byte) 29, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][281] = new TextureOffset((byte) 20, (byte) 0, (byte) 0, (byte) 31, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][282] = new TextureOffset((byte) 21, (byte) 0, (byte) 0, (byte) 14, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][283] = new TextureOffset((byte) 21, (byte) 0, (byte) 0, (byte) 30, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][284] = new TextureOffset((byte) 21, (byte) 0, (byte) 0, (byte) 29, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][285] = new TextureOffset((byte) 21, (byte) 0, (byte) 0, (byte) 31, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][286] = new TextureOffset((byte) 22, (byte) 0, (byte) 0, (byte) 14, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][287] = new TextureOffset((byte) 22, (byte) 0, (byte) 0, (byte) 30, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][288] = new TextureOffset((byte) 22, (byte) 0, (byte) 0, (byte) 29, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][289] = new TextureOffset((byte) 22, (byte) 0, (byte) 0, (byte) 31, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][290] = new TextureOffset((byte) 14, (byte) 0, (byte) 0, (byte) 0, (byte) 14, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][291] = new TextureOffset((byte) 14, (byte) 0, (byte) 0, (byte) 0, (byte) 30, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][292] = new TextureOffset((byte) 14, (byte) 0, (byte) 0, (byte) 0, (byte) 29, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][293] = new TextureOffset((byte) 14, (byte) 0, (byte) 0, (byte) 0, (byte) 31, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][294] = new TextureOffset((byte) 29, (byte) 0, (byte) 0, (byte) 0, (byte) 14, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][295] = new TextureOffset((byte) 29, (byte) 0, (byte) 0, (byte) 0, (byte) 30, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][296] = new TextureOffset((byte) 29, (byte) 0, (byte) 0, (byte) 0, (byte) 29, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][297] = new TextureOffset((byte) 29, (byte) 0, (byte) 0, (byte) 0, (byte) 31, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][298] = new TextureOffset((byte) 30, (byte) 0, (byte) 0, (byte) 0, (byte) 14, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][299] = new TextureOffset((byte) 30, (byte) 0, (byte) 0, (byte) 0, (byte) 30, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][300] = new TextureOffset((byte) 30, (byte) 0, (byte) 0, (byte) 0, (byte) 29, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][301] = new TextureOffset((byte) 30, (byte) 0, (byte) 0, (byte) 0, (byte) 31, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][302] = new TextureOffset((byte) 31, (byte) 0, (byte) 0, (byte) 0, (byte) 14, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][303] = new TextureOffset((byte) 31, (byte) 0, (byte) 0, (byte) 0, (byte) 30, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][304] = new TextureOffset((byte) 31, (byte) 0, (byte) 0, (byte) 0, (byte) 29, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][305] = new TextureOffset((byte) 31, (byte) 0, (byte) 0, (byte) 0, (byte) 31, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][306] = new TextureOffset((byte) 11, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 14);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][307] = new TextureOffset((byte) 11, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 29);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][308] = new TextureOffset((byte) 11, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 30);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][309] = new TextureOffset((byte) 11, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 31);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][310] = new TextureOffset((byte) 24, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 14);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][311] = new TextureOffset((byte) 24, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 29);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][312] = new TextureOffset((byte) 24, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 30);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][313] = new TextureOffset((byte) 24, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 31);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][314] = new TextureOffset((byte) 23, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 14);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][315] = new TextureOffset((byte) 23, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 29);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][316] = new TextureOffset((byte) 23, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 30);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][317] = new TextureOffset((byte) 23, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 31);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][318] = new TextureOffset((byte) 25, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 14);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][319] = new TextureOffset((byte) 25, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 29);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][320] = new TextureOffset((byte) 25, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 30);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][321] = new TextureOffset((byte) 25, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 31);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][322] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 13, (byte) 0, (byte) 7);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][323] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 27, (byte) 0, (byte) 7);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][324] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 13, (byte) 0, (byte) 20);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][325] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 27, (byte) 0, (byte) 20);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][326] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 26, (byte) 0, (byte) 7);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][327] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 28, (byte) 0, (byte) 7);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][328] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 26, (byte) 0, (byte) 20);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][329] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 28, (byte) 0, (byte) 20);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][330] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 13, (byte) 0, (byte) 21);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][331] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 27, (byte) 0, (byte) 21);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][332] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 13, (byte) 0, (byte) 22);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][333] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 27, (byte) 0, (byte) 22);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][334] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 26, (byte) 0, (byte) 21);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][335] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 28, (byte) 0, (byte) 21);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][336] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 26, (byte) 0, (byte) 22);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][337] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 28, (byte) 0, (byte) 22);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][338] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 7, (byte) 13, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][339] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 7, (byte) 27, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][340] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 20, (byte) 13, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][341] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 20, (byte) 27, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][342] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 7, (byte) 26, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][343] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 7, (byte) 28, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][344] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 20, (byte) 26, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][345] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 20, (byte) 28, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][346] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 21, (byte) 13, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][347] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 21, (byte) 27, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][348] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 22, (byte) 13, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][349] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 22, (byte) 27, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][350] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 21, (byte) 26, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][351] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 21, (byte) 28, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][352] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 22, (byte) 26, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][353] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 22, (byte) 28, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][354] = new TextureOffset((byte) 0, (byte) 0, (byte) 7, (byte) 0, (byte) 0, (byte) 13);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][355] = new TextureOffset((byte) 0, (byte) 0, (byte) 20, (byte) 0, (byte) 0, (byte) 13);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][356] = new TextureOffset((byte) 0, (byte) 0, (byte) 7, (byte) 0, (byte) 0, (byte) 27);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][357] = new TextureOffset((byte) 0, (byte) 0, (byte) 20, (byte) 0, (byte) 0, (byte) 27);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][358] = new TextureOffset((byte) 0, (byte) 0, (byte) 21, (byte) 0, (byte) 0, (byte) 13);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][359] = new TextureOffset((byte) 0, (byte) 0, (byte) 22, (byte) 0, (byte) 0, (byte) 13);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][360] = new TextureOffset((byte) 0, (byte) 0, (byte) 21, (byte) 0, (byte) 0, (byte) 27);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][361] = new TextureOffset((byte) 0, (byte) 0, (byte) 22, (byte) 0, (byte) 0, (byte) 27);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][362] = new TextureOffset((byte) 0, (byte) 0, (byte) 7, (byte) 0, (byte) 0, (byte) 26);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][363] = new TextureOffset((byte) 0, (byte) 0, (byte) 20, (byte) 0, (byte) 0, (byte) 26);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][364] = new TextureOffset((byte) 0, (byte) 0, (byte) 7, (byte) 0, (byte) 0, (byte) 28);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][365] = new TextureOffset((byte) 0, (byte) 0, (byte) 20, (byte) 0, (byte) 0, (byte) 28);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][366] = new TextureOffset((byte) 0, (byte) 0, (byte) 21, (byte) 0, (byte) 0, (byte) 26);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][367] = new TextureOffset((byte) 0, (byte) 0, (byte) 22, (byte) 0, (byte) 0, (byte) 26);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][368] = new TextureOffset((byte) 0, (byte) 0, (byte) 21, (byte) 0, (byte) 0, (byte) 28);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][369] = new TextureOffset((byte) 0, (byte) 0, (byte) 22, (byte) 0, (byte) 0, (byte) 28);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][370] = new TextureOffset((byte) 0, (byte) 0, (byte) 13, (byte) 0, (byte) 7, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][371] = new TextureOffset((byte) 0, (byte) 0, (byte) 27, (byte) 0, (byte) 7, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][372] = new TextureOffset((byte) 0, (byte) 0, (byte) 13, (byte) 0, (byte) 20, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][373] = new TextureOffset((byte) 0, (byte) 0, (byte) 27, (byte) 0, (byte) 20, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][374] = new TextureOffset((byte) 0, (byte) 0, (byte) 26, (byte) 0, (byte) 7, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][375] = new TextureOffset((byte) 0, (byte) 0, (byte) 28, (byte) 0, (byte) 7, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][376] = new TextureOffset((byte) 0, (byte) 0, (byte) 26, (byte) 0, (byte) 20, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][377] = new TextureOffset((byte) 0, (byte) 0, (byte) 28, (byte) 0, (byte) 20, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][378] = new TextureOffset((byte) 0, (byte) 0, (byte) 13, (byte) 0, (byte) 21, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][379] = new TextureOffset((byte) 0, (byte) 0, (byte) 27, (byte) 0, (byte) 21, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][380] = new TextureOffset((byte) 0, (byte) 0, (byte) 13, (byte) 0, (byte) 22, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][381] = new TextureOffset((byte) 0, (byte) 0, (byte) 27, (byte) 0, (byte) 22, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][382] = new TextureOffset((byte) 0, (byte) 0, (byte) 26, (byte) 0, (byte) 21, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][383] = new TextureOffset((byte) 0, (byte) 0, (byte) 28, (byte) 0, (byte) 21, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][384] = new TextureOffset((byte) 0, (byte) 0, (byte) 26, (byte) 0, (byte) 22, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][385] = new TextureOffset((byte) 0, (byte) 0, (byte) 28, (byte) 0, (byte) 22, (byte) 0);

		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][0] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][1] = new TextureOffset((byte) 0, (byte) 0, (byte) 8, (byte) 8, (byte) 8, (byte) 8);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][2] = new TextureOffset((byte) 0, (byte) 0, (byte) 2, (byte) 2, (byte) 2, (byte) 2);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][3] = new TextureOffset((byte) 1, (byte) 1, (byte) 0, (byte) 0, (byte) 4, (byte) 1);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][4] = new TextureOffset((byte) 4, (byte) 4, (byte) 0, (byte) 0, (byte) 1, (byte) 4);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][5] = new TextureOffset((byte) 8, (byte) 2, (byte) 1, (byte) 4, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][6] = new TextureOffset((byte) 2, (byte) 8, (byte) 4, (byte) 1, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][7] = new TextureOffset((byte) 0, (byte) 0, (byte) 10, (byte) 10, (byte) 10, (byte) 10);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][8] = new TextureOffset((byte) 5, (byte) 5, (byte) 0, (byte) 0, (byte) 5, (byte) 5);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][9] = new TextureOffset((byte) 10, (byte) 10, (byte) 5, (byte) 5, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][10] = new TextureOffset((byte) 0, (byte) 1, (byte) 0, (byte) 8, (byte) 12, (byte) 9);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][11] = new TextureOffset((byte) 0, (byte) 1, (byte) 0, (byte) 8, (byte) 18, (byte) 19);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][12] = new TextureOffset((byte) 0, (byte) 4, (byte) 8, (byte) 0, (byte) 9, (byte) 12);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][13] = new TextureOffset((byte) 0, (byte) 4, (byte) 8, (byte) 0, (byte) 19, (byte) 18);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][14] = new TextureOffset((byte) 0, (byte) 2, (byte) 9, (byte) 12, (byte) 0, (byte) 8);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][15] = new TextureOffset((byte) 0, (byte) 2, (byte) 19, (byte) 18, (byte) 0, (byte) 8);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][16] = new TextureOffset((byte) 0, (byte) 8, (byte) 12, (byte) 9, (byte) 8, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][17] = new TextureOffset((byte) 0, (byte) 8, (byte) 18, (byte) 19, (byte) 8, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][18] = new TextureOffset((byte) 1, (byte) 0, (byte) 0, (byte) 2, (byte) 6, (byte) 3);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][19] = new TextureOffset((byte) 1, (byte) 0, (byte) 0, (byte) 2, (byte) 17, (byte) 16);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][20] = new TextureOffset((byte) 4, (byte) 0, (byte) 2, (byte) 0, (byte) 3, (byte) 6);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][21] = new TextureOffset((byte) 4, (byte) 0, (byte) 2, (byte) 0, (byte) 16, (byte) 17);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][22] = new TextureOffset((byte) 8, (byte) 0, (byte) 3, (byte) 6, (byte) 0, (byte) 2);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][23] = new TextureOffset((byte) 8, (byte) 0, (byte) 16, (byte) 17, (byte) 0, (byte) 2);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][24] = new TextureOffset((byte) 2, (byte) 0, (byte) 6, (byte) 3, (byte) 2, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][25] = new TextureOffset((byte) 2, (byte) 0, (byte) 17, (byte) 16, (byte) 2, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][26] = new TextureOffset((byte) 9, (byte) 3, (byte) 0, (byte) 4, (byte) 0, (byte) 1);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][27] = new TextureOffset((byte) 19, (byte) 16, (byte) 0, (byte) 4, (byte) 0, (byte) 1);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][28] = new TextureOffset((byte) 3, (byte) 9, (byte) 0, (byte) 1, (byte) 4, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][29] = new TextureOffset((byte) 16, (byte) 19, (byte) 0, (byte) 1, (byte) 4, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][30] = new TextureOffset((byte) 12, (byte) 6, (byte) 1, (byte) 0, (byte) 0, (byte) 4);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][31] = new TextureOffset((byte) 18, (byte) 17, (byte) 1, (byte) 0, (byte) 0, (byte) 4);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][32] = new TextureOffset((byte) 6, (byte) 12, (byte) 4, (byte) 0, (byte) 1, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][33] = new TextureOffset((byte) 17, (byte) 18, (byte) 4, (byte) 0, (byte) 1, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][34] = new TextureOffset((byte) 0, (byte) 5, (byte) 0, (byte) 0, (byte) 13, (byte) 13);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][35] = new TextureOffset((byte) 0, (byte) 5, (byte) 0, (byte) 0, (byte) 26, (byte) 27);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][36] = new TextureOffset((byte) 0, (byte) 5, (byte) 0, (byte) 0, (byte) 27, (byte) 26);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][37] = new TextureOffset((byte) 0, (byte) 5, (byte) 0, (byte) 0, (byte) 28, (byte) 28);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][38] = new TextureOffset((byte) 0, (byte) 10, (byte) 13, (byte) 13, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][39] = new TextureOffset((byte) 0, (byte) 10, (byte) 27, (byte) 26, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][40] = new TextureOffset((byte) 0, (byte) 10, (byte) 26, (byte) 27, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][41] = new TextureOffset((byte) 0, (byte) 10, (byte) 28, (byte) 28, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][42] = new TextureOffset((byte) 5, (byte) 0, (byte) 0, (byte) 0, (byte) 7, (byte) 7);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][43] = new TextureOffset((byte) 5, (byte) 0, (byte) 0, (byte) 0, (byte) 21, (byte) 20);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][44] = new TextureOffset((byte) 5, (byte) 0, (byte) 0, (byte) 0, (byte) 20, (byte) 21);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][45] = new TextureOffset((byte) 5, (byte) 0, (byte) 0, (byte) 0, (byte) 22, (byte) 22);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][46] = new TextureOffset((byte) 10, (byte) 0, (byte) 7, (byte) 7, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][47] = new TextureOffset((byte) 10, (byte) 0, (byte) 20, (byte) 21, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][48] = new TextureOffset((byte) 10, (byte) 0, (byte) 21, (byte) 20, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][49] = new TextureOffset((byte) 10, (byte) 0, (byte) 22, (byte) 22, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][50] = new TextureOffset((byte) 13, (byte) 7, (byte) 0, (byte) 0, (byte) 0, (byte) 5);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][51] = new TextureOffset((byte) 27, (byte) 20, (byte) 0, (byte) 0, (byte) 0, (byte) 5);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][52] = new TextureOffset((byte) 26, (byte) 21, (byte) 0, (byte) 0, (byte) 0, (byte) 5);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][53] = new TextureOffset((byte) 28, (byte) 22, (byte) 0, (byte) 0, (byte) 0, (byte) 5);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][54] = new TextureOffset((byte) 0, (byte) 0, (byte) 11, (byte) 14, (byte) 0, (byte) 10);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][55] = new TextureOffset((byte) 0, (byte) 0, (byte) 23, (byte) 30, (byte) 0, (byte) 10);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][56] = new TextureOffset((byte) 0, (byte) 0, (byte) 24, (byte) 29, (byte) 0, (byte) 10);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][57] = new TextureOffset((byte) 0, (byte) 0, (byte) 25, (byte) 31, (byte) 0, (byte) 10);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][58] = new TextureOffset((byte) 7, (byte) 13, (byte) 0, (byte) 0, (byte) 5, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][59] = new TextureOffset((byte) 20, (byte) 27, (byte) 0, (byte) 0, (byte) 5, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][60] = new TextureOffset((byte) 21, (byte) 26, (byte) 0, (byte) 0, (byte) 5, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][61] = new TextureOffset((byte) 22, (byte) 28, (byte) 0, (byte) 0, (byte) 5, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][62] = new TextureOffset((byte) 0, (byte) 0, (byte) 14, (byte) 11, (byte) 10, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][63] = new TextureOffset((byte) 0, (byte) 0, (byte) 30, (byte) 23, (byte) 10, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][64] = new TextureOffset((byte) 0, (byte) 0, (byte) 29, (byte) 24, (byte) 10, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][65] = new TextureOffset((byte) 0, (byte) 0, (byte) 31, (byte) 25, (byte) 10, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][66] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 10, (byte) 14, (byte) 11);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][67] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 10, (byte) 30, (byte) 23);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][68] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 10, (byte) 29, (byte) 24);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][69] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 10, (byte) 31, (byte) 25);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][70] = new TextureOffset((byte) 11, (byte) 11, (byte) 0, (byte) 5, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][71] = new TextureOffset((byte) 23, (byte) 24, (byte) 0, (byte) 5, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][72] = new TextureOffset((byte) 24, (byte) 23, (byte) 0, (byte) 5, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][73] = new TextureOffset((byte) 25, (byte) 25, (byte) 0, (byte) 5, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][74] = new TextureOffset((byte) 0, (byte) 0, (byte) 10, (byte) 0, (byte) 11, (byte) 14);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][75] = new TextureOffset((byte) 0, (byte) 0, (byte) 10, (byte) 0, (byte) 23, (byte) 30);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][76] = new TextureOffset((byte) 0, (byte) 0, (byte) 10, (byte) 0, (byte) 24, (byte) 29);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][77] = new TextureOffset((byte) 0, (byte) 0, (byte) 10, (byte) 0, (byte) 25, (byte) 31);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][78] = new TextureOffset((byte) 14, (byte) 14, (byte) 5, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][79] = new TextureOffset((byte) 29, (byte) 30, (byte) 5, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][80] = new TextureOffset((byte) 30, (byte) 29, (byte) 5, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][81] = new TextureOffset((byte) 31, (byte) 31, (byte) 5, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][82] = new TextureOffset((byte) 0, (byte) 3, (byte) 0, (byte) 12, (byte) 0, (byte) 9);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][83] = new TextureOffset((byte) 0, (byte) 3, (byte) 0, (byte) 18, (byte) 0, (byte) 9);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][84] = new TextureOffset((byte) 0, (byte) 3, (byte) 0, (byte) 12, (byte) 0, (byte) 19);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][85] = new TextureOffset((byte) 0, (byte) 3, (byte) 0, (byte) 18, (byte) 0, (byte) 19);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][86] = new TextureOffset((byte) 0, (byte) 16, (byte) 0, (byte) 12, (byte) 0, (byte) 9);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][87] = new TextureOffset((byte) 0, (byte) 16, (byte) 0, (byte) 18, (byte) 0, (byte) 9);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][88] = new TextureOffset((byte) 0, (byte) 16, (byte) 0, (byte) 12, (byte) 0, (byte) 19);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][89] = new TextureOffset((byte) 0, (byte) 16, (byte) 0, (byte) 18, (byte) 0, (byte) 19);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][90] = new TextureOffset((byte) 0, (byte) 9, (byte) 0, (byte) 9, (byte) 12, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][91] = new TextureOffset((byte) 0, (byte) 9, (byte) 0, (byte) 9, (byte) 18, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][92] = new TextureOffset((byte) 0, (byte) 9, (byte) 0, (byte) 19, (byte) 12, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][93] = new TextureOffset((byte) 0, (byte) 9, (byte) 0, (byte) 19, (byte) 18, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][94] = new TextureOffset((byte) 0, (byte) 19, (byte) 0, (byte) 9, (byte) 12, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][95] = new TextureOffset((byte) 0, (byte) 19, (byte) 0, (byte) 9, (byte) 18, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][96] = new TextureOffset((byte) 0, (byte) 19, (byte) 0, (byte) 19, (byte) 12, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][97] = new TextureOffset((byte) 0, (byte) 19, (byte) 0, (byte) 19, (byte) 18, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][98] = new TextureOffset((byte) 0, (byte) 6, (byte) 9, (byte) 0, (byte) 0, (byte) 12);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][99] = new TextureOffset((byte) 0, (byte) 6, (byte) 19, (byte) 0, (byte) 0, (byte) 12);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][100] = new TextureOffset((byte) 0, (byte) 6, (byte) 9, (byte) 0, (byte) 0, (byte) 18);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][101] = new TextureOffset((byte) 0, (byte) 6, (byte) 19, (byte) 0, (byte) 0, (byte) 18);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][102] = new TextureOffset((byte) 0, (byte) 17, (byte) 9, (byte) 0, (byte) 0, (byte) 12);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][103] = new TextureOffset((byte) 0, (byte) 17, (byte) 19, (byte) 0, (byte) 0, (byte) 12);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][104] = new TextureOffset((byte) 0, (byte) 17, (byte) 9, (byte) 0, (byte) 0, (byte) 18);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][105] = new TextureOffset((byte) 0, (byte) 17, (byte) 19, (byte) 0, (byte) 0, (byte) 18);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][106] = new TextureOffset((byte) 0, (byte) 12, (byte) 12, (byte) 0, (byte) 9, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][107] = new TextureOffset((byte) 0, (byte) 12, (byte) 18, (byte) 0, (byte) 9, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][108] = new TextureOffset((byte) 0, (byte) 12, (byte) 12, (byte) 0, (byte) 19, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][109] = new TextureOffset((byte) 0, (byte) 12, (byte) 18, (byte) 0, (byte) 19, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][110] = new TextureOffset((byte) 0, (byte) 18, (byte) 12, (byte) 0, (byte) 9, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][111] = new TextureOffset((byte) 0, (byte) 18, (byte) 18, (byte) 0, (byte) 9, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][112] = new TextureOffset((byte) 0, (byte) 18, (byte) 12, (byte) 0, (byte) 19, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][113] = new TextureOffset((byte) 0, (byte) 18, (byte) 18, (byte) 0, (byte) 19, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][114] = new TextureOffset((byte) 9, (byte) 0, (byte) 0, (byte) 6, (byte) 0, (byte) 3);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][115] = new TextureOffset((byte) 9, (byte) 0, (byte) 0, (byte) 17, (byte) 0, (byte) 3);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][116] = new TextureOffset((byte) 9, (byte) 0, (byte) 0, (byte) 6, (byte) 0, (byte) 16);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][117] = new TextureOffset((byte) 9, (byte) 0, (byte) 0, (byte) 17, (byte) 0, (byte) 16);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][118] = new TextureOffset((byte) 19, (byte) 0, (byte) 0, (byte) 6, (byte) 0, (byte) 3);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][119] = new TextureOffset((byte) 19, (byte) 0, (byte) 0, (byte) 17, (byte) 0, (byte) 3);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][120] = new TextureOffset((byte) 19, (byte) 0, (byte) 0, (byte) 6, (byte) 0, (byte) 16);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][121] = new TextureOffset((byte) 19, (byte) 0, (byte) 0, (byte) 17, (byte) 0, (byte) 16);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][122] = new TextureOffset((byte) 3, (byte) 0, (byte) 0, (byte) 3, (byte) 6, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][123] = new TextureOffset((byte) 3, (byte) 0, (byte) 0, (byte) 3, (byte) 17, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][124] = new TextureOffset((byte) 3, (byte) 0, (byte) 0, (byte) 16, (byte) 6, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][125] = new TextureOffset((byte) 3, (byte) 0, (byte) 0, (byte) 16, (byte) 17, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][126] = new TextureOffset((byte) 16, (byte) 0, (byte) 0, (byte) 3, (byte) 6, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][127] = new TextureOffset((byte) 16, (byte) 0, (byte) 0, (byte) 3, (byte) 17, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][128] = new TextureOffset((byte) 16, (byte) 0, (byte) 0, (byte) 16, (byte) 6, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][129] = new TextureOffset((byte) 16, (byte) 0, (byte) 0, (byte) 16, (byte) 17, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][130] = new TextureOffset((byte) 12, (byte) 0, (byte) 3, (byte) 0, (byte) 0, (byte) 6);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][131] = new TextureOffset((byte) 12, (byte) 0, (byte) 16, (byte) 0, (byte) 0, (byte) 6);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][132] = new TextureOffset((byte) 12, (byte) 0, (byte) 3, (byte) 0, (byte) 0, (byte) 17);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][133] = new TextureOffset((byte) 12, (byte) 0, (byte) 16, (byte) 0, (byte) 0, (byte) 17);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][134] = new TextureOffset((byte) 18, (byte) 0, (byte) 3, (byte) 0, (byte) 0, (byte) 6);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][135] = new TextureOffset((byte) 18, (byte) 0, (byte) 16, (byte) 0, (byte) 0, (byte) 6);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][136] = new TextureOffset((byte) 18, (byte) 0, (byte) 3, (byte) 0, (byte) 0, (byte) 17);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][137] = new TextureOffset((byte) 18, (byte) 0, (byte) 16, (byte) 0, (byte) 0, (byte) 17);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][138] = new TextureOffset((byte) 6, (byte) 0, (byte) 6, (byte) 0, (byte) 3, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][139] = new TextureOffset((byte) 6, (byte) 0, (byte) 17, (byte) 0, (byte) 3, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][140] = new TextureOffset((byte) 6, (byte) 0, (byte) 6, (byte) 0, (byte) 16, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][141] = new TextureOffset((byte) 6, (byte) 0, (byte) 17, (byte) 0, (byte) 16, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][142] = new TextureOffset((byte) 17, (byte) 0, (byte) 6, (byte) 0, (byte) 3, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][143] = new TextureOffset((byte) 17, (byte) 0, (byte) 17, (byte) 0, (byte) 3, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][144] = new TextureOffset((byte) 17, (byte) 0, (byte) 6, (byte) 0, (byte) 16, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][145] = new TextureOffset((byte) 17, (byte) 0, (byte) 17, (byte) 0, (byte) 16, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][146] = new TextureOffset((byte) 15, (byte) 15, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][147] = new TextureOffset((byte) 33, (byte) 34, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][148] = new TextureOffset((byte) 34, (byte) 33, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][149] = new TextureOffset((byte) 35, (byte) 35, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][150] = new TextureOffset((byte) 36, (byte) 40, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][151] = new TextureOffset((byte) 37, (byte) 42, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][152] = new TextureOffset((byte) 38, (byte) 41, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][153] = new TextureOffset((byte) 39, (byte) 43, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][154] = new TextureOffset((byte) 40, (byte) 36, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][155] = new TextureOffset((byte) 41, (byte) 38, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][156] = new TextureOffset((byte) 42, (byte) 37, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][157] = new TextureOffset((byte) 43, (byte) 39, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][158] = new TextureOffset((byte) 44, (byte) 44, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][159] = new TextureOffset((byte) 45, (byte) 46, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][160] = new TextureOffset((byte) 46, (byte) 45, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][161] = new TextureOffset((byte) 47, (byte) 47, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][162] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 15, (byte) 15);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][163] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 40, (byte) 33);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][164] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 33, (byte) 40);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][165] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 41, (byte) 41);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][166] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 36, (byte) 34);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][167] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 44, (byte) 35);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][168] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 37, (byte) 42);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][169] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 45, (byte) 43);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][170] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 34, (byte) 36);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][171] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 42, (byte) 37);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][172] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 35, (byte) 44);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][173] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 43, (byte) 45);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][174] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 38);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][175] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 46, (byte) 39);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][176] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 39, (byte) 46);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][177] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 47, (byte) 47);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][178] = new TextureOffset((byte) 0, (byte) 0, (byte) 15, (byte) 15, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][179] = new TextureOffset((byte) 0, (byte) 0, (byte) 33, (byte) 40, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][180] = new TextureOffset((byte) 0, (byte) 0, (byte) 40, (byte) 33, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][181] = new TextureOffset((byte) 0, (byte) 0, (byte) 41, (byte) 41, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][182] = new TextureOffset((byte) 0, (byte) 0, (byte) 34, (byte) 36, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][183] = new TextureOffset((byte) 0, (byte) 0, (byte) 35, (byte) 44, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][184] = new TextureOffset((byte) 0, (byte) 0, (byte) 42, (byte) 37, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][185] = new TextureOffset((byte) 0, (byte) 0, (byte) 43, (byte) 45, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][186] = new TextureOffset((byte) 0, (byte) 0, (byte) 36, (byte) 34, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][187] = new TextureOffset((byte) 0, (byte) 0, (byte) 37, (byte) 42, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][188] = new TextureOffset((byte) 0, (byte) 0, (byte) 44, (byte) 35, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][189] = new TextureOffset((byte) 0, (byte) 0, (byte) 45, (byte) 43, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][190] = new TextureOffset((byte) 0, (byte) 0, (byte) 38, (byte) 38, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][191] = new TextureOffset((byte) 0, (byte) 0, (byte) 39, (byte) 46, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][192] = new TextureOffset((byte) 0, (byte) 0, (byte) 46, (byte) 39, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][193] = new TextureOffset((byte) 0, (byte) 0, (byte) 47, (byte) 47, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][194] = new TextureOffset((byte) 0, (byte) 14, (byte) 13, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][195] = new TextureOffset((byte) 0, (byte) 14, (byte) 27, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][196] = new TextureOffset((byte) 0, (byte) 14, (byte) 26, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][197] = new TextureOffset((byte) 0, (byte) 14, (byte) 28, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][198] = new TextureOffset((byte) 0, (byte) 30, (byte) 13, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][199] = new TextureOffset((byte) 0, (byte) 30, (byte) 27, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][200] = new TextureOffset((byte) 0, (byte) 30, (byte) 26, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][201] = new TextureOffset((byte) 0, (byte) 30, (byte) 28, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][202] = new TextureOffset((byte) 0, (byte) 29, (byte) 13, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][203] = new TextureOffset((byte) 0, (byte) 29, (byte) 27, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][204] = new TextureOffset((byte) 0, (byte) 29, (byte) 26, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][205] = new TextureOffset((byte) 0, (byte) 29, (byte) 28, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][206] = new TextureOffset((byte) 0, (byte) 31, (byte) 13, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][207] = new TextureOffset((byte) 0, (byte) 31, (byte) 27, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][208] = new TextureOffset((byte) 0, (byte) 31, (byte) 26, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][209] = new TextureOffset((byte) 0, (byte) 31, (byte) 28, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][210] = new TextureOffset((byte) 0, (byte) 11, (byte) 0, (byte) 13, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][211] = new TextureOffset((byte) 0, (byte) 11, (byte) 0, (byte) 26, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][212] = new TextureOffset((byte) 0, (byte) 11, (byte) 0, (byte) 27, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][213] = new TextureOffset((byte) 0, (byte) 11, (byte) 0, (byte) 28, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][214] = new TextureOffset((byte) 0, (byte) 24, (byte) 0, (byte) 13, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][215] = new TextureOffset((byte) 0, (byte) 24, (byte) 0, (byte) 26, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][216] = new TextureOffset((byte) 0, (byte) 24, (byte) 0, (byte) 27, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][217] = new TextureOffset((byte) 0, (byte) 24, (byte) 0, (byte) 28, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][218] = new TextureOffset((byte) 0, (byte) 23, (byte) 0, (byte) 13, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][219] = new TextureOffset((byte) 0, (byte) 23, (byte) 0, (byte) 26, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][220] = new TextureOffset((byte) 0, (byte) 23, (byte) 0, (byte) 27, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][221] = new TextureOffset((byte) 0, (byte) 23, (byte) 0, (byte) 28, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][222] = new TextureOffset((byte) 0, (byte) 25, (byte) 0, (byte) 13, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][223] = new TextureOffset((byte) 0, (byte) 25, (byte) 0, (byte) 26, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][224] = new TextureOffset((byte) 0, (byte) 25, (byte) 0, (byte) 27, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][225] = new TextureOffset((byte) 0, (byte) 25, (byte) 0, (byte) 28, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][226] = new TextureOffset((byte) 0, (byte) 13, (byte) 0, (byte) 0, (byte) 13, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][227] = new TextureOffset((byte) 0, (byte) 13, (byte) 0, (byte) 0, (byte) 26, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][228] = new TextureOffset((byte) 0, (byte) 13, (byte) 0, (byte) 0, (byte) 27, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][229] = new TextureOffset((byte) 0, (byte) 13, (byte) 0, (byte) 0, (byte) 28, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][230] = new TextureOffset((byte) 0, (byte) 27, (byte) 0, (byte) 0, (byte) 13, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][231] = new TextureOffset((byte) 0, (byte) 27, (byte) 0, (byte) 0, (byte) 26, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][232] = new TextureOffset((byte) 0, (byte) 27, (byte) 0, (byte) 0, (byte) 27, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][233] = new TextureOffset((byte) 0, (byte) 27, (byte) 0, (byte) 0, (byte) 28, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][234] = new TextureOffset((byte) 0, (byte) 26, (byte) 0, (byte) 0, (byte) 13, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][235] = new TextureOffset((byte) 0, (byte) 26, (byte) 0, (byte) 0, (byte) 26, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][236] = new TextureOffset((byte) 0, (byte) 26, (byte) 0, (byte) 0, (byte) 27, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][237] = new TextureOffset((byte) 0, (byte) 26, (byte) 0, (byte) 0, (byte) 28, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][238] = new TextureOffset((byte) 0, (byte) 28, (byte) 0, (byte) 0, (byte) 13, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][239] = new TextureOffset((byte) 0, (byte) 28, (byte) 0, (byte) 0, (byte) 26, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][240] = new TextureOffset((byte) 0, (byte) 28, (byte) 0, (byte) 0, (byte) 27, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][241] = new TextureOffset((byte) 0, (byte) 28, (byte) 0, (byte) 0, (byte) 28, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][242] = new TextureOffset((byte) 0, (byte) 7, (byte) 0, (byte) 0, (byte) 0, (byte) 13);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][243] = new TextureOffset((byte) 0, (byte) 7, (byte) 0, (byte) 0, (byte) 0, (byte) 27);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][244] = new TextureOffset((byte) 0, (byte) 7, (byte) 0, (byte) 0, (byte) 0, (byte) 26);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][245] = new TextureOffset((byte) 0, (byte) 7, (byte) 0, (byte) 0, (byte) 0, (byte) 28);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][246] = new TextureOffset((byte) 0, (byte) 20, (byte) 0, (byte) 0, (byte) 0, (byte) 13);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][247] = new TextureOffset((byte) 0, (byte) 20, (byte) 0, (byte) 0, (byte) 0, (byte) 27);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][248] = new TextureOffset((byte) 0, (byte) 20, (byte) 0, (byte) 0, (byte) 0, (byte) 26);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][249] = new TextureOffset((byte) 0, (byte) 20, (byte) 0, (byte) 0, (byte) 0, (byte) 28);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][250] = new TextureOffset((byte) 0, (byte) 21, (byte) 0, (byte) 0, (byte) 0, (byte) 13);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][251] = new TextureOffset((byte) 0, (byte) 21, (byte) 0, (byte) 0, (byte) 0, (byte) 27);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][252] = new TextureOffset((byte) 0, (byte) 21, (byte) 0, (byte) 0, (byte) 0, (byte) 26);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][253] = new TextureOffset((byte) 0, (byte) 21, (byte) 0, (byte) 0, (byte) 0, (byte) 28);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][254] = new TextureOffset((byte) 0, (byte) 22, (byte) 0, (byte) 0, (byte) 0, (byte) 13);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][255] = new TextureOffset((byte) 0, (byte) 22, (byte) 0, (byte) 0, (byte) 0, (byte) 27);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][256] = new TextureOffset((byte) 0, (byte) 22, (byte) 0, (byte) 0, (byte) 0, (byte) 26);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][257] = new TextureOffset((byte) 0, (byte) 22, (byte) 0, (byte) 0, (byte) 0, (byte) 28);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][258] = new TextureOffset((byte) 14, (byte) 0, (byte) 7, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][259] = new TextureOffset((byte) 14, (byte) 0, (byte) 20, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][260] = new TextureOffset((byte) 14, (byte) 0, (byte) 21, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][261] = new TextureOffset((byte) 14, (byte) 0, (byte) 22, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][262] = new TextureOffset((byte) 29, (byte) 0, (byte) 7, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][263] = new TextureOffset((byte) 29, (byte) 0, (byte) 20, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][264] = new TextureOffset((byte) 29, (byte) 0, (byte) 21, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][265] = new TextureOffset((byte) 29, (byte) 0, (byte) 22, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][266] = new TextureOffset((byte) 30, (byte) 0, (byte) 7, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][267] = new TextureOffset((byte) 30, (byte) 0, (byte) 20, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][268] = new TextureOffset((byte) 30, (byte) 0, (byte) 21, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][269] = new TextureOffset((byte) 30, (byte) 0, (byte) 22, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][270] = new TextureOffset((byte) 31, (byte) 0, (byte) 7, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][271] = new TextureOffset((byte) 31, (byte) 0, (byte) 20, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][272] = new TextureOffset((byte) 31, (byte) 0, (byte) 21, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][273] = new TextureOffset((byte) 31, (byte) 0, (byte) 22, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][274] = new TextureOffset((byte) 11, (byte) 0, (byte) 0, (byte) 7, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][275] = new TextureOffset((byte) 11, (byte) 0, (byte) 0, (byte) 21, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][276] = new TextureOffset((byte) 11, (byte) 0, (byte) 0, (byte) 20, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][277] = new TextureOffset((byte) 11, (byte) 0, (byte) 0, (byte) 22, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][278] = new TextureOffset((byte) 23, (byte) 0, (byte) 0, (byte) 7, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][279] = new TextureOffset((byte) 23, (byte) 0, (byte) 0, (byte) 21, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][280] = new TextureOffset((byte) 23, (byte) 0, (byte) 0, (byte) 20, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][281] = new TextureOffset((byte) 23, (byte) 0, (byte) 0, (byte) 22, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][282] = new TextureOffset((byte) 24, (byte) 0, (byte) 0, (byte) 7, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][283] = new TextureOffset((byte) 24, (byte) 0, (byte) 0, (byte) 21, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][284] = new TextureOffset((byte) 24, (byte) 0, (byte) 0, (byte) 20, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][285] = new TextureOffset((byte) 24, (byte) 0, (byte) 0, (byte) 22, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][286] = new TextureOffset((byte) 25, (byte) 0, (byte) 0, (byte) 7, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][287] = new TextureOffset((byte) 25, (byte) 0, (byte) 0, (byte) 21, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][288] = new TextureOffset((byte) 25, (byte) 0, (byte) 0, (byte) 20, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][289] = new TextureOffset((byte) 25, (byte) 0, (byte) 0, (byte) 22, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][290] = new TextureOffset((byte) 7, (byte) 0, (byte) 0, (byte) 0, (byte) 7, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][291] = new TextureOffset((byte) 7, (byte) 0, (byte) 0, (byte) 0, (byte) 21, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][292] = new TextureOffset((byte) 7, (byte) 0, (byte) 0, (byte) 0, (byte) 20, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][293] = new TextureOffset((byte) 7, (byte) 0, (byte) 0, (byte) 0, (byte) 22, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][294] = new TextureOffset((byte) 20, (byte) 0, (byte) 0, (byte) 0, (byte) 7, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][295] = new TextureOffset((byte) 20, (byte) 0, (byte) 0, (byte) 0, (byte) 21, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][296] = new TextureOffset((byte) 20, (byte) 0, (byte) 0, (byte) 0, (byte) 20, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][297] = new TextureOffset((byte) 20, (byte) 0, (byte) 0, (byte) 0, (byte) 22, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][298] = new TextureOffset((byte) 21, (byte) 0, (byte) 0, (byte) 0, (byte) 7, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][299] = new TextureOffset((byte) 21, (byte) 0, (byte) 0, (byte) 0, (byte) 21, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][300] = new TextureOffset((byte) 21, (byte) 0, (byte) 0, (byte) 0, (byte) 20, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][301] = new TextureOffset((byte) 21, (byte) 0, (byte) 0, (byte) 0, (byte) 22, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][302] = new TextureOffset((byte) 22, (byte) 0, (byte) 0, (byte) 0, (byte) 7, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][303] = new TextureOffset((byte) 22, (byte) 0, (byte) 0, (byte) 0, (byte) 21, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][304] = new TextureOffset((byte) 22, (byte) 0, (byte) 0, (byte) 0, (byte) 20, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][305] = new TextureOffset((byte) 22, (byte) 0, (byte) 0, (byte) 0, (byte) 22, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][306] = new TextureOffset((byte) 13, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 7);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][307] = new TextureOffset((byte) 13, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 20);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][308] = new TextureOffset((byte) 13, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 21);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][309] = new TextureOffset((byte) 13, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 22);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][310] = new TextureOffset((byte) 27, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 7);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][311] = new TextureOffset((byte) 27, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 20);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][312] = new TextureOffset((byte) 27, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 21);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][313] = new TextureOffset((byte) 27, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 22);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][314] = new TextureOffset((byte) 26, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 7);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][315] = new TextureOffset((byte) 26, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 20);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][316] = new TextureOffset((byte) 26, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 21);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][317] = new TextureOffset((byte) 26, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 22);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][318] = new TextureOffset((byte) 28, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 7);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][319] = new TextureOffset((byte) 28, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 20);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][320] = new TextureOffset((byte) 28, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 21);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][321] = new TextureOffset((byte) 28, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 22);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][322] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 14, (byte) 0, (byte) 11);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][323] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 30, (byte) 0, (byte) 11);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][324] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 14, (byte) 0, (byte) 23);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][325] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 30, (byte) 0, (byte) 23);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][326] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 29, (byte) 0, (byte) 11);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][327] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 31, (byte) 0, (byte) 11);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][328] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 29, (byte) 0, (byte) 23);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][329] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 31, (byte) 0, (byte) 23);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][330] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 14, (byte) 0, (byte) 24);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][331] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 30, (byte) 0, (byte) 24);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][332] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 14, (byte) 0, (byte) 25);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][333] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 30, (byte) 0, (byte) 25);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][334] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 29, (byte) 0, (byte) 24);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][335] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 31, (byte) 0, (byte) 24);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][336] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 29, (byte) 0, (byte) 25);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][337] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 31, (byte) 0, (byte) 25);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][338] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 11, (byte) 14, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][339] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 11, (byte) 30, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][340] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 23, (byte) 14, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][341] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 23, (byte) 30, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][342] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 11, (byte) 29, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][343] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 11, (byte) 31, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][344] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 23, (byte) 29, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][345] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 23, (byte) 31, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][346] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 24, (byte) 14, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][347] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 24, (byte) 30, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][348] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 25, (byte) 14, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][349] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 25, (byte) 30, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][350] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 24, (byte) 29, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][351] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 24, (byte) 31, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][352] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 25, (byte) 29, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][353] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 25, (byte) 31, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][354] = new TextureOffset((byte) 0, (byte) 0, (byte) 11, (byte) 0, (byte) 0, (byte) 14);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][355] = new TextureOffset((byte) 0, (byte) 0, (byte) 23, (byte) 0, (byte) 0, (byte) 14);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][356] = new TextureOffset((byte) 0, (byte) 0, (byte) 11, (byte) 0, (byte) 0, (byte) 30);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][357] = new TextureOffset((byte) 0, (byte) 0, (byte) 23, (byte) 0, (byte) 0, (byte) 30);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][358] = new TextureOffset((byte) 0, (byte) 0, (byte) 24, (byte) 0, (byte) 0, (byte) 14);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][359] = new TextureOffset((byte) 0, (byte) 0, (byte) 25, (byte) 0, (byte) 0, (byte) 14);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][360] = new TextureOffset((byte) 0, (byte) 0, (byte) 24, (byte) 0, (byte) 0, (byte) 30);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][361] = new TextureOffset((byte) 0, (byte) 0, (byte) 25, (byte) 0, (byte) 0, (byte) 30);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][362] = new TextureOffset((byte) 0, (byte) 0, (byte) 11, (byte) 0, (byte) 0, (byte) 29);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][363] = new TextureOffset((byte) 0, (byte) 0, (byte) 23, (byte) 0, (byte) 0, (byte) 29);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][364] = new TextureOffset((byte) 0, (byte) 0, (byte) 11, (byte) 0, (byte) 0, (byte) 31);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][365] = new TextureOffset((byte) 0, (byte) 0, (byte) 23, (byte) 0, (byte) 0, (byte) 31);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][366] = new TextureOffset((byte) 0, (byte) 0, (byte) 24, (byte) 0, (byte) 0, (byte) 29);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][367] = new TextureOffset((byte) 0, (byte) 0, (byte) 25, (byte) 0, (byte) 0, (byte) 29);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][368] = new TextureOffset((byte) 0, (byte) 0, (byte) 24, (byte) 0, (byte) 0, (byte) 31);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][369] = new TextureOffset((byte) 0, (byte) 0, (byte) 25, (byte) 0, (byte) 0, (byte) 31);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][370] = new TextureOffset((byte) 0, (byte) 0, (byte) 14, (byte) 0, (byte) 11, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][371] = new TextureOffset((byte) 0, (byte) 0, (byte) 30, (byte) 0, (byte) 11, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][372] = new TextureOffset((byte) 0, (byte) 0, (byte) 14, (byte) 0, (byte) 23, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][373] = new TextureOffset((byte) 0, (byte) 0, (byte) 30, (byte) 0, (byte) 23, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][374] = new TextureOffset((byte) 0, (byte) 0, (byte) 29, (byte) 0, (byte) 11, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][375] = new TextureOffset((byte) 0, (byte) 0, (byte) 31, (byte) 0, (byte) 11, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][376] = new TextureOffset((byte) 0, (byte) 0, (byte) 29, (byte) 0, (byte) 23, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][377] = new TextureOffset((byte) 0, (byte) 0, (byte) 31, (byte) 0, (byte) 23, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][378] = new TextureOffset((byte) 0, (byte) 0, (byte) 14, (byte) 0, (byte) 24, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][379] = new TextureOffset((byte) 0, (byte) 0, (byte) 30, (byte) 0, (byte) 24, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][380] = new TextureOffset((byte) 0, (byte) 0, (byte) 14, (byte) 0, (byte) 25, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][381] = new TextureOffset((byte) 0, (byte) 0, (byte) 30, (byte) 0, (byte) 25, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][382] = new TextureOffset((byte) 0, (byte) 0, (byte) 29, (byte) 0, (byte) 24, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][383] = new TextureOffset((byte) 0, (byte) 0, (byte) 31, (byte) 0, (byte) 24, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][384] = new TextureOffset((byte) 0, (byte) 0, (byte) 29, (byte) 0, (byte) 25, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_90.index][385] = new TextureOffset((byte) 0, (byte) 0, (byte) 31, (byte) 0, (byte) 25, (byte) 0);

		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][0] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][1] = new TextureOffset((byte) 0, (byte) 0, (byte) 4, (byte) 4, (byte) 4, (byte) 4);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][2] = new TextureOffset((byte) 0, (byte) 0, (byte) 1, (byte) 1, (byte) 1, (byte) 1);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][3] = new TextureOffset((byte) 8, (byte) 8, (byte) 0, (byte) 0, (byte) 2, (byte) 8);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][4] = new TextureOffset((byte) 2, (byte) 2, (byte) 0, (byte) 0, (byte) 8, (byte) 2);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][5] = new TextureOffset((byte) 4, (byte) 1, (byte) 8, (byte) 2, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][6] = new TextureOffset((byte) 1, (byte) 4, (byte) 2, (byte) 8, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][7] = new TextureOffset((byte) 0, (byte) 0, (byte) 5, (byte) 5, (byte) 5, (byte) 5);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][8] = new TextureOffset((byte) 10, (byte) 10, (byte) 0, (byte) 0, (byte) 10, (byte) 10);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][9] = new TextureOffset((byte) 5, (byte) 5, (byte) 10, (byte) 10, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][10] = new TextureOffset((byte) 0, (byte) 8, (byte) 0, (byte) 4, (byte) 6, (byte) 12);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][11] = new TextureOffset((byte) 0, (byte) 8, (byte) 0, (byte) 4, (byte) 17, (byte) 18);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][12] = new TextureOffset((byte) 0, (byte) 2, (byte) 4, (byte) 0, (byte) 12, (byte) 6);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][13] = new TextureOffset((byte) 0, (byte) 2, (byte) 4, (byte) 0, (byte) 18, (byte) 17);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][14] = new TextureOffset((byte) 0, (byte) 1, (byte) 12, (byte) 6, (byte) 0, (byte) 4);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][15] = new TextureOffset((byte) 0, (byte) 1, (byte) 18, (byte) 17, (byte) 0, (byte) 4);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][16] = new TextureOffset((byte) 0, (byte) 4, (byte) 6, (byte) 12, (byte) 4, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][17] = new TextureOffset((byte) 0, (byte) 4, (byte) 17, (byte) 18, (byte) 4, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][18] = new TextureOffset((byte) 8, (byte) 0, (byte) 0, (byte) 1, (byte) 3, (byte) 9);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][19] = new TextureOffset((byte) 8, (byte) 0, (byte) 0, (byte) 1, (byte) 16, (byte) 19);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][20] = new TextureOffset((byte) 2, (byte) 0, (byte) 1, (byte) 0, (byte) 9, (byte) 3);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][21] = new TextureOffset((byte) 2, (byte) 0, (byte) 1, (byte) 0, (byte) 19, (byte) 16);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][22] = new TextureOffset((byte) 4, (byte) 0, (byte) 9, (byte) 3, (byte) 0, (byte) 1);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][23] = new TextureOffset((byte) 4, (byte) 0, (byte) 19, (byte) 16, (byte) 0, (byte) 1);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][24] = new TextureOffset((byte) 1, (byte) 0, (byte) 3, (byte) 9, (byte) 1, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][25] = new TextureOffset((byte) 1, (byte) 0, (byte) 16, (byte) 19, (byte) 1, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][26] = new TextureOffset((byte) 12, (byte) 9, (byte) 0, (byte) 2, (byte) 0, (byte) 8);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][27] = new TextureOffset((byte) 18, (byte) 19, (byte) 0, (byte) 2, (byte) 0, (byte) 8);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][28] = new TextureOffset((byte) 9, (byte) 12, (byte) 0, (byte) 8, (byte) 2, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][29] = new TextureOffset((byte) 19, (byte) 18, (byte) 0, (byte) 8, (byte) 2, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][30] = new TextureOffset((byte) 6, (byte) 3, (byte) 8, (byte) 0, (byte) 0, (byte) 2);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][31] = new TextureOffset((byte) 17, (byte) 16, (byte) 8, (byte) 0, (byte) 0, (byte) 2);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][32] = new TextureOffset((byte) 3, (byte) 6, (byte) 2, (byte) 0, (byte) 8, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][33] = new TextureOffset((byte) 16, (byte) 17, (byte) 2, (byte) 0, (byte) 8, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][34] = new TextureOffset((byte) 0, (byte) 10, (byte) 0, (byte) 0, (byte) 14, (byte) 14);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][35] = new TextureOffset((byte) 0, (byte) 10, (byte) 0, (byte) 0, (byte) 29, (byte) 30);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][36] = new TextureOffset((byte) 0, (byte) 10, (byte) 0, (byte) 0, (byte) 30, (byte) 29);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][37] = new TextureOffset((byte) 0, (byte) 10, (byte) 0, (byte) 0, (byte) 31, (byte) 31);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][38] = new TextureOffset((byte) 0, (byte) 5, (byte) 14, (byte) 14, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][39] = new TextureOffset((byte) 0, (byte) 5, (byte) 30, (byte) 29, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][40] = new TextureOffset((byte) 0, (byte) 5, (byte) 29, (byte) 30, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][41] = new TextureOffset((byte) 0, (byte) 5, (byte) 31, (byte) 31, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][42] = new TextureOffset((byte) 10, (byte) 0, (byte) 0, (byte) 0, (byte) 11, (byte) 11);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][43] = new TextureOffset((byte) 10, (byte) 0, (byte) 0, (byte) 0, (byte) 24, (byte) 23);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][44] = new TextureOffset((byte) 10, (byte) 0, (byte) 0, (byte) 0, (byte) 23, (byte) 24);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][45] = new TextureOffset((byte) 10, (byte) 0, (byte) 0, (byte) 0, (byte) 25, (byte) 25);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][46] = new TextureOffset((byte) 5, (byte) 0, (byte) 11, (byte) 11, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][47] = new TextureOffset((byte) 5, (byte) 0, (byte) 23, (byte) 24, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][48] = new TextureOffset((byte) 5, (byte) 0, (byte) 24, (byte) 23, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][49] = new TextureOffset((byte) 5, (byte) 0, (byte) 25, (byte) 25, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][50] = new TextureOffset((byte) 14, (byte) 11, (byte) 0, (byte) 0, (byte) 0, (byte) 10);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][51] = new TextureOffset((byte) 30, (byte) 23, (byte) 0, (byte) 0, (byte) 0, (byte) 10);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][52] = new TextureOffset((byte) 29, (byte) 24, (byte) 0, (byte) 0, (byte) 0, (byte) 10);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][53] = new TextureOffset((byte) 31, (byte) 25, (byte) 0, (byte) 0, (byte) 0, (byte) 10);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][54] = new TextureOffset((byte) 0, (byte) 0, (byte) 13, (byte) 7, (byte) 0, (byte) 5);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][55] = new TextureOffset((byte) 0, (byte) 0, (byte) 26, (byte) 21, (byte) 0, (byte) 5);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][56] = new TextureOffset((byte) 0, (byte) 0, (byte) 27, (byte) 20, (byte) 0, (byte) 5);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][57] = new TextureOffset((byte) 0, (byte) 0, (byte) 28, (byte) 22, (byte) 0, (byte) 5);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][58] = new TextureOffset((byte) 11, (byte) 14, (byte) 0, (byte) 0, (byte) 10, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][59] = new TextureOffset((byte) 23, (byte) 30, (byte) 0, (byte) 0, (byte) 10, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][60] = new TextureOffset((byte) 24, (byte) 29, (byte) 0, (byte) 0, (byte) 10, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][61] = new TextureOffset((byte) 25, (byte) 31, (byte) 0, (byte) 0, (byte) 10, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][62] = new TextureOffset((byte) 0, (byte) 0, (byte) 7, (byte) 13, (byte) 5, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][63] = new TextureOffset((byte) 0, (byte) 0, (byte) 21, (byte) 26, (byte) 5, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][64] = new TextureOffset((byte) 0, (byte) 0, (byte) 20, (byte) 27, (byte) 5, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][65] = new TextureOffset((byte) 0, (byte) 0, (byte) 22, (byte) 28, (byte) 5, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][66] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 5, (byte) 7, (byte) 13);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][67] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 5, (byte) 21, (byte) 26);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][68] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 5, (byte) 20, (byte) 27);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][69] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 5, (byte) 22, (byte) 28);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][70] = new TextureOffset((byte) 13, (byte) 13, (byte) 0, (byte) 10, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][71] = new TextureOffset((byte) 26, (byte) 27, (byte) 0, (byte) 10, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][72] = new TextureOffset((byte) 27, (byte) 26, (byte) 0, (byte) 10, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][73] = new TextureOffset((byte) 28, (byte) 28, (byte) 0, (byte) 10, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][74] = new TextureOffset((byte) 0, (byte) 0, (byte) 5, (byte) 0, (byte) 13, (byte) 7);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][75] = new TextureOffset((byte) 0, (byte) 0, (byte) 5, (byte) 0, (byte) 26, (byte) 21);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][76] = new TextureOffset((byte) 0, (byte) 0, (byte) 5, (byte) 0, (byte) 27, (byte) 20);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][77] = new TextureOffset((byte) 0, (byte) 0, (byte) 5, (byte) 0, (byte) 28, (byte) 22);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][78] = new TextureOffset((byte) 7, (byte) 7, (byte) 10, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][79] = new TextureOffset((byte) 20, (byte) 21, (byte) 10, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][80] = new TextureOffset((byte) 21, (byte) 20, (byte) 10, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][81] = new TextureOffset((byte) 22, (byte) 22, (byte) 10, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][82] = new TextureOffset((byte) 0, (byte) 9, (byte) 0, (byte) 6, (byte) 0, (byte) 12);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][83] = new TextureOffset((byte) 0, (byte) 9, (byte) 0, (byte) 17, (byte) 0, (byte) 12);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][84] = new TextureOffset((byte) 0, (byte) 9, (byte) 0, (byte) 6, (byte) 0, (byte) 18);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][85] = new TextureOffset((byte) 0, (byte) 9, (byte) 0, (byte) 17, (byte) 0, (byte) 18);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][86] = new TextureOffset((byte) 0, (byte) 19, (byte) 0, (byte) 6, (byte) 0, (byte) 12);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][87] = new TextureOffset((byte) 0, (byte) 19, (byte) 0, (byte) 17, (byte) 0, (byte) 12);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][88] = new TextureOffset((byte) 0, (byte) 19, (byte) 0, (byte) 6, (byte) 0, (byte) 18);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][89] = new TextureOffset((byte) 0, (byte) 19, (byte) 0, (byte) 17, (byte) 0, (byte) 18);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][90] = new TextureOffset((byte) 0, (byte) 12, (byte) 0, (byte) 12, (byte) 6, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][91] = new TextureOffset((byte) 0, (byte) 12, (byte) 0, (byte) 12, (byte) 17, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][92] = new TextureOffset((byte) 0, (byte) 12, (byte) 0, (byte) 18, (byte) 6, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][93] = new TextureOffset((byte) 0, (byte) 12, (byte) 0, (byte) 18, (byte) 17, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][94] = new TextureOffset((byte) 0, (byte) 18, (byte) 0, (byte) 12, (byte) 6, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][95] = new TextureOffset((byte) 0, (byte) 18, (byte) 0, (byte) 12, (byte) 17, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][96] = new TextureOffset((byte) 0, (byte) 18, (byte) 0, (byte) 18, (byte) 6, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][97] = new TextureOffset((byte) 0, (byte) 18, (byte) 0, (byte) 18, (byte) 17, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][98] = new TextureOffset((byte) 0, (byte) 3, (byte) 12, (byte) 0, (byte) 0, (byte) 6);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][99] = new TextureOffset((byte) 0, (byte) 3, (byte) 18, (byte) 0, (byte) 0, (byte) 6);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][100] = new TextureOffset((byte) 0, (byte) 3, (byte) 12, (byte) 0, (byte) 0, (byte) 17);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][101] = new TextureOffset((byte) 0, (byte) 3, (byte) 18, (byte) 0, (byte) 0, (byte) 17);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][102] = new TextureOffset((byte) 0, (byte) 16, (byte) 12, (byte) 0, (byte) 0, (byte) 6);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][103] = new TextureOffset((byte) 0, (byte) 16, (byte) 18, (byte) 0, (byte) 0, (byte) 6);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][104] = new TextureOffset((byte) 0, (byte) 16, (byte) 12, (byte) 0, (byte) 0, (byte) 17);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][105] = new TextureOffset((byte) 0, (byte) 16, (byte) 18, (byte) 0, (byte) 0, (byte) 17);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][106] = new TextureOffset((byte) 0, (byte) 6, (byte) 6, (byte) 0, (byte) 12, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][107] = new TextureOffset((byte) 0, (byte) 6, (byte) 17, (byte) 0, (byte) 12, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][108] = new TextureOffset((byte) 0, (byte) 6, (byte) 6, (byte) 0, (byte) 18, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][109] = new TextureOffset((byte) 0, (byte) 6, (byte) 17, (byte) 0, (byte) 18, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][110] = new TextureOffset((byte) 0, (byte) 17, (byte) 6, (byte) 0, (byte) 12, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][111] = new TextureOffset((byte) 0, (byte) 17, (byte) 17, (byte) 0, (byte) 12, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][112] = new TextureOffset((byte) 0, (byte) 17, (byte) 6, (byte) 0, (byte) 18, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][113] = new TextureOffset((byte) 0, (byte) 17, (byte) 17, (byte) 0, (byte) 18, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][114] = new TextureOffset((byte) 12, (byte) 0, (byte) 0, (byte) 3, (byte) 0, (byte) 9);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][115] = new TextureOffset((byte) 12, (byte) 0, (byte) 0, (byte) 16, (byte) 0, (byte) 9);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][116] = new TextureOffset((byte) 12, (byte) 0, (byte) 0, (byte) 3, (byte) 0, (byte) 19);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][117] = new TextureOffset((byte) 12, (byte) 0, (byte) 0, (byte) 16, (byte) 0, (byte) 19);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][118] = new TextureOffset((byte) 18, (byte) 0, (byte) 0, (byte) 3, (byte) 0, (byte) 9);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][119] = new TextureOffset((byte) 18, (byte) 0, (byte) 0, (byte) 16, (byte) 0, (byte) 9);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][120] = new TextureOffset((byte) 18, (byte) 0, (byte) 0, (byte) 3, (byte) 0, (byte) 19);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][121] = new TextureOffset((byte) 18, (byte) 0, (byte) 0, (byte) 16, (byte) 0, (byte) 19);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][122] = new TextureOffset((byte) 9, (byte) 0, (byte) 0, (byte) 9, (byte) 3, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][123] = new TextureOffset((byte) 9, (byte) 0, (byte) 0, (byte) 9, (byte) 16, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][124] = new TextureOffset((byte) 9, (byte) 0, (byte) 0, (byte) 19, (byte) 3, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][125] = new TextureOffset((byte) 9, (byte) 0, (byte) 0, (byte) 19, (byte) 16, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][126] = new TextureOffset((byte) 19, (byte) 0, (byte) 0, (byte) 9, (byte) 3, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][127] = new TextureOffset((byte) 19, (byte) 0, (byte) 0, (byte) 9, (byte) 16, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][128] = new TextureOffset((byte) 19, (byte) 0, (byte) 0, (byte) 19, (byte) 3, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][129] = new TextureOffset((byte) 19, (byte) 0, (byte) 0, (byte) 19, (byte) 16, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][130] = new TextureOffset((byte) 6, (byte) 0, (byte) 9, (byte) 0, (byte) 0, (byte) 3);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][131] = new TextureOffset((byte) 6, (byte) 0, (byte) 19, (byte) 0, (byte) 0, (byte) 3);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][132] = new TextureOffset((byte) 6, (byte) 0, (byte) 9, (byte) 0, (byte) 0, (byte) 16);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][133] = new TextureOffset((byte) 6, (byte) 0, (byte) 19, (byte) 0, (byte) 0, (byte) 16);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][134] = new TextureOffset((byte) 17, (byte) 0, (byte) 9, (byte) 0, (byte) 0, (byte) 3);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][135] = new TextureOffset((byte) 17, (byte) 0, (byte) 19, (byte) 0, (byte) 0, (byte) 3);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][136] = new TextureOffset((byte) 17, (byte) 0, (byte) 9, (byte) 0, (byte) 0, (byte) 16);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][137] = new TextureOffset((byte) 17, (byte) 0, (byte) 19, (byte) 0, (byte) 0, (byte) 16);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][138] = new TextureOffset((byte) 3, (byte) 0, (byte) 3, (byte) 0, (byte) 9, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][139] = new TextureOffset((byte) 3, (byte) 0, (byte) 16, (byte) 0, (byte) 9, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][140] = new TextureOffset((byte) 3, (byte) 0, (byte) 3, (byte) 0, (byte) 19, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][141] = new TextureOffset((byte) 3, (byte) 0, (byte) 16, (byte) 0, (byte) 19, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][142] = new TextureOffset((byte) 16, (byte) 0, (byte) 3, (byte) 0, (byte) 9, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][143] = new TextureOffset((byte) 16, (byte) 0, (byte) 16, (byte) 0, (byte) 9, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][144] = new TextureOffset((byte) 16, (byte) 0, (byte) 3, (byte) 0, (byte) 19, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][145] = new TextureOffset((byte) 16, (byte) 0, (byte) 16, (byte) 0, (byte) 19, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][146] = new TextureOffset((byte) 15, (byte) 15, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][147] = new TextureOffset((byte) 40, (byte) 33, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][148] = new TextureOffset((byte) 33, (byte) 40, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][149] = new TextureOffset((byte) 41, (byte) 41, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][150] = new TextureOffset((byte) 34, (byte) 36, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][151] = new TextureOffset((byte) 42, (byte) 37, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][152] = new TextureOffset((byte) 35, (byte) 44, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][153] = new TextureOffset((byte) 43, (byte) 45, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][154] = new TextureOffset((byte) 36, (byte) 34, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][155] = new TextureOffset((byte) 44, (byte) 35, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][156] = new TextureOffset((byte) 37, (byte) 42, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][157] = new TextureOffset((byte) 45, (byte) 43, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][158] = new TextureOffset((byte) 38, (byte) 38, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][159] = new TextureOffset((byte) 46, (byte) 39, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][160] = new TextureOffset((byte) 39, (byte) 46, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][161] = new TextureOffset((byte) 47, (byte) 47, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][162] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 15, (byte) 15);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][163] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 36, (byte) 40);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][164] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 40, (byte) 36);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][165] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 44, (byte) 44);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][166] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 34, (byte) 33);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][167] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 41);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][168] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 42, (byte) 37);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][169] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 46, (byte) 45);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][170] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 33, (byte) 34);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][171] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 37, (byte) 42);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][172] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 41, (byte) 38);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][173] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 45, (byte) 46);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][174] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 35, (byte) 35);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][175] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 39, (byte) 43);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][176] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 43, (byte) 39);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][177] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 47, (byte) 47);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][178] = new TextureOffset((byte) 0, (byte) 0, (byte) 15, (byte) 15, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][179] = new TextureOffset((byte) 0, (byte) 0, (byte) 40, (byte) 36, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][180] = new TextureOffset((byte) 0, (byte) 0, (byte) 36, (byte) 40, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][181] = new TextureOffset((byte) 0, (byte) 0, (byte) 44, (byte) 44, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][182] = new TextureOffset((byte) 0, (byte) 0, (byte) 33, (byte) 34, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][183] = new TextureOffset((byte) 0, (byte) 0, (byte) 41, (byte) 38, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][184] = new TextureOffset((byte) 0, (byte) 0, (byte) 37, (byte) 42, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][185] = new TextureOffset((byte) 0, (byte) 0, (byte) 45, (byte) 46, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][186] = new TextureOffset((byte) 0, (byte) 0, (byte) 34, (byte) 33, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][187] = new TextureOffset((byte) 0, (byte) 0, (byte) 42, (byte) 37, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][188] = new TextureOffset((byte) 0, (byte) 0, (byte) 38, (byte) 41, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][189] = new TextureOffset((byte) 0, (byte) 0, (byte) 46, (byte) 45, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][190] = new TextureOffset((byte) 0, (byte) 0, (byte) 35, (byte) 35, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][191] = new TextureOffset((byte) 0, (byte) 0, (byte) 43, (byte) 39, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][192] = new TextureOffset((byte) 0, (byte) 0, (byte) 39, (byte) 43, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][193] = new TextureOffset((byte) 0, (byte) 0, (byte) 47, (byte) 47, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][194] = new TextureOffset((byte) 0, (byte) 7, (byte) 14, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][195] = new TextureOffset((byte) 0, (byte) 7, (byte) 30, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][196] = new TextureOffset((byte) 0, (byte) 7, (byte) 29, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][197] = new TextureOffset((byte) 0, (byte) 7, (byte) 31, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][198] = new TextureOffset((byte) 0, (byte) 21, (byte) 14, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][199] = new TextureOffset((byte) 0, (byte) 21, (byte) 30, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][200] = new TextureOffset((byte) 0, (byte) 21, (byte) 29, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][201] = new TextureOffset((byte) 0, (byte) 21, (byte) 31, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][202] = new TextureOffset((byte) 0, (byte) 20, (byte) 14, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][203] = new TextureOffset((byte) 0, (byte) 20, (byte) 30, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][204] = new TextureOffset((byte) 0, (byte) 20, (byte) 29, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][205] = new TextureOffset((byte) 0, (byte) 20, (byte) 31, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][206] = new TextureOffset((byte) 0, (byte) 22, (byte) 14, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][207] = new TextureOffset((byte) 0, (byte) 22, (byte) 30, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][208] = new TextureOffset((byte) 0, (byte) 22, (byte) 29, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][209] = new TextureOffset((byte) 0, (byte) 22, (byte) 31, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][210] = new TextureOffset((byte) 0, (byte) 13, (byte) 0, (byte) 14, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][211] = new TextureOffset((byte) 0, (byte) 13, (byte) 0, (byte) 29, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][212] = new TextureOffset((byte) 0, (byte) 13, (byte) 0, (byte) 30, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][213] = new TextureOffset((byte) 0, (byte) 13, (byte) 0, (byte) 31, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][214] = new TextureOffset((byte) 0, (byte) 27, (byte) 0, (byte) 14, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][215] = new TextureOffset((byte) 0, (byte) 27, (byte) 0, (byte) 29, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][216] = new TextureOffset((byte) 0, (byte) 27, (byte) 0, (byte) 30, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][217] = new TextureOffset((byte) 0, (byte) 27, (byte) 0, (byte) 31, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][218] = new TextureOffset((byte) 0, (byte) 26, (byte) 0, (byte) 14, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][219] = new TextureOffset((byte) 0, (byte) 26, (byte) 0, (byte) 29, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][220] = new TextureOffset((byte) 0, (byte) 26, (byte) 0, (byte) 30, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][221] = new TextureOffset((byte) 0, (byte) 26, (byte) 0, (byte) 31, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][222] = new TextureOffset((byte) 0, (byte) 28, (byte) 0, (byte) 14, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][223] = new TextureOffset((byte) 0, (byte) 28, (byte) 0, (byte) 29, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][224] = new TextureOffset((byte) 0, (byte) 28, (byte) 0, (byte) 30, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][225] = new TextureOffset((byte) 0, (byte) 28, (byte) 0, (byte) 31, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][226] = new TextureOffset((byte) 0, (byte) 14, (byte) 0, (byte) 0, (byte) 14, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][227] = new TextureOffset((byte) 0, (byte) 14, (byte) 0, (byte) 0, (byte) 29, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][228] = new TextureOffset((byte) 0, (byte) 14, (byte) 0, (byte) 0, (byte) 30, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][229] = new TextureOffset((byte) 0, (byte) 14, (byte) 0, (byte) 0, (byte) 31, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][230] = new TextureOffset((byte) 0, (byte) 30, (byte) 0, (byte) 0, (byte) 14, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][231] = new TextureOffset((byte) 0, (byte) 30, (byte) 0, (byte) 0, (byte) 29, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][232] = new TextureOffset((byte) 0, (byte) 30, (byte) 0, (byte) 0, (byte) 30, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][233] = new TextureOffset((byte) 0, (byte) 30, (byte) 0, (byte) 0, (byte) 31, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][234] = new TextureOffset((byte) 0, (byte) 29, (byte) 0, (byte) 0, (byte) 14, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][235] = new TextureOffset((byte) 0, (byte) 29, (byte) 0, (byte) 0, (byte) 29, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][236] = new TextureOffset((byte) 0, (byte) 29, (byte) 0, (byte) 0, (byte) 30, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][237] = new TextureOffset((byte) 0, (byte) 29, (byte) 0, (byte) 0, (byte) 31, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][238] = new TextureOffset((byte) 0, (byte) 31, (byte) 0, (byte) 0, (byte) 14, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][239] = new TextureOffset((byte) 0, (byte) 31, (byte) 0, (byte) 0, (byte) 29, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][240] = new TextureOffset((byte) 0, (byte) 31, (byte) 0, (byte) 0, (byte) 30, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][241] = new TextureOffset((byte) 0, (byte) 31, (byte) 0, (byte) 0, (byte) 31, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][242] = new TextureOffset((byte) 0, (byte) 11, (byte) 0, (byte) 0, (byte) 0, (byte) 14);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][243] = new TextureOffset((byte) 0, (byte) 11, (byte) 0, (byte) 0, (byte) 0, (byte) 30);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][244] = new TextureOffset((byte) 0, (byte) 11, (byte) 0, (byte) 0, (byte) 0, (byte) 29);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][245] = new TextureOffset((byte) 0, (byte) 11, (byte) 0, (byte) 0, (byte) 0, (byte) 31);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][246] = new TextureOffset((byte) 0, (byte) 23, (byte) 0, (byte) 0, (byte) 0, (byte) 14);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][247] = new TextureOffset((byte) 0, (byte) 23, (byte) 0, (byte) 0, (byte) 0, (byte) 30);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][248] = new TextureOffset((byte) 0, (byte) 23, (byte) 0, (byte) 0, (byte) 0, (byte) 29);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][249] = new TextureOffset((byte) 0, (byte) 23, (byte) 0, (byte) 0, (byte) 0, (byte) 31);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][250] = new TextureOffset((byte) 0, (byte) 24, (byte) 0, (byte) 0, (byte) 0, (byte) 14);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][251] = new TextureOffset((byte) 0, (byte) 24, (byte) 0, (byte) 0, (byte) 0, (byte) 30);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][252] = new TextureOffset((byte) 0, (byte) 24, (byte) 0, (byte) 0, (byte) 0, (byte) 29);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][253] = new TextureOffset((byte) 0, (byte) 24, (byte) 0, (byte) 0, (byte) 0, (byte) 31);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][254] = new TextureOffset((byte) 0, (byte) 25, (byte) 0, (byte) 0, (byte) 0, (byte) 14);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][255] = new TextureOffset((byte) 0, (byte) 25, (byte) 0, (byte) 0, (byte) 0, (byte) 30);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][256] = new TextureOffset((byte) 0, (byte) 25, (byte) 0, (byte) 0, (byte) 0, (byte) 29);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][257] = new TextureOffset((byte) 0, (byte) 25, (byte) 0, (byte) 0, (byte) 0, (byte) 31);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][258] = new TextureOffset((byte) 7, (byte) 0, (byte) 11, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][259] = new TextureOffset((byte) 7, (byte) 0, (byte) 23, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][260] = new TextureOffset((byte) 7, (byte) 0, (byte) 24, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][261] = new TextureOffset((byte) 7, (byte) 0, (byte) 25, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][262] = new TextureOffset((byte) 20, (byte) 0, (byte) 11, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][263] = new TextureOffset((byte) 20, (byte) 0, (byte) 23, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][264] = new TextureOffset((byte) 20, (byte) 0, (byte) 24, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][265] = new TextureOffset((byte) 20, (byte) 0, (byte) 25, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][266] = new TextureOffset((byte) 21, (byte) 0, (byte) 11, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][267] = new TextureOffset((byte) 21, (byte) 0, (byte) 23, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][268] = new TextureOffset((byte) 21, (byte) 0, (byte) 24, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][269] = new TextureOffset((byte) 21, (byte) 0, (byte) 25, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][270] = new TextureOffset((byte) 22, (byte) 0, (byte) 11, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][271] = new TextureOffset((byte) 22, (byte) 0, (byte) 23, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][272] = new TextureOffset((byte) 22, (byte) 0, (byte) 24, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][273] = new TextureOffset((byte) 22, (byte) 0, (byte) 25, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][274] = new TextureOffset((byte) 13, (byte) 0, (byte) 0, (byte) 11, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][275] = new TextureOffset((byte) 13, (byte) 0, (byte) 0, (byte) 24, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][276] = new TextureOffset((byte) 13, (byte) 0, (byte) 0, (byte) 23, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][277] = new TextureOffset((byte) 13, (byte) 0, (byte) 0, (byte) 25, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][278] = new TextureOffset((byte) 26, (byte) 0, (byte) 0, (byte) 11, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][279] = new TextureOffset((byte) 26, (byte) 0, (byte) 0, (byte) 24, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][280] = new TextureOffset((byte) 26, (byte) 0, (byte) 0, (byte) 23, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][281] = new TextureOffset((byte) 26, (byte) 0, (byte) 0, (byte) 25, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][282] = new TextureOffset((byte) 27, (byte) 0, (byte) 0, (byte) 11, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][283] = new TextureOffset((byte) 27, (byte) 0, (byte) 0, (byte) 24, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][284] = new TextureOffset((byte) 27, (byte) 0, (byte) 0, (byte) 23, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][285] = new TextureOffset((byte) 27, (byte) 0, (byte) 0, (byte) 25, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][286] = new TextureOffset((byte) 28, (byte) 0, (byte) 0, (byte) 11, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][287] = new TextureOffset((byte) 28, (byte) 0, (byte) 0, (byte) 24, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][288] = new TextureOffset((byte) 28, (byte) 0, (byte) 0, (byte) 23, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][289] = new TextureOffset((byte) 28, (byte) 0, (byte) 0, (byte) 25, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][290] = new TextureOffset((byte) 11, (byte) 0, (byte) 0, (byte) 0, (byte) 11, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][291] = new TextureOffset((byte) 11, (byte) 0, (byte) 0, (byte) 0, (byte) 24, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][292] = new TextureOffset((byte) 11, (byte) 0, (byte) 0, (byte) 0, (byte) 23, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][293] = new TextureOffset((byte) 11, (byte) 0, (byte) 0, (byte) 0, (byte) 25, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][294] = new TextureOffset((byte) 23, (byte) 0, (byte) 0, (byte) 0, (byte) 11, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][295] = new TextureOffset((byte) 23, (byte) 0, (byte) 0, (byte) 0, (byte) 24, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][296] = new TextureOffset((byte) 23, (byte) 0, (byte) 0, (byte) 0, (byte) 23, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][297] = new TextureOffset((byte) 23, (byte) 0, (byte) 0, (byte) 0, (byte) 25, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][298] = new TextureOffset((byte) 24, (byte) 0, (byte) 0, (byte) 0, (byte) 11, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][299] = new TextureOffset((byte) 24, (byte) 0, (byte) 0, (byte) 0, (byte) 24, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][300] = new TextureOffset((byte) 24, (byte) 0, (byte) 0, (byte) 0, (byte) 23, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][301] = new TextureOffset((byte) 24, (byte) 0, (byte) 0, (byte) 0, (byte) 25, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][302] = new TextureOffset((byte) 25, (byte) 0, (byte) 0, (byte) 0, (byte) 11, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][303] = new TextureOffset((byte) 25, (byte) 0, (byte) 0, (byte) 0, (byte) 24, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][304] = new TextureOffset((byte) 25, (byte) 0, (byte) 0, (byte) 0, (byte) 23, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][305] = new TextureOffset((byte) 25, (byte) 0, (byte) 0, (byte) 0, (byte) 25, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][306] = new TextureOffset((byte) 14, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 11);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][307] = new TextureOffset((byte) 14, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 23);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][308] = new TextureOffset((byte) 14, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 24);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][309] = new TextureOffset((byte) 14, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 25);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][310] = new TextureOffset((byte) 30, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 11);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][311] = new TextureOffset((byte) 30, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 23);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][312] = new TextureOffset((byte) 30, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 24);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][313] = new TextureOffset((byte) 30, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 25);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][314] = new TextureOffset((byte) 29, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 11);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][315] = new TextureOffset((byte) 29, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 23);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][316] = new TextureOffset((byte) 29, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 24);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][317] = new TextureOffset((byte) 29, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 25);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][318] = new TextureOffset((byte) 31, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 11);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][319] = new TextureOffset((byte) 31, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 23);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][320] = new TextureOffset((byte) 31, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 24);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][321] = new TextureOffset((byte) 31, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 25);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][322] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 7, (byte) 0, (byte) 13);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][323] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 21, (byte) 0, (byte) 13);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][324] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 7, (byte) 0, (byte) 26);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][325] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 21, (byte) 0, (byte) 26);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][326] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 20, (byte) 0, (byte) 13);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][327] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 22, (byte) 0, (byte) 13);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][328] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 20, (byte) 0, (byte) 26);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][329] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 22, (byte) 0, (byte) 26);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][330] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 7, (byte) 0, (byte) 27);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][331] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 21, (byte) 0, (byte) 27);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][332] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 7, (byte) 0, (byte) 28);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][333] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 21, (byte) 0, (byte) 28);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][334] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 20, (byte) 0, (byte) 27);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][335] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 22, (byte) 0, (byte) 27);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][336] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 20, (byte) 0, (byte) 28);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][337] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 22, (byte) 0, (byte) 28);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][338] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 13, (byte) 7, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][339] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 13, (byte) 21, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][340] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 26, (byte) 7, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][341] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 26, (byte) 21, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][342] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 13, (byte) 20, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][343] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 13, (byte) 22, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][344] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 26, (byte) 20, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][345] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 26, (byte) 22, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][346] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 27, (byte) 7, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][347] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 27, (byte) 21, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][348] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 28, (byte) 7, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][349] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 28, (byte) 21, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][350] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 27, (byte) 20, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][351] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 27, (byte) 22, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][352] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 28, (byte) 20, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][353] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 28, (byte) 22, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][354] = new TextureOffset((byte) 0, (byte) 0, (byte) 13, (byte) 0, (byte) 0, (byte) 7);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][355] = new TextureOffset((byte) 0, (byte) 0, (byte) 26, (byte) 0, (byte) 0, (byte) 7);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][356] = new TextureOffset((byte) 0, (byte) 0, (byte) 13, (byte) 0, (byte) 0, (byte) 21);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][357] = new TextureOffset((byte) 0, (byte) 0, (byte) 26, (byte) 0, (byte) 0, (byte) 21);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][358] = new TextureOffset((byte) 0, (byte) 0, (byte) 27, (byte) 0, (byte) 0, (byte) 7);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][359] = new TextureOffset((byte) 0, (byte) 0, (byte) 28, (byte) 0, (byte) 0, (byte) 7);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][360] = new TextureOffset((byte) 0, (byte) 0, (byte) 27, (byte) 0, (byte) 0, (byte) 21);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][361] = new TextureOffset((byte) 0, (byte) 0, (byte) 28, (byte) 0, (byte) 0, (byte) 21);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][362] = new TextureOffset((byte) 0, (byte) 0, (byte) 13, (byte) 0, (byte) 0, (byte) 20);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][363] = new TextureOffset((byte) 0, (byte) 0, (byte) 26, (byte) 0, (byte) 0, (byte) 20);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][364] = new TextureOffset((byte) 0, (byte) 0, (byte) 13, (byte) 0, (byte) 0, (byte) 22);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][365] = new TextureOffset((byte) 0, (byte) 0, (byte) 26, (byte) 0, (byte) 0, (byte) 22);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][366] = new TextureOffset((byte) 0, (byte) 0, (byte) 27, (byte) 0, (byte) 0, (byte) 20);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][367] = new TextureOffset((byte) 0, (byte) 0, (byte) 28, (byte) 0, (byte) 0, (byte) 20);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][368] = new TextureOffset((byte) 0, (byte) 0, (byte) 27, (byte) 0, (byte) 0, (byte) 22);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][369] = new TextureOffset((byte) 0, (byte) 0, (byte) 28, (byte) 0, (byte) 0, (byte) 22);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][370] = new TextureOffset((byte) 0, (byte) 0, (byte) 7, (byte) 0, (byte) 13, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][371] = new TextureOffset((byte) 0, (byte) 0, (byte) 21, (byte) 0, (byte) 13, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][372] = new TextureOffset((byte) 0, (byte) 0, (byte) 7, (byte) 0, (byte) 26, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][373] = new TextureOffset((byte) 0, (byte) 0, (byte) 21, (byte) 0, (byte) 26, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][374] = new TextureOffset((byte) 0, (byte) 0, (byte) 20, (byte) 0, (byte) 13, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][375] = new TextureOffset((byte) 0, (byte) 0, (byte) 22, (byte) 0, (byte) 13, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][376] = new TextureOffset((byte) 0, (byte) 0, (byte) 20, (byte) 0, (byte) 26, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][377] = new TextureOffset((byte) 0, (byte) 0, (byte) 22, (byte) 0, (byte) 26, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][378] = new TextureOffset((byte) 0, (byte) 0, (byte) 7, (byte) 0, (byte) 27, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][379] = new TextureOffset((byte) 0, (byte) 0, (byte) 21, (byte) 0, (byte) 27, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][380] = new TextureOffset((byte) 0, (byte) 0, (byte) 7, (byte) 0, (byte) 28, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][381] = new TextureOffset((byte) 0, (byte) 0, (byte) 21, (byte) 0, (byte) 28, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][382] = new TextureOffset((byte) 0, (byte) 0, (byte) 20, (byte) 0, (byte) 27, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][383] = new TextureOffset((byte) 0, (byte) 0, (byte) 22, (byte) 0, (byte) 27, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][384] = new TextureOffset((byte) 0, (byte) 0, (byte) 20, (byte) 0, (byte) 28, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_180.index][385] = new TextureOffset((byte) 0, (byte) 0, (byte) 22, (byte) 0, (byte) 28, (byte) 0);

		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][0] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][1] = new TextureOffset((byte) 0, (byte) 0, (byte) 2, (byte) 2, (byte) 2, (byte) 2);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][2] = new TextureOffset((byte) 0, (byte) 0, (byte) 8, (byte) 8, (byte) 8, (byte) 8);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][3] = new TextureOffset((byte) 4, (byte) 4, (byte) 0, (byte) 0, (byte) 1, (byte) 4);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][4] = new TextureOffset((byte) 1, (byte) 1, (byte) 0, (byte) 0, (byte) 4, (byte) 1);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][5] = new TextureOffset((byte) 2, (byte) 8, (byte) 4, (byte) 1, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][6] = new TextureOffset((byte) 8, (byte) 2, (byte) 1, (byte) 4, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][7] = new TextureOffset((byte) 0, (byte) 0, (byte) 10, (byte) 10, (byte) 10, (byte) 10);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][8] = new TextureOffset((byte) 5, (byte) 5, (byte) 0, (byte) 0, (byte) 5, (byte) 5);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][9] = new TextureOffset((byte) 10, (byte) 10, (byte) 5, (byte) 5, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][10] = new TextureOffset((byte) 0, (byte) 4, (byte) 0, (byte) 2, (byte) 3, (byte) 6);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][11] = new TextureOffset((byte) 0, (byte) 4, (byte) 0, (byte) 2, (byte) 16, (byte) 17);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][12] = new TextureOffset((byte) 0, (byte) 1, (byte) 2, (byte) 0, (byte) 6, (byte) 3);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][13] = new TextureOffset((byte) 0, (byte) 1, (byte) 2, (byte) 0, (byte) 17, (byte) 16);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][14] = new TextureOffset((byte) 0, (byte) 8, (byte) 6, (byte) 3, (byte) 0, (byte) 2);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][15] = new TextureOffset((byte) 0, (byte) 8, (byte) 17, (byte) 16, (byte) 0, (byte) 2);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][16] = new TextureOffset((byte) 0, (byte) 2, (byte) 3, (byte) 6, (byte) 2, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][17] = new TextureOffset((byte) 0, (byte) 2, (byte) 16, (byte) 17, (byte) 2, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][18] = new TextureOffset((byte) 4, (byte) 0, (byte) 0, (byte) 8, (byte) 9, (byte) 12);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][19] = new TextureOffset((byte) 4, (byte) 0, (byte) 0, (byte) 8, (byte) 19, (byte) 18);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][20] = new TextureOffset((byte) 1, (byte) 0, (byte) 8, (byte) 0, (byte) 12, (byte) 9);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][21] = new TextureOffset((byte) 1, (byte) 0, (byte) 8, (byte) 0, (byte) 18, (byte) 19);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][22] = new TextureOffset((byte) 2, (byte) 0, (byte) 12, (byte) 9, (byte) 0, (byte) 8);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][23] = new TextureOffset((byte) 2, (byte) 0, (byte) 18, (byte) 19, (byte) 0, (byte) 8);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][24] = new TextureOffset((byte) 8, (byte) 0, (byte) 9, (byte) 12, (byte) 8, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][25] = new TextureOffset((byte) 8, (byte) 0, (byte) 19, (byte) 18, (byte) 8, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][26] = new TextureOffset((byte) 6, (byte) 12, (byte) 0, (byte) 1, (byte) 0, (byte) 4);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][27] = new TextureOffset((byte) 17, (byte) 18, (byte) 0, (byte) 1, (byte) 0, (byte) 4);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][28] = new TextureOffset((byte) 12, (byte) 6, (byte) 0, (byte) 4, (byte) 1, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][29] = new TextureOffset((byte) 18, (byte) 17, (byte) 0, (byte) 4, (byte) 1, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][30] = new TextureOffset((byte) 3, (byte) 9, (byte) 4, (byte) 0, (byte) 0, (byte) 1);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][31] = new TextureOffset((byte) 16, (byte) 19, (byte) 4, (byte) 0, (byte) 0, (byte) 1);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][32] = new TextureOffset((byte) 9, (byte) 3, (byte) 1, (byte) 0, (byte) 4, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][33] = new TextureOffset((byte) 19, (byte) 16, (byte) 1, (byte) 0, (byte) 4, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][34] = new TextureOffset((byte) 0, (byte) 5, (byte) 0, (byte) 0, (byte) 7, (byte) 7);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][35] = new TextureOffset((byte) 0, (byte) 5, (byte) 0, (byte) 0, (byte) 20, (byte) 21);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][36] = new TextureOffset((byte) 0, (byte) 5, (byte) 0, (byte) 0, (byte) 21, (byte) 20);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][37] = new TextureOffset((byte) 0, (byte) 5, (byte) 0, (byte) 0, (byte) 22, (byte) 22);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][38] = new TextureOffset((byte) 0, (byte) 10, (byte) 7, (byte) 7, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][39] = new TextureOffset((byte) 0, (byte) 10, (byte) 21, (byte) 20, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][40] = new TextureOffset((byte) 0, (byte) 10, (byte) 20, (byte) 21, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][41] = new TextureOffset((byte) 0, (byte) 10, (byte) 22, (byte) 22, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][42] = new TextureOffset((byte) 5, (byte) 0, (byte) 0, (byte) 0, (byte) 13, (byte) 13);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][43] = new TextureOffset((byte) 5, (byte) 0, (byte) 0, (byte) 0, (byte) 27, (byte) 26);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][44] = new TextureOffset((byte) 5, (byte) 0, (byte) 0, (byte) 0, (byte) 26, (byte) 27);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][45] = new TextureOffset((byte) 5, (byte) 0, (byte) 0, (byte) 0, (byte) 28, (byte) 28);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][46] = new TextureOffset((byte) 10, (byte) 0, (byte) 13, (byte) 13, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][47] = new TextureOffset((byte) 10, (byte) 0, (byte) 26, (byte) 27, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][48] = new TextureOffset((byte) 10, (byte) 0, (byte) 27, (byte) 26, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][49] = new TextureOffset((byte) 10, (byte) 0, (byte) 28, (byte) 28, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][50] = new TextureOffset((byte) 7, (byte) 13, (byte) 0, (byte) 0, (byte) 0, (byte) 5);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][51] = new TextureOffset((byte) 21, (byte) 26, (byte) 0, (byte) 0, (byte) 0, (byte) 5);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][52] = new TextureOffset((byte) 20, (byte) 27, (byte) 0, (byte) 0, (byte) 0, (byte) 5);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][53] = new TextureOffset((byte) 22, (byte) 28, (byte) 0, (byte) 0, (byte) 0, (byte) 5);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][54] = new TextureOffset((byte) 0, (byte) 0, (byte) 14, (byte) 11, (byte) 0, (byte) 10);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][55] = new TextureOffset((byte) 0, (byte) 0, (byte) 29, (byte) 24, (byte) 0, (byte) 10);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][56] = new TextureOffset((byte) 0, (byte) 0, (byte) 30, (byte) 23, (byte) 0, (byte) 10);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][57] = new TextureOffset((byte) 0, (byte) 0, (byte) 31, (byte) 25, (byte) 0, (byte) 10);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][58] = new TextureOffset((byte) 13, (byte) 7, (byte) 0, (byte) 0, (byte) 5, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][59] = new TextureOffset((byte) 26, (byte) 21, (byte) 0, (byte) 0, (byte) 5, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][60] = new TextureOffset((byte) 27, (byte) 20, (byte) 0, (byte) 0, (byte) 5, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][61] = new TextureOffset((byte) 28, (byte) 22, (byte) 0, (byte) 0, (byte) 5, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][62] = new TextureOffset((byte) 0, (byte) 0, (byte) 11, (byte) 14, (byte) 10, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][63] = new TextureOffset((byte) 0, (byte) 0, (byte) 24, (byte) 29, (byte) 10, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][64] = new TextureOffset((byte) 0, (byte) 0, (byte) 23, (byte) 30, (byte) 10, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][65] = new TextureOffset((byte) 0, (byte) 0, (byte) 25, (byte) 31, (byte) 10, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][66] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 10, (byte) 11, (byte) 14);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][67] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 10, (byte) 24, (byte) 29);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][68] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 10, (byte) 23, (byte) 30);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][69] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 10, (byte) 25, (byte) 31);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][70] = new TextureOffset((byte) 14, (byte) 14, (byte) 0, (byte) 5, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][71] = new TextureOffset((byte) 29, (byte) 30, (byte) 0, (byte) 5, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][72] = new TextureOffset((byte) 30, (byte) 29, (byte) 0, (byte) 5, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][73] = new TextureOffset((byte) 31, (byte) 31, (byte) 0, (byte) 5, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][74] = new TextureOffset((byte) 0, (byte) 0, (byte) 10, (byte) 0, (byte) 14, (byte) 11);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][75] = new TextureOffset((byte) 0, (byte) 0, (byte) 10, (byte) 0, (byte) 29, (byte) 24);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][76] = new TextureOffset((byte) 0, (byte) 0, (byte) 10, (byte) 0, (byte) 30, (byte) 23);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][77] = new TextureOffset((byte) 0, (byte) 0, (byte) 10, (byte) 0, (byte) 31, (byte) 25);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][78] = new TextureOffset((byte) 11, (byte) 11, (byte) 5, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][79] = new TextureOffset((byte) 23, (byte) 24, (byte) 5, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][80] = new TextureOffset((byte) 24, (byte) 23, (byte) 5, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][81] = new TextureOffset((byte) 25, (byte) 25, (byte) 5, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][82] = new TextureOffset((byte) 0, (byte) 12, (byte) 0, (byte) 3, (byte) 0, (byte) 6);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][83] = new TextureOffset((byte) 0, (byte) 12, (byte) 0, (byte) 16, (byte) 0, (byte) 6);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][84] = new TextureOffset((byte) 0, (byte) 12, (byte) 0, (byte) 3, (byte) 0, (byte) 17);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][85] = new TextureOffset((byte) 0, (byte) 12, (byte) 0, (byte) 16, (byte) 0, (byte) 17);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][86] = new TextureOffset((byte) 0, (byte) 18, (byte) 0, (byte) 3, (byte) 0, (byte) 6);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][87] = new TextureOffset((byte) 0, (byte) 18, (byte) 0, (byte) 16, (byte) 0, (byte) 6);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][88] = new TextureOffset((byte) 0, (byte) 18, (byte) 0, (byte) 3, (byte) 0, (byte) 17);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][89] = new TextureOffset((byte) 0, (byte) 18, (byte) 0, (byte) 16, (byte) 0, (byte) 17);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][90] = new TextureOffset((byte) 0, (byte) 6, (byte) 0, (byte) 6, (byte) 3, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][91] = new TextureOffset((byte) 0, (byte) 6, (byte) 0, (byte) 6, (byte) 16, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][92] = new TextureOffset((byte) 0, (byte) 6, (byte) 0, (byte) 17, (byte) 3, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][93] = new TextureOffset((byte) 0, (byte) 6, (byte) 0, (byte) 17, (byte) 16, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][94] = new TextureOffset((byte) 0, (byte) 17, (byte) 0, (byte) 6, (byte) 3, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][95] = new TextureOffset((byte) 0, (byte) 17, (byte) 0, (byte) 6, (byte) 16, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][96] = new TextureOffset((byte) 0, (byte) 17, (byte) 0, (byte) 17, (byte) 3, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][97] = new TextureOffset((byte) 0, (byte) 17, (byte) 0, (byte) 17, (byte) 16, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][98] = new TextureOffset((byte) 0, (byte) 9, (byte) 6, (byte) 0, (byte) 0, (byte) 3);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][99] = new TextureOffset((byte) 0, (byte) 9, (byte) 17, (byte) 0, (byte) 0, (byte) 3);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][100] = new TextureOffset((byte) 0, (byte) 9, (byte) 6, (byte) 0, (byte) 0, (byte) 16);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][101] = new TextureOffset((byte) 0, (byte) 9, (byte) 17, (byte) 0, (byte) 0, (byte) 16);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][102] = new TextureOffset((byte) 0, (byte) 19, (byte) 6, (byte) 0, (byte) 0, (byte) 3);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][103] = new TextureOffset((byte) 0, (byte) 19, (byte) 17, (byte) 0, (byte) 0, (byte) 3);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][104] = new TextureOffset((byte) 0, (byte) 19, (byte) 6, (byte) 0, (byte) 0, (byte) 16);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][105] = new TextureOffset((byte) 0, (byte) 19, (byte) 17, (byte) 0, (byte) 0, (byte) 16);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][106] = new TextureOffset((byte) 0, (byte) 3, (byte) 3, (byte) 0, (byte) 6, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][107] = new TextureOffset((byte) 0, (byte) 3, (byte) 16, (byte) 0, (byte) 6, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][108] = new TextureOffset((byte) 0, (byte) 3, (byte) 3, (byte) 0, (byte) 17, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][109] = new TextureOffset((byte) 0, (byte) 3, (byte) 16, (byte) 0, (byte) 17, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][110] = new TextureOffset((byte) 0, (byte) 16, (byte) 3, (byte) 0, (byte) 6, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][111] = new TextureOffset((byte) 0, (byte) 16, (byte) 16, (byte) 0, (byte) 6, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][112] = new TextureOffset((byte) 0, (byte) 16, (byte) 3, (byte) 0, (byte) 17, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][113] = new TextureOffset((byte) 0, (byte) 16, (byte) 16, (byte) 0, (byte) 17, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][114] = new TextureOffset((byte) 6, (byte) 0, (byte) 0, (byte) 9, (byte) 0, (byte) 12);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][115] = new TextureOffset((byte) 6, (byte) 0, (byte) 0, (byte) 19, (byte) 0, (byte) 12);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][116] = new TextureOffset((byte) 6, (byte) 0, (byte) 0, (byte) 9, (byte) 0, (byte) 18);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][117] = new TextureOffset((byte) 6, (byte) 0, (byte) 0, (byte) 19, (byte) 0, (byte) 18);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][118] = new TextureOffset((byte) 17, (byte) 0, (byte) 0, (byte) 9, (byte) 0, (byte) 12);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][119] = new TextureOffset((byte) 17, (byte) 0, (byte) 0, (byte) 19, (byte) 0, (byte) 12);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][120] = new TextureOffset((byte) 17, (byte) 0, (byte) 0, (byte) 9, (byte) 0, (byte) 18);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][121] = new TextureOffset((byte) 17, (byte) 0, (byte) 0, (byte) 19, (byte) 0, (byte) 18);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][122] = new TextureOffset((byte) 12, (byte) 0, (byte) 0, (byte) 12, (byte) 9, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][123] = new TextureOffset((byte) 12, (byte) 0, (byte) 0, (byte) 12, (byte) 19, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][124] = new TextureOffset((byte) 12, (byte) 0, (byte) 0, (byte) 18, (byte) 9, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][125] = new TextureOffset((byte) 12, (byte) 0, (byte) 0, (byte) 18, (byte) 19, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][126] = new TextureOffset((byte) 18, (byte) 0, (byte) 0, (byte) 12, (byte) 9, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][127] = new TextureOffset((byte) 18, (byte) 0, (byte) 0, (byte) 12, (byte) 19, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][128] = new TextureOffset((byte) 18, (byte) 0, (byte) 0, (byte) 18, (byte) 9, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][129] = new TextureOffset((byte) 18, (byte) 0, (byte) 0, (byte) 18, (byte) 19, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][130] = new TextureOffset((byte) 3, (byte) 0, (byte) 12, (byte) 0, (byte) 0, (byte) 9);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][131] = new TextureOffset((byte) 3, (byte) 0, (byte) 18, (byte) 0, (byte) 0, (byte) 9);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][132] = new TextureOffset((byte) 3, (byte) 0, (byte) 12, (byte) 0, (byte) 0, (byte) 19);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][133] = new TextureOffset((byte) 3, (byte) 0, (byte) 18, (byte) 0, (byte) 0, (byte) 19);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][134] = new TextureOffset((byte) 16, (byte) 0, (byte) 12, (byte) 0, (byte) 0, (byte) 9);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][135] = new TextureOffset((byte) 16, (byte) 0, (byte) 18, (byte) 0, (byte) 0, (byte) 9);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][136] = new TextureOffset((byte) 16, (byte) 0, (byte) 12, (byte) 0, (byte) 0, (byte) 19);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][137] = new TextureOffset((byte) 16, (byte) 0, (byte) 18, (byte) 0, (byte) 0, (byte) 19);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][138] = new TextureOffset((byte) 9, (byte) 0, (byte) 9, (byte) 0, (byte) 12, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][139] = new TextureOffset((byte) 9, (byte) 0, (byte) 19, (byte) 0, (byte) 12, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][140] = new TextureOffset((byte) 9, (byte) 0, (byte) 9, (byte) 0, (byte) 18, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][141] = new TextureOffset((byte) 9, (byte) 0, (byte) 19, (byte) 0, (byte) 18, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][142] = new TextureOffset((byte) 19, (byte) 0, (byte) 9, (byte) 0, (byte) 12, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][143] = new TextureOffset((byte) 19, (byte) 0, (byte) 19, (byte) 0, (byte) 12, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][144] = new TextureOffset((byte) 19, (byte) 0, (byte) 9, (byte) 0, (byte) 18, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][145] = new TextureOffset((byte) 19, (byte) 0, (byte) 19, (byte) 0, (byte) 18, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][146] = new TextureOffset((byte) 15, (byte) 15, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][147] = new TextureOffset((byte) 36, (byte) 40, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][148] = new TextureOffset((byte) 40, (byte) 36, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][149] = new TextureOffset((byte) 44, (byte) 44, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][150] = new TextureOffset((byte) 33, (byte) 34, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][151] = new TextureOffset((byte) 37, (byte) 42, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][152] = new TextureOffset((byte) 41, (byte) 38, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][153] = new TextureOffset((byte) 45, (byte) 46, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][154] = new TextureOffset((byte) 34, (byte) 33, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][155] = new TextureOffset((byte) 38, (byte) 41, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][156] = new TextureOffset((byte) 42, (byte) 37, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][157] = new TextureOffset((byte) 46, (byte) 45, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][158] = new TextureOffset((byte) 35, (byte) 35, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][159] = new TextureOffset((byte) 39, (byte) 43, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][160] = new TextureOffset((byte) 43, (byte) 39, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][161] = new TextureOffset((byte) 47, (byte) 47, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][162] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 15, (byte) 15);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][163] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 34, (byte) 36);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][164] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 36, (byte) 34);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][165] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 38);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][166] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 33, (byte) 40);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][167] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 35, (byte) 44);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][168] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 37, (byte) 42);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][169] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 39, (byte) 46);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][170] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 40, (byte) 33);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][171] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 42, (byte) 37);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][172] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 44, (byte) 35);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][173] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 46, (byte) 39);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][174] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 41, (byte) 41);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][175] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 43, (byte) 45);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][176] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 45, (byte) 43);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][177] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 47, (byte) 47);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][178] = new TextureOffset((byte) 0, (byte) 0, (byte) 15, (byte) 15, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][179] = new TextureOffset((byte) 0, (byte) 0, (byte) 36, (byte) 34, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][180] = new TextureOffset((byte) 0, (byte) 0, (byte) 34, (byte) 36, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][181] = new TextureOffset((byte) 0, (byte) 0, (byte) 38, (byte) 38, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][182] = new TextureOffset((byte) 0, (byte) 0, (byte) 40, (byte) 33, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][183] = new TextureOffset((byte) 0, (byte) 0, (byte) 44, (byte) 35, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][184] = new TextureOffset((byte) 0, (byte) 0, (byte) 42, (byte) 37, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][185] = new TextureOffset((byte) 0, (byte) 0, (byte) 46, (byte) 39, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][186] = new TextureOffset((byte) 0, (byte) 0, (byte) 33, (byte) 40, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][187] = new TextureOffset((byte) 0, (byte) 0, (byte) 37, (byte) 42, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][188] = new TextureOffset((byte) 0, (byte) 0, (byte) 35, (byte) 44, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][189] = new TextureOffset((byte) 0, (byte) 0, (byte) 39, (byte) 46, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][190] = new TextureOffset((byte) 0, (byte) 0, (byte) 41, (byte) 41, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][191] = new TextureOffset((byte) 0, (byte) 0, (byte) 45, (byte) 43, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][192] = new TextureOffset((byte) 0, (byte) 0, (byte) 43, (byte) 45, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][193] = new TextureOffset((byte) 0, (byte) 0, (byte) 47, (byte) 47, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][194] = new TextureOffset((byte) 0, (byte) 11, (byte) 7, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][195] = new TextureOffset((byte) 0, (byte) 11, (byte) 21, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][196] = new TextureOffset((byte) 0, (byte) 11, (byte) 20, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][197] = new TextureOffset((byte) 0, (byte) 11, (byte) 22, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][198] = new TextureOffset((byte) 0, (byte) 24, (byte) 7, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][199] = new TextureOffset((byte) 0, (byte) 24, (byte) 21, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][200] = new TextureOffset((byte) 0, (byte) 24, (byte) 20, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][201] = new TextureOffset((byte) 0, (byte) 24, (byte) 22, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][202] = new TextureOffset((byte) 0, (byte) 23, (byte) 7, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][203] = new TextureOffset((byte) 0, (byte) 23, (byte) 21, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][204] = new TextureOffset((byte) 0, (byte) 23, (byte) 20, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][205] = new TextureOffset((byte) 0, (byte) 23, (byte) 22, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][206] = new TextureOffset((byte) 0, (byte) 25, (byte) 7, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][207] = new TextureOffset((byte) 0, (byte) 25, (byte) 21, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][208] = new TextureOffset((byte) 0, (byte) 25, (byte) 20, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][209] = new TextureOffset((byte) 0, (byte) 25, (byte) 22, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][210] = new TextureOffset((byte) 0, (byte) 14, (byte) 0, (byte) 7, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][211] = new TextureOffset((byte) 0, (byte) 14, (byte) 0, (byte) 20, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][212] = new TextureOffset((byte) 0, (byte) 14, (byte) 0, (byte) 21, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][213] = new TextureOffset((byte) 0, (byte) 14, (byte) 0, (byte) 22, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][214] = new TextureOffset((byte) 0, (byte) 30, (byte) 0, (byte) 7, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][215] = new TextureOffset((byte) 0, (byte) 30, (byte) 0, (byte) 20, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][216] = new TextureOffset((byte) 0, (byte) 30, (byte) 0, (byte) 21, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][217] = new TextureOffset((byte) 0, (byte) 30, (byte) 0, (byte) 22, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][218] = new TextureOffset((byte) 0, (byte) 29, (byte) 0, (byte) 7, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][219] = new TextureOffset((byte) 0, (byte) 29, (byte) 0, (byte) 20, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][220] = new TextureOffset((byte) 0, (byte) 29, (byte) 0, (byte) 21, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][221] = new TextureOffset((byte) 0, (byte) 29, (byte) 0, (byte) 22, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][222] = new TextureOffset((byte) 0, (byte) 31, (byte) 0, (byte) 7, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][223] = new TextureOffset((byte) 0, (byte) 31, (byte) 0, (byte) 20, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][224] = new TextureOffset((byte) 0, (byte) 31, (byte) 0, (byte) 21, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][225] = new TextureOffset((byte) 0, (byte) 31, (byte) 0, (byte) 22, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][226] = new TextureOffset((byte) 0, (byte) 7, (byte) 0, (byte) 0, (byte) 7, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][227] = new TextureOffset((byte) 0, (byte) 7, (byte) 0, (byte) 0, (byte) 20, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][228] = new TextureOffset((byte) 0, (byte) 7, (byte) 0, (byte) 0, (byte) 21, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][229] = new TextureOffset((byte) 0, (byte) 7, (byte) 0, (byte) 0, (byte) 22, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][230] = new TextureOffset((byte) 0, (byte) 21, (byte) 0, (byte) 0, (byte) 7, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][231] = new TextureOffset((byte) 0, (byte) 21, (byte) 0, (byte) 0, (byte) 20, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][232] = new TextureOffset((byte) 0, (byte) 21, (byte) 0, (byte) 0, (byte) 21, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][233] = new TextureOffset((byte) 0, (byte) 21, (byte) 0, (byte) 0, (byte) 22, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][234] = new TextureOffset((byte) 0, (byte) 20, (byte) 0, (byte) 0, (byte) 7, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][235] = new TextureOffset((byte) 0, (byte) 20, (byte) 0, (byte) 0, (byte) 20, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][236] = new TextureOffset((byte) 0, (byte) 20, (byte) 0, (byte) 0, (byte) 21, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][237] = new TextureOffset((byte) 0, (byte) 20, (byte) 0, (byte) 0, (byte) 22, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][238] = new TextureOffset((byte) 0, (byte) 22, (byte) 0, (byte) 0, (byte) 7, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][239] = new TextureOffset((byte) 0, (byte) 22, (byte) 0, (byte) 0, (byte) 20, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][240] = new TextureOffset((byte) 0, (byte) 22, (byte) 0, (byte) 0, (byte) 21, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][241] = new TextureOffset((byte) 0, (byte) 22, (byte) 0, (byte) 0, (byte) 22, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][242] = new TextureOffset((byte) 0, (byte) 13, (byte) 0, (byte) 0, (byte) 0, (byte) 7);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][243] = new TextureOffset((byte) 0, (byte) 13, (byte) 0, (byte) 0, (byte) 0, (byte) 21);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][244] = new TextureOffset((byte) 0, (byte) 13, (byte) 0, (byte) 0, (byte) 0, (byte) 20);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][245] = new TextureOffset((byte) 0, (byte) 13, (byte) 0, (byte) 0, (byte) 0, (byte) 22);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][246] = new TextureOffset((byte) 0, (byte) 26, (byte) 0, (byte) 0, (byte) 0, (byte) 7);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][247] = new TextureOffset((byte) 0, (byte) 26, (byte) 0, (byte) 0, (byte) 0, (byte) 21);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][248] = new TextureOffset((byte) 0, (byte) 26, (byte) 0, (byte) 0, (byte) 0, (byte) 20);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][249] = new TextureOffset((byte) 0, (byte) 26, (byte) 0, (byte) 0, (byte) 0, (byte) 22);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][250] = new TextureOffset((byte) 0, (byte) 27, (byte) 0, (byte) 0, (byte) 0, (byte) 7);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][251] = new TextureOffset((byte) 0, (byte) 27, (byte) 0, (byte) 0, (byte) 0, (byte) 21);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][252] = new TextureOffset((byte) 0, (byte) 27, (byte) 0, (byte) 0, (byte) 0, (byte) 20);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][253] = new TextureOffset((byte) 0, (byte) 27, (byte) 0, (byte) 0, (byte) 0, (byte) 22);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][254] = new TextureOffset((byte) 0, (byte) 28, (byte) 0, (byte) 0, (byte) 0, (byte) 7);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][255] = new TextureOffset((byte) 0, (byte) 28, (byte) 0, (byte) 0, (byte) 0, (byte) 21);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][256] = new TextureOffset((byte) 0, (byte) 28, (byte) 0, (byte) 0, (byte) 0, (byte) 20);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][257] = new TextureOffset((byte) 0, (byte) 28, (byte) 0, (byte) 0, (byte) 0, (byte) 22);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][258] = new TextureOffset((byte) 11, (byte) 0, (byte) 13, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][259] = new TextureOffset((byte) 11, (byte) 0, (byte) 26, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][260] = new TextureOffset((byte) 11, (byte) 0, (byte) 27, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][261] = new TextureOffset((byte) 11, (byte) 0, (byte) 28, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][262] = new TextureOffset((byte) 23, (byte) 0, (byte) 13, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][263] = new TextureOffset((byte) 23, (byte) 0, (byte) 26, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][264] = new TextureOffset((byte) 23, (byte) 0, (byte) 27, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][265] = new TextureOffset((byte) 23, (byte) 0, (byte) 28, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][266] = new TextureOffset((byte) 24, (byte) 0, (byte) 13, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][267] = new TextureOffset((byte) 24, (byte) 0, (byte) 26, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][268] = new TextureOffset((byte) 24, (byte) 0, (byte) 27, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][269] = new TextureOffset((byte) 24, (byte) 0, (byte) 28, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][270] = new TextureOffset((byte) 25, (byte) 0, (byte) 13, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][271] = new TextureOffset((byte) 25, (byte) 0, (byte) 26, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][272] = new TextureOffset((byte) 25, (byte) 0, (byte) 27, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][273] = new TextureOffset((byte) 25, (byte) 0, (byte) 28, (byte) 0, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][274] = new TextureOffset((byte) 14, (byte) 0, (byte) 0, (byte) 13, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][275] = new TextureOffset((byte) 14, (byte) 0, (byte) 0, (byte) 27, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][276] = new TextureOffset((byte) 14, (byte) 0, (byte) 0, (byte) 26, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][277] = new TextureOffset((byte) 14, (byte) 0, (byte) 0, (byte) 28, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][278] = new TextureOffset((byte) 29, (byte) 0, (byte) 0, (byte) 13, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][279] = new TextureOffset((byte) 29, (byte) 0, (byte) 0, (byte) 27, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][280] = new TextureOffset((byte) 29, (byte) 0, (byte) 0, (byte) 26, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][281] = new TextureOffset((byte) 29, (byte) 0, (byte) 0, (byte) 28, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][282] = new TextureOffset((byte) 30, (byte) 0, (byte) 0, (byte) 13, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][283] = new TextureOffset((byte) 30, (byte) 0, (byte) 0, (byte) 27, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][284] = new TextureOffset((byte) 30, (byte) 0, (byte) 0, (byte) 26, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][285] = new TextureOffset((byte) 30, (byte) 0, (byte) 0, (byte) 28, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][286] = new TextureOffset((byte) 31, (byte) 0, (byte) 0, (byte) 13, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][287] = new TextureOffset((byte) 31, (byte) 0, (byte) 0, (byte) 27, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][288] = new TextureOffset((byte) 31, (byte) 0, (byte) 0, (byte) 26, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][289] = new TextureOffset((byte) 31, (byte) 0, (byte) 0, (byte) 28, (byte) 0, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][290] = new TextureOffset((byte) 13, (byte) 0, (byte) 0, (byte) 0, (byte) 13, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][291] = new TextureOffset((byte) 13, (byte) 0, (byte) 0, (byte) 0, (byte) 27, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][292] = new TextureOffset((byte) 13, (byte) 0, (byte) 0, (byte) 0, (byte) 26, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][293] = new TextureOffset((byte) 13, (byte) 0, (byte) 0, (byte) 0, (byte) 28, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][294] = new TextureOffset((byte) 26, (byte) 0, (byte) 0, (byte) 0, (byte) 13, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][295] = new TextureOffset((byte) 26, (byte) 0, (byte) 0, (byte) 0, (byte) 27, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][296] = new TextureOffset((byte) 26, (byte) 0, (byte) 0, (byte) 0, (byte) 26, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][297] = new TextureOffset((byte) 26, (byte) 0, (byte) 0, (byte) 0, (byte) 28, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][298] = new TextureOffset((byte) 27, (byte) 0, (byte) 0, (byte) 0, (byte) 13, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][299] = new TextureOffset((byte) 27, (byte) 0, (byte) 0, (byte) 0, (byte) 27, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][300] = new TextureOffset((byte) 27, (byte) 0, (byte) 0, (byte) 0, (byte) 26, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][301] = new TextureOffset((byte) 27, (byte) 0, (byte) 0, (byte) 0, (byte) 28, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][302] = new TextureOffset((byte) 28, (byte) 0, (byte) 0, (byte) 0, (byte) 13, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][303] = new TextureOffset((byte) 28, (byte) 0, (byte) 0, (byte) 0, (byte) 27, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][304] = new TextureOffset((byte) 28, (byte) 0, (byte) 0, (byte) 0, (byte) 26, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][305] = new TextureOffset((byte) 28, (byte) 0, (byte) 0, (byte) 0, (byte) 28, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][306] = new TextureOffset((byte) 7, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 13);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][307] = new TextureOffset((byte) 7, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 26);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][308] = new TextureOffset((byte) 7, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 27);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][309] = new TextureOffset((byte) 7, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 28);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][310] = new TextureOffset((byte) 21, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 13);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][311] = new TextureOffset((byte) 21, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 26);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][312] = new TextureOffset((byte) 21, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 27);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][313] = new TextureOffset((byte) 21, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 28);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][314] = new TextureOffset((byte) 20, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 13);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][315] = new TextureOffset((byte) 20, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 26);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][316] = new TextureOffset((byte) 20, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 27);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][317] = new TextureOffset((byte) 20, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 28);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][318] = new TextureOffset((byte) 22, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 13);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][319] = new TextureOffset((byte) 22, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 26);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][320] = new TextureOffset((byte) 22, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 27);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][321] = new TextureOffset((byte) 22, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 28);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][322] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 11, (byte) 0, (byte) 14);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][323] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 24, (byte) 0, (byte) 14);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][324] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 11, (byte) 0, (byte) 29);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][325] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 24, (byte) 0, (byte) 29);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][326] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 23, (byte) 0, (byte) 14);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][327] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 25, (byte) 0, (byte) 14);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][328] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 23, (byte) 0, (byte) 29);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][329] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 25, (byte) 0, (byte) 29);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][330] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 11, (byte) 0, (byte) 30);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][331] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 24, (byte) 0, (byte) 30);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][332] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 11, (byte) 0, (byte) 31);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][333] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 24, (byte) 0, (byte) 31);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][334] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 23, (byte) 0, (byte) 30);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][335] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 25, (byte) 0, (byte) 30);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][336] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 23, (byte) 0, (byte) 31);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][337] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 25, (byte) 0, (byte) 31);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][338] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 14, (byte) 11, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][339] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 14, (byte) 24, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][340] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 29, (byte) 11, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][341] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 29, (byte) 24, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][342] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 14, (byte) 23, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][343] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 14, (byte) 25, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][344] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 29, (byte) 23, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][345] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 29, (byte) 25, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][346] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 30, (byte) 11, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][347] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 30, (byte) 24, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][348] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 31, (byte) 11, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][349] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 31, (byte) 24, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][350] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 30, (byte) 23, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][351] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 30, (byte) 25, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][352] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 31, (byte) 23, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][353] = new TextureOffset((byte) 0, (byte) 0, (byte) 0, (byte) 31, (byte) 25, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][354] = new TextureOffset((byte) 0, (byte) 0, (byte) 14, (byte) 0, (byte) 0, (byte) 11);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][355] = new TextureOffset((byte) 0, (byte) 0, (byte) 29, (byte) 0, (byte) 0, (byte) 11);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][356] = new TextureOffset((byte) 0, (byte) 0, (byte) 14, (byte) 0, (byte) 0, (byte) 24);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][357] = new TextureOffset((byte) 0, (byte) 0, (byte) 29, (byte) 0, (byte) 0, (byte) 24);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][358] = new TextureOffset((byte) 0, (byte) 0, (byte) 30, (byte) 0, (byte) 0, (byte) 11);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][359] = new TextureOffset((byte) 0, (byte) 0, (byte) 31, (byte) 0, (byte) 0, (byte) 11);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][360] = new TextureOffset((byte) 0, (byte) 0, (byte) 30, (byte) 0, (byte) 0, (byte) 24);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][361] = new TextureOffset((byte) 0, (byte) 0, (byte) 31, (byte) 0, (byte) 0, (byte) 24);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][362] = new TextureOffset((byte) 0, (byte) 0, (byte) 14, (byte) 0, (byte) 0, (byte) 23);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][363] = new TextureOffset((byte) 0, (byte) 0, (byte) 29, (byte) 0, (byte) 0, (byte) 23);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][364] = new TextureOffset((byte) 0, (byte) 0, (byte) 14, (byte) 0, (byte) 0, (byte) 25);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][365] = new TextureOffset((byte) 0, (byte) 0, (byte) 29, (byte) 0, (byte) 0, (byte) 25);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][366] = new TextureOffset((byte) 0, (byte) 0, (byte) 30, (byte) 0, (byte) 0, (byte) 23);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][367] = new TextureOffset((byte) 0, (byte) 0, (byte) 31, (byte) 0, (byte) 0, (byte) 23);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][368] = new TextureOffset((byte) 0, (byte) 0, (byte) 30, (byte) 0, (byte) 0, (byte) 25);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][369] = new TextureOffset((byte) 0, (byte) 0, (byte) 31, (byte) 0, (byte) 0, (byte) 25);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][370] = new TextureOffset((byte) 0, (byte) 0, (byte) 11, (byte) 0, (byte) 14, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][371] = new TextureOffset((byte) 0, (byte) 0, (byte) 24, (byte) 0, (byte) 14, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][372] = new TextureOffset((byte) 0, (byte) 0, (byte) 11, (byte) 0, (byte) 29, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][373] = new TextureOffset((byte) 0, (byte) 0, (byte) 24, (byte) 0, (byte) 29, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][374] = new TextureOffset((byte) 0, (byte) 0, (byte) 23, (byte) 0, (byte) 14, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][375] = new TextureOffset((byte) 0, (byte) 0, (byte) 25, (byte) 0, (byte) 14, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][376] = new TextureOffset((byte) 0, (byte) 0, (byte) 23, (byte) 0, (byte) 29, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][377] = new TextureOffset((byte) 0, (byte) 0, (byte) 25, (byte) 0, (byte) 29, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][378] = new TextureOffset((byte) 0, (byte) 0, (byte) 11, (byte) 0, (byte) 30, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][379] = new TextureOffset((byte) 0, (byte) 0, (byte) 24, (byte) 0, (byte) 30, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][380] = new TextureOffset((byte) 0, (byte) 0, (byte) 11, (byte) 0, (byte) 31, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][381] = new TextureOffset((byte) 0, (byte) 0, (byte) 24, (byte) 0, (byte) 31, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][382] = new TextureOffset((byte) 0, (byte) 0, (byte) 23, (byte) 0, (byte) 30, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][383] = new TextureOffset((byte) 0, (byte) 0, (byte) 25, (byte) 0, (byte) 30, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][384] = new TextureOffset((byte) 0, (byte) 0, (byte) 23, (byte) 0, (byte) 31, (byte) 0);
		CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_270.index][385] = new TextureOffset((byte) 0, (byte) 0, (byte) 25, (byte) 0, (byte) 31, (byte) 0);

		// Simple join offsets are a subset of connected corner offsets
		for (Rotation rotation : Rotation.values()) {
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][0] = CONNECTED_CORNER_TEXTURE_OFFSETS[Rotation.ROTATE_NONE.index][0];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][1] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][1];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][2] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][2];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][3] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][3];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][4] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][4];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][5] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][5];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][6] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][6];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][7] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][7];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][8] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][8];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][9] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][9];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][10] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][10];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][11] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][12];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][12] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][14];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][13] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][16];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][14] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][18];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][15] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][20];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][16] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][22];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][17] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][24];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][18] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][26];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][19] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][28];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][20] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][30];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][21] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][32];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][22] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][34];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][23] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][38];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][24] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][42];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][25] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][46];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][26] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][50];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][27] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][54];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][28] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][58];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][29] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][62];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][30] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][66];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][31] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][70];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][32] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][74];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][33] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][78];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][34] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][82];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][35] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][90];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][36] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][98];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][37] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][106];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][38] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][114];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][39] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][122];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][40] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][130];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][41] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][138];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][42] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][146];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][43] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][162];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][44] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][178];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][45] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][194];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][46] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][210];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][47] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][226];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][48] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][242];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][49] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][258];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][50] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][274];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][51] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][290];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][52] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][306];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][53] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][322];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][54] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][338];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][55] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][354];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][56] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][370];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][57] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][146];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][58] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][146];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][59] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][178];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][60] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][178];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][61] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][162];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][62] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][162];
			SIMPLE_JOIN_TEXTURE_OFFSETS[rotation.index][63] = CONNECTED_CORNER_TEXTURE_OFFSETS[rotation.index][0];
		}
	}

}
