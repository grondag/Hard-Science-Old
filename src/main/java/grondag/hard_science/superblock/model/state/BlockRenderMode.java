package grondag.hard_science.superblock.model.state;

import grondag.exotic_matter.render.RenderPass;
import net.minecraft.util.BlockRenderLayer;

/**
 * Superblocks rendering characteristics for non-TESR renders.
 * Stored in block instance and determines if renders as normal block 
 * or as TESR. <br><br>
 * 
 * For normal renders, determines which layers are included and of those,
 * which are shaded or flat rendering. Flat is necessary for full brightness renders.
 * 
 * For TESR, look to RenderPassSet for rendering characteristic.
 * 
 * @author grondag
 *
 */
public enum BlockRenderMode
{
    SOLID_SHADED(RenderPass.SOLID_SHADED),
    TRANSLUCENT_SHADED(RenderPass.TRANSLUCENT_SHADED),
    BOTH_SHADED(RenderPass.SOLID_SHADED, RenderPass.TRANSLUCENT_SHADED),
    SOLID_FLAT(RenderPass.SOLID_FLAT),
    TRANSLUCENT_FLAT(RenderPass.TRANSLUCENT_FLAT),
    BOTH_FLAT(RenderPass.SOLID_FLAT, RenderPass.TRANSLUCENT_FLAT),
    SOLID_SH_TRANS_FLAT(RenderPass.SOLID_SHADED, RenderPass.TRANSLUCENT_FLAT),
    SOLID_FLAT_TRANS_SH(RenderPass.SOLID_FLAT, RenderPass.TRANSLUCENT_SHADED),
    TESR();
    
    
    /**
     * Sizes quad container - values range from 0 (empty) to 2 (both SOLID and TRANLUCENT)
     */
    public final RenderLayout renderLayout;

    public final boolean isSolidLayerFlatLighting;
    public final boolean isTranlucentLayerFlatLighting;

    private BlockRenderMode(RenderPass...passes)
    {
        this.renderLayout = new RenderLayout(passes);
        this.isSolidLayerFlatLighting = this.renderLayout.containsRenderPass(RenderPass.SOLID_FLAT);
        this.isTranlucentLayerFlatLighting = this.renderLayout.containsRenderPass(RenderPass.TRANSLUCENT_FLAT);
    }

    /**
     * True if given block render layer should be rendered with flat lighting.
     */
    public boolean isBlockLayerFlat(BlockRenderLayer layer)
    {
        switch(layer)
        {
        case SOLID:
            return this.isSolidLayerFlatLighting;
            
        case TRANSLUCENT:
            return this.isTranlucentLayerFlatLighting;
            
        default:
            return false;        
        }
    }
 }
