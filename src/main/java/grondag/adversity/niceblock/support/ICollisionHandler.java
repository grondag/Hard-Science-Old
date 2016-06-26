package grondag.adversity.niceblock.support;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Implement to provide specialized collision handling to NiceBlock for blocks
 * with non-standard shapes.
 */
public interface ICollisionHandler {
    
    public long getCollisionKey(World worldIn, BlockPos pos, IBlockState state);

    public List<AxisAlignedBB> getModelBounds(long collisionKey);

}
