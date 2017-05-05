package grondag.adversity.superblock.model.layout;

public enum SurfaceLayer
{
    /** 
     * Textures the entire shape.  
     * Must always be present.
     * Inner-most Z-position when other layers are present.
     * Individual quads can be solid or translucent, shaded or lit.
     * May be compatible with limited shapes.
     * Generally no dependencies.
     * Has one or more colors.
     */
    BASE,
    
    /**
     * Textures surfaces that are "cut" into the shape. 
     * Acts identical to a base layer otherwise.
     */
    CUT,
    
    
    /**
     * Provides color, dirt or other character to a base layer.
     * Optional.
     * Middle z-position when other layers are present.
     * Will generally be partial quads, translucent or clipped.
     * Individual quads can be solid or translucent, shaded or lit.
     * Has one or more colors.
     * May be compatible with limited shapes.
     * May be compatible with limited base layers.
     */
    DETAIL,
    
    OVERLAY
}
