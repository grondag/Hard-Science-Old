package grondag.hard_science.gui;

import grondag.exotic_matter.model.texture.TextureScale;
import grondag.exotic_matter.varia.HorizontalAlignment;
import grondag.exotic_matter.varia.VerticalAlignment;
import grondag.exotic_matter.world.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiUtil
{

    public static final int MOUSE_LEFT = 0;
    public static final int MOUSE_MIDDLE = 2;
    public static final int MOUSE_RIGHT = 1;
    
    public static final double GOLDEN_RATIO = 1.618033988;
    /** 
     * Same as vanilla routine but accepts double values.  
     * Does not alter blend state so if you need alpha rendering have to do that before calling.
     * Doing it here causes problems because it doesn't know what to restore it to.
     */
    public static void drawRect(double left, double top, double right, double bottom, int color)
    {
        if (left < right)
        {
            double i = left;
            left = right;
            right = i;
        }
    
        if (top < bottom)
        {
            double j = top;
            top = bottom;
            bottom = j;
        }
    
        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();

//        GlStateManager.enableBlend();
//        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.color(f, f1, f2, f3);
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION);
        vertexbuffer.pos(left, bottom, 0.0D).endVertex();
        vertexbuffer.pos(right, bottom, 0.0D).endVertex();
        vertexbuffer.pos(right, top, 0.0D).endVertex();
        vertexbuffer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
//      GlStateManager.disableBlend();
    }

    /**
     * Draws a horizontal of the given pixelWidth between two points.
     */
    public static void drawHorizontalLine(double startX, double endX, double y, double width, int color)
    {
        if (endX < startX)
        {
            double x = startX;
            startX = endX;
            endX = x;
        }
        
        double halfWidth = width / 2;

        drawRect(startX - halfWidth, y - halfWidth, endX + halfWidth, y + halfWidth, color);
    }

    /**
     * Draws a vertical of the given pixelWidth between two points.
     */
    public static void drawVerticalLine(double x, double startY, double endY, double width, int color)
    {
        if (endY < startY)
        {
            double y = startY;
            startY = endY;
            endY = y;
        }

        double halfWidth = width / 2;
        
        drawRect(x - halfWidth, startY - halfWidth, x + halfWidth, endY + halfWidth, color);
    }
    
