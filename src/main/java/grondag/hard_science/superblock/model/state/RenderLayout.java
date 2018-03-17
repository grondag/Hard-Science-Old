package grondag.hard_science.superblock.model.state;

import java.util.List;

import grondag.exotic_matter.varia.BinaryEnumSet;
import net.minecraft.util.BlockRenderLayer;

/**
 * Allows static access during enum initialization.
 *
 */
public class RenderLayout
{
    public static final BinaryEnumSet<RenderPass> BENUMSET_RENDER_PASS = new BinaryEnumSet<RenderPass>(RenderPass.class);
    public static final BinaryEnumSet<BlockRenderLayer> BENUMSET_BLOCK_RENDER_LAYER = new BinaryEnumSet<BlockRenderLayer>(BlockRenderLayer.class);
    
    public static int blockRenderLayerFlagsFromRenderPasses(RenderPass...passes)
    {
        int flags = 0;
        for(RenderPass pass : passes)
        {
            flags = BENUMSET_BLOCK_RENDER_LAYER.setFlagForValue(pass.blockRenderLayer, flags, true);
        }
        return flags;
    }
    
    /** 
     * How many render passes in this set.
     */
    public final int renderPassCount;
    
    public final int renderPassFlags;

    public final List<RenderPass> renderPassList;

    /**
     * Sizes quad container - values range from 0 (empty) to 2 (both SOLID and TRANLUCENT)
     */
    public final int blockLayerCount;
    
    public final int blockLayerFlags;
    
    public final List<BlockRenderLayer> blockLayerList;
    
    private final int[] blockLayerContainerIndexes = new int[BlockRenderLayer.values().length];
    
    private final BlockRenderLayer[] containerLayers;
    
    public RenderLayout(RenderPass...passes)
    {
        this.renderPassFlags = BENUMSET_RENDER_PASS.getFlagsForIncludedValues(passes);;
        this.renderPassList =  BENUMSET_RENDER_PASS.getValuesForSetFlags(this.renderPassFlags);
        this.renderPassCount = this.renderPassList.size();
        
        this.blockLayerFlags = blockRenderLayerFlagsFromRenderPasses(passes);
        this.blockLayerList =  BENUMSET_BLOCK_RENDER_LAYER.getValuesForSetFlags(this.blockLayerFlags);
        this.blockLayerCount = this.blockLayerList.size();
        
        this.containerLayers = new BlockRenderLayer[this.blockLayerCount];
                
        int nextContainerIndex = 0;
        for(BlockRenderLayer layer : BlockRenderLayer.values())
        {
            if(this.containsBlockRenderLayer(layer))
            {
                this.containerLayers[nextContainerIndex] = layer;
                this.blockLayerContainerIndexes[layer.ordinal()] = nextContainerIndex++;
            }
            else
            {
                this.blockLayerContainerIndexes[layer.ordinal()] = -1;
            }
        }
    }
    
    public boolean containsRenderPass(RenderPass pass)
    {
        return BENUMSET_RENDER_PASS.isFlagSetForValue(pass, this.renderPassFlags);
    }

    public boolean containsBlockRenderLayer(BlockRenderLayer layer)
    {
        return BENUMSET_BLOCK_RENDER_LAYER.isFlagSetForValue(layer, this.blockLayerFlags);
    }
    
    /**
     * If block layer is present returns container index (0 or 1) where quads for the layer should be kept.
     * Returns -1 if layer not present.
     */
    public int containerIndexFromBlockRenderLayer(BlockRenderLayer layer)
    {
        return this.blockLayerContainerIndexes[layer.ordinal()];
    }
    
    /** 
     * Convenience method 
     */
    public int containerIndexFromRenderPass(RenderPass pass)
    {
        return this.containerIndexFromBlockRenderLayer(pass.blockRenderLayer);
    }
    
    /**
     * Returns null if given container index is out of range.
     * @return 
     */
    public BlockRenderLayer BlockRenderLayerFromContainerIndex(int containerIndex)
    {
        if(containerIndex < 0 || containerIndex >= this.blockLayerCount)
        {
            return null;
        }
        else
        {
            return this.containerLayers[containerIndex];
        }
    }
}
