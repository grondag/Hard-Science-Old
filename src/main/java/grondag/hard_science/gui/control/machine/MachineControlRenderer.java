package grondag.hard_science.gui.control.machine;

import org.lwjgl.opengl.GL11;

import grondag.hard_science.init.ModModels;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class MachineControlRenderer
{
    public static class RenderBounds
    {
        public final double left;
        public final double top;
        public final double width;
        public final double height;
        public double right() { return left + width; }
        public double bottom() { return top + height; }
        
        public RenderBounds (double left, double top, double width, double height)
        {
            this.left = left;
            this.top = top;
            this.height = height;
            this.width = width;
        }
        
        public RenderBounds (double left, double top, double size)
        {
            this(left, top, size, size);
        }
    }
    
    public static final RenderBounds BOUNDS_ON_OFF = new RenderBounds(1.0-(0.15), 0.02, 0.14);
    public static final RenderBounds BOUNDS_NAME = new RenderBounds(0.18, 0.04, 0.5, 0.12);
    public static final RenderBounds BOUNDS_SYMBOL = new RenderBounds(0.03, 0.03, 0.12, 0.12);
    
    public static class BinaryGlTexture 
    {
        public final int trueTextureID;
        public final int falseTextureID;
        
        public BinaryGlTexture(int trueTextureID, int falseTextureID)
        {
            this.trueTextureID = trueTextureID;
            this.falseTextureID = falseTextureID;
        }
        
        public int apply(boolean selector)
        {
            return selector ? trueTextureID : falseTextureID;
        }
    }
    
    /**
     * Alpha is 0-255
     */
    public static void renderBinaryTexture(Tessellator tessellator, BufferBuilder buffer, RenderBounds bounds, BinaryGlTexture texture, boolean selector, int alpha)
    {
        renderTextureInBounds(tessellator, buffer, bounds, texture.apply(selector), alpha);
    }
    
    public static void renderBinaryTexture(RenderBounds bounds, BinaryGlTexture texture, boolean selector, int alpha)
    {
        renderTextureInBounds(bounds, texture.apply(selector), alpha);
    }
    
    /**
     * Use {@link #renderMachineText(Tessellator, BufferBuilder, RenderBounds, String, int)}
     * when you already have tessellator and buffer on the stack.
     */
    public static void renderMachineText(RenderBounds bounds, String text, int alpha)
    {
        Tessellator tes = Tessellator.getInstance();
        renderMachineText(tes, tes.getBuffer(), bounds, text, alpha);
    }
    
    /**
     * Use this version when you already have tessellator/buffer references on the stack.
     */
    public static void renderMachineText(Tessellator tessellator, BufferBuilder buffer, RenderBounds bounds, String text, int alpha)
    {
        ModModels.FONT_ORBITRON.drawLine(bounds.left, bounds.top, text, bounds.height, 0f, 255, 255, 255, alpha); 
    }
    
    /**
     * Use {@link #renderTextureInBounds(Tessellator, BufferBuilder, RenderBounds, int, int)}
     * when you already have tessellator/buffer references on the stack.
     */
    public static void renderTextureInBounds(RenderBounds bounds, int glTextureID, int alpha)
    {
        Tessellator tes = Tessellator.getInstance();
        renderTextureInBounds(tes, tes.getBuffer(), bounds, glTextureID, alpha);
    }
    
    /**
     * Use this version when you already have tessellator/buffer references on the stack.
     */
    public static void renderTextureInBounds(Tessellator tessellator, BufferBuilder buffer, RenderBounds bounds, int glTextureID, int alpha)
    {
        GlStateManager.bindTexture(glTextureID);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);        
        bufferControlQuad(buffer, bounds.left, bounds.top, bounds.right(), bounds.bottom(), 
                0, 0, 0, 1, 1, alpha, 0xFF, 0xFF, 0xFF);
        tessellator.draw();
    }
    
    /**
     * Renders a textured quad on the face of the machine.
     * x,y coordinates are 0-1 position on the face.  0,0 is upper left.
     * u,v coordinate are also 0-1 within the currently bound texture.
     * Always full brightness.
     */
    public static void bufferControlQuad
    (
            BufferBuilder buffer, 
            double xMin, double yMin, double xMax, double yMax, 
            double depth,
            double uMin, double vMin, double uMax, double vMax,
            int alpha, int red, int green, int blue)
    {
        buffer.pos(xMin, yMin, depth).color(red, green, blue, alpha).tex(uMin, vMin).lightmap(0x00f0, 0x00f0).endVertex();
        buffer.pos(xMin, yMax, depth).color(red, green, blue, alpha).tex(uMin, vMax).lightmap(0x00f0, 0x00f0).endVertex();
        buffer.pos(xMax, yMax, depth).color(red, green, blue, alpha).tex(uMax, vMax).lightmap(0x00f0, 0x00f0).endVertex();
        buffer.pos(xMax, yMin, depth).color(red, green, blue, alpha).tex(uMax, vMin).lightmap(0x00f0, 0x00f0).endVertex();
    }
}
