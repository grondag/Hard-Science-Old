package grondag.hard_science.virtualblock;

import java.util.ArrayList;

import grondag.hard_science.library.world.WorldMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ExcavationRenderTracker extends WorldMap<Int2ObjectOpenHashMap<ExcavationRenderEntry>>
{

    /**
     * 
     */
    private static final long serialVersionUID = 3622879833810354529L;
    
    public static final ExcavationRenderTracker INSTANCE = new ExcavationRenderTracker();

    @Override
    protected Int2ObjectOpenHashMap<ExcavationRenderEntry> load(int dimension)
    {
        return new Int2ObjectOpenHashMap<ExcavationRenderEntry>();
    }
    
    public synchronized void add(EntityPlayer player, ExcavationRenderEntry entry)
    {
        this.get(player.world).put(entry.id, entry);
    }
    
    /**
     * Keep reference to avoid garbage creation
     */
    private static final ArrayList<ExcavationRenderEntry> secondPass = new ArrayList<ExcavationRenderEntry>();
    
    public void render(RenderGlobal renderGlobal, double partialTicks)
    {
        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        if(entity == null) return;

        Int2ObjectOpenHashMap<ExcavationRenderEntry> excavations = this.get(entity.world);
        if(excavations == null || excavations.isEmpty()) return;
        
        ICamera camera = new Frustum();
        double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
        camera.setPosition(d0, d1, d2);
        
        
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        
        secondPass.clear();
        
        for(ExcavationRenderEntry ex : excavations.values())
        {
            if(camera.isBoundingBoxInFrustum(ex.aabb))
            {
                if(ex.drawBounds(bufferbuilder, entity, d0, d1, d2, (float) partialTicks)) secondPass.add(ex);
            }
        }
        
        tessellator.draw();
        
        GlStateManager.enableDepth();

        if(!secondPass.isEmpty())
        {
            // prevent z-fighting
            GlStateManager.enablePolygonOffset();
            GlStateManager.doPolygonOffset(-1, -1);
            
            bufferbuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);
            for(ExcavationRenderEntry ex : secondPass)
            {
                ex.drawBox(bufferbuilder, entity, d0, d1, d2, (float) partialTicks);
            }
            tessellator.draw();
            
            GlStateManager.disablePolygonOffset();
        }
        
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }
}
