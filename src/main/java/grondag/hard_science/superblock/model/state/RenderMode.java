package grondag.hard_science.superblock.model.state;

import net.minecraft.util.BlockRenderLayer;

/**
 * Combinations of BlockRenderLayer and LightingMode used by SuperBlock rendering.
 */
public enum RenderMode
{
    SOLID_SHADED(BlockRenderLayer.SOLID, false),
    SOLID_GLOW(BlockRenderLayer.SOLID, true),
    CUTOUT_SHADED(BlockRenderLayer.CUTOUT_MIPPED, false),
    CUTOUT_GLOW(BlockRenderLayer.CUTOUT_MIPPED, true),
    TRANSLUCENT_SHADED(BlockRenderLayer.TRANSLUCENT, false),
    TRANSLUCENT_GLOW(BlockRenderLayer.TRANSLUCENT, true);
    
    public final BlockRenderLayer renderLayer;
    public final boolean isFullBrightness;
    
    private RenderMode(BlockRenderLayer layer, boolean isFullBrightness)
    {
        this.renderLayer = layer;
        this.isFullBrightness = isFullBrightness;
    }
}
