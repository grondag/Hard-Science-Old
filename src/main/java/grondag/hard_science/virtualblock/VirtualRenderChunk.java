package grondag.hard_science.virtualblock;

import static grondag.hard_science.superblock.placement.PlacementItemRenderer.COLOR_DELETED_RGBA;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import grondag.hard_science.library.world.ChunkBlockMap;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class VirtualRenderChunk extends ChunkBlockMap<VirtualRenderEntry>
{
    public VirtualRenderChunk(BlockPos pos)
    {
        super(pos);
    }
    
    @SideOnly(Side.CLIENT)
    public void render(double d0, double d1, double d2)
    {
        if(this.isEmpty()) return;
                
        List<Pair<BlockPos, VirtualRenderEntry>> placements = this.asSortedList();
        
     
        GlStateManager.disableDepth();
        
        for(Pair<BlockPos, VirtualRenderEntry> placement : placements)
        {
            if(placement.getRight().isExcavation)
            {
                BlockPos pos = placement.getLeft();
                AxisAlignedBB blockAABB = Block.FULL_BLOCK_AABB.offset(pos.getX(), pos.getY(), pos.getZ());
                RenderGlobal.drawSelectionBoundingBox(blockAABB.offset(-d0, -d1, -d2), COLOR_DELETED_RGBA[0], COLOR_DELETED_RGBA[1], COLOR_DELETED_RGBA[2], COLOR_DELETED_RGBA[3]);
            }
        }
        
        GlStateManager.enableDepth();

    }
}
