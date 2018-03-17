package grondag.hard_science.gui.control.machine;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import grondag.exotic_matter.varia.HorizontalAlignment;
import grondag.exotic_matter.world.Rotation;
import grondag.exotic_matter.world.WorldInfo;
import grondag.hard_science.gui.control.machine.RenderBounds.AbstractRadialRenderBounds;
import grondag.hard_science.gui.control.machine.RenderBounds.RadialRenderBounds;
import grondag.hard_science.gui.control.machine.RenderBounds.RectRenderBounds;
import grondag.hard_science.init.ModModels;
import grondag.hard_science.library.font.RasterFont;
import grondag.hard_science.library.render.QuadBakery;
import grondag.hard_science.library.render.TextureHelper;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.machines.energy.ClientEnergyInfo;
import grondag.hard_science.machines.energy.MachinePower;
import grondag.hard_science.machines.support.MachineControlState;
import grondag.hard_science.machines.support.MachineControlState.MachineState;
import grondag.hard_science.machines.support.MachineStatusState;
import grondag.hard_science.superblock.items.SuperItemBlock;
import grondag.hard_science.superblock.texture.EnhancedSprite;
import grondag.hard_science.superblock.texture.TexturePalletteRegistry.TexturePallette;
import grondag.hard_science.superblock.texture.Textures;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MachineControlRenderer
{
    
    /**
     * Standard position for labels under radial gauges.
     */
    private static double GAUGE_LABEL_TOP = 0.78;
    
    /**
     * Standard size for labels under radial gauges.
     */
    private static double GAUGE_LABEL_HEIGHT = 0.25;
    
//    /**
//     * Alpha is 0-255
//     */
//    public static void renderBinaryTexture(Tessellator tessellator, BufferBuilder buffer, AbstractRectRenderBounds bounds, BinaryGlTexture texture, boolean selector, int alpha)
//    {
//        renderTextureInBounds(tessellator, buffer, bounds, texture.apply(selector), alpha);
//    }

//    public static void renderBinaryTexture(AbstractRectRenderBounds bounds, BinaryGlTexture texture, boolean selector, int alpha)
//    {
//        renderTextureInBounds(bounds, texture.apply(selector), alpha);
//    }


    public static void renderBinarySprite(Tessellator tessellator, BufferBuilder buffer, RenderBounds<?> bounds, BinaryReference<TextureAtlasSprite> texture, boolean selector, int color)
    {
        renderSpriteInBounds(tessellator, buffer, bounds, texture.apply(selector), color, Rotation.ROTATE_NONE);
    }

    public static void renderBinarySprite(RenderBounds<?> bounds,  BinaryReference<TextureAtlasSprite> texture, boolean selector, int color)
    {
        renderSpriteInBounds(bounds, texture.apply(selector), color, Rotation.ROTATE_NONE);
    }
        
    public static void renderMachineText(RasterFont font, RenderBounds<?> bounds, String text, HorizontalAlignment alignment, int colorARGB)
    {
        Tessellator tes = Tessellator.getInstance();
        renderMachineText(tes, tes.getBuffer(), font, bounds, text, alignment, colorARGB);
    }

    public static void renderMachineText(Tessellator tessellator, BufferBuilder buffer, RasterFont font, RenderBounds<?> bounds, String text, HorizontalAlignment alignment, int colorARGB)
    {
        switch(alignment)
        {
        case CENTER:
        {
            // need to scale font pixelWidth to height of the line
            double diff = bounds.width() - font.getWidth(text) * bounds.height() / font.fontHeight;
            renderMachineText(tessellator, buffer, font, bounds.offset(diff / 2, 0), text, colorARGB);
            break;
        }

        case RIGHT:
        {
            // need to scale font pixelWidth to height of the line
            double diff = bounds.width() - font.getWidth(text) * bounds.height() / font.fontHeight;
            renderMachineText(tessellator, buffer, font, bounds.offset(diff, 0), text, colorARGB);
            break;
        }

        case LEFT:
        default:
            renderMachineText(tessellator, buffer, font, bounds, text, colorARGB);
            break;

        }
    }

    /**
     * Use {@link #renderMachineText(Tessellator, BufferBuilder, RectRenderBounds, String, int)}
     * when you already have tessellator and buffer on the stack.
     */
    public static void renderMachineText(RasterFont font, RenderBounds<?> bounds, String text, int colorARGB)
    {
        Tessellator tes = Tessellator.getInstance();
        renderMachineText(tes, tes.getBuffer(), font, bounds, text, colorARGB);
    }

    /**
     * Use this version when you already have tessellator/buffer references on the stack.
     */
    public static void renderMachineText(Tessellator tessellator, BufferBuilder buffer, RasterFont font, RenderBounds<?> bounds, String text, int colorARGB)
    {
        font.drawLine(bounds.left(), bounds.top(), text, bounds.height(), 0f, (colorARGB >> 16) & 0xFF, (colorARGB >> 8) & 0xFF, colorARGB & 0xFF, (colorARGB >> 24) & 0xFF);
    }
    
    
    public static void renderMachineTextMonospaced(RasterFont font, RenderBounds<?> bounds, String text, HorizontalAlignment alignment, int colorARGB)
    {
        Tessellator tes = Tessellator.getInstance();
        renderMachineTextMonospaced(tes, tes.getBuffer(), font, bounds, text, alignment, colorARGB);
    }

    public static void renderMachineTextMonospaced(Tessellator tessellator, BufferBuilder buffer, RasterFont font, RenderBounds<?> bounds, String text, HorizontalAlignment alignment, int color)
    {
        switch(alignment)
        {
        case CENTER:
        {
            // need to scale font pixelWidth to height of the line
            double diff = bounds.width() - font.getWidthMonospaced(text) * bounds.height() / font.fontHeight;
            renderMachineTextMonospaced(tessellator, buffer, font, bounds.offset(diff / 2, 0), text, color);
            break;
        }

        case RIGHT:
        {
            // need to scale font pixelWidth to height of the line
            double diff = bounds.width() - font.getWidthMonospaced(text) * bounds.height() / font.fontHeight;
            renderMachineTextMonospaced(tessellator, buffer, font, bounds.offset(diff, 0), text, color);
            break;
        }

        case LEFT:
        default:
            renderMachineTextMonospaced(tessellator, buffer, font, bounds, text, color);
            break;

        }
    }

    /**
     * Use {@link #renderMachineText(Tessellator, BufferBuilder, RectRenderBounds, String, int)}
     * when you already have tessellator and buffer on the stack.
     */
    public static void renderMachineTextMonospaced(RasterFont font, RenderBounds<?> bounds, String text, int colorARGB)
    {
        Tessellator tes = Tessellator.getInstance();
        renderMachineTextMonospaced(tes, tes.getBuffer(), font, bounds, text, colorARGB);
    }

    /**
     * Use this version when you already have tessellator/buffer references on the stack.
     */
    public static void renderMachineTextMonospaced(Tessellator tessellator, BufferBuilder buffer, RasterFont font, RenderBounds<?> bounds, String text, int colorARGB)
    {
        font.drawLineMonospaced(bounds.left(), bounds.top(), text, bounds.height(), 0f,  (colorARGB >> 16) & 0xFF, (colorARGB >> 8) & 0xFF, colorARGB & 0xFF, (colorARGB >> 24) & 0xFF);
    }

    /**
     * Use {@link #renderSpriteInBounds(Tessellator, BufferBuilder, RectRenderBounds, TextureAtlasSprite, int)}
     * when you already have tessellator/buffer references on the stack.
     */
    public static void renderSpriteInBounds(RenderBounds<?> bounds, TextureAtlasSprite sprite, int color, Rotation rotation)
    {
        Tessellator tes = Tessellator.getInstance();
        renderSpriteInBounds(tes, tes.getBuffer(), bounds, sprite, color, rotation);
    }

    /**
     * Renders a textured quad on the face of the machine.
     * x,y coordinates are 0-1 position on the face.  0,0 is upper left.
     * u,v coordinate are also 0-1 within the currently bound texture.
     * Always full brightness.
     */
    public static void renderSpriteInBounds(Tessellator tessellator, BufferBuilder buffer, RenderBounds<?> bounds, TextureAtlasSprite sprite, int color, Rotation rotation)
    {
        renderSpriteInBounds(tessellator, buffer, bounds.left(), bounds.top(), bounds.width(), bounds.height(), sprite, color, rotation);
    }

    public static void renderSpriteInBounds(Tessellator tessellator, BufferBuilder buffer, 
            double left, double top, double width, double height, 
            TextureAtlasSprite sprite, int color, Rotation rotation)
    {
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        GlStateManager.bindTexture(ModModels.TEX_BLOCKS);
        TextureHelper.setTextureBlurMipmap(true, true);
        final float margin = (sprite.getMaxU() - sprite.getMinU()) * QuadBakery.UV_EPS;

        bufferControlQuad(buffer, left, top, left + width, top + height, 
                sprite.getMinU() + margin, 
                sprite.getMinV() + margin,
                sprite.getMaxU() - margin,
                sprite.getMaxV() - margin,
                color, 
                rotation);
 
        Tessellator.getInstance().draw();
    }

//    /**
//     * Use {@link #renderTextureInBounds(Tessellator, BufferBuilder, RectRenderBounds, int, int)}
//     * when you already have tessellator/buffer references on the stack.
//     */
//    public static void renderTextureInBounds(AbstractRectRenderBounds bounds, int glTextureID, int alpha)
//    {
//        Tessellator tes = Tessellator.getInstance();
//        renderTextureInBounds(tes, tes.getBuffer(), bounds, glTextureID, alpha);
//    }

    
//    /**
//     * Use this version when you already have tessellator/buffer references on the stack.
//     */
//    public static void renderTextureInBounds(Tessellator tessellator, BufferBuilder buffer, AbstractRectRenderBounds bounds, int glTextureID, int alpha)
//    {
//        GlStateManager.bindTexture(glTextureID);
//        TextureHelper.setTextureBlurMipmap(true, true);
//        //GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
//        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);        
//        bufferControlQuad(buffer, bounds.left(), bounds.top(), bounds.right(), bounds.bottom(), 
//                0, 0, 1, 1, alpha, 0xFF, 0xFF, 0xFF);
//        tessellator.draw();
//    }
    
  
//    public static void renderTextureInBoundsWithColor(AbstractRectRenderBounds bounds, int glTextureID, int colorARGB)
//    {
//        Tessellator tes = Tessellator.getInstance();
//        renderTextureInBounds(tes, tes.getBuffer(), bounds, glTextureID, colorARGB);
//    }

//    /**
//     * Use this version when you already have tessellator/buffer references on the stack.
//     */
//    public static void renderTextureInBoundsWithColor(Tessellator tessellator, BufferBuilder buffer, AbstractRectRenderBounds bounds, int glTextureID, int colorARGB)
//    {
//        GlStateManager.bindTexture(glTextureID);
//        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
//        TextureHelper.setTextureBlurMipmap(true, true);
////        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
//        bufferControlQuad(buffer, bounds.left(), bounds.top(), bounds.right(), bounds.bottom(), 
//                0, 0, 1, 1, (colorARGB >> 24) & 0xFF, (colorARGB >> 16) & 0xFF, (colorARGB >> 8) & 0xFF, colorARGB & 0xFF);
//        tessellator.draw();
//    }
    
    // Currently not used
    // Would need to revise to use texture atlas if reviving
//    /**
//     * maxLevel is the max number of bars to render and should be a power of 4.
//     * level is how many of the bars should be lit.
//     */
//    public static void renderLinearProgress(RectRenderBounds bounds, int glTextureID, int level, int maxLevel, boolean isHorizontal, int colorARGB)
//    {
//        Tessellator tes = Tessellator.getInstance();
//        renderLinearProgress(tes, tes.getBuffer(), bounds, glTextureID, level, maxLevel, isHorizontal, colorARGB);
//    }
//
//    /**
//     * Use this version when you already have tessellator/buffer references on the stack.
//     */
//    public static void renderLinearProgress(Tessellator tessellator, BufferBuilder buffer, RectRenderBounds bounds, int glTextureID, int level, int maxLevel, boolean isHorizontal, int colorARGB)
//    {
//        if(maxLevel == 0) return;
//        
//        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
//        
//        GlStateManager.bindTexture(ModModels.TEX_LINEAR_GAUGE_MARKS);
//        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
//        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
//       
//        TextureHelper.setTextureBlurMipmap(true, true);
////        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
//
//        double uvMax = maxLevel / 4.0;
//        double topFactor = (maxLevel - level) / (double) maxLevel;
//        
//        if(isHorizontal)
//        {
//            bufferControlQuad(buffer, bounds.left(), bounds.top(), bounds.right(), bounds.bottom(), 
//                    0, 0, 1, uvMax, (colorARGB & 0xFF000000) | 0x909090,
//                    Rotation.ROTATE_90);
//        }
//        else
//        {
//            bufferControlQuad(buffer, bounds.left(), bounds.top(), bounds.right(), bounds.bottom(), 
//                    0, 0, 1, uvMax, (colorARGB & 0xFF000000) | 0x909090);
//        }
//        tessellator.draw();
//        
//        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
//        
//        GlStateManager.bindTexture(glTextureID);
//        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
//        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
//        TextureHelper.setTextureBlurMipmap(true, true);
////        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
//        
//        if(isHorizontal)
//        {
//            bufferControlQuad(buffer, bounds.left(), bounds.top(), bounds.right() - bounds.width * topFactor, bounds.bottom(), 
//                    0, topFactor * uvMax, 1, uvMax, (colorARGB >> 24) & 0xFF, (colorARGB >> 16) & 0xFF, (colorARGB >> 8) & 0xFF, colorARGB & 0xFF,
//                    Rotation.ROTATE_90);
//        }
//        else
//        {
//            bufferControlQuad(buffer, bounds.left(), bounds.top() + bounds.height * topFactor, bounds.right(), bounds.bottom(), 
//                    0, topFactor * uvMax, 1, uvMax, (colorARGB >> 24) & 0xFF, (colorARGB >> 16) & 0xFF, (colorARGB >> 8) & 0xFF, colorARGB & 0xFF);
//        }
//        
//        
//        tessellator.draw();
//        
//        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
//        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
//    }


//    public static void renderRadialTexture(Tessellator tessellator, BufferBuilder buffer, AbstractRadialRenderBounds bounds, int arcStartDegrees, int arcLengthDegrees, int glTextureID, int colorARGB)
//    {
//        if(arcLengthDegrees <= 0) return;
//
//        GlStateManager.bindTexture(glTextureID);
//        TextureHelper.setTextureBlurMipmap(true, true);
////        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
//        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);  
//
//        int endDegrees = arcStartDegrees + arcLengthDegrees;
//
//        int currentUnmaskedEdge = ((arcStartDegrees + 45) / 90);
//        // +44 instead of +45 so that we don't go past corner needlessly
//        // corners can be rendered as part of either edge
//        int endUnmaskedEdge = ((endDegrees + 44) / 90);
//
//        boolean isNotDone = true;
//
//        while(isNotDone)
//        {
//            // starting point
//            bufferRadialEdgePoint(buffer, bounds, arcStartDegrees, colorARGB);
//
//            bufferRadialMidPoint(buffer, bounds, colorARGB);
//
//            if(currentUnmaskedEdge == endUnmaskedEdge)
//            {
//                // single edge, buffer middle again, and then end point, then done!
//                bufferRadialMidPoint(buffer, bounds, colorARGB);
//                bufferRadialEdgePoint(buffer, bounds, endDegrees, colorARGB);
//                isNotDone = false;
//            }
//            else
//            {
//                // go to next edge
//
//                if(++currentUnmaskedEdge == endUnmaskedEdge)
//                {
//                    // at the last edge, buffer end point and then starting corner of this edge, then done
//                    bufferRadialEdgePoint(buffer, bounds, endDegrees, colorARGB);
//                    bufferRadialCornerPoint(buffer, bounds, currentUnmaskedEdge, colorARGB);
//                    isNotDone = false;
//                }
//                else
//                {
//                    // at least one more edge to go.
//                    // buffer start of next edge, start of current edge and start a new quad.
//                    bufferRadialCornerPoint(buffer, bounds, currentUnmaskedEdge + 1, colorARGB);
//                    bufferRadialCornerPoint(buffer, bounds, currentUnmaskedEdge, colorARGB);
//                    currentUnmaskedEdge++;
//                    arcStartDegrees = EDGE_START_DEGREES[currentUnmaskedEdge & 3];
//                }
//            }
//        }
//
//        tessellator.draw();
//    }

    public static void renderRadialSprite(Tessellator tessellator, BufferBuilder buffer, AbstractRadialRenderBounds bounds, int arcStartDegrees, int arcLengthDegrees, TexturePallette texture, int colorARGB)
    {
        if(arcLengthDegrees <= 0) return;

        EnhancedSprite sprite = texture.getSampleSprite();
        
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
            bufferRadialEdgePoint(buffer, bounds, sprite, arcStartDegrees, colorARGB);

            bufferRadialMidPoint(buffer, bounds, sprite, colorARGB);

            if(currentUnmaskedEdge == endUnmaskedEdge)
            {
                // single edge, buffer middle again, and then end point, then done!
                bufferRadialMidPoint(buffer, bounds, sprite, colorARGB);
                bufferRadialEdgePoint(buffer, bounds, sprite, endDegrees, colorARGB);
                isNotDone = false;
            }
            else
            {
                // go to next edge

                if(++currentUnmaskedEdge == endUnmaskedEdge)
                {
                    // at the last edge, buffer end point and then starting corner of this edge, then done
                    bufferRadialEdgePoint(buffer, bounds, sprite, endDegrees, colorARGB);
                    bufferRadialCornerPoint(buffer, bounds, sprite, currentUnmaskedEdge, colorARGB);
                    isNotDone = false;
                }
                else
                {
                    // at least one more edge to go.
                    // buffer start of next edge, start of current edge and start a new quad.
                    bufferRadialCornerPoint(buffer, bounds, sprite, currentUnmaskedEdge + 1, colorARGB);
                    bufferRadialCornerPoint(buffer, bounds, sprite, currentUnmaskedEdge, colorARGB);
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
    private static final float[] EDGE_START_X = { 0.0f, 1.0f, 1.0f, 0.0f }; 

    /** 
     * Y coordinate for the starting point of the ordinal edge.  Top is 0.
     */
    private static final float[] EDGE_START_Y = { 0.0f, 0.0f, 1.0f, 1.0f }; 

    private static final int[] EDGE_START_DEGREES = { 315, 45, 135, 225 };

//    private static void bufferRadialMidPoint(BufferBuilder buffer, AbstractRadialRenderBounds bounds, int colorARGB)
//    {
//        buffer.pos(bounds.centerX, bounds.centerY, 0)
//        .color((colorARGB >> 16) & 0xFF, (colorARGB >> 8) & 0xFF, colorARGB & 0xFF, (colorARGB >> 24) & 0xFF)
//        .tex(0.5, 0.5).lightmap(0x00f0, 0x00f0).endVertex();
//    }

//    /**
//     * Buffers starting point (int a clockwise rotation) of the given edge. 0 is top. 
//     */
//    private static void bufferRadialCornerPoint(BufferBuilder buffer, AbstractRadialRenderBounds bounds, int edge, int colorARGB)
//    {   
//        edge &= 3;
//        buffer.pos(bounds.left() + EDGE_START_X[edge] * bounds.width(), 
//                bounds.top() + EDGE_START_Y[edge] * bounds.height(), 0)
//        .color((colorARGB >> 16) & 0xFF, (colorARGB >> 8) & 0xFF, colorARGB & 0xFF, (colorARGB >> 24) & 0xFF)
//        .tex(EDGE_START_X[edge], EDGE_START_Y[edge]).lightmap(0x00f0, 0x00f0).endVertex();
//    }

//    private static void bufferRadialEdgePoint(BufferBuilder buffer, AbstractRadialRenderBounds bounds, int degrees, int colorARGB)
//    {   
//        int segment = (degrees / 45) & 7;
//        int offset = segment * 45;
//        double radians = Math.toRadians((degrees % 360) - offset);
//        double xUnit = SEGMENT_FUNC_X[segment].applyAsDouble(radians);
//        double yUnit = SEGMENT_FUNC_Y[segment].applyAsDouble(radians);
//        double xActual = bounds.left() + xUnit * bounds.width();
//        double yActual = bounds.top() + yUnit * bounds.height();
//        buffer.pos(xActual, yActual, 0)
//        .color((colorARGB >> 16) & 0xFF, (colorARGB >> 8) & 0xFF, colorARGB & 0xFF, (colorARGB >> 24) & 0xFF)
//        .tex(xUnit, yUnit).lightmap(0x00f0, 0x00f0).endVertex();
//    }

    private static void bufferRadialMidPoint(BufferBuilder buffer, AbstractRadialRenderBounds bounds, EnhancedSprite sprite, int colorARGB)
    {
        buffer.pos(bounds.centerX, bounds.centerY, 0)
        .color((colorARGB >> 16) & 0xFF, (colorARGB >> 8) & 0xFF, colorARGB & 0xFF, (colorARGB >> 24) & 0xFF)
        .tex(sprite.safeInterpolatedU(0.5f), sprite.safeInterpolatedV(0.5f))
        .lightmap(0x00f0, 0x00f0).endVertex();
    }
    
    /**
     * Buffers starting point (int a clockwise rotation) of the given edge. 0 is top. 
     */
    private static void bufferRadialCornerPoint(BufferBuilder buffer, AbstractRadialRenderBounds bounds, EnhancedSprite sprite, int edge, int colorARGB)
    {   
        edge &= 3;
        buffer.pos(bounds.left() + EDGE_START_X[edge] * bounds.width(), 
                bounds.top() + EDGE_START_Y[edge] * bounds.height(), 0)
        .color((colorARGB >> 16) & 0xFF, (colorARGB >> 8) & 0xFF, colorARGB & 0xFF, (colorARGB >> 24) & 0xFF)
        .tex(sprite.safeInterpolatedU(EDGE_START_X[edge]), sprite.safeInterpolatedV(EDGE_START_Y[edge]))
        .lightmap(0x00f0, 0x00f0).endVertex();
    }

    private static void bufferRadialEdgePoint(BufferBuilder buffer, AbstractRadialRenderBounds bounds, EnhancedSprite sprite, int degrees, int colorARGB)
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
        .tex(sprite.safeInterpolatedU(xUnit), sprite.safeInterpolatedV(yUnit))
        .lightmap(0x00f0, 0x00f0).endVertex();
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
            double xMin,
            double yMin,
            double xMax,
            double yMax, 
            double uMin,
            double vMin,
            double uMax,
            double vMax,
            int colorARGB)
    {
        bufferControlQuad(buffer, xMin, yMin, xMax, yMax, 
                uMin, vMin, uMax, vMax, 
                (colorARGB >> 24) & 0xFF, (colorARGB >> 16) & 0xFF, (colorARGB >> 8) & 0xFF, colorARGB & 0xFF, Rotation.ROTATE_NONE);
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
            double xMin,
            double yMin,
            double xMax,
            double yMax, 
            double uMin,
            double vMin,
            double uMax,
            double vMax,
            int colorARGB,
            Rotation rotation)
    {
        bufferControlQuad(buffer, xMin, yMin, xMax, yMax, 
                uMin, vMin, uMax, vMax, 
                (colorARGB >> 24) & 0xFF, (colorARGB >> 16) & 0xFF, (colorARGB >> 8) & 0xFF, colorARGB & 0xFF, rotation);
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
            double xMin, 
            double yMin, 
            double xMax, 
            double yMax, 
            double uMin,
            double vMin, 
            double uMax, 
            double vMax, 
            int alpha,
            int red, 
            int green, 
            int blue)
    {
        bufferControlQuad(buffer, xMin, yMin, xMax, yMax, uMin, vMin, uMax, vMax, alpha, red, green, blue, Rotation.ROTATE_NONE);
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
            double xMin, 
            double yMin, 
            double xMax, 
            double yMax, 
            double uMin,
            double vMin, 
            double uMax, 
            double vMax, 
            int alpha,
            int red, 
            int green, 
            int blue, 
            Rotation rotation)
    {
        switch(rotation)
        {
            case ROTATE_NONE:
            default:
                buffer.pos(xMin, yMin, 0).color(red, green, blue, alpha).tex(uMin, vMin).lightmap(0x00f0, 0x00f0).endVertex();
                buffer.pos(xMin, yMax, 0).color(red, green, blue, alpha).tex(uMin, vMax).lightmap(0x00f0, 0x00f0).endVertex();
                buffer.pos(xMax, yMax, 0).color(red, green, blue, alpha).tex(uMax, vMax).lightmap(0x00f0, 0x00f0).endVertex();
                buffer.pos(xMax, yMin, 0).color(red, green, blue, alpha).tex(uMax, vMin).lightmap(0x00f0, 0x00f0).endVertex();
                break;
    
            case ROTATE_90:
                buffer.pos(xMin, yMin, 0).color(red, green, blue, alpha).tex(uMin, vMax).lightmap(0x00f0, 0x00f0).endVertex();
                buffer.pos(xMin, yMax, 0).color(red, green, blue, alpha).tex(uMax, vMax).lightmap(0x00f0, 0x00f0).endVertex();
                buffer.pos(xMax, yMax, 0).color(red, green, blue, alpha).tex(uMax, vMin).lightmap(0x00f0, 0x00f0).endVertex();
                buffer.pos(xMax, yMin, 0).color(red, green, blue, alpha).tex(uMin, vMin).lightmap(0x00f0, 0x00f0).endVertex();
                break;
    
            case ROTATE_180:
                buffer.pos(xMin, yMin, 0).color(red, green, blue, alpha).tex(uMax, vMax).lightmap(0x00f0, 0x00f0).endVertex();
                buffer.pos(xMin, yMax, 0).color(red, green, blue, alpha).tex(uMax, vMin).lightmap(0x00f0, 0x00f0).endVertex();
                buffer.pos(xMax, yMax, 0).color(red, green, blue, alpha).tex(uMin, vMin).lightmap(0x00f0, 0x00f0).endVertex();
                buffer.pos(xMax, yMin, 0).color(red, green, blue, alpha).tex(uMin, vMax).lightmap(0x00f0, 0x00f0).endVertex();
                break;
            
            case ROTATE_270:
                buffer.pos(xMin, yMin, 0).color(red, green, blue, alpha).tex(uMax, vMin).lightmap(0x00f0, 0x00f0).endVertex();
                buffer.pos(xMin, yMax, 0).color(red, green, blue, alpha).tex(uMin, vMin).lightmap(0x00f0, 0x00f0).endVertex();
                buffer.pos(xMax, yMax, 0).color(red, green, blue, alpha).tex(uMin, vMax).lightmap(0x00f0, 0x00f0).endVertex();
                buffer.pos(xMax, yMin, 0).color(red, green, blue, alpha).tex(uMax, vMax).lightmap(0x00f0, 0x00f0).endVertex();
                break;
        }
        
    }
 
    public static void renderFabricationProgress(RectRenderBounds.RadialRenderBounds bounds, MachineTileEntity te, int alpha)
    {
        Tessellator tes = Tessellator.getInstance();
        renderFabricationProgress(tes, tes.getBuffer(), bounds, te, alpha);
    }
    
    public static void renderFabricationProgress(Tessellator tessellator, BufferBuilder buffer, RadialRenderBounds bounds, MachineTileEntity te, int alpha)
    {
        renderProgress(bounds, te, alpha);
        
        if(te.clientState().controlState.getMachineState() == MachineState.FABRICATING)
        {
            MachineControlRenderer.renderItem(tessellator, buffer, bounds.innerBounds(), te.clientState().getStatusStack(), alpha);
        }
        else if(!te.clientState().isOn()) 
            return;
        else 
        {
            MachineControlRenderer.renderRadialSprite(tessellator, buffer, bounds, (int)(WorldInfo.currentTimeMillis() & 2047) * 360 / 2048, 30, 
                    Textures.MACHINE_GAUGE_FULL_MARKS, alpha << 24 | 0x40FF40);

            if(te.clientState().controlState.getMachineState() == MachineState.THINKING 
                    && te.clientState().bufferInfo.hasFailureCause() && MachineControlRenderer.warningLightBlinkOn())
            {
                MachineControlRenderer.renderSpriteInBounds(tessellator, buffer, bounds.innerBounds(), 
                        Textures.DECAL_MATERIAL_SHORTAGE.getSampleSprite(), alpha << 24 | 0xFFFF40, Rotation.ROTATE_NONE);
                MachineControlRenderer.renderSpriteInBounds(tessellator, buffer, bounds, Textures.MACHINE_GAUGE_INNER.getSampleSprite(), alpha << 24 | 0xFFFF40, Rotation.ROTATE_NONE);
            }
        }
    }
    
    public static void renderPower(
            RadialRenderBounds bounds, 
            ClientEnergyInfo mpi,
            int alpha)
    {
        Tessellator tes = Tessellator.getInstance();
        renderPower(tes, tes.getBuffer(), bounds, mpi, alpha);
    }

    public static void renderPower(
            Tessellator tessellator, 
            BufferBuilder buffer, 
            RadialRenderBounds bounds, 
            ClientEnergyInfo mpi,
            final int alpha)
    {
     
        if(mpi == null) return;
        
        if(bounds != null)
        {     
            final int alphaShifted = alpha << 24;
            
            // render marks
            MachineControlRenderer.renderSpriteInBounds(tessellator, buffer, bounds, Textures.MACHINE_GAGUE_MARKS.getSampleSprite(), (alpha << 24) | 0xFFFFFF, Rotation.ROTATE_NONE);

            // render level
            int arcLength = (int)(mpi.deviceDrawWatts() * 270 / mpi.maxDeviceDrawWatts());
            renderRadialSprite(tessellator, buffer, bounds, 225, arcLength, Textures.MACHINE_GAUGE_MAIN, alphaShifted | 0xFFFFBF);

            if(mpi.isFailureCause() && warningLightBlinkOn())
            {
                renderSpriteInBounds(tessellator, buffer, bounds,Textures.MACHINE_GAUGE_INNER.getSampleSprite(), alphaShifted | ModModels.COLOR_FAILURE, Rotation.ROTATE_NONE);
            }
            
            renderSpriteInBounds(tessellator, buffer, bounds.innerBounds(), Textures.DECAL_ELECTRICITY.getSampleSprite(), alphaShifted | ModModels.COLOR_POWER, Rotation.ROTATE_NONE); 

            renderMachineText(tessellator, buffer, ModModels.FONT_RENDERER_SMALL,
                    new RectRenderBounds(bounds.left(), bounds.top() + bounds.height() * 0.77, bounds.width(), bounds.height() * 0.25),
                    MachinePower.formatPower(Math.round(mpi.deviceDrawWatts()), false), HorizontalAlignment.CENTER, alphaShifted | ModModels.COLOR_POWER);
        }
    }
    
    public static void renderFuelCell(
            RadialRenderBounds bounds, 
            @Nonnull ClientEnergyInfo mpi,
            int alpha)
    {
        Tessellator tes = Tessellator.getInstance();
        renderGenerator(tes, tes.getBuffer(), bounds, mpi, alpha);
    }

    public static void renderGenerator(
            Tessellator tessellator, 
            BufferBuilder buffer, 
            RadialRenderBounds bounds, 
            @Nonnull ClientEnergyInfo mpi,
            final int alpha)
    {
       
        final int alphaShifted = alpha << 24;
        
        // render marks
        MachineControlRenderer.renderSpriteInBounds(tessellator, buffer, bounds, Textures.MACHINE_GAGUE_MARKS.getSampleSprite(), alphaShifted | 0xFFFFFF, Rotation.ROTATE_NONE);

        float output = mpi.generationWatts();
        
        if(output > 0)
        {
            // render level
            int arcLength = (int)(output * 270 / mpi.maxGenerationWatts());
            renderRadialSprite(tessellator, buffer, bounds, 225, arcLength, Textures.MACHINE_GAUGE_MAIN, alphaShifted | ModModels.COLOR_FUEL_CELL);
        }
        renderSpriteInBounds(tessellator, buffer, bounds.innerBounds(), Textures.DECAL_FLAME.getSampleSprite(), alphaShifted | ModModels.COLOR_FUEL_CELL, Rotation.ROTATE_NONE); 

        renderMachineText(tessellator, buffer, ModModels.FONT_RENDERER_SMALL,
                new RectRenderBounds(bounds.left(), bounds.top() + bounds.height() * GAUGE_LABEL_TOP, bounds.width(), bounds.height() * GAUGE_LABEL_HEIGHT),
                MachinePower.formatPower(Math.round(output), false), HorizontalAlignment.CENTER, alphaShifted | ModModels.COLOR_FUEL_CELL);
        
    }
    
    public static void renderBattery(
            RadialRenderBounds bounds, 
            @Nonnull ClientEnergyInfo mpi,
            int alpha)
    {
        Tessellator tes = Tessellator.getInstance();
        renderBattery(tes, tes.getBuffer(), bounds, mpi, alpha);
    }

    /** Where the battery texture starts 0-1 */
    private static final double POWER_LEFT = 0.066;
    /** Where the battery texture ends 0-1 */
    private static final double POWER_RIGHT = 0.9;
    private static final double POWER_WIDTH = POWER_RIGHT - POWER_LEFT;
    
    public static void renderBattery(
            Tessellator tessellator, 
            BufferBuilder buffer, 
            RadialRenderBounds bounds, 
            @Nonnull ClientEnergyInfo xmpi,
            final int alpha)
    {
        
        // render background
        MachineControlRenderer.renderSpriteInBounds(tessellator, buffer, bounds, Textures.MACHINE_POWER_BACKGROUND.getSampleSprite(), (alpha << 24) | 0xFFFFFF, Rotation.ROTATE_NONE);

        // render in/out level
        if(xmpi.netStorageWatts() > 0 && xmpi.maxChargeWatts() > 0)
        {
            int arcLength = Math.min(180, (int)(xmpi.netStorageWatts() * 180 / xmpi.maxChargeWatts()));
            renderRadialSprite(tessellator, buffer, bounds, 270, arcLength, Textures.MACHINE_GAUGE_MAIN, (alpha << 24) | ModModels.COLOR_BATTERY);
            
            renderMachineText(tessellator, buffer, ModModels.FONT_RENDERER_SMALL, new RectRenderBounds(bounds.left(), bounds.top() + bounds.height() * 0.3, bounds.width(), bounds.height() * 0.22),
                    MachinePower.formatPower(Math.round(xmpi.netStorageWatts()), true), HorizontalAlignment.CENTER, (alpha << 24) | ModModels.COLOR_BATTERY);
        }
        else if(xmpi.netStorageWatts() < 0 && xmpi.maxDischargeWatts() > 0)
        {
            int arcLength = (int)(-xmpi.netStorageWatts() * 180 / xmpi.maxDischargeWatts());
            renderRadialSprite(tessellator, buffer, bounds, 450 - arcLength, arcLength, Textures.MACHINE_GAUGE_MAIN, (alpha << 24) | ModModels.COLOR_BATTERY_DRAIN);
            
            renderMachineText(tessellator, buffer, ModModels.FONT_RENDERER_SMALL, new RectRenderBounds(bounds.left(), bounds.top() + bounds.height() * 0.3, bounds.width(), bounds.height() * 0.22),
                    MachinePower.formatPower((long) -xmpi.netStorageWatts(), false), HorizontalAlignment.CENTER, (alpha << 24) | ModModels.COLOR_BATTERY_DRAIN);
        }

        // render power storage
        long j = xmpi.storedEnergyJoules();
        if(j > 0)
        {
            EnhancedSprite sprite = Textures.MACHINE_POWER_FOREGROUND.getSampleSprite();
            
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);   
            double right = POWER_LEFT + POWER_WIDTH * j / xmpi.maxStoredEnergyJoules();
            bufferControlQuad(buffer, bounds.left(), bounds.top(), bounds.left() + right * bounds.width(), bounds.bottom(), 
                    sprite.safeMinU(), sprite.safeMinV(), sprite.safeInterpolatedU(right), sprite.safeMaxV(), 
                    alpha, 0xFF, 0xFF, 0xFF);
            tessellator.draw();
        }
            
        renderMachineText(tessellator, buffer, ModModels.FONT_RENDERER_SMALL,
                new RectRenderBounds(bounds.left(), bounds.top() + bounds.height() * 0.75, bounds.width(), bounds.height() * 0.3),
                MachinePower.formatEnergy((long) xmpi.storedEnergyJoules(), false), HorizontalAlignment.CENTER, (alpha << 24) | ModModels.COLOR_BATTERY);
        
    }
    
    public static void renderProgress(RectRenderBounds.RadialRenderBounds bounds, MachineTileEntity te, int alpha)
    {
        Tessellator tes = Tessellator.getInstance();
        renderProgress(tes, tes.getBuffer(), bounds, te, alpha);
    }

    public static void renderProgress(Tessellator tessellator, BufferBuilder buffer, RectRenderBounds.RadialRenderBounds bounds, MachineTileEntity te, int alpha)
    {
        int duration, remaining, arcLength;
        
        MachineStatusState statusState = te.clientState().statusState;
        if(statusState.hasBacklog())
        {    
            duration = statusState.getMaxBacklog();
            remaining = statusState.getCurrentBacklog();
            arcLength = duration > 0 ? 360 * (duration - remaining) / duration : 0;
            MachineControlRenderer.renderRadialSprite(tessellator, buffer, bounds, 0, arcLength, Textures.MACHINE_GAUGE_MAIN, alpha << 24 | 0x40FF40);
        }

        MachineControlState controlState = te.clientState().controlState;
        if(controlState.hasJobTicks())
        {    
            duration = controlState.getJobDurationTicks();
            remaining = controlState.getJobRemainingTicks();
            arcLength = duration > 0 ? 360 * (duration - remaining) / duration : 0;
            MachineControlRenderer.renderRadialSprite(tessellator, buffer, bounds, 0, arcLength, Textures.MACHINE_GAUGE_INNER, alpha << 24 | 0x40FFFF);
        }
    }

    public static boolean warningLightBlinkOn()
    {
        return (WorldInfo.currentTimeMillis() & 0x400) == 0x400;
    }

//    public static void renderGauge(RadialGaugeSpec spec, MachineTileEntity te, BufferDelegate materialBuffer, int alpha)
//    {
//        Tessellator tes = Tessellator.getInstance();
//        renderGauge(tes, tes.getBuffer(), spec, te, materialBuffer, alpha);
//    }
    
    /**
     * Use this version when you already have tessellator/buffer references on the stack.
     */
//    public static void renderGauge(Tessellator tessellator, BufferBuilder buffer, RadialGaugeSpec spec, MachineTileEntity te, BufferDelegate materialBuffer, int alpha)
//    {
//        // render marks
//        MachineControlRenderer.renderSpriteInBounds(tessellator, buffer, spec, Textures.MACHINE_GAGUE_MARKS.getSampleSprite(), (alpha << 24) | 0xFFFFFF, Rotation.ROTATE_NONE);
//
//        // render level
//        int arcLength = (int)(materialBuffer.fullness() * 270);
//        renderRadialSprite(tessellator, buffer, spec, 225, arcLength, Textures.MACHINE_GAUGE_MAIN, (alpha << 24) | (spec.color & 0xFFFFFF));
//
//        if(materialBuffer.isFailureCause() && warningLightBlinkOn())
//        {
//            renderSpriteInBounds(tessellator, buffer, spec, Textures.MACHINE_GAUGE_INNER.getSampleSprite(), (alpha << 24) | ModModels.COLOR_FAILURE, Rotation.ROTATE_NONE);
//        }
//        
//        /** Can look away from a machine for five seconds before flow tracking turns off to save CPU */
//        if(CommonProxy.currentTimeMillis() - te.lastInViewMillis() < 5000)
//        {
//            // log scale, anything less than one item is 1/10 of quarter arc, 64+ items is quarter arc
//            final float deltaIn = materialBuffer.getDeltaIn();
//            if(deltaIn > 0.012f)
//            {
//                int deltaLength = Math.round(deltaIn * 135);
//                renderRadialSprite(tessellator, buffer, spec, 225, deltaLength, Textures.MACHINE_GAUGE_INNER, (alpha << 24) | 0x20FF20);
//            }
//            
//            // log scale, anything less than one item is 1/10 of quarter arc, 64+ items is quarter arc
//            final float deltaOut = materialBuffer.getDeltaOut();
//            if(deltaOut > 0.012f)
//            {
//                int deltaLength =  Math.round(deltaOut * 135);
//                renderRadialSprite(tessellator, buffer, spec, 135 - deltaLength, deltaLength, Textures.MACHINE_GAUGE_INNER, (alpha << 24) | 0xFF2020);
//            }
//        }
//        
//        renderSpriteInBounds(tessellator, buffer, spec.spriteLeft, spec.spriteTop, spec.spriteSize, spec.spriteSize, spec.sprite, (alpha << 24) | (spec.color & 0xFFFFFF), 
//                spec.rotation);
//
//        renderMachineText(tessellator, buffer, ModModels.FONT_RENDERER_SMALL,
//                new RectRenderBounds(spec.left(), spec.top() + spec.height() * GAUGE_LABEL_TOP, spec.width(), spec.height() * GAUGE_LABEL_HEIGHT),
//                VolumeUnits.formatVolume(materialBuffer.getLevelNanoLiters(), false), HorizontalAlignment.CENTER, (alpha << 24) | 0xFFFFFF);
//
//        // draw text if provided
//        if(spec.formula != null)
//        {
//            RadialRenderBounds inner = spec.innerBounds();
//            // need to scale font pixelWidth to height of the line
//            double height = inner.height * 0.6;
//            double margin = (inner.width - ModModels.FONT_RENDERER_SMALL.getWidthFormula(spec.formula) * height / ModModels.FONT_RENDERER_SMALL.fontHeight) * 0.5;
//            ModModels.FONT_RENDERER_SMALL.drawFormula(inner.left + margin, inner.top + inner.height * 0.15, spec.formula, height, 0.02f, (spec.formulaColor >> 16) & 0xFF, (spec.formulaColor >> 8) & 0xFF, spec.formulaColor & 0xFF, alpha);
//        }
//    }
    
//    @Deprecated
//    public static void renderCMY(RadialRenderBounds bounds, BufferDelegate cyan, BufferDelegate magenta, BufferDelegate yellow, int alpha)
//    {
//        Tessellator tes = Tessellator.getInstance();
//        renderCMY(tes, tes.getBuffer(), bounds, cyan, magenta, yellow, alpha);
//    }
    
    /**
     * Use this version when you already have tessellator/buffer references on the stack.
     */
//    @Deprecated
//    public static void renderCMY(Tessellator tessellator, BufferBuilder buffer, RadialRenderBounds bounds, BufferDelegate cyan, BufferDelegate magenta, BufferDelegate yellow, int alpha)
//    {
//        // render marks
//        MachineControlRenderer.renderSpriteInBounds(tessellator, buffer, bounds, Textures.MACHINE_GAGUE_MARKS.getSampleSprite(), (alpha << 24) | 0xFFFFFF, Rotation.ROTATE_NONE);
//
//        final int alphaShifted = alpha << 24;
//        
//        // render levels
//        int start = 225;
//        int arcLength = (int)(cyan.fullness() * 90);
//        renderRadialSprite(tessellator, buffer, bounds, start, arcLength, Textures.MACHINE_GAUGE_MAIN, alphaShifted | MatterColors.CYAN);
//
//        start += arcLength;
//        arcLength = (int)(magenta.fullness() * 90);
//        renderRadialSprite(tessellator, buffer, bounds, start, arcLength, Textures.MACHINE_GAUGE_MAIN, alphaShifted | MatterColors.MAGENTA);
//        
//        start += arcLength;
//        arcLength = (int)(yellow.fullness() * 90);
//        renderRadialSprite(tessellator, buffer, bounds, start, arcLength, Textures.MACHINE_GAUGE_MAIN, alphaShifted | MatterColors.YELLOW);
//        
//        if(warningLightBlinkOn() && (cyan.isFailureCause() || magenta.isFailureCause() || yellow.isFailureCause()))
//        {
//            renderSpriteInBounds(tessellator, buffer, bounds, Textures.MACHINE_GAUGE_INNER.getSampleSprite(), alphaShifted | ModModels.COLOR_FAILURE, Rotation.ROTATE_NONE);
//        }
//        
//        renderSpriteInBounds(tessellator, buffer, bounds.innerBounds(), Textures.DECAL_CMY.getSampleSprite(), alphaShifted | 0xFFFFFF, Rotation.ROTATE_NONE);
//
//        int timeCheck = (int) (CommonProxy.currentTimeMillis() >> 10) % 3;
//        
//        if(timeCheck == 0)
//        {
//            renderMachineText(tessellator, buffer, ModModels.FONT_RENDERER_SMALL,
//                    new RectRenderBounds(bounds.left(), bounds.top() +bounds.height() * GAUGE_LABEL_TOP, bounds.width(), bounds.height() * GAUGE_LABEL_HEIGHT),
//                    VolumeUnits.formatVolume(cyan.getLevelNanoLiters(), false), HorizontalAlignment.CENTER, alphaShifted | MatterColors.CYAN);            
//        }
//        else if(timeCheck == 1)
//        {
//            renderMachineText(tessellator, buffer, ModModels.FONT_RENDERER_SMALL,
//                    new RectRenderBounds(bounds.left(), bounds.top() +bounds.height() * GAUGE_LABEL_TOP, bounds.width(), bounds.height() * GAUGE_LABEL_HEIGHT),
//                    VolumeUnits.formatVolume(magenta.getLevelNanoLiters(), false), HorizontalAlignment.CENTER, alphaShifted | MatterColors.MAGENTA);            
//        }
//        else
//        {
//            renderMachineText(tessellator, buffer, ModModels.FONT_RENDERER_SMALL,
//                    new RectRenderBounds(bounds.left(), bounds.top() +bounds.height() * GAUGE_LABEL_TOP, bounds.width(), bounds.height() * GAUGE_LABEL_HEIGHT),
//                    VolumeUnits.formatVolume(yellow.getLevelNanoLiters(), false), HorizontalAlignment.CENTER, alphaShifted | MatterColors.YELLOW);     
//        }
//
//      
//    }
    

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
    public static void renderItem(Tessellator tessellator, BufferBuilder buffer, AbstractRadialRenderBounds spec, @Nullable ItemStack stack, int alpha)
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
        GlStateManager.bindTexture(ModModels.TEX_BLOCKS);
        TextureHelper.setTextureBlurMipmap(true, true);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.disableColorMaterial();
        GlStateManager.depthMask(false);
        GlStateManager.enableAlpha();
 
 
        // prevent z-fighting
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(-1, -1);

        //        GlStateManager.enableAlpha();
        //        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);

        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }

 
    /** 
     * Call after {@link #setupMachineRendering()} to put things back to "normal" for GUI rendering.
     */
    public static void restoreGUIRendering()
    {
        TextureHelper.setTextureClamped(true);
        TextureHelper.setTextureBlurMipmap(false, true);
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
        TextureHelper.setTextureBlurMipmap(false, true);
        GlStateManager.enableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        GlStateManager.enableColorMaterial();
        GlStateManager.depthMask(false);
        GlStateManager.disablePolygonOffset();
    }

    public static void renderRedstoneControl(MachineTileEntity mte, Tessellator tessellator, BufferBuilder buffer, RadialRenderBounds boundsRedstone,
            int displayAlpha)
    {
        MachineControlRenderer.renderSpriteInBounds(tessellator, buffer, boundsRedstone, 
                mte.clientState().statusState.hasRedstonePower() ? ModModels.SPRITE_REDSTONE_TORCH_LIT : ModModels.SPRITE_REDSTONE_TORCH_UNLIT, 
                (displayAlpha << 24) | 0xFFFFFF, Rotation.ROTATE_NONE);

        if(!mte.clientState().isRedstoneControlEnabled()) 
        {
            MachineControlRenderer.renderSpriteInBounds(tessellator, buffer, boundsRedstone, 
                    Textures.DECAL_NO.getSampleSprite(), (displayAlpha << 24) | ModModels.COLOR_NO, Rotation.ROTATE_NONE);
        }
    }
}
