package grondag.adversity.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;

public class GuiUtil
{

    public static final double GOLDEN_RATIO = 1.618033988;
    /** 
     * Same as vanilla routine but accepts double values
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
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(f, f1, f2, f3);
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION);
        vertexbuffer.pos(left, bottom, 0.0D).endVertex();
        vertexbuffer.pos(right, bottom, 0.0D).endVertex();
        vertexbuffer.pos(right, top, 0.0D).endVertex();
        vertexbuffer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    /**
     * Draws a horizontal of the given width between two points.
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
     * Draws a vertical of the given width between two points.
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
    
    public static void drawBox(double left, double top, double right, double bottom, double lineWidth, int color)
    {
        drawVerticalLine(left, top, bottom, lineWidth, color);
        drawVerticalLine(right, top, bottom, lineWidth, color);
        drawHorizontalLine(left, right, top, lineWidth, color);
        drawHorizontalLine(left, right, bottom, lineWidth, color);
    }
    
    public static void drawQuad(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3, int color)
    {
        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
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
    public static void drawTexturedRectWithColor(double xCoord, double yCoord, double zLevel, TextureAtlasSprite textureSprite, double widthIn, double heightIn, int color)
    {
        float alpha = (float)(color >> 24 & 255) / 255.0F;
        float red = (float)(color >> 16 & 255) / 255.0F;
        float green = (float)(color >> 8 & 255) / 255.0F;
        float blue = (float)(color & 255) / 255.0F;
        
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1, 1, 1, 1);

        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        vertexbuffer.pos((double)(xCoord + 0), (double)(yCoord + heightIn), zLevel)
            .tex((double)textureSprite.getMinU(), (double)textureSprite.getMaxV())
            .color(red, green, blue, alpha).endVertex();
        vertexbuffer.pos((double)(xCoord + widthIn), (double)(yCoord + heightIn), zLevel)
            .tex((double)textureSprite.getMaxU(), (double)textureSprite.getMaxV())
            .color(red, green, blue, alpha).endVertex();
        vertexbuffer.pos((double)(xCoord + widthIn), (double)(yCoord + 0), zLevel)
            .tex((double)textureSprite.getMaxU(), (double)textureSprite.getMinV())
            .color(red, green, blue, alpha).endVertex();
        vertexbuffer.pos((double)(xCoord + 0), (double)(yCoord + 0), zLevel)
            .tex((double)textureSprite.getMinU(), (double)textureSprite.getMinV())
            .color(red, green, blue, alpha).endVertex();
        tessellator.draw();
    }
    
    public static void playPressedSound(Minecraft mc)
    {
        mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
    
    // hat tip to McJty
    public static boolean renderItemAndEffectIntoGui(Minecraft mc, RenderItem itemRender, ItemStack itm, double x, double y, double scale)
    {
        GlStateManager.color(1F, 1F, 1F);

        boolean rc = false;

        if (itm != null && itm.getItem() != null) {
            rc = true;
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 0.0F, 32.0F);
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.scale(scale, scale, 1);
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableLighting();
            short short1 = 240;
            short short2 = 240;
            net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, short1 / 1.0F, short2 / 1.0F);
            itemRender.renderItemAndEffectIntoGUI(itm, (int)(x / scale), (int)(y / scale));
//            renderItemOverlayIntoGUI(mc.fontRenderer, itm, x, y, txt, txt.length() - 2);
//            itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, itm, x, y, txt);
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableLighting();
        }

        return rc;
    }

    /**
     * Renders the specified text to the screen, center-aligned. Args : renderer, string, x, y, color
     */
    public static void drawCenteredStringNoShadow(FontRenderer fontRendererIn, String text, float x, float y, int color)
    {
        fontRendererIn.drawString(text, x - fontRendererIn.getStringWidth(text) / 2, y, color, false);
    }

    /**
     * Renders the specified text to the screen. Args : renderer, string, x, y, color
     */
    public static void drawStringNoShadow(FontRenderer fontRendererIn, String text, int x, int y, int color)
    {
        fontRendererIn.drawString(text, x, y, color, false);
    }
    
}
