package grondag.adversity.superblock.model.state;

import net.minecraft.util.BlockRenderLayer;

public class RenderLayerHelper
{
    public static int makeRenderLayerFlags(BlockRenderLayer... renderLayers)
    {
        int layerFlags = 0;
        for(BlockRenderLayer layer : renderLayers)
        {
            layerFlags |= 1 << layer.ordinal();
        }
        return layerFlags;
    }
}
