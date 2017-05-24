package grondag.adversity.superblock.model.state;

import net.minecraft.util.BlockRenderLayer;

public class RenderLayerHelper
{
    public static final int TRANSLUCENT_FLAG = 1 << BlockRenderLayer.TRANSLUCENT.ordinal(); 
    public static final int CUTOUT_FLAG = 1 << BlockRenderLayer.CUTOUT.ordinal(); 
    public static final int CUTOUT_MIPPED_FLAG = 1 << BlockRenderLayer.CUTOUT_MIPPED.ordinal(); 
    public static final int SOLID_FLAG = 1 << BlockRenderLayer.SOLID.ordinal(); 
    
    public static int makeRenderLayerFlags(BlockRenderLayer... renderLayers)
    {
        int layerFlags = 0;
        for(BlockRenderLayer layer : renderLayers)
        {
            layerFlags |= 1 << layer.ordinal();
        }
        return layerFlags;
    }
    
    /** 
     * Retrieves the most transparent render layer from a flag value
     * earlier returned from makeRenderLayerFlags
     * Handles alpha as "most" transparent, but in practice only one non-solid
     * layer is used for a given texture.
     */
    public static BlockRenderLayer getMostTransparentLayerFromFlags(int layerFlags)
    {
        if((layerFlags & TRANSLUCENT_FLAG)  == TRANSLUCENT_FLAG) return BlockRenderLayer.TRANSLUCENT;
        if((layerFlags & CUTOUT_MIPPED_FLAG)  == CUTOUT_MIPPED_FLAG) return BlockRenderLayer.CUTOUT_MIPPED;
        if((layerFlags & CUTOUT_FLAG)  == CUTOUT_FLAG) return BlockRenderLayer.CUTOUT;
        return BlockRenderLayer.SOLID;
    }
}
