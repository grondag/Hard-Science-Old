package grondag.adversity.niceblock.support;

import grondag.adversity.niceblock.newmodel.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.lwjgl.opengl.GL11;

/**
 * Common event handler for NiceBlocks for things that don't belong in a block instance.
 * Currently the only thing we need to handle is the block highlight.
 */
public class NiceBlockHighlighter {

	public static final NiceBlockHighlighter instance = new NiceBlockHighlighter();

	/**
	 * Check for niceblocks that need a custom block highlight and draw if found.
	 * Adapted from the vanilla highlight code.
	 */
	@SubscribeEvent
	public void onDrawBlockHighlightEvent(DrawBlockHighlightEvent event) {
	    
        BlockPos pos = event.target.getBlockPos();
        if(pos != null && event.player != null)
        {
    		IBlockState bs = event.player.worldObj.getBlockState(pos);
    		if (bs != null && bs.getBlock() instanceof NiceBlock) {
    			NiceBlock nb = (NiceBlock) bs.getBlock();
    			if (nb.needsCustomHighlight()) {
    
    				GlStateManager.enableBlend();
    				GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    				GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
    				GL11.glLineWidth(2.0F);
    				GlStateManager.disableTexture2D();
    				GlStateManager.depthMask(false);
    				double d0 = event.player.lastTickPosX + (event.player.posX - event.player.lastTickPosX) * event.partialTicks;
    				double d1 = event.player.lastTickPosY + (event.player.posY - event.player.lastTickPosY) * event.partialTicks;
    				double d2 = event.player.lastTickPosZ + (event.player.posZ - event.player.lastTickPosZ) * event.partialTicks;
    
    				for (AxisAlignedBB aabb : nb.getSelectionBoundingBoxes(event.player.worldObj, pos, bs)) {
    					RenderGlobal.drawSelectionBoundingBox(aabb.expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D).offset(-d0, -d1, -d2));
    				}
    
    				GlStateManager.depthMask(true);
    				GlStateManager.enableTexture2D();
    				GlStateManager.disableBlend();
    				GlStateManager.enableAlpha();
    
    				event.setCanceled(true);
    			}
    		}
		}
	}
}
