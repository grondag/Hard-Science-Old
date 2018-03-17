package grondag.hard_science.library.refractory;

import grondag.hard_science.superblock.virtual.VirtualBlock;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class WorldHelperLeftovers
{

    /**
     * Convenience method to keep code more readable.
     * Call with replaceVirtualBlocks = true to behave as if virtual blocks not present.
     * Should generally be true if placing a normal block.
     */
    public static boolean isBlockReplaceable(IBlockAccess worldIn, BlockPos pos, boolean replaceVirtualBlocks)
    {
        if(replaceVirtualBlocks)
        {
            return worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos);
        }
        else
        {
            Block block = worldIn.getBlockState(pos).getBlock();
            return !VirtualBlock.isVirtualBlock(block) && block.isReplaceable(worldIn, pos);
        }
        
    }
}
