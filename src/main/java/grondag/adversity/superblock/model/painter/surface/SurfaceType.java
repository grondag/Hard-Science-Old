package grondag.adversity.superblock.model.painter.surface;

/**
 * All surfaces in a model are assigned a surface type.
 * Each surface type in a model is assigned one or more painters.
 * Painter(s) assigned to that paint type will paint all surfaces in the model with the given type.
 */
public enum SurfaceType
{
    /** Surface that comprises most of the model. Can glow or be translucent. May or may not align with a block face. */
    MAIN,
    
    /**
     * Block-aligned faces that should be distinct from the main surface. 
     * Painters can detect which face a quad is for, so this is NOT needed to implement something like grass.
     * Typical use case is for flow block or CSG cuts of large shapes where the nominal surface
     * is defined as MAIN and the block-aligned cut surfaces are BLOCKFACE.
     */
    BLOCKFACE,
    
    /**
     * Cuts into the block that don't align with a block face.
     */
    CUT,
    
    /** 
     * Surface that should be distinct from other surfaces. 
     * Typically used for inset lamp surfaces. 
     */
    LAMP
}
