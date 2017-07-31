package grondag.hard_science.superblock.model.state;

import net.minecraft.util.BlockRenderLayer;

/**
 * Combinations of BlockRenderLayer (normal block rendering) and TESR rendering used for SuperBlock rendering.
 */
public enum RenderMode
{
    SOLID_SHADED(BlockRenderLayer.SOLID, false),
    SOLID_TESR(BlockRenderLayer.SOLID, true),
//    CUTOUT_SHADED(BlockRenderLayer.CUTOUT_MIPPED, false),
//    CUTOUT_TESR(BlockRenderLayer.CUTOUT_MIPPED, true),
    TRANSLUCENT_SHADED(BlockRenderLayer.TRANSLUCENT, false),
    TRANSLUCENT_TESR(BlockRenderLayer.TRANSLUCENT, true);
    
    public final BlockRenderLayer renderLayer;
    public final boolean needsTESR;
    
    private RenderMode(BlockRenderLayer layer, boolean needsTESR)
    {
        this.renderLayer = layer;
        this.needsTESR = needsTESR;
    }

    public RenderMode withTESR()
    {
        switch(this)
        {
//        case CUTOUT_SHADED:
//            return CUTOUT_TESR;
            
        case SOLID_SHADED:
            return SOLID_TESR;
            
        case TRANSLUCENT_SHADED:
            return TRANSLUCENT_TESR;
            
//        case CUTOUT_TESR:
        case SOLID_TESR:
        case TRANSLUCENT_TESR:
        default:
            return this;
        
        }
    }

    public static RenderMode find(BlockRenderLayer brl, boolean needsTESR)
    {
        switch(brl)
        {
        case CUTOUT:
        case CUTOUT_MIPPED:
        case SOLID:
        default:
            return needsTESR ? SOLID_TESR : SOLID_SHADED;
            
        case TRANSLUCENT:
            return needsTESR ? TRANSLUCENT_TESR : TRANSLUCENT_SHADED;
        
        }
      
    }
}
