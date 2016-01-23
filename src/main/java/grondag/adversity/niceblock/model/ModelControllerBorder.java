package grondag.adversity.niceblock.model;

import com.google.common.collect.ImmutableMap;

import grondag.adversity.Adversity;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblock.NiceBlock;
import grondag.adversity.niceblock.NiceSubstance;
import grondag.adversity.niceblock.NiceBlock.TestForCompleteMatch;
import grondag.adversity.niceblock.support.CornerRecipeFinder;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;

public class ModelControllerBorder extends ModelController {
	
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

	public ModelControllerBorder(int textureIndex, int alternateCount, EnumWorldBlockLayer renderLayer, boolean isShaded, int color) {
		super(textureIndex, alternateCount, false, renderLayer, isShaded, false, color);
		textureCount = 15;
	}

	@Override
	public NiceModelBorder getModel(NiceSubstance substance) {
		return new NiceModelBorder(substance, this);
	}

	@Override
	public int getVariantID(IExtendedBlockState state, IBlockAccess worldIn, BlockPos pos) {

		NiceBlock.TestForCompleteMatch test = new TestForCompleteMatch(state);
		NeighborBlocks neighbors = new NeighborBlocks(worldIn, pos);
		NeighborTestResults mates = neighbors.getNeighborTestResults(test);

		CornerRecipeFinder finder = CONNECTED_CORNER_RECIPE_LOOKUP[mates.upBit()][mates.downBit()]
				[mates.eastBit()][mates.westBit()]
				[mates.northBit()][mates.southBit()];

		return finder.getRecipe(test, worldIn, pos);
	}

	@Override
	protected String getTextureName(NiceSubstance substance, int offset) {
		
		int position = this.textureIndex + offset;
		return "adversity:blocks/bordertest/bordertest_" + (position >> 3) + "_" + (position & 7);		
	}
	
	static {
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
	}

}