package grondag.hard_science.library.world;

import net.minecraft.util.math.BlockPos.MutableBlockPos;

/**
 * Iterators for multi-block regions.
 * Mainly used for species detection.
 */
public interface IBlockRegion
{
    /** All positions on the surface of the region.*/
    public Iterable<MutableBlockPos> surfacePositions();
    
    /** All positions adjacent to the surface of the region. */
    public Iterable<MutableBlockPos> adjacentPositions();
}
