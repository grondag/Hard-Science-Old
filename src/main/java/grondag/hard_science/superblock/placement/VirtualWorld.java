package grondag.hard_science.superblock.placement;

import javax.annotation.Nullable;

import grondag.hard_science.library.world.ChunkMap;
import net.minecraft.util.math.BlockPos;

public class VirtualWorld extends ChunkMap<VirtualChunk>
{
    public final int dimensionID;
    
    public VirtualWorld(int dimensionID)
    {
        super();
        this.dimensionID = dimensionID;
    }
    
    @Override
    protected VirtualChunk newEntry(BlockPos pos)
    {
        return new VirtualChunk(pos);
    }
    
    /**
     * Returns null if state is not loaded from world.
     */
    @Nullable
    public VirtualState getVirtualState(BlockPos pos)
    {
        VirtualChunk chunk = this.getIfExists(pos);
        return chunk == null ? null : chunk.get(pos);
    }

   
}