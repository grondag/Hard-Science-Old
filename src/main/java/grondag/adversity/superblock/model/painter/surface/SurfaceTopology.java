package grondag.adversity.superblock.model.painter.surface;

/**
 * Used by painters to know how to UV map a surface for painting.
 */
public enum SurfaceTopology
{
    /** 
     * Surface represents six faces of a single block.
     * Each quad in this surface will be tagged with a side
     * and will have raw UV values in the range 0 - 1.
     */
    CUBIC,
    
    /** 
     * Surface represents a continuous surface that is sliced into
     * quads that are each one block high and wide.
     * Quads tile in both dimensions and can extend for any distance.
     * Raw UV values can be any positive or negative value, with 
     * integers representing block edges.  
     * For example, a quad with UV box of -1, -1, 2, 2 would represent
     * a 3x3 tiled surface and should generate at least 9 painted quads.
     */
    TILED,
 
    /**
     * Similar to TILED, except that tiles must wrap in one dimension.
     * "Wrap" means tiles must join with self without any seam.
     * 
     * Length of 1.0 in UV coordinates as generated by shape will be
     * square but will only be *approximately* one block-width in world.
     * Thus, textures applied by painter may appear slightly stretched or compressed in world.
     * 
     * Min U or V be 0 and max of same will be an integer.
     */
    CYLINDRICAL,
    
    /**
     * Texture wraps in both dimensions.  
     * 
     * Length of 1.0 in UV coordinates as generated by shape will be
     * square but will only be *approximately* one block-width in world.
     * Thus, textures applied by painter may appear slightly stretched or compressed in world.
     * 
     * Min UV will be 0,0 and Max UV will be integers.
     */
    TOROIDAL
    
}