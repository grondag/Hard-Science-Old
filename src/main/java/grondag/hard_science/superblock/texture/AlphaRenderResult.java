package grondag.hard_science.superblock.texture;

/**
 * Describes if/how this texture can be rendered in alpha or cutout modes.
 * Used to select the optimal (or least bad) block render layer for each paint layer.
 */
public enum AlphaRenderResult
{
    
    /** Texture has no alpha channel or is fully opaque everywhere. 
     * Rendering in cutout or translucent render layer will give a solid texture
     * unless color is set to a transparent value.
     */
    SOLID,
    
    /**
     * Texture has an alpha channel and can be rendered in either 
     * cutout or translucent render layer. Cutout is optimal.
     */
    CUTOUT_OPTIMAL,
    
    /**
     * Texture has an alpha channel and can be rendered in either 
     * cutout or translucent render layer. Translucent is optimal.
     */
    TRANSLUCENT_OPTIMAL,
    
    /**
     * Texture has an alpha channel and can only be rendered in 
     * translucent render layer. Rendering in cutout layer will give bad results.
     */
    TRANSLUCENT_ONLY
    
}
