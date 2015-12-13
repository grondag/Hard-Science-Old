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

	@Override
    public void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity)
    {
        AxisAlignedBB axisalignedbb = this.getCollisionBoundingBox(worldIn, pos, state);

        if (axisalignedbb != null && mask.intersectsWith(axisalignedbb))
        {
            list.add(axisalignedbb);
        }
    }

	@Override
    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state)
    {
        return new AxisAlignedBB((double)pos.getX() + 0.4, (double)pos.getY() + 0.0, (double)pos.getZ() + 0.4, (double)pos.getX() + 0.6, (double)pos.getY() + 1.0, (double)pos.getZ() + 0.6);
    }
	
	// TODO
	// Add handler for DrawBlockHighlightEvent(context, player, target, subID, currentItem, partialTicks)
	
	
}
