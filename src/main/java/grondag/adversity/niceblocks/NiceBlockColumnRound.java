package grondag.adversity.niceblocks;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;

public class NiceBlockColumnRound extends NiceBlock{

	public NiceBlockColumnRound(String name, NiceBlockStyle style, NicePlacement placer, NiceSubstance[] substances) {
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

	// TODO
	// Add handler for DrawBlockHighlightEvent(context, player, target, subID, currentItem, partialTicks)
	
	
}
