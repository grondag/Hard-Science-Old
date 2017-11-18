package grondag.hard_science.superblock.placement;

import grondag.hard_science.library.world.ChunkBlockMap;
import net.minecraft.util.math.BlockPos;

public class VirtualChunk extends ChunkBlockMap<VirtualState>
{
    public VirtualChunk(BlockPos pos)
    {
        super(pos);
    }

}
