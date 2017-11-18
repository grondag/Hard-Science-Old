package grondag.hard_science.virtualblock;

import grondag.hard_science.library.world.ChunkMap;
import grondag.hard_science.library.world.WorldMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class VirtualRenderTracker extends WorldMap<ChunkMap<VirtualRenderChunk>>
{
    /**
     * 
     */
    private static final long serialVersionUID = -4740641406707843687L;
    

    @Override
    protected ChunkMap<VirtualRenderChunk> load(int dimension)
    {
        return new ChunkMap<VirtualRenderChunk>() {

            @Override
            protected VirtualRenderChunk newEntry(BlockPos pos)
            {
                return new VirtualRenderChunk(pos);
            }};
    }
    
    @SideOnly(Side.CLIENT)
    public void render(RenderGlobal renderGlobal, double partialTicks)
    {
        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        if(entity == null) return;
        
        ICamera camera = new Frustum();
        double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
        camera.setPosition(d0, d1, d2);
        
        
        ChunkMap<VirtualRenderChunk> chunks = this.get(entity.world);
        if(chunks == null) return;
        
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        
        for(VirtualRenderChunk chunk : chunks)
        {
            if(camera.isBoundingBoxInFrustum(chunk.chunkAABB))
            {
                chunk.render(d0, d1, d2);
            }
        }
        
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }

    public void addPlacement(World world, BlockPos placedPos, ItemStack placedStack)
    {
        this.get(world).getOrCreate(placedPos).put(placedPos, new VirtualRenderEntry(placedStack));
    }
}
