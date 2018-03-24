package grondag.hard_science.superblock.virtual;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import grondag.exotic_matter.ClientProxy;
import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ExcavationRenderManager
{
    private static final Int2ObjectOpenHashMap<ExcavationRenderer> excavations = new Int2ObjectOpenHashMap<ExcavationRenderer>();
    
    /**
     * Updated whenever map changes.  Can be accessed safely from render thread without
     * causing any concurrency problems because setting/accessing array value is safe.
     * Could make volatile but not really a problem if a couple frames use stale data.
     */
    private static ExcavationRenderer[] renderCopy = new ExcavationRenderer[0];
    
    /**
     * Keep reference to avoid garbage creation
     */
    @SideOnly(Side.CLIENT)
    private static final ArrayList<ExcavationRenderer> secondPass = new ArrayList<ExcavationRenderer>();
    
    @SideOnly(Side.CLIENT)
    public static void render(RenderGlobal renderGlobal, double partialTicks)
    {
        Entity player = Minecraft.getMinecraft().player;
        if(player == null) return;

        if(renderCopy == null || renderCopy.length == 0) return;
        
        ICamera camera = ClientProxy.camera();
        
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.glLineWidth(2);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        
        secondPass.clear();
        
        double d0 = ClientProxy.cameraX();
        double d1 = ClientProxy.cameraY();
        double d2 = ClientProxy.cameraZ();
        
        for(ExcavationRenderer ex : renderCopy)
        {
            if(ex.bounds() != null && camera.isBoundingBoxInFrustum(ex.bounds()))
            {
                if(ex.drawBounds(bufferbuilder, player, d0, d1, d2, (float) partialTicks)) secondPass.add(ex);
            }
        }
        
        tessellator.draw();
        
        if(!secondPass.isEmpty())
        {

            GlStateManager.glLineWidth(1);
            bufferbuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            for(ExcavationRenderer ex : secondPass)
            {
                ex.drawGrid(bufferbuilder, d0, d1, d2);
            }
            tessellator.draw();
            
            GlStateManager.enableDepth();
            
            // prevent z-fighting
            GlStateManager.enablePolygonOffset();
            GlStateManager.doPolygonOffset(-1, -1);
            
            bufferbuilder.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            for(ExcavationRenderer ex : secondPass)
            {
                ex.drawBox(bufferbuilder, d0, d1, d2);
            }
            tessellator.draw();
            
            GlStateManager.disablePolygonOffset();
        }
        
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }
    
    public static void clear()
    {
        excavations.clear();
        renderCopy = new ExcavationRenderer[0];
    }
    
    public static void addOrUpdate(ExcavationRenderer... renders)
    {
        for(ExcavationRenderer render : renders)
        {
            excavations.put(render.id, render);
        }
        renderCopy = excavations.values().toArray(new ExcavationRenderer[excavations.size()]);
        if(Configurator.logExcavationRenderTracking) Log.info("mass update, excavationSize = %d, renderSize = %d", excavations.size(), renderCopy.length);
    }
    
    public static void addOrUpdate(ExcavationRenderer render)
    {
        excavations.put(render.id, render);
        renderCopy = excavations.values().toArray(new ExcavationRenderer[excavations.size()]);
        if(Configurator.logExcavationRenderTracking) Log.info("addOrUpdate id = %d, excavationSize = %d, renderSize = %d", render.id, excavations.size(), renderCopy.length);
    }
    
    public static void remove(int id)
    {
        excavations.remove(id);
        renderCopy = excavations.values().toArray(new ExcavationRenderer[excavations.size()]);
        if(Configurator.logExcavationRenderTracking) Log.info("remove id = %d, excavationSize = %d, renderSize = %d", id, excavations.size(), renderCopy.length);
    }
}
