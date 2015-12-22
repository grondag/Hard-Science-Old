package grondag.adversity.niceblock;

import grondag.adversity.niceblock.support.NicePlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

public class NiceBlockColumnRound extends NiceBlock {

	public NiceBlockColumnRound(String name, NiceStyle style, NicePlacement placer, NiceSubstance[] substances) {
		super(name, style, placer, substances);
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
