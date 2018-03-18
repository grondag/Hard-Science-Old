package grondag.hard_science.superblock.varia;

import grondag.exotic_matter.ConfigXM;
import grondag.exotic_matter.model.ISuperBlock;
import grondag.exotic_matter.model.ISuperModelState;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;


public class BlockHighlighter 
{
	/**
	 * Check for blocks that need a custom block highlight and draw if checked.
	 * Adapted from the vanilla highlight code.
	 */
	public static void handleDrawBlockHighlightEvent(DrawBlockHighlightEvent event) 
	{
        BlockPos pos = event.getTarget().getBlockPos();
        if(pos != null && event.getPlayer() != null)
        {
            World world = event.getPlayer().world;
    		IBlockState bs = world.getBlockState(pos);
    		if (bs != null && bs.getBlock() instanceof ISuperBlock) 
    		{
    		    ISuperBlock block = (ISuperBlock) bs.getBlock();
    		    ISuperModelState modelState = block.getModelStateAssumeStateIsCurrent(bs, world, pos, true);
    		    drawBlockHighlight(modelState, pos, event.getPlayer(), event.getPartialTicks(), false);
				event.setCanceled(true);
    		}
		}
	}
	
	public static final float[] COLOR_HIGHLIGHT = {0.6f, 0, 0, 0};
	public static final float[] COLOR_PREVIEW = {1, 1, 1, 1};
	public static final float[] COLOR_HIDDEN = {1, 1, 1, 0};
	public static final float[] COLOR_PLACEMENT = {1, 0, 1, 1};
	public static final float[] COLOR_DELETION = {1, 1, 0, 1};
	
	public static void drawBlockHighlight(ISuperModelState modelState, BlockPos pos, EntityPlayer player, float partialTicks, boolean isPreview)
	{
	    double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
	    double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
	    double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
	    
	    float[] colorARGBfloat = isPreview ? COLOR_PREVIEW : COLOR_HIGHLIGHT;

	    if(isPreview) GlStateManager.disableDepth();
	    GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        // Draw collision boxes
        for (AxisAlignedBB aabb : modelState.collisionBoxes(pos)) 
        {
            if(!isPreview) aabb = aabb.expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D);
            RenderGlobal.drawSelectionBoundingBox(aabb.offset(-d0, -d1, -d2), colorARGBfloat[1], colorARGBfloat[2], colorARGBfloat[3], colorARGBfloat[0]);
        }

        // Debug Feature: draw outline of block boundaries for non-square blocks
        if(ConfigXM.RENDER.debugDrawBlockBoundariesForNonCubicBlocks)
        {
            AxisAlignedBB aabb = Block.FULL_BLOCK_AABB.offset(pos.getX(), pos.getY(), pos.getZ());
            if(!isPreview) aabb = aabb.expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D);
            RenderGlobal.drawSelectionBoundingBox(aabb.offset(-d0, -d1, -d2), 0.8F, 1.0F, 1.0F, 0.3F);
        }
        
        if(isPreview) GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
	}
	
	/*
	 * If hiddenColor is provided, occluded part of box will still be drawn - in that color.
	 */
	public static void drawAABB(AxisAlignedBB aabb, EntityPlayer player, float partialTicks, float[] colorARGB, float[] hiddenColorARGB)
    {
        double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
        

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
       
        aabb = aabb.expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D);

        if(hiddenColorARGB != null) 
        {
            GlStateManager.glLineWidth(1.0F);
            GlStateManager.disableDepth();
            RenderGlobal.drawSelectionBoundingBox(aabb.offset(-d0, -d1, -d2), hiddenColorARGB[1], hiddenColorARGB[2], hiddenColorARGB[3], hiddenColorARGB[0]);
            GlStateManager.enableDepth();
        }
        
        GlStateManager.glLineWidth(2.0F);
        RenderGlobal.drawSelectionBoundingBox(aabb.offset(-d0, -d1, -d2), colorARGB[1], colorARGB[2], colorARGB[3], colorARGB[0]);
        
        
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }
}
