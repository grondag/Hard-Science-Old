package grondag.hard_science.superblock.model.state;


import static grondag.exotic_matter.render.RenderPass.*;

import grondag.exotic_matter.render.RenderPass;

public enum RenderPassSet
{
    SOLID_S(SOLID_SHADED),
    SOLID_F(SOLID_FLAT),
    SOLID_SF(SOLID_SHADED, SOLID_FLAT),
    TRANS_S(TRANSLUCENT_SHADED),
    TRANS_F(TRANSLUCENT_FLAT),
    TRANS_SF(TRANSLUCENT_SHADED, TRANSLUCENT_FLAT),
    
    SOLID_F_TRANS_S(SOLID_FLAT, TRANSLUCENT_SHADED),
    SOLID_F_TRANS_F(SOLID_FLAT, TRANSLUCENT_FLAT),
    SOLID_F_TRANS_SF(SOLID_FLAT, TRANSLUCENT_SHADED, TRANSLUCENT_FLAT),
    
    SOLID_S_TRANS_S(SOLID_SHADED, TRANSLUCENT_SHADED),
    SOLID_S_TRANS_F(SOLID_SHADED, TRANSLUCENT_FLAT),
    SOLID_S_TRANS_SF(SOLID_SHADED, TRANSLUCENT_SHADED, TRANSLUCENT_FLAT),
    
    SOLID_SF_TRANS_S(SOLID_SHADED, SOLID_FLAT, TRANSLUCENT_SHADED),
    SOLID_SF_TRANS_F(SOLID_SHADED, SOLID_FLAT, TRANSLUCENT_FLAT),
    SOLID_SF_TRANS_SF(SOLID_SHADED, SOLID_FLAT, TRANSLUCENT_SHADED, TRANSLUCENT_FLAT),
    NONE();
    
    public final BlockRenderMode blockRenderMode;
    
    public final RenderLayout renderLayout;
    
    /**
     * Used by builder to know if needs to consume minimal glow ingredient.
     */
    public final boolean hasFlatRenderPass;
    
    private RenderPassSet(RenderPass... passes)
    {
        this.renderLayout = new RenderLayout(passes);
        
        this.hasFlatRenderPass = this.renderLayout.containsRenderPass(SOLID_FLAT) || this.renderLayout.containsRenderPass(TRANSLUCENT_FLAT);
        
        // if no block render mode matches then must be rendered as TESR
        BlockRenderMode brm = BlockRenderMode.TESR;
        for(BlockRenderMode mode : BlockRenderMode.values())
        {
            if(mode.renderLayout.renderPassFlags == this.renderLayout.renderPassFlags)
            {
                brm = mode;
                break;
            }
        }
        this.blockRenderMode = brm;
    }
  
    public boolean canRenderAsNormalBlock()
    {
        return this.blockRenderMode != BlockRenderMode.TESR;
    }
    
    private static class Finder
    {
        private static final RenderPassSet[] LOOKUP = new RenderPassSet[RenderPassSet.values().length];
        
        static
        {
            for(RenderPassSet set : RenderPassSet.values())
            {
                LOOKUP[set.renderLayout.renderPassFlags] = set;
            }
        }
    }

    /**
     * Use BENUMSET_RENDER_PASS to compute flags.
     */
    public static RenderPassSet findByFlags(int flags)
    {
        return Finder.LOOKUP[flags];
    }
}
