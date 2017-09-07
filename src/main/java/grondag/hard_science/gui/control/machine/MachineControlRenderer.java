package grondag.hard_science.gui.control.machine;

import java.util.function.DoubleUnaryOperator;
import org.lwjgl.opengl.GL11;

import grondag.hard_science.gui.control.machine.MachineControlRenderer.RenderBounds;
import grondag.hard_science.init.ModModels;
import grondag.hard_science.library.varia.HorizontalAlignment;
import grondag.hard_science.machines.base.MachineTileEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class MachineControlRenderer
{
    public static class RenderBounds
    {
        private final double left;
        private final double top;
        private final double width;
        private final double height;
        
        
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
        public double left() { return left; }
        public double top() { return top; }
        public double width() { return width; }
        public double height() { return height; }
        public double right() { return left + width; }
        public double bottom() { return top + height; }

        public RenderBounds offset(double x, double y)
        {
            return new RenderBounds(this.left + x, this.top + y, this.width, this.height);
        }
        
        public boolean contains(double x, double y)
        {
            return !(x < this.left || x > this.right() || y < this.top || y > this.bottom());
        }
    }
    
    public static class RadialRenderBounds extends RenderBounds
    {
        private final double centerX;
        private final double centerY;
        private final double radius;
        
        public RadialRenderBounds (double centerX, double centerY, double radius)
        {
            super(centerX - radius, centerY - radius, radius * 2);
            this.centerX = centerX;
            this.centerY = centerY;
            this.radius = radius;
        }
        
        public double centerX() { return centerX; }
        public double centerY() { return centerY; }
        public double radius() { return radius; }
    }
    
    public static class RadialGaugeSpec extends RadialRenderBounds
    {
        /**
         * Index of resource in TileEntity machine buffer manager
         */
        public final int bufferIndex;
        
        /**
         * Color for render of level gauge.
         */
        public final int color;
        
        /**
         * MC texture sprite for center of gauge
         */
        public final TextureAtlasSprite sprite;

        public final double spriteScale;
        public final double spriteLeft;
        public final double spriteTop;
        public final double spriteSize;
        
        /** 
         * SpriteScale is multiplied by radius to get size for rendering the sprite. 
         * Value of 0.75 is normal rendering size for a square block texture.
         * Some smaller item textures are easier to read if rendered a little bigger.
         */
        public RadialGaugeSpec(int bufferIndex, RadialRenderBounds bounds, double spriteScale, TextureAtlasSprite sprite, int color)
        {
            super(bounds.centerX, bounds.centerY, bounds.radius);
            this.spriteScale = spriteScale;
            this.spriteSize = this.radius() * spriteScale;
            this.spriteLeft = this.centerX() - this.spriteSize / 2.0;
            this.spriteTop = this.centerY() - this.spriteSize / 2.0;
            this.bufferIndex = bufferIndex;
            this.color = color;
            this.sprite = sprite;
        }
        
    }
    
    
    public static final RenderBounds BOUNDS_ON_OFF = new RenderBounds(1.0-(0.15), 0.02, 0.14);
    public static final RenderBounds BOUNDS_NAME = new RenderBounds(0.25, 0.04, 0.5, 0.12);
    public static final RenderBounds BOUNDS_SYMBOL = new RenderBounds(0.03, 0.03, 0.12, 0.12);
    public static final RenderBounds BOUNDS_REDSTONE = new RenderBounds(0.82, 0.78, 0.12, 0.12);
    
    public static final RadialRenderBounds BOUNDS_GAUGE[] = 
    {
        new RadialRenderBounds(0.12, 0.88, 0.08),
        new RadialRenderBounds(0.30, 0.88, 0.08),
        new RadialRenderBounds(0.48, 0.88, 0.08),
        new RadialRenderBounds(0.66, 0.88, 0.08),
        new RadialRenderBounds(0.12, 0.70, 0.08),
        new RadialRenderBounds(0.30, 0.70, 0.08),
        new RadialRenderBounds(0.48, 0.70, 0.08),
        new RadialRenderBounds(0.66, 0.70, 0.08)
    };
    
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
    
    public static void renderMachineText(RenderBounds bounds, String text, HorizontalAlignment alignment, int alpha)
    {
        Tessellator tes = Tessellator.getInstance();
        renderMachineText(tes, tes.getBuffer(), bounds, text, alignment, alpha);
    }
    
    public static void renderMachineText(Tessellator tessellator, BufferBuilder buffer, RenderBounds bounds, String text, HorizontalAlignment alignment, int alpha)
    {
        switch(alignment)
        {
        case CENTER:
        {
            // need to scale font width to height of the line
            double diff = bounds.width - ModModels.FONT_ORBITRON.getWidth(text) * bounds.height / ModModels.FONT_ORBITRON.fontHeight;
            renderMachineText(tessellator, buffer, bounds.offset(diff / 2, 0), text, alpha);
            break;
        }
        
        case RIGHT:
        {
         // need to scale font width to height of the line
            double diff = bounds.width - ModModels.FONT_ORBITRON.getWidth(text) * bounds.height / ModModels.FONT_ORBITRON.fontHeight;
            renderMachineText(tessellator, buffer, bounds.offset(diff, 0), text, alpha);
            break;
        }
        
        case LEFT:
        default:
            renderMachineText(tessellator, buffer, bounds, text, alpha);
            break;
        
        }
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
        ModModels.FONT_ORBITRON.drawLine(bounds.left(), bounds.top(), text, bounds.height(), 0f, 255, 255, 255, alpha); 
    }
    
    /**
     * Use {@link #renderSpriteInBounds(Tessellator, BufferBuilder, RenderBounds, TextureAtlasSprite, int)}
     * when you already have tessellator/buffer references on the stack.
     */
    public static void renderSpriteInBounds(RenderBounds bounds, TextureAtlasSprite sprite, int alpha)
    {
        Tessellator tes = Tessellator.getInstance();
        renderSpriteInBounds(tes, tes.getBuffer(), bounds, sprite, alpha);
    }
    
    /**
     * Renders a textured quad on the face of the machine.
     * x,y coordinates are 0-1 position on the face.  0,0 is upper left.
     * u,v coordinate are also 0-1 within the currently bound texture.
     * Always full brightness.
     */
    public static void renderSpriteInBounds(Tessellator tessellator, BufferBuilder buffer, RenderBounds bounds, TextureAtlasSprite sprite, int alpha)
    {
        renderSpriteInBounds(tessellator, buffer, bounds.left, bounds.top, bounds.width, bounds.height, sprite, alpha);
    }
    
    public static void renderSpriteInBounds(Tessellator tessellator, BufferBuilder buffer, 
            double left, double top, double width, double height, 
            TextureAtlasSprite sprite, int alpha)
    {
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        GlStateManager.bindTexture(ModModels.TEX_BLOCKS);
        bufferControlQuad(buffer, left, top, left + width, top + height, 
                sprite.getMinU(), 
                sprite.getMinV(), 
                sprite.getMaxU(), 
                sprite.getMaxV(), 
                alpha, 0xFF, 0xFF, 0xFF);
        Tessellator.getInstance().draw();
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
        bufferControlQuad(buffer, bounds.left(), bounds.top(), bounds.right(), bounds.bottom(), 
                0, 0, 1, 1, alpha, 0xFF, 0xFF, 0xFF);
        tessellator.draw();
    }
    
    public static void renderTextureInBoundsWithColor(RenderBounds bounds, int glTextureID, int colorARGB)
    {
        Tessellator tes = Tessellator.getInstance();
        renderTextureInBounds(tes, tes.getBuffer(), bounds, glTextureID, colorARGB);
    }
    
    /**
     * Use this version when you already have tessellator/buffer references on the stack.
     */
    public static void renderTextureInBoundsWithColor(Tessellator tessellator, BufferBuilder buffer, RenderBounds bounds, int glTextureID, int colorARGB)
    {
        GlStateManager.bindTexture(glTextureID);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);        
        bufferControlQuad(buffer, bounds.left(), bounds.top(), bounds.right(), bounds.bottom(), 
                0, 0, 1, 1, (colorARGB >> 24) & 0xFF, (colorARGB >> 16) & 0xFF, (colorARGB >> 8) & 0xFF, colorARGB & 0xFF);
        tessellator.draw();
    }
    
    /**
     * Use this version when you already have tessellator/buffer references on the stack.
     * Arc starts at top and degrees move clockwise.
     */
    public static void renderRadialTexture(Tessellator tessellator, BufferBuilder buffer, RadialRenderBounds bounds, int arcStartDegrees, int arcLengthDegrees, int glTextureID, int colorARGB)
    {
        if(arcLengthDegrees <= 0) return;
        
        GlStateManager.bindTexture(glTextureID);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);  

        int endDegrees = arcStartDegrees + arcLengthDegrees;
        
        int currentUnmaskedEdge = ((arcStartDegrees + 45) / 90);
        // +44 instead of +45 so that we don't go past corner needlessly
        // corners can be rendered as part of either edge
        int endUnmaskedEdge = ((endDegrees + 44) / 90);

        boolean isNotDone = true;
        
        while(isNotDone)
        {
            // starting point
            bufferRadialEdgePoint(buffer, bounds, arcStartDegrees, colorARGB);
            
            bufferRadialMidPoint(buffer, bounds, colorARGB);
            
            if(currentUnmaskedEdge == endUnmaskedEdge)
            {
                // single edge, buffer middle again, and then end point, then done!
                bufferRadialMidPoint(buffer, bounds, colorARGB);
                bufferRadialEdgePoint(buffer, bounds, endDegrees, colorARGB);
                isNotDone = false;
            }
            else
            {
                // go to next edge
                
                if(++currentUnmaskedEdge == endUnmaskedEdge)
                {
                    // at the last edge, buffer end point and then starting corner of this edge, then done
                    bufferRadialEdgePoint(buffer, bounds, endDegrees, colorARGB);
                    bufferRadialCornerPoint(buffer, bounds, currentUnmaskedEdge, colorARGB);
                    isNotDone = false;
                }
                else
                {
                    // at least one more edge to go.
                    // buffer start of next edge, start of current edge and start a new quad.
                    bufferRadialCornerPoint(buffer, bounds, currentUnmaskedEdge + 1, colorARGB);
                    bufferRadialCornerPoint(buffer, bounds, currentUnmaskedEdge, colorARGB);
                    currentUnmaskedEdge++;
                    arcStartDegrees = EDGE_START_DEGREES[currentUnmaskedEdge & 3];
                }
            }
        }
        
