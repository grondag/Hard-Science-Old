package grondag.hard_science.superblock.model.state;

import net.minecraft.util.BlockRenderLayer;

//FIXME should be called rendermode or something sim - does not always imply separate passes
public enum RenderPass
{
    SOLID_SHADED(BlockRenderLayer.SOLID, true),
    SOLID_FLAT(BlockRenderLayer.SOLID, false),
    TRANSLUCENT_SHADED(BlockRenderLayer.TRANSLUCENT, true),
    TRANSLUCENT_FLAT(BlockRenderLayer.TRANSLUCENT, false);
    
    
    public final BlockRenderLayer blockRenderLayer;
    public final boolean isShaded;
    
    private RenderPass(BlockRenderLayer brl, boolean isShaded)
    {
        this.blockRenderLayer = brl;
        this.isShaded = isShaded;
    }
    
    /**
     * Returns flat/shaded version of given shaded/flat value
     */
    public RenderPass flipShading()
    {
        switch(this)
        {
        case SOLID_FLAT:
            return SOLID_SHADED;
        case SOLID_SHADED:
            return SOLID_FLAT;
        case TRANSLUCENT_FLAT:
            return TRANSLUCENT_SHADED;
        case TRANSLUCENT_SHADED:
            return TRANSLUCENT_FLAT;
        default:
            return null;
        }
    }
}
