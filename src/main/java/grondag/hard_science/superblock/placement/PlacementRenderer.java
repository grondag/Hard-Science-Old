package grondag.hard_science.superblock.placement;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import grondag.hard_science.Configurator;
import grondag.hard_science.Configurator.Render.PreviewMode;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.varia.BlockHighlighter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PlacementRenderer
{
    public static void renderOverlay(RenderWorldLastEvent event, EntityPlayerSP player, ItemStack stack, PlacementItem item)
    {
        // abort if turned off
        if(Configurator.RENDER.previewSetting == PreviewMode.NONE) return;

        ModelState modelState = PlacementItem.getStackModelState(stack);
        
        if(modelState == null) return;
        
        Minecraft mc = Minecraft.getMinecraft();
        
        // abort if out of range
        if(ForgeHooks.rayTraceEyeHitVec(player, mc.playerController.getBlockReachDistance() + 1) == null) return;
        
        RayTraceResult target = mc.objectMouseOver;
        
        if(target.typeOfHit != RayTraceResult.Type.BLOCK) return;
        
        BlockPos pos = target.getBlockPos();
        
        if(player.world.isAirBlock(pos)) return;
        
        Vec3d hitVec = target.hitVec;
        
        float xHit = (float)(hitVec.x - (double)pos.getX());
        float yHit = (float)(hitVec.y - (double)pos.getY());
        float zHit = (float)(hitVec.z - (double)pos.getZ());
        
        List<Pair<BlockPos, ItemStack>> placements = modelState.getShape().getPlacementHandler()
                .getPlacementResults(player, player.world, target.getBlockPos(), player.getActiveHand(), target.sideHit, xHit, 
                        yHit, zHit, stack);
                 
        if(placements.isEmpty()) return;
        
        
        for(Pair<BlockPos, ItemStack> placement : placements)
        {
            ModelState placementModelState = PlacementItem.getStackModelState(placement.getRight());
            
            if(placementModelState != null)
            {
                if(Configurator.RENDER.previewSetting == PreviewMode.OUTLINE)
                {
                    BlockHighlighter.drawBlockHighlight(placementModelState, placement.getLeft(), player, event.getPartialTicks(), true);
                }
            }
        }

            
    }
}
