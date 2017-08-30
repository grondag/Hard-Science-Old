package grondag.hard_science.library.font;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTBgra;
import org.lwjgl.opengl.EXTTextureCompressionS3TC;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.glu.GLU;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

/**
 * TrueTyper: Open Source TTF implementation for Minecraft. Modified from Slick2D - under BSD Licensing - http://slick.ninjacave.com/license/
 * <p/>
 * Copyright (c) 2013 - Slick2D
 * <p/>
 * All rights reserved.
 * 
 * Grondag: note this has been heavily modified from original.
 */

public class TrueTypeFont
{
    /**
     * Array that holds necessary information about the font characters
     */
    private GlyphInfo[] glyphArray = new GlyphInfo[256];

    /**
     * Map of user defined font characters (Character <-> IntObject)
     */
    private Int2ObjectOpenHashMap<GlyphInfo> customGlyphs = new Int2ObjectOpenHashMap<GlyphInfo>();

    /**
     * Boolean flag on whether AntiAliasing is enabled or not
     */
    protected boolean antiAlias;

    /**
     * Font's size
     */
    private float fontSize = 0;

    /**
     * Font's height
     */
    private float fontHeight = 0;

    /**
     * Texture used to cache the font 0-255 characters
     */
    private int fontTextureID;

    /**
     * Default font texture width
     */
    private int textureWidth = 1024;

    /**
     * Default font texture height
     */
    private int textureHeight = 1024;

    /**
     * A reference to Java's AWT Font that we create our font texture from
     */
    protected Font font;

    /**
     * The font metrics for our Java AWT font
     */
    private FontMetrics fontMetrics;


    private class GlyphInfo
    {

        /**
         * Character's width
         */
        public final int width;

        /**
         * Character's height
         */
        public final int height;

        /**
         * Character's x location within texture
         */
        @SuppressWarnings("unused")
        public final int positionX;

        /**
         * Character's y position within texture
         */
        @SuppressWarnings("unused")
        public final int positionY;
        
        public final float uMin;
        public final float vMin;
        public final float uMax;
        public final float vMax;
        
        public GlyphInfo(int width, int height, int positionX, int positionY)
        {
            this.positionX = positionX;
            this.positionY = positionY;
            this.width = width;
            this.height = height;
            this.uMin = (float) positionX / textureWidth;
            this.uMax = (float) (positionX + width) / textureWidth;
            this.vMin = (float) positionY / textureHeight;
            this.vMax = (float) (positionY + height) / textureHeight;
        }
    }

    public TrueTypeFont(Font font, boolean antiAlias, char[] additionalChars)
    {
        this.font = font;
        this.fontSize = font.getSize() + 3;
        this.antiAlias = antiAlias;

        createSet(additionalChars);
        System.out.println("TrueTypeFont loaded: " + font + " - AntiAlias = " + antiAlias);
        fontHeight -= 1;
        if (fontHeight <= 0)
            fontHeight = 1;
    }

    public TrueTypeFont(Font font, boolean antiAlias)
    {
        this(font, antiAlias, null);
    }

