package grondag.hard_science.superblock.block;

import grondag.hard_science.superblock.model.state.RenderLayout;
import net.minecraft.util.BlockRenderLayer;

/**
 * Only purpose is to exclude tile entities that don't need TESR
 * from chunk rendering loop.
 */
public class SuperTileEntityTESR extends SuperTileEntity
{
    /**
     * Determines which pass(es) TESR should render
     */
    protected boolean renderSolid = false;
    protected boolean renderTranslucent = false;
    
    @Override
    public boolean shouldRenderInPass(int pass)
    {
        return pass == 0 ? this.renderSolid : this.renderTranslucent;
    }
    
    @Override
    protected void onModelStateChange(boolean refreshClientRenderState)
    {
        super.onModelStateChange(refreshClientRenderState);
        
        if(this.modelState != null) 
        {
            RenderLayout renderLayout = modelState.getRenderPassSet().renderLayout;
            this.renderSolid = renderLayout.containsBlockRenderLayer(BlockRenderLayer.SOLID);
            this.renderTranslucent = renderLayout.containsBlockRenderLayer(BlockRenderLayer.TRANSLUCENT);
        }
    }
}
