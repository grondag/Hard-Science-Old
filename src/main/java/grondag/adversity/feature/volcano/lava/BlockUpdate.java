package grondag.adversity.feature.volcano.lava;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public class BlockUpdate
{
    public final IBlockState state;
    public final BlockPos pos;
    
    public BlockUpdate(BlockPos pos, IBlockState state)
    {
        this.pos = pos;
        this.state = state;
    }
}