//  private static void drawLine(int x1, int y1, int x2, int y2, int color) {
//  float f3 = (color >> 24 & 255) / 255.0F;
//  float f = (color >> 16 & 255) / 255.0F;
//  float f1 = (color >> 8 & 255) / 255.0F;
//  float f2 = (color & 255) / 255.0F;
//  Tessellator tessellator = Tessellator.getInstance();
//  VertexBuffer buffer = tessellator.getBuffer();
//
//  buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
//  GlStateManager.enableBlend();
//  GlStateManager.disableTexture2D();
//  GlStateManager.disableDepth();
//  GL11.glLineWidth(2.0f);
//  GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
//  GlStateManager.color(f, f1, f2, f3);
//  buffer.pos(x1, y1, 0.0D).endVertex();
//  buffer.pos(x2, y2, 0.0D).endVertex();
//  tessellator.draw();
//  GlStateManager.enableTexture2D();
//  GlStateManager.enableDepth();
//  GlStateManager.disableBlend();
//}
    
    public static void drawBoxRightBottom(double left, double top, double right, double bottom, double lineWidth, int color)
    {
        drawVerticalLine(left, top, bottom, lineWidth, color);
        drawVerticalLine(right, top, bottom, lineWidth, color);
        drawHorizontalLine(left, right, top, lineWidth, color);
        drawHorizontalLine(left, right, bottom, lineWidth, color);
    }
    
    public static void drawBoxWidthHeight(double left, double top, double width, double height, double lineWidth, int color)
    {
        drawBoxRightBottom(left, top, left + width, top + height, lineWidth, color);
    }
    
    public static void drawQuad(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3, int color)
    {
        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(f, f1, f2, f3);
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION);
        vertexbuffer.pos(x0, y0, 0.0D).endVertex();
        vertexbuffer.pos(x1, y1, 0.0D).endVertex();
        vertexbuffer.pos(x2, y2, 0.0D).endVertex();
        vertexbuffer.pos(x3, y3, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
    
    /**
     * Draws a rectangle using the provide texture sprite and color
     */
    public static void drawTexturedRectWithColor(double xCoord, double yCoord, double zLevel, TextureAtlasSprite textureSprite, double widthIn, double heightIn, int color, Rotation rotation, boolean useAlpha)
    {
        drawTexturedRectWithColor(heightIn, heightIn, heightIn, textureSprite, heightIn, heightIn, color, TextureScale.SINGLE, rotation, useAlpha);
    }
    
    private static double[][] rotatedUV(double minU, double minV, double maxU, double maxV, Rotation rotation)
    {
        double[][] result = new double[2][4];
        
        int i;
        
        switch(rotation)
        {
        case ROTATE_NONE:
        default:
            i = 0;
            break;
            
        case ROTATE_90:
            i = 3;
            break;
            
        case ROTATE_180:
            i = 2;
            break;

        case ROTATE_270:
            i = 1;
            break;
        }
        
        result[0][i] = minU;
        result[1][i] = maxV;
        i = (i + 1) & 3;
        result[0][i] = maxU;
        result[1][i] = maxV;
        i = (i + 1) & 3;
        result[0][i] = maxU;
        result[1][i] = minV;
        i = (i + 1) & 3;
        result[0][i] = minU;
        result[1][i] = minV;
        
        return result;
    }
    public static void drawTexturedRectWithColor(double xCoord, double yCoord, double zLevel, TextureAtlasSprite textureSprite, double widthIn, double heightIn, int color, TextureScale scale, Rotation rotation, boolean useAlpha)
    {
        float alpha = (float)(color >> 24 & 255) / 255.0F;
        float red = (float)(color >> 16 & 255) / 255.0F;
        float green = (float)(color >> 8 & 255) / 255.0F;
        float blue = (float)(color & 255) / 255.0F;
        
        
        double minU = textureSprite.getMinU();
        double minV = textureSprite.getMinV();
        double maxU = minU + (textureSprite.getMaxU() - minU)  / scale.sliceCount;
        double maxV = minV + (textureSprite.getMaxV() - minV)  / scale.sliceCount;
        double uv[][] = rotatedUV(minU, minV, maxU, maxV, rotation);
        
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        GlStateManager.enableTexture2D();
        
        if(useAlpha)
        {
            GlStateManager.enableAlpha();  // should already be, but make sure
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
        else
        {
            GlStateManager.disableBlend();
            GlStateManager.disableAlpha();
        }
        
        GlStateManager.color(1, 1, 1, 1);

        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        vertexbuffer.pos((double)(xCoord + 0), (double)(yCoord + heightIn), zLevel)
            .tex(uv[0][0], uv[1][0])
            .color(red, green, blue, alpha).endVertex();
        vertexbuffer.pos((double)(xCoord + widthIn), (double)(yCoord + heightIn), zLevel)
            .tex(uv[0][1], uv[1][1])
            .color(red, green, blue, alpha).endVertex();
        vertexbuffer.pos((double)(xCoord + widthIn), (double)(yCoord + 0), zLevel)
            .tex(uv[0][2], uv[1][2])
            .color(red, green, blue, alpha).endVertex();
        vertexbuffer.pos((double)(xCoord + 0), (double)(yCoord + 0), zLevel)
            .tex(uv[0][3], uv[1][3])
            .color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        
        if(useAlpha)
        {
            GlStateManager.disableBlend();
        }
        else
        {
            GlStateManager.enableAlpha();
        }
        
        textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        
    }
    
    public static void playPressedSound(Minecraft mc)
    {
        mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
    
    public static boolean  renderItemAndEffectIntoGui(IGuiRenderContext renderContext, ItemStack itm, double x, double y, double contentSize) 
    {
        return renderItemAndEffectIntoGui(renderContext.minecraft(), renderContext.renderItem(), itm, x, y, contentSize);
    }
    
    /** 
     * Size is in pixels.
     * Hat tip to McJty.
     */
    public static boolean renderItemAndEffectIntoGui(Minecraft mc, RenderItem itemRender, ItemStack itm, double x, double y, double contentSize)
    {
        boolean rc = false;

        if (itm != null && itm.getItem() != null) {
            rc = true;
            
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, 0);
            GlStateManager.scale(1 / 16f, 1 / 16f, 1 / 16f);
            GlStateManager.scale(contentSize, contentSize, contentSize);

            GlStateManager.enableRescaleNormal();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableDepth();
 
            itemRender.renderItemAndEffectIntoGUI(itm, 0, 0);

            GlStateManager.popMatrix();
            GlStateManager.enableLighting();
            RenderHelper.disableStandardItemLighting();
//            RenderHelper.enableStandardItemLighting();
        }

        return rc;
    }
    
    /**
     * Renders the specified text to the screen, center-aligned. Args : renderer, string, x, y, color
     */
    public static void drawAlignedStringNoShadow(FontRenderer fontRendererIn, String text, float x, float y, float width, float height, int color, HorizontalAlignment hAlign, VerticalAlignment vAlign)
    {

        switch(hAlign)
        {
        case RIGHT:
            x += width - fontRendererIn.getStringWidth(text);
            break;
            
        case CENTER:
            x += (width - fontRendererIn.getStringWidth(text)) / 2;
            break;
            
        case LEFT:
        default:
            break;
        
        }
        
        switch(vAlign)
        {
        case BOTTOM:
            y += height - fontRendererIn.FONT_HEIGHT;
            break;
            
        case MIDDLE:
            y += (height - fontRendererIn.FONT_HEIGHT) / 2;
            break;
            
        case TOP:
        default:
            break;
        
        }
        fontRendererIn.drawString(text, x, y, color, false);
    }
    
    public static void drawAlignedStringNoShadow(FontRenderer fontRendererIn, String text, double x, double y, double width, double height, int color, HorizontalAlignment hAlign, VerticalAlignment vAlign)
    {
        drawAlignedStringNoShadow(fontRendererIn, text, (float)x, (float)y, (float)width, (float)height, color, hAlign, vAlign);
    }

    /**
     * Renders the specified text to the screen. Args : renderer, string, x, y, color
     */
    public static void drawStringNoShadow(FontRenderer fontRendererIn, String text, int x, int y, int color)
    {
        fontRendererIn.drawString(text, x, y, color, false);
    }
    
}
