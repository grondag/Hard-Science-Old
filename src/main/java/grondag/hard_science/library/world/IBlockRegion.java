package grondag.hard_science.library.world;

import net.minecraft.util.math.BlockPos.MutableBlockPos;

/**
 * Iterators for multi-block regions.
 */
public interface IBlockRegion
{

    /** All positions contained in the region, including interior positions if it is hollow */
    public Iterable<MutableBlockPos> allPositions();
    
    /** All positions on the surface of the region. Will be same as {@link #allPositions()} if region is not at least 3x3x3 */
    public Iterable<MutableBlockPos> surfacePositions();
    
    /** Positions that belong the region, excluding interior positions if hollow, but not excluding any excluded positions. */
    public Iterable<MutableBlockPos> positions();
    
    /** All positions on the surface of the region. Will be same as {@link #allPositions()} if region is not at least 3x3x3 */
    public Iterable<MutableBlockPos> adjacentPositions();
    
    /**
     * All positions included in the region. Excludes interior positions if hollow, and excludes any excluded positions.
     */
    public Iterable<MutableBlockPos> includedPositions();
        
}