//        bufferControlQuad(buffer, bounds.left(), bounds.top(), bounds.right(), bounds.bottom(), 
//                0, 0, 0, 1, 1, colorARGB);
        
//        buffer.pos(xMin, yMin, depth).color(red, green, blue, alpha).tex(uMin, vMin).lightmap(0x00f0, 0x00f0).endVertex();
//        buffer.pos(xMin, yMax, depth).color(red, green, blue, alpha).tex(uMin, vMax).lightmap(0x00f0, 0x00f0).endVertex();
//        buffer.pos(xMax, yMax, depth).color(red, green, blue, alpha).tex(uMax, vMax).lightmap(0x00f0, 0x00f0).endVertex();
//        buffer.pos(xMax, yMin, depth).color(red, green, blue, alpha).tex(uMax, vMin).lightmap(0x00f0, 0x00f0).endVertex();
        
        tessellator.draw();
    }
    
    // segments start at top right and work around to top left
    private static final DoubleUnaryOperator[] SEGMENT_FUNC_X = 
    {
        new DoubleUnaryOperator() { public double applyAsDouble(double value) { return 0.5 + Math.tan(value) / 2.0; }},
        
        new DoubleUnaryOperator() { public double applyAsDouble(double value) { return 1.0; }},
        new DoubleUnaryOperator() { public double applyAsDouble(double value) { return 1.0; }},
        new DoubleUnaryOperator() { public double applyAsDouble(double value) { return 1.0 - Math.tan(value) / 2.0; }},
        
        new DoubleUnaryOperator() { public double applyAsDouble(double value) { return 0.5 - Math.tan(value) / 2.0; }},
        new DoubleUnaryOperator() { public double applyAsDouble(double value) { return 0; }},
        new DoubleUnaryOperator() { public double applyAsDouble(double value) { return 0; }},
        new DoubleUnaryOperator() { public double applyAsDouble(double value) { return Math.tan(value) / 2.0; }}
    };
    
    private static final DoubleUnaryOperator[] SEGMENT_FUNC_Y = 
    {
        new DoubleUnaryOperator() { public double applyAsDouble(double value) { return 0.0; }},
        
        new DoubleUnaryOperator() { public double applyAsDouble(double value) { return Math.tan(value) / 2.0; }},
        new DoubleUnaryOperator() { public double applyAsDouble(double value) { return 0.5 + Math.tan(value) / 2.0; }},
        new DoubleUnaryOperator() { public double applyAsDouble(double value) { return 1.0; }},
        new DoubleUnaryOperator() { public double applyAsDouble(double value) { return 1.0; }},
        
        new DoubleUnaryOperator() { public double applyAsDouble(double value) { return 1.0 - Math.tan(value) / 2.0; }},
        new DoubleUnaryOperator() { public double applyAsDouble(double value) { return 0.5 - Math.tan(value) / 2.0; }},
        new DoubleUnaryOperator() { public double applyAsDouble(double value) { return 0; }}
    };
    
    /** 
     * X coordinate for the starting point of the ordinal edge.  Top is 0.
     */
    private static final double[] EDGE_START_X = { 0.0, 1.0, 1.0, 0.0 }; 

    /** 
     * Y coordinate for the starting point of the ordinal edge.  Top is 0.
     */
    private static final double[] EDGE_START_Y = { 0.0, 0.0, 1.0, 1.0 }; 
    
    private static final int[] EDGE_START_DEGREES = { 315, 45, 135, 225 };
    
    private static void bufferRadialMidPoint(BufferBuilder buffer, RadialRenderBounds bounds, int colorARGB)
    {
        buffer.pos(bounds.centerX, bounds.centerY, 0)
            .color((colorARGB >> 16) & 0xFF, (colorARGB >> 8) & 0xFF, colorARGB & 0xFF, (colorARGB >> 24) & 0xFF)
            .tex(0.5, 0.5).lightmap(0x00f0, 0x00f0).endVertex();
    }
    
    /**
     * Buffers starting point (int a clockwise rotation) of the given edge. 0 is top. 
     */
    private static void bufferRadialCornerPoint(BufferBuilder buffer, RadialRenderBounds bounds, int edge, int colorARGB)
    {   
        edge &= 3;
        buffer.pos(bounds.left() + EDGE_START_X[edge] * bounds.width(), 
                bounds.top() + EDGE_START_Y[edge] * bounds.height(), 0)
        .color((colorARGB >> 16) & 0xFF, (colorARGB >> 8) & 0xFF, colorARGB & 0xFF, (colorARGB >> 24) & 0xFF)
        .tex(EDGE_START_X[edge], EDGE_START_Y[edge]).lightmap(0x00f0, 0x00f0).endVertex();
    }
    
    private static void bufferRadialEdgePoint(BufferBuilder buffer, RadialRenderBounds bounds, int degrees, int colorARGB)
    {   
        int segment = (degrees / 45) & 7;
        int offset = segment * 45;
        double radians = Math.toRadians((degrees % 360) - offset);
        double xUnit = SEGMENT_FUNC_X[segment].applyAsDouble(radians);
        double yUnit = SEGMENT_FUNC_Y[segment].applyAsDouble(radians);
        double xActual = bounds.left() + xUnit * bounds.width();
        double yActual = bounds.top() + yUnit * bounds.height();
        buffer.pos(xActual, yActual, 0)
            .color((colorARGB >> 16) & 0xFF, (colorARGB >> 8) & 0xFF, colorARGB & 0xFF, (colorARGB >> 24) & 0xFF)
            .tex(xUnit, yUnit).lightmap(0x00f0, 0x00f0).endVertex();
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
            double uMin,
            double vMin, double uMax, double vMax, int colorARGB)
    {
        bufferControlQuad(buffer, xMin, yMin, xMax, yMax, 
                uMin, vMin, uMax, vMax, 
                (colorARGB >> 24) & 0xFF, (colorARGB >> 16) & 0xFF, (colorARGB >> 8) & 0xFF, colorARGB & 0xFF);
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
            double uMin,
            double vMin, double uMax, double vMax, int alpha,
            int red, int green, int blue)
    {
        buffer.pos(xMin, yMin, 0).color(red, green, blue, alpha).tex(uMin, vMin).lightmap(0x00f0, 0x00f0).endVertex();
        buffer.pos(xMin, yMax, 0).color(red, green, blue, alpha).tex(uMin, vMax).lightmap(0x00f0, 0x00f0).endVertex();
        buffer.pos(xMax, yMax, 0).color(red, green, blue, alpha).tex(uMax, vMax).lightmap(0x00f0, 0x00f0).endVertex();
        buffer.pos(xMax, yMin, 0).color(red, green, blue, alpha).tex(uMax, vMin).lightmap(0x00f0, 0x00f0).endVertex();
    }

    public static void renderGauge(RadialGaugeSpec spec, int level64K, int delta64K, int alpha)
    {
        Tessellator tes = Tessellator.getInstance();
        renderGauge(tes, tes.getBuffer(), spec, level64K, delta64K, alpha);
    }
    
    /**
     * Use this version when you already have tessellator/buffer references on the stack.
     * @param tessellator
     * @param buffer
     * @param radialRenderBounds
     * @param resourceColor
     * @param level64K
     * @param delta64K
     * @param alpha
     */
    public static void renderGauge(Tessellator tessellator, BufferBuilder buffer, RadialGaugeSpec spec, int level64K, int delta64K, int alpha)
    {
        
        
        // render marks
        MachineControlRenderer.renderTextureInBoundsWithColor(tessellator, buffer, spec, ModModels.TEX_GAUGE_BACKGROUND, (alpha << 24) | 0xFFFFFF);

        // render level
        int arcLength = ((level64K * 270) >> 16);
        renderRadialTexture(tessellator, buffer, spec, 225, arcLength, ModModels.TEX_GAUGE_MAIN, (alpha << 24) | (spec.color & 0xFFFFFF));
        
        if(delta64K != 0)
        {
            int deltaLength =  ((delta64K * 270) >> 16);
            
            if(delta64K < 0)
            {
                renderRadialTexture(tessellator, buffer, spec, 225 + arcLength, deltaLength, ModModels.TEX_GAUGE_MINOR, (alpha << 24) | 0xFF2020);
            }
            else
            {
                renderRadialTexture(tessellator, buffer, spec, 225 + arcLength - deltaLength, deltaLength, ModModels.TEX_GAUGE_MINOR, (alpha << 24) | 0x20FF20);
             
            }
        }
       
        renderSpriteInBounds(tessellator, buffer, spec.spriteLeft, spec.spriteTop, spec.spriteSize, spec.spriteSize, spec.sprite, alpha);
        
        renderMachineText(tessellator, buffer, 
                new RenderBounds(spec.left(), spec.top() + spec.height() * 0.75, spec.width(), spec.height() * 0.3),
                Integer.toString(level64K >> 10), HorizontalAlignment.CENTER, alpha);
                
    }
    
    public static void setupMachineRendering()
    {
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.disableColorMaterial();
        GlStateManager.depthMask(false);
        
        // prevent z-fighting
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(-1, -1);

        //        GlStateManager.enableAlpha();
        //        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.DST_ALPHA);
        
    }
    
    /** 
     * Call after {@link #setupMachineRendering()} to put things back to "normal" for GUI rendering.
     */
    public static void restoreGUIRendering()
    {
        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        GlStateManager.disableColorMaterial();
        GlStateManager.depthMask(true);
        GlStateManager.disablePolygonOffset();
    }
    
    /** 
     * Call after {@link #setupMachineRendering()} to put things back to "normal" for other TESR that might be rendering in world.
     */
    public static void restoreWorldRendering()
    {
        GlStateManager.enableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        GlStateManager.enableColorMaterial();
        GlStateManager.depthMask(false);
        GlStateManager.disablePolygonOffset();

    }

    public static void renderRedstoneControl(MachineTileEntity mte, Tessellator tessellator, BufferBuilder buffer, RenderBounds boundsRedstone,
            int displayAlpha)
    {
        MachineControlRenderer.renderSpriteInBounds(tessellator, buffer, boundsRedstone, 
                mte.hasRedstonePowerSignal() ? ModModels.SPRITE_REDSTONE_TORCH_LIT : ModModels.SPRITE_REDSTONE_TORCH_UNLIT, displayAlpha);
        
        if(!mte.isRedstoneControlEnabled()) 
        {
            MachineControlRenderer.renderTextureInBounds(tessellator, buffer, boundsRedstone, ModModels.TEX_NO, displayAlpha);
        }
    }
}
