package grondag.hard_science.superblock.placement;

import org.apache.commons.lang3.tuple.Pair;

import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Companion class for PlacementItem that handles rendering.
 * @author grondag
 *
 */
@Deprecated
@SideOnly(Side.CLIENT)
//FIXME: remove
public class PlacementItemRenderer
{
    public static float[] COLOR_DELETED_RGBA = {1.0f, 0.3f, 0.3f, 1.0f};
    public static float[] COLOR_PLACED_RGBA = {1.0f, 1.0f, 1.0f, 1.0f};
    public static float[] COLOR_BLOCKED_RGBA = {1.0f, 1.0f, 0.3f, 1.0f};
    
    
    public static void renderOverlay(RenderWorldLastEvent event, EntityPlayerSP player, ItemStack stack, PlacementItem item)
    {
        PlacementResult result = PlacementHandler.predictPlacementResults(player, stack, item);
        result.builder().renderPreview(event, player);

        //FIXME: remove
//        double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks();
//        double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks();
//        double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks();
//
//        GlStateManager.enableBlend();
//        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//        GlStateManager.disableTexture2D();
//        GlStateManager.depthMask(false);
//        
//        
//        // render region AABB - preference given to place over deletion
//        if(result.hasPlacementAABB())
//        {
//            AxisAlignedBB regionAABB = result.placementAABB().toAABB().offset(-d0, -d1, -d2);
//            
//            boolean isBlocked = !result.hasPlacementList() && result.hasExclusionList();
//            float[] color = isBlocked ? COLOR_BLOCKED_RGBA : PlacementItem.isDeleteModeEnabled(stack) ? COLOR_DELETED_RGBA : COLOR_PLACED_RGBA;
//            // draw once without depth to show any blocked regions
//            GlStateManager.disableDepth();
//            GlStateManager.glLineWidth(1.0F);
//            RenderGlobal.drawSelectionBoundingBox(regionAABB, COLOR_BLOCKED_RGBA[0], COLOR_BLOCKED_RGBA[1], COLOR_BLOCKED_RGBA[2], COLOR_BLOCKED_RGBA[3]);
//            
//            // draw again with depth to show unblocked region
//            GlStateManager.enableDepth();
//            GlStateManager.glLineWidth(2.0F);
//            RenderGlobal.drawSelectionBoundingBox(regionAABB, color[0], color[1], color[2], color[3]);
//            
//            GlStateManager.disableDepth();
//            
//            if(isBlocked)
//            {
//                for(BlockPos pos : result.exclusions())
//                {
//                    AxisAlignedBB blockAABB = Block.FULL_BLOCK_AABB.offset(pos.getX(), pos.getY(), pos.getZ());
//                    RenderGlobal.drawSelectionBoundingBox(blockAABB.offset(-d0, -d1, -d2), color[0], color[1], color[2], color[3]);
//                }
//            }
//            else if(result.hasPlacementList())
//            {
//                for(Pair<BlockPos, ItemStack> placement : result.placements())
//                {
//                    ModelState placementModelState = PlacementItem.getStackModelState(placement.getRight());
//                    if(placementModelState == null)
//                    {
//                        // No model state, draw generic box
//                        BlockPos pos = placement.getLeft();
//                        AxisAlignedBB blockAABB = Block.FULL_BLOCK_AABB.offset(pos.getX(), pos.getY(), pos.getZ());
//                        RenderGlobal.drawSelectionBoundingBox(blockAABB.offset(-d0, -d1, -d2), color[0], color[1], color[2], color[3]);
//                    }
//                    else
//                    {
//                        // Draw collision boxes
//                        for (AxisAlignedBB blockAABB : placementModelState.collisionBoxes(placement.getLeft())) 
//                        {
//                            RenderGlobal.drawSelectionBoundingBox(blockAABB.offset(-d0, -d1, -d2), color[0], color[1], color[2], color[3]);
//                        }
//                    }
//                }
//            }
//        }
//        
//        GlStateManager.enableDepth();
//        GlStateManager.depthMask(true);
//        GlStateManager.enableTexture2D();
//        GlStateManager.disableBlend();
//        GlStateManager.enableAlpha();
    }
}
