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
    
    public long getCollisionKey(IBlockState state, World worldIn, BlockPos pos);

    public List<AxisAlignedBB> getModelBounds(IBlockState state, World worldIn, BlockPos pos);
    
    /** for composite keys */
    public int getKeyBitLength();

}
