package grondag.hard_science.gui.control.machine;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import grondag.hard_science.ClientEventHandler;
import grondag.hard_science.CommonProxy;
import grondag.hard_science.Configurator;
import grondag.hard_science.init.ModModels;
import grondag.hard_science.library.varia.HorizontalAlignment;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.machines.support.MaterialBuffer;
import grondag.hard_science.machines.support.MachineControlState.MachineState;
import grondag.hard_science.superblock.items.SuperItemBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;

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
        
        private RadialRenderBounds innerBounds;

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
        public RadialRenderBounds innerBounds()
        {
            if(this.innerBounds == null)
            {
                this.innerBounds = new RadialRenderBounds(this.centerX, this.centerY, this.radius / 2);
            }
            return this.innerBounds;
        }
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

    public static final RadialRenderBounds BOUNDS_PROGRESS = new RadialRenderBounds(0.24, 0.38, 0.20);
 

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
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
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
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        bufferControlQuad(buffer, bounds.left(), bounds.top(), bounds.right(), bounds.bottom(), 
                0, 0, 1, 1, (colorARGB >> 24) & 0xFF, (colorARGB >> 16) & 0xFF, (colorARGB >> 8) & 0xFF, colorARGB & 0xFF);
        tessellator.draw();
    }
    
    /**
     * maxLevel is the max number of bars to render and should be a power of 4.
     * level is how many of the bars should be lit.
     */
    public static void renderLinearProgress(RenderBounds bounds, int glTextureID, int level, int maxLevel, boolean isHorizontal, int colorARGB)
    {
        Tessellator tes = Tessellator.getInstance();
        renderLinearProgress(tes, tes.getBuffer(), bounds, glTextureID, level, maxLevel, isHorizontal, colorARGB);
    }

    /**
     * Use this version when you already have tessellator/buffer references on the stack.
     */
    public static void renderLinearProgress(Tessellator tessellator, BufferBuilder buffer, RenderBounds bounds, int glTextureID, int level, int maxLevel, boolean isHorizontal, int colorARGB)
    {
        if(maxLevel == 0) return;
        
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        
        GlStateManager.bindTexture(ModModels.TEX_LINEAR_GAUGE_MARKS);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
       
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);

        double uvMax = maxLevel / 4.0;
        double topFactor = (maxLevel - level) / (double) maxLevel;
        
        if(isHorizontal)
        {
            bufferRotatedControlQuad(buffer, bounds.left(), bounds.top(), bounds.right(), bounds.bottom(), 
                    0, 0, 1, uvMax, (colorARGB & 0xFF000000) | 0x909090);
        }
        else
        {
            bufferControlQuad(buffer, bounds.left(), bounds.top(), bounds.right(), bounds.bottom(), 
                    0, 0, 1, uvMax, (colorARGB & 0xFF000000) | 0x909090);
        }
        tessellator.draw();
        
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        
        GlStateManager.bindTexture(glTextureID);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        
        if(isHorizontal)
        {
            bufferRotatedControlQuad(buffer, bounds.left(), bounds.top(), bounds.right() - bounds.width * topFactor, bounds.bottom(), 
                    0, topFactor * uvMax, 1, uvMax, (colorARGB >> 24) & 0xFF, (colorARGB >> 16) & 0xFF, (colorARGB >> 8) & 0xFF, colorARGB & 0xFF);
        }
        else
        {
            bufferControlQuad(buffer, bounds.left(), bounds.top() + bounds.height * topFactor, bounds.right(), bounds.bottom(), 
                    0, topFactor * uvMax, 1, uvMax, (colorARGB >> 24) & 0xFF, (colorARGB >> 16) & 0xFF, (colorARGB >> 8) & 0xFF, colorARGB & 0xFF);
        }
        
        
        tessellator.draw();
        
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_NEAREST);
        
    }

    /**
     * Use this version when you already have tessellator/buffer references on the stack.
     * Arc starts at top and degrees move clockwise.
     */
    public static void renderRadialTexture(Tessellator tessellator, BufferBuilder buffer, RadialRenderBounds bounds, int arcStartDegrees, int arcLengthDegrees, int glTextureID, int colorARGB)
    {
        if(arcLengthDegrees <= 0) return;

        GlStateManager.bindTexture(glTextureID);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
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
    
    /**
     * Just like {@link #bufferControlQuad(BufferBuilder, double, double, double, double, double, double, double, double, int)}
     * except rotates UV coordinates 90 degrees clockwise.
     */
    public static void bufferRotatedControlQuad
    (
            BufferBuilder buffer, 
            double xMin, double yMin, double xMax, double yMax, 
            double uMin,
            double vMin, double uMax, double vMax, int colorARGB)
    {
        bufferRotatedControlQuad(buffer, xMin, yMin, xMax, yMax, 
                uMin, vMin, uMax, vMax, 
                (colorARGB >> 24) & 0xFF, (colorARGB >> 16) & 0xFF, (colorARGB >> 8) & 0xFF, colorARGB & 0xFF);
    }

    /**
     * Just like {@link #bufferControlQuad(BufferBuilder, double, double, double, double, double, double, double, double, int, int, int, int)}
     * except rotates UV coordinates 90 degrees clockwise.
     */
    public static void bufferRotatedControlQuad
    (
            BufferBuilder buffer, 
            double xMin, double yMin, double xMax, double yMax, 
            double uMin,
            double vMin, double uMax, double vMax, int alpha,
            int red, int green, int blue)
    {
        buffer.pos(xMin, yMin, 0).color(red, green, blue, alpha).tex(uMin, vMax).lightmap(0x00f0, 0x00f0).endVertex();
        buffer.pos(xMin, yMax, 0).color(red, green, blue, alpha).tex(uMax, vMax).lightmap(0x00f0, 0x00f0).endVertex();
        buffer.pos(xMax, yMax, 0).color(red, green, blue, alpha).tex(uMax, vMin).lightmap(0x00f0, 0x00f0).endVertex();
        buffer.pos(xMax, yMin, 0).color(red, green, blue, alpha).tex(uMin, vMin).lightmap(0x00f0, 0x00f0).endVertex();
    }

    public static void renderFabricationProgress(RadialRenderBounds bounds, MachineTileEntity te, int alpha)
    {
        Tessellator tes = Tessellator.getInstance();
        renderFabricationProgress(tes, tes.getBuffer(), bounds, te, alpha);
    }
    
    public static void renderFabricationProgress(Tessellator tessellator, BufferBuilder buffer, RadialRenderBounds bounds, MachineTileEntity te, int alpha)
    {
        renderProgress(bounds, te, alpha);
        
        if(te.getMachineState() == MachineState.FABRICATING)
        {
            MachineControlRenderer.renderItem(tessellator, buffer, bounds.innerBounds(), te.getStatusStack(), alpha);
        }
        else if(!te.isOn()) 
            return;
        else 
        {
            MachineControlRenderer.renderRadialTexture(tessellator, buffer, bounds, (int)(CommonProxy.currentTimeMillis() & 2047) * 360 / 2048, 30, ModModels.TEX_RADIAL_GAUGE_FULL_MARKS, alpha << 24 | 0x40FF40);

            if(te.getMachineState() == MachineState.THINKING && te.getBufferManager().hasFailureCauseClientSideOnly() && MachineControlRenderer.warningLightBlinkOn())
            {
                MachineControlRenderer.renderTextureInBoundsWithColor(tessellator, buffer, bounds.innerBounds(), ModModels.TEX_MATERIAL_SHORTAGE, alpha << 24 | 0xFFFF40);
                MachineControlRenderer.renderTextureInBoundsWithColor(tessellator, buffer, bounds, ModModels.TEX_RADIAL_GAUGE_MINOR, alpha << 24 | 0xFFFF40);
            }
        }
    }
    
    public static void renderProgress(RadialRenderBounds bounds, MachineTileEntity te, int alpha)
    {
        Tessellator tes = Tessellator.getInstance();
        renderProgress(tes, tes.getBuffer(), bounds, te, alpha);
    }

    public static void renderProgress(Tessellator tessellator, BufferBuilder buffer, RadialRenderBounds bounds, MachineTileEntity te, int alpha)
    {
        int duration, remaining, arcLength;

        if(te.hasBacklog())
        {    
            duration = te.getMaxBacklog();
            remaining = te.getCurrentBacklog();
            arcLength = duration > 0 ? 360 * (duration - remaining) / duration : 0;
            MachineControlRenderer.renderRadialTexture(tessellator, buffer, bounds, 0, arcLength, ModModels.TEX_RADIAL_GAUGE_MAIN, alpha << 24 | 0x40FF40);
        }

        if(te.hasJobTicks())
        {    
            duration = te.getJobDurationTicks();
            remaining = te.getJobRemainingTicks();
            arcLength = duration > 0 ? 360 * (duration - remaining) / duration : 0;
            MachineControlRenderer.renderRadialTexture(tessellator, buffer, bounds, 0, arcLength, ModModels.TEX_RADIAL_GAUGE_MINOR, alpha << 24 | 0x40FFFF);
        }

    }

    public static boolean warningLightBlinkOn()
    {
        return (CommonProxy.currentTimeMillis() & 0x400) == 0x400;
    }

    public static void renderGauge(RadialGaugeSpec spec, MachineTileEntity te, MaterialBuffer materialBuffer, int alpha)
    {
        Tessellator tes = Tessellator.getInstance();
        renderGauge(tes, tes.getBuffer(), spec, te, materialBuffer, alpha);
    }
    
    /**
     * Use this version when you already have tessellator/buffer references on the stack.
     */
    public static void renderGauge(Tessellator tessellator, BufferBuilder buffer, RadialGaugeSpec spec, MachineTileEntity te, MaterialBuffer materialBuffer, int alpha)
    {

        final int level64K = materialBuffer.getLevel();
        
        // render marks
        MachineControlRenderer.renderTextureInBoundsWithColor(tessellator, buffer, spec, ModModels.TEX_RADIAL_GAUGE_MARKS, (alpha << 24) | 0xFFFFFF);

        // render level
        int arcLength = ((level64K * 270) >> 16);
        renderRadialTexture(tessellator, buffer, spec, 225, arcLength, ModModels.TEX_RADIAL_GAUGE_MAIN, (alpha << 24) | (spec.color & 0xFFFFFF));

        if(materialBuffer.isFailureCause() && warningLightBlinkOn())
        {
            renderTextureInBoundsWithColor(tessellator, buffer, spec, ModModels.TEX_RADIAL_GAUGE_MINOR, (alpha << 24) | 0xFF2020);
        }
        
        /** Can look away from a machine for five seconds before flow tracking turns off to save CPU */
        if(CommonProxy.currentTimeMillis() - te.lastInViewMillis < 5000)
        {
            // log scale, anything less than one item is 1/10 of quarter arc, 64+ items is quarter arc
            final float deltaPlus = materialBuffer.getAvgDeltaPlus();
            if(deltaPlus > 0.012f)
            {
                int deltaLength = Math.round(deltaPlus * 135);
                renderRadialTexture(tessellator, buffer, spec, 225, deltaLength, ModModels.TEX_RADIAL_GAUGE_MINOR, (alpha << 24) | 0x20FF20);
            }
            
            // log scale, anything less than one item is 1/10 of quarter arc, 64+ items is quarter arc
            final float deltaMinus = materialBuffer.getAvgDeltaMinus();
            if(deltaMinus > 0.012f)
            {
                int deltaLength =  Math.round(deltaMinus * 135);
                renderRadialTexture(tessellator, buffer, spec, 135 - deltaLength, deltaLength, ModModels.TEX_RADIAL_GAUGE_MINOR, (alpha << 24) | 0xFF2020);
            }
        }
        
        renderSpriteInBounds(tessellator, buffer, spec.spriteLeft, spec.spriteTop, spec.spriteSize, spec.spriteSize, spec.sprite, alpha);

        renderMachineText(tessellator, buffer, 
                new RenderBounds(spec.left(), spec.top() + spec.height() * 0.75, spec.width(), spec.height() * 0.3),
                Integer.toString(level64K >> 10), HorizontalAlignment.CENTER, alpha);

    }

    /** 
     * Attempts to render an item on machine face without looking like turd candy
     * and shitting all over the rendering state.<br><br>
     * 
     * For a ried time this method was named pleaseJustRenderTheDamnItemWithoutFuckingUpAllTheThingsIsThatTooMuchToAsk.<br><br>
     * 
     * NOTE: Changes rendering state and restores it to machine rendering.
     * If you call this from somewhere other than machine rendering, 
     * you'll probably need to do more clean up.
     */
    public static void renderItem(Tessellator tessellator, BufferBuilder buffer, RadialRenderBounds spec, @Nullable ItemStack stack, int alpha)
    {
        if(stack == null || stack.isEmpty()) return;
        
        GlStateManager.pushMatrix();
        
        // Begin with reasonable defaults
        restoreWorldRendering();

        // Still don't know how lightmaps work.  My main interaction with them seems to be circumventing them.
        OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, 240.f, 240.0f );

        // Position the item so it is centered in the bounding box
        GlStateManager.translate( spec.centerX, spec.centerY, 0 );

        // scale and flatten
        GlStateManager.scale( spec.width(), spec.height(), 0.000001f );

        // prevent z-fighting
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(-1, -1);
        
        // I don't know?
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        
        // Position item so that the lights look okay
        // The values used in enableGuiStandardLighting don't work well here
        // These were arrived at via trial and error
        GlStateManager.pushMatrix();
        GlStateManager.rotate(350, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(188, 1.0F, 0.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
        
        // prevent normals getting borked due to squished Z axis
        // this is the primary reason we have a custom item rendering routine - the standard one turns this on
        GlStateManager.disableRescaleNormal();
        
        // set up the stuff that block rendering would need/expect
        IBakedModel bakedmodel = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, null, null);
        GlStateManager.bindTexture(ModModels.TEX_BLOCKS);
        ModModels.ITEX_BLOCKS.setBlurMipmap(false, false);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        if (bakedmodel.isGui3d())
        {
            GlStateManager.enableLighting();
        }
        else
        {
            GlStateManager.disableLighting();
        }
        
        // at last, render the effing item!
        bakedmodel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(bakedmodel, ItemCameraTransforms.TransformType.GUI, false);
        if(stack.getItem() instanceof SuperItemBlock)
        {
            GlStateManager.pushMatrix();
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);
            
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
            
            // super item models have only general quads
            List<BakedQuad> quads = bakedmodel.getQuads((IBlockState)null, (EnumFacing)null, 0L);
            
            
            int i = 0;
            for (int j = quads.size(); i < j; ++i)
            {
                BakedQuad bakedquad = quads.get(i);
                net.minecraftforge.client.model.pipeline.LightUtil.renderQuadColor(buffer, bakedquad, 0xFFFFFFFF);
            }
            
            // for supermodelblocks with translucency, may need to sort quads for depth to get good results?
            
//            buffer.sortVertexData((float) TileEntityRendererDispatcher.staticPlayerX,
//                    (float) TileEntityRendererDispatcher.staticPlayerY, (float) TileEntityRendererDispatcher.staticPlayerZ);
            
            tessellator.draw();
            
            GlStateManager.popMatrix();
        }
        else
        {
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, bakedmodel);
        }
        ;
        
        // clean up our mess
        GlStateManager.disableAlpha();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.bindTexture(ModModels.TEX_BLOCKS);
        ModModels.ITEX_BLOCKS.restoreLastBlurMipmap();
        GlStateManager.enableRescaleNormal();
        
        GlStateManager.popMatrix();
        setupMachineRendering();
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
