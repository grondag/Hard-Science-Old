package grondag.adversity.niceblock.support;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Implement to provide specialized collision handling to NiceBlock for blocks
 * with non-standard shapes.
 */
public interface ICollisionHandler {

	public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end);

	public void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity);

	public AxisAlignedBB getCollisionBoundingBox(IBlockAccess worldIn, BlockPos pos, IBlockState state);

	/**
	 * Used for rendering selection boxes when the selection box is not a cube.
	 * Boxes are derived from the block's collision hit boxes.
	 */
	public List<AxisAlignedBB> getSelectionBoundingBoxes(World worldIn, BlockPos pos, IBlockState state);

}
