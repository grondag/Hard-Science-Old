package grondag.adversity.niceblock;

import grondag.adversity.niceblock.newmodel.BaseMaterial;
import grondag.adversity.niceblock.newmodel.NiceBlock;
import grondag.adversity.niceblock.support.NicePlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * For blocks that don't fully occlude neighbors and require special hit boxes.
 */
public class NiceBlockNonCubic extends NiceBlock {

	public NiceBlockNonCubic(NiceStyle style, NicePlacement placer, BaseMaterial material, int metaCount) {
		super(style, placer, material, metaCount);
	}

	@Override
	public boolean isNormalCube() {
		return false;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockAccess world, BlockPos pos) {
		return false;
	}

	@Override
	public boolean isFullBlock() {
		return false;
	}

	@Override
	public boolean isFullCube() {
		return false;
	}

	@Override
	public boolean needsCustomHighlight() {
		return true;
	}

}
