package grondag.hard_science.superblock.model.state;


import static grondag.hard_science.superblock.model.state.RenderPass.*;

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
    
    private RenderPassSet(RenderPass... passes)
    {
        this.renderLayout = new RenderLayout(passes);
        
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