    private BufferedImage getFontImage(char ch)
    {
        // Create a temporary image to extract the character's size
        BufferedImage tempfontImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) tempfontImage.getGraphics();
        if (antiAlias)
        {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g.setFont(font);
        fontMetrics = g.getFontMetrics();
        float charwidth = fontMetrics.charWidth(ch) + 8;

        if (charwidth <= 0)
        {
            charwidth = 7;
        }
        float charheight = fontMetrics.getHeight() + 3;
        if (charheight <= 0)
        {
            charheight = fontSize;
        }

        // Create another image holding the character we are creating
        BufferedImage fontImage;
        fontImage = new BufferedImage((int) charwidth, (int) charheight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gt = (Graphics2D) fontImage.getGraphics();
        if (antiAlias)
        {
            gt.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        gt.setFont(font);

        gt.setColor(Color.WHITE);
        int charx = 3;
        int chary = 1;
        gt.drawString(String.valueOf(ch), (charx), (chary) + fontMetrics.getAscent());

        return fontImage;

    }

    private void createSet(char[] customCharsArray)
    {
        // If there are custom chars then I expand the font texture twice
        if (customCharsArray != null && customCharsArray.length > 0)
        {
            textureWidth *= 2;
        }

        // In any case this should be done in other way. Texture with size 512x512
        // can maintain only 256 characters with resolution of 32x32. The texture
        // size should be calculated dynamicaly by looking at character sizes.

        try
        {

            BufferedImage imgTemp = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) imgTemp.getGraphics();

            g.setColor(new Color(0, 0, 0, 1));
            g.fillRect(0, 0, textureWidth, textureHeight);

            int rowHeight = 0;
            int positionX = 0;
            int positionY = 0;

            int customCharsLength = (customCharsArray != null) ? customCharsArray.length : 0;

            for (int i = 0; i < 256 + customCharsLength; i++)
            {

                // get 0-255 characters and then custom characters
                char ch = (i < 256) ? (char) i : customCharsArray[i - 256];

                BufferedImage fontImage = getFontImage(ch);


                
                int w = fontImage.getWidth();
                int h = fontImage.getHeight();

                if (positionX + w >= textureWidth)
                {
                    positionX = 0;
                    positionY += rowHeight;
                    rowHeight = 0;
                }
                
                GlyphInfo glyph = new GlyphInfo(w, h, positionX, positionY);

                if (glyph.height > fontHeight)
                {
                    fontHeight = glyph.height;
                }

                if (glyph.height > rowHeight)
                {
                    rowHeight = glyph.height;
                }

                // Draw it here
                g.drawImage(fontImage, positionX, positionY, null);

                positionX += glyph.width;

                if (i < 256)
                { // standard characters
                    glyphArray[i] = glyph;
                }
                else
                { // custom characters
                    customGlyphs.put(ch, glyph);
                }

                fontImage = null;
            }

            fontTextureID = loadImage(imgTemp);

            // .getTexture(font.toString(), imgTemp);

        }
        catch (Exception e)
        {
            System.err.println("Failed to create font.");
            e.printStackTrace();
        }
    }

    public float getWidth(String text)
    {
//        float totalwidth = 0;
//        int currentChar = 0;
//        float lastWidth = -10f;
//        for (int i = 0; i < whatchars.length(); i++)
//        {
//            currentChar = whatchars.charAt(i);
//            GlyphInfo floatObject;
//            if (currentChar < 256)
//            {
//                floatObject = glyphArray[currentChar];
//            }
//            else
//            {
//                floatObject = customGlyphs.get(currentChar);
//            }
//
//            if (floatObject != null)
//            {
//                totalwidth += floatObject.width / 2;
//                lastWidth = floatObject.width;
//            }
//        }
        // System.out.println("Size: "+totalwidth);
        return this.fontMetrics.stringWidth(text);
        // return (totalwidth);
    }

    public float getHeight()
    {
        return fontHeight;
    }

    public float getHeight(String HeightString)
    {
        return fontHeight;
    }

    public float getLineHeight()
    {
        return fontHeight;
    }

    public String trimStringToWidth(String text, int width)
    {
        StringBuilder stringbuilder = new StringBuilder();
        int i = 0;
        int j = 0;
        int k = 1;
        boolean flag = false;
        boolean flag1 = false;

        for (int l = j; l >= 0 && l < text.length() && i < width; l += k)
        {
            char c0 = text.charAt(l);
            int i1 = (int) this.getWidth("" + c0);

            if (flag)
            {
                flag = false;

                if (c0 != 108 && c0 != 76)
                {
                    if (c0 == 114 || c0 == 82)
                    {
                        flag1 = false;
                    }
                }
                else
                {
                    flag1 = true;
                }
            }
            else if (i1 < 0)
            {
                flag = true;
            }
            else
            {
                i += i1;

                if (flag1)
                {
                    ++i;
                }
            }

            if (i > width)
            {
                break;
            }

            stringbuilder.append(c0);
        }

        return stringbuilder.toString();
    }

    /**
     * Draws a single line of text at the given x, y coordinate, with z depth
     * Rendering will start at x and y and extend right and down.
     * GL matrix should be set to that +y is in the down direction for the viewer.
     */
    public void drawLine(float xLeft, float yTop, String text, float lineHeight, float zDepth, int red, int green, int blue, int alpha)
    {

        GlStateManager.bindTexture(fontTextureID);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        float x = xLeft;
        float scaleFactor = lineHeight / this.fontHeight;
        
        for(char c : text.toCharArray())
        {
            GlyphInfo g = getGlyph(c);
            drawQuad(buffer, x, yTop, scaleFactor, g, red, green, blue, alpha);
            x += g.width * scaleFactor;
        }
        Tessellator.getInstance().draw();

    }

    private GlyphInfo getGlyph(char c)
    {
        return c < 256 ? glyphArray[c] : customGlyphs.get(c);
    }
    
   

    private void drawQuad(BufferBuilder buffer, float xLeft, float yTop, float scaleFactor, GlyphInfo glyph, int red, int green, int blue, int alpha)
    {
        double xRight = xLeft + glyph.width * scaleFactor;
        double yBottom = yTop + glyph.height * scaleFactor;

        buffer.pos(xLeft, yTop, 0).color(red, green, blue, alpha).tex(glyph.uMin, glyph.vMin).lightmap(0x00f0, 0x00f0).endVertex();
        buffer.pos(xLeft, yBottom, 0).color(red, green, blue, alpha).tex(glyph.uMin, glyph.vMax).lightmap(0x00f0, 0x00f0).endVertex();
        buffer.pos(xRight, yBottom, 0).color(red, green, blue, alpha).tex(glyph.uMax, glyph.vMax).lightmap(0x00f0, 0x00f0).endVertex();
        buffer.pos(xRight, yTop, 0).color(red, green, blue, alpha).tex(glyph.uMax, glyph.vMin).lightmap(0x00f0, 0x00f0).endVertex();
    }
    
    public static int loadImage(BufferedImage bufferedImage)
    {
        try
        {
            short width = (short) bufferedImage.getWidth();
            short height = (short) bufferedImage.getHeight();
            // textureLoader.bpp = bufferedImage.getColorModel().hasAlpha() ? (byte)32 : (byte)24;
            int bpp = (byte) bufferedImage.getColorModel().getPixelSize();
            ByteBuffer byteBuffer;
            DataBuffer db = bufferedImage.getData().getDataBuffer();
            if (db instanceof DataBufferInt)
            {
                int intI[] = ((DataBufferInt) (bufferedImage.getData().getDataBuffer())).getData();
                byte newI[] = new byte[intI.length * 4];
                for (int i = 0; i < intI.length; i++)
                {
                    byte b[] = intToByteArray(intI[i]);
                    int newIndex = i * 4;

                    newI[newIndex] = b[1];
                    newI[newIndex + 1] = b[2];
                    newI[newIndex + 2] = b[3];
                    newI[newIndex + 3] = b[0];
                }

                byteBuffer = ByteBuffer.allocateDirect(width * height * (bpp / 8)).order(ByteOrder.nativeOrder()).put(newI);
            }
            else
            {
                byteBuffer = ByteBuffer.allocateDirect(width * height * (bpp / 8)).order(ByteOrder.nativeOrder())
                        .put(((DataBufferByte) (bufferedImage.getData().getDataBuffer())).getData());
            }
            byteBuffer.flip();

            int internalFormat = GL11.GL_RGBA8, format = GL11.GL_RGBA;
            IntBuffer textureId = BufferUtils.createIntBuffer(1);

            GL11.glGenTextures(textureId);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId.get(0));

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_NEAREST);

            GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

            //GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
            
//            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, EXTTextureCompressionS3TC.GL_COMPRESSED_RGBA_S3TC_DXT5_EXT, width, width, 0, EXTBgra.GL_BGRA_EXT, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, DATA_BUFFER[lod]);

            GLU.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, internalFormat, width, height, format, GL11.GL_UNSIGNED_BYTE, byteBuffer);
            return textureId.get(0);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return -1;
    }

    public static boolean isSupported(String fontname)
    {
        Font font[] = getFonts();
        for (int i = font.length - 1; i >= 0; i--)
        {
            if (font[i].getName().equalsIgnoreCase(fontname))
                return true;
        }
        return false;
    }

    public static Font[] getFonts()
    {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    }

    public static byte[] intToByteArray(int value)
    {
        return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
    }

    public void destroy()
    {
        IntBuffer scratch = BufferUtils.createIntBuffer(1);
        scratch.put(0, fontTextureID);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL11.glDeleteTextures(scratch);
    }
}