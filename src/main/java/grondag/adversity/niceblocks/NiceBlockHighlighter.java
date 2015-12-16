package grondag.adversity.niceblocks;

import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NiceBlockHighlighter {
	
	public static final NiceBlockHighlighter instance = new NiceBlockHighlighter();
	
	@SubscribeEvent
	public void onDrawBlockHighlightEvent(DrawBlockHighlightEvent event) {
		IBlockState bs = event.player.worldObj.getBlockState(event.target.getBlockPos());
		if(bs.getBlock() instanceof NiceBlock){
			NiceBlock nb = (NiceBlock)bs.getBlock();
			if (nb.needsCustomHighlight()){
		        
	            GlStateManager.enableBlend();
	            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
	            GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
	            GL11.glLineWidth(2.0F);
	            GlStateManager.disableTexture2D();
	            GlStateManager.depthMask(false);
	            float f = 0.002F;

                double d0 = event.player.lastTickPosX + (event.player.posX - event.player.lastTickPosX) * (double)event.partialTicks;
                double d1 = event.player.lastTickPosY + (event.player.posY - event.player.lastTickPosY) * (double)event.partialTicks;
                double d2 = event.player.lastTickPosZ + (event.player.posZ - event.player.lastTickPosZ) * (double)event.partialTicks;
                
		        for(AxisAlignedBB aabb : nb.getSelectionBoundingBoxes(event.player.worldObj, event.target.getBlockPos(), bs)){
		        	event.context.func_181561_a(aabb.expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D).offset(-d0, -d1, -d2));
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
