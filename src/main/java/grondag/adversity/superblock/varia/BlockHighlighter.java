package grondag.adversity.superblock.varia;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import grondag.adversity.Configurator;
import grondag.adversity.superblock.block.SuperBlock;


public class BlockHighlighter 
{

	/**
	 * Check for blocks that need a custom block highlight and draw if found.
	 * Adapted from the vanilla highlight code.
	 */
	public static void handleDrawBlockHighlightEvent(DrawBlockHighlightEvent event) 
	{
	    
        BlockPos pos = event.getTarget().getBlockPos();
        if(pos != null && event.getPlayer() != null)
        {
    		IBlockState bs = event.getPlayer().world.getBlockState(pos);
    		if (bs != null && bs.getBlock() instanceof SuperBlock) {
    		    SuperBlock nb = (SuperBlock) bs.getBlock();
    
		        GlStateManager.enableBlend();
	            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
	            GlStateManager.glLineWidth(2.0F);
	            GlStateManager.disableTexture2D();
	            GlStateManager.depthMask(false);
				
				double d0 = event.getPlayer().lastTickPosX + (event.getPlayer().posX - event.getPlayer().lastTickPosX) * event.getPartialTicks();
				double d1 = event.getPlayer().lastTickPosY + (event.getPlayer().posY - event.getPlayer().lastTickPosY) * event.getPartialTicks();
				double d2 = event.getPlayer().lastTickPosZ + (event.getPlayer().posZ - event.getPlayer().lastTickPosZ) * event.getPartialTicks();

				// Draw collision boxes
				for (AxisAlignedBB aabb : nb.getSelectionBoundingBoxes(event.getPlayer().world, pos, bs)) {
					RenderGlobal.drawSelectionBoundingBox(aabb.expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D).offset(-d0, -d1, -d2), 0.0F, 0.0F, 0.0F, 0.4F);
				}

				// Debug Feature: draw outline of block boundaries for non-square blocks
				if(Configurator.RENDER.debugDrawBlockBoundariesForNonCubicBlocks)
				{
                    AxisAlignedBB aabb = new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(pos.getX(), pos.getY(), pos.getZ());
                    RenderGlobal.drawSelectionBoundingBox(aabb.expand(0.002D, 0.002D, 0.002D).offset(-d0, -d1, -d2), 0.8F, 1.0F, 1.0F, 0.3F);
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