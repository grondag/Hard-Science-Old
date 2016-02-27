package grondag.adversity.niceblock.model;

import com.google.common.collect.ImmutableMap;

import grondag.adversity.Adversity;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblock.newmodel.NiceBlock;
import grondag.adversity.niceblock.newmodel.NiceBlock.TestForCompleteMatch;
import grondag.adversity.niceblock.newmodel.color.NiceColor;
import grondag.adversity.niceblock.support.CornerStateFinder;
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
	protected static CornerStateFinder[][][][][][] CONNECTED_CORNER_RECIPE_LOOKUP = new CornerStateFinder[2][2][2][2][2][2];
	
	protected final NiceColor firstColor;

	public ModelControllerBorder(String textureName, int alternateCount, EnumWorldBlockLayer renderLayer, boolean isShaded, NiceColor firstColor) {
		super(textureName, alternateCount, renderLayer, isShaded, false);
		this.firstColor = firstColor;
		textureCount = 15;
	}

	@Override
	public NiceModelBorder getModel(int meta) {
		return new NiceModelBorder(this, meta, NiceColor.values()[firstColor.ordinal() + meta]);
	}

	@Override
	public int getVariantID(IExtendedBlockState state, IBlockAccess worldIn, BlockPos pos) {

		NiceBlock.TestForCompleteMatch test = new TestForCompleteMatch(state);
		NeighborBlocks neighbors = new NeighborBlocks(worldIn, pos);
		NeighborTestResults mates = neighbors.getNeighborTestResults(test);

		CornerStateFinder finder = CONNECTED_CORNER_RECIPE_LOOKUP[mates.upBit()][mates.downBit()]
				[mates.eastBit()][mates.westBit()]
				[mates.northBit()][mates.southBit()];

		return finder.getRecipe(test, worldIn, pos);
	}
	
	static {
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][1][1][1][1] = new CornerStateFinder(0);
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][1][1][1][0] = new CornerStateFinder(162, "UE", "UW", "DE", "DW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][1][1][0][1] = new CornerStateFinder(162, "UE", "UW", "DE", "DW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][1][1][0][0] = new CornerStateFinder(162, "UE", "UW", "DE", "DW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][1][0][1][1] = new CornerStateFinder(178, "UN", "US", "DN", "DS");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][1][0][1][0] = new CornerStateFinder(322, "UN", "UE", "DN", "DE");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][1][0][0][1] = new CornerStateFinder(338, "UE", "US", "DE", "DS");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][1][0][0][0] = new CornerStateFinder(66, "UE", "DE");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][0][1][1][1] = new CornerStateFinder(178, "UN", "US", "DN", "DS");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][0][1][1][0] = new CornerStateFinder(354, "UN", "UW", "DN", "DW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][0][1][0][1] = new CornerStateFinder(370, "US", "UW", "DS", "DW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][0][1][0][0] = new CornerStateFinder(74, "UW", "DW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][0][0][1][1] = new CornerStateFinder(178, "UN", "US", "DN", "DS");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][0][0][1][0] = new CornerStateFinder(54, "UN", "DN");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][0][0][0][1] = new CornerStateFinder(62, "US", "DS");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][1][0][0][0][0] = new CornerStateFinder(7);
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][1][1][1][1] = new CornerStateFinder(146, "NE", "SE", "SW", "NW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][1][1][1][0] = new CornerStateFinder(242, "UE", "UW", "NE", "NW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][1][1][0][1] = new CornerStateFinder(226, "UE", "UW", "SE", "SW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][1][1][0][0] = new CornerStateFinder(34, "UE", "UW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][1][0][1][1] = new CornerStateFinder(210, "UN", "US", "NE", "SE");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][1][0][1][0] = new CornerStateFinder(82, "UN", "UE", "NE");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][1][0][0][1] = new CornerStateFinder(90, "UE", "US", "SE");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][1][0][0][0] = new CornerStateFinder(10, "UE");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][0][1][1][1] = new CornerStateFinder(194, "UN", "US", "SW", "NW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][0][1][1][0] = new CornerStateFinder(98, "UN", "UW", "NW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][0][1][0][1] = new CornerStateFinder(106, "US", "UW", "SW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][0][1][0][0] = new CornerStateFinder(12, "UW");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][0][0][1][1] = new CornerStateFinder(38, "UN", "US");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][0][0][1][0] = new CornerStateFinder(14, "UN");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][0][0][0][1] = new CornerStateFinder(16, "US");
		CONNECTED_CORNER_RECIPE_LOOKUP[1][0][0][0][0][0] = new CornerStateFinder(1);
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][1][1][1][1] = new CornerStateFinder(146, "NE", "SE", "SW", "NW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][1][1][1][0] = new CornerStateFinder(306, "DE", "DW", "NE", "NW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][1][1][0][1] = new CornerStateFinder(290, "DE", "DW", "SE", "SW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][1][1][0][0] = new CornerStateFinder(42, "DE", "DW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][1][0][1][1] = new CornerStateFinder(274, "DN", "DS", "NE", "SE");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][1][0][1][0] = new CornerStateFinder(114, "DN", "DE", "NE");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][1][0][0][1] = new CornerStateFinder(122, "DE", "DS", "SE");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][1][0][0][0] = new CornerStateFinder(18, "DE");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][0][1][1][1] = new CornerStateFinder(258, "DN", "DS", "SW", "NW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][0][1][1][0] = new CornerStateFinder(130, "DN", "DW", "NW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][0][1][0][1] = new CornerStateFinder(138, "DS", "DW", "SW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][0][1][0][0] = new CornerStateFinder(20, "DW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][0][0][1][1] = new CornerStateFinder(46, "DN", "DS");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][0][0][1][0] = new CornerStateFinder(22, "DN");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][0][0][0][1] = new CornerStateFinder(24, "DS");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][1][0][0][0][0] = new CornerStateFinder(2);
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][1][1][1][1] = new CornerStateFinder(146, "NE", "SE", "SW", "NW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][1][1][1][0] = new CornerStateFinder(50, "NE", "NW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][1][1][0][1] = new CornerStateFinder(58, "SE", "SW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][1][1][0][0] = new CornerStateFinder(8);
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][1][0][1][1] = new CornerStateFinder(70, "NE", "SE");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][1][0][1][0] = new CornerStateFinder(26, "NE");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][1][0][0][1] = new CornerStateFinder(28, "SE");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][1][0][0][0] = new CornerStateFinder(3);
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][0][1][1][1] = new CornerStateFinder(78, "SW", "NW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][0][1][1][0] = new CornerStateFinder(30, "NW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][0][1][0][1] = new CornerStateFinder(32, "SW");
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][0][1][0][0] = new CornerStateFinder(4);
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][0][0][1][1] = new CornerStateFinder(9);
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][0][0][1][0] = new CornerStateFinder(5);
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][0][0][0][1] = new CornerStateFinder(6);
		CONNECTED_CORNER_RECIPE_LOOKUP[0][0][0][0][0][0] = new CornerStateFinder(0);
	}

}
