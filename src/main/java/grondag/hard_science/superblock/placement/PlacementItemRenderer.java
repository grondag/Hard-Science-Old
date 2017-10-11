package grondag.hard_science.superblock.placement;

import org.apache.commons.lang3.tuple.Pair;

import grondag.hard_science.Configurator;
import grondag.hard_science.Configurator.Render.PreviewMode;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.varia.BlockHighlighter;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Companion class for PlacementItem that handles rendering.
 * @author grondag
 *
 */
@SideOnly(Side.CLIENT)
public class PlacementItemRenderer
{
    public static void renderOverlay(RenderWorldLastEvent event, EntityPlayerSP player, ItemStack stack, PlacementItem item)
    {
        PlacementResult result = IPlacementHandler.predictPlacementResults(player, stack, item);
        
        // render region AABB - preference given to place over deletion
        if(result.hasPlacementAABB())
        {
            BlockHighlighter.drawAABB(result.placementAABB(), player, 0, BlockHighlighter.COLOR_PLACEMENT, BlockHighlighter.COLOR_HIDDEN);
        }
        else if(result.hasDeletiopnAABB())
        {
            BlockHighlighter.drawAABB(result.deletionAABB(), player, 0, BlockHighlighter.COLOR_DELETION, BlockHighlighter.COLOR_HIDDEN);
        }
        
        // render individual block outlines if enabled or if more than one to be placed
        if(result.hasPlacementList() && Configurator.RENDER.previewSetting == PreviewMode.OUTLINE || result.placements().size() > 1)
        {
            for(Pair<BlockPos, ItemStack> placement : result.placements())
            {
                ModelState placementModelState = PlacementItem.getStackModelState(placement.getRight());
                
                if(placementModelState != null)
                {
                    BlockHighlighter.drawBlockHighlight(placementModelState, placement.getLeft(), player, event.getPartialTicks(), true);
                }
            }
        }
        
        //TODO: draw deletion boxes?
        
//        Minecraft mc = Minecraft.getMinecraft();
//        
//        if(PlacementItem.operationInProgress(stack) == PlacementOperation.SELECTING)
//        {
//            BlockPos startPos = PlacementItem.operationPosition(stack);
//            if(startPos == null) return;
//            
//            BlockPos endPos = null;
//            if(PlacementItem.isFloatingSelectionEnabled(stack))
//            {
//                endPos = PlacementItem.getFloatingSelectionBlockPos(stack, player);
//            }
//            
//            if(endPos == null)
//            {
//                if(ForgeHooks.rayTraceEyeHitVec(player, mc.playerController.getBlockReachDistance() + 1) == null) return;
//                
//                RayTraceResult target = mc.objectMouseOver;
//                
//                if(target.typeOfHit != RayTraceResult.Type.BLOCK) return;
//                
//                endPos = target.getBlockPos().offset(target.sideHit);
//            }
//            
//            if(endPos == null) return;
//            
//            BlockHighlighter.drawAABB(startPos, endPos, player, 0, true);
//            
//            return;
//        }
//        else if(ModPlayerCaps.isModifierKeyPressed(player, ModifierKey.CTRL_KEY))
//        {
//            BlockPos pos = null;
//            
//            // draw potential region start if ctrl key down
//            if(PlacementItem.isFloatingSelectionEnabled(stack))
//            {
//                pos = PlacementItem.getFloatingSelectionBlockPos(stack, player);
//            }
//            
//            if(pos == null)
//            {
//                if(ForgeHooks.rayTraceEyeHitVec(player, mc.playerController.getBlockReachDistance() + 1) == null) return;
//                
//                RayTraceResult target = mc.objectMouseOver;
//                
//                if(target.typeOfHit != RayTraceResult.Type.BLOCK) return;
//                
//                pos = target.getBlockPos().offset(target.sideHit);
//            }
//            
//            if(pos != null) 
//            {
//                BlockHighlighter.drawAABB(pos, pos, player, 0, true);
//                return;
//            }
//        }
//        
//        // if get to here, in placement mode - exit if turned off
//        if(Configurator.RENDER.previewSetting == PreviewMode.NONE) return;
//
//        ModelState modelState = PlacementItem.getStackModelState(stack);
//        
//        if(modelState == null) return;
//        
//        // abort if out of range
//        if(ForgeHooks.rayTraceEyeHitVec(player, mc.playerController.getBlockReachDistance() + 1) == null) return;
//        
//        RayTraceResult target = mc.objectMouseOver;
//        
//        if(target.typeOfHit != RayTraceResult.Type.BLOCK) return;
//        
//        BlockPos pos = target.getBlockPos();
//        
//        if(player.world.isAirBlock(pos)) return;
//        
//        Vec3d hitVec = target.hitVec;
//        
//        float xHit = (float)(hitVec.x - (double)pos.getX());
//        float yHit = (float)(hitVec.y - (double)pos.getY());
//        float zHit = (float)(hitVec.z - (double)pos.getZ());
//        
//        List<Pair<BlockPos, ItemStack>> placements = modelState.getShape().getPlacementHandler()
//                .getPlacementResults(player, player.world, target.getBlockPos(), player.getActiveHand(), target.sideHit, xHit, 
//                        yHit, zHit, stack);
//                 
//        if(placements.isEmpty()) return;
//        
//        
//        for(Pair<BlockPos, ItemStack> placement : placements)
//        {
//            ModelState placementModelState = PlacementItem.getStackModelState(placement.getRight());
//            
//            if(placementModelState != null)
//            {
//                if(Configurator.RENDER.previewSetting == PreviewMode.OUTLINE)
//                {
//                    BlockHighlighter.drawBlockHighlight(placementModelState, placement.getLeft(), player, event.getPartialTicks(), true);
//                }
//            }
//        }
//
//            
    }
}
