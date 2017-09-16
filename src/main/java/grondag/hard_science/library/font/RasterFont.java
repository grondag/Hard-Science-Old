package grondag.hard_science.library.font;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import grondag.hard_science.init.ModModels;
import grondag.hard_science.library.render.CubeInputs;
import grondag.hard_science.library.render.QuadBakery;
import grondag.hard_science.library.render.RawQuad;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;

/**
 * Static version of TrueTypeFont that uses the block texture map.
 * Enables basic text rendering on blocks and items.
 */

public class RasterFont
{
    /**
     * Array that holds necessary information about the font characters
     */
    private GlyphInfo[] glyphArray = new GlyphInfo[256];

    private final TextureAtlasSprite sprite;

    /** determined by first character - assumes all the same */
    public final int fontHeight;
    
    /** 
     * Adjust horizontal spacing if font needs it.
     */
    public final int horizontalSpacing;
    
    /**
     * Pixel width to use for monospace rendering of numbers.
     */ 
    public final int monoWidth;

    public class GlyphInfo
    {

        /**
         * Character's pixelWidth
         */
        public final int pixelWidth;
        
        /**
         * Includes the padding constant.
         */
        public final int renderWidth;

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
        
        public final float uInterpolatedMin;
        public final float vInterpolatedMin;
        public final float uInterpolatedMax;
        public final float vInterpolatedMax;
        
        /** 
         * Shift character this many pixels right if rendering monospace.
         */
        public final int monoOffset;
        
        private GlyphInfo(int width, int height, int positionX, int positionY, boolean enableMonospace)
        {
            this.positionX = positionX;
            this.positionY = positionY;
            this.pixelWidth = width;
            this.renderWidth = width + horizontalSpacing;
            this.height = height;
            int size = sprite.getIconWidth();
            
            this.uMin = 16f * positionX / size;
            this.uMax = 16f * (positionX + width) / size;
            this.vMin = 16f * positionY / size;
            this.vMax = 16f * (positionY + height) / size;
            
            this.uInterpolatedMin = sprite.getInterpolatedU(this.uMin);
            this.uInterpolatedMax = sprite.getInterpolatedU(this.uMax);
            this.vInterpolatedMin = sprite.getInterpolatedV(this.vMin);
            this.vInterpolatedMax = sprite.getInterpolatedV(this.vMax);
            this.monoOffset = enableMonospace ? (monoWidth - width) / 2 : 0;
        }
    }
    
    
    /**
     * Array order is character, pixelWidth, height, xLeft, yTop
     */
    public RasterFont(TextureAtlasSprite sprite, int horizontalSpacing, int[][] fontData)
    {
        this.sprite = sprite;
        this.horizontalSpacing = horizontalSpacing;
        this.fontHeight = fontData[0][2];
        
        int maxWidth = 0;
        for(int[] data : fontData)
        {
            if(Character.isDigit(data[0]) && data[1] > maxWidth) maxWidth = data[1];
        }
        this.monoWidth = maxWidth;
        
        for(int[] data : fontData)
        {
            this.glyphArray[data[0]] = new GlyphInfo(data[1], data[2], data[3], data[4], Character.isDigit(data[0]));
        }
    }

    /**
     * This has been hacked up to create the raster - has no use during run time.
     */
//    private void createSet(char[] customCharsArray)
//    {
//        // If there are custom chars then I expand the font texture twice
//        if (customCharsArray != null && customCharsArray.length > 0)
//        {
//            textureWidth *= 2;
//        }
//
//        // In any case this should be done in other way. Texture with size 512x512
//        // can maintain only 256 characters with resolution of 32x32. The texture
//        // size should be calculated dynamicaly by looking at character sizes.
//
//        try
//        {
//
//            BufferedImage glyphMap = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
//            Graphics2D g = (Graphics2D) glyphMap.getGraphics();
//
//            g.setColor(new Color(0, 0, 0, 1));
//            g.fillRect(0, 0, textureWidth, textureHeight);
//
//            int positionX = 0;
//            int positionY = 0;
//
//            String usedChars = "+-%.=?!/0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
//            for (char ch : usedChars.toCharArray())
//            {
//
//                BufferedImage fontImage = getFontImage(ch);
//
//
//                if (positionX + fontImage.getWidth() >= textureWidth)
//                {
//                    positionX = 0;
//                    positionY += this.fontHeight;
//                }
//                
//                GlyphInfo glyph = new GlyphInfo(fontImage.getWidth() - GLYPH_MARGIN * 2, this.fontHeight, positionX + GLYPH_MARGIN, positionY);
//
//
//                // Draw it here
//                g.drawImage(fontImage, positionX, positionY, null);
//
//                positionX += fontImage.getWidth();
//
//                Log.info("{ %d, %d, %d, %d, %d },", (int)ch, glyph.width, glyph.height, glyph.positionX, glyph.positionY);
//                
//                glyphArray[ch] = glyph;
//              
//
//                fontImage = null;
//            }
//
//            //used when creating the raster - quick hack, not pretty
//            File file = new File("font.png");
//            if(file.exists()) file.delete();
//            file.createNewFile();
//            FileImageOutputStream output = new FileImageOutputStream(file);
//            PNGImageWriterSpi spiPNG = new PNGImageWriterSpi();
//            PNGImageWriter writer = (PNGImageWriter) spiPNG.createWriterInstance();
//            writer.setOutput(output);
//            writer.write(glyphMap);
//            output.close();
//
//
//            
//            fontTextureID = loadImage(glyphMap);
//
//        }
//        catch (Exception e)
//        {
//            System.err.println("Failed to create font.");
//            e.printStackTrace();
//        }
//    }

    public GlyphInfo getGlyphInfo(char c)
    {
        if(c < 0 || c > 255) return null;
        return this.glyphArray[c];
    }
    
    public int getWidth(String text)
    {
        int result = 0;
        for(char c : text.toCharArray())
        {
            if(this.glyphArray[c] != null) result += this.glyphArray[c].renderWidth;
        }
        return result;
    }
    
    public int getMonospacedWidth(String text)
    {
        int result = 0;
        for(char c : text.toCharArray())
        {
            if(this.glyphArray[c] != null) result += this.glyphArray[c].monoOffset > 0 ? this.monoWidth : this.glyphArray[c].renderWidth;
        }
        return result;
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
    public void drawLine(double xLeft, double yTop, String text, double lineHeight, double zDepth, int red, int green, int blue, int alpha)
    {
        GlStateManager.bindTexture(ModModels.TEX_BLOCKS);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        double x = xLeft;
        double scaleFactor = lineHeight / this.fontHeight;
        
        for(char c : text.toCharArray())
        {
            GlyphInfo g = this.glyphArray[c];
            if(g != null)
            {
            bufferQuad(buffer, x, yTop, scaleFactor, g, red, green, blue, alpha);
            x += (g.renderWidth) * scaleFactor;
            }
        }
        Tessellator.getInstance().draw();

    }

    /**
     * Just like {@link #drawLine(double, double, String, double, double, int, int, int, int)} but
     * with monospace character spacing for numbers. 
     */
    public void drawLineMonospaced(double xLeft, double yTop, String text, double lineHeight, double zDepth, int red, int green, int blue, int alpha)
    {
        GlStateManager.bindTexture(ModModels.TEX_BLOCKS);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        double x = xLeft;
        double scaleFactor = lineHeight / this.fontHeight;
        
        for(char c : text.toCharArray())
        {
            GlyphInfo g = this.glyphArray[c];
            if(g != null)
            {
            bufferQuad(buffer, x + g.monoOffset * scaleFactor, yTop, scaleFactor, g, red, green, blue, alpha);
            x += (g.monoOffset > 0 ? this.monoWidth : g.pixelWidth) * scaleFactor;
            }
        }
        Tessellator.getInstance().draw();

    }
    
    private void bufferQuad(BufferBuilder buffer, double xLeft, double yTop, double scaleFactor, GlyphInfo glyph, int red, int green, int blue, int alpha)
    {
        double xRight = xLeft + glyph.pixelWidth * scaleFactor;
        double yBottom = yTop + glyph.height * scaleFactor;

        buffer.pos(xLeft, yTop, 0).color(red, green, blue, alpha).tex(glyph.uInterpolatedMin, glyph.vInterpolatedMin).lightmap(0x00f0, 0x00f0).endVertex();
        buffer.pos(xLeft, yBottom, 0).color(red, green, blue, alpha).tex(glyph.uInterpolatedMin, glyph.vInterpolatedMax).lightmap(0x00f0, 0x00f0).endVertex();
        buffer.pos(xRight, yBottom, 0).color(red, green, blue, alpha).tex(glyph.uInterpolatedMax, glyph.vInterpolatedMax).lightmap(0x00f0, 0x00f0).endVertex();
        buffer.pos(xRight, yTop, 0).color(red, green, blue, alpha).tex(glyph.uInterpolatedMax, glyph.vInterpolatedMin).lightmap(0x00f0, 0x00f0).endVertex();
    }
    
    public List<BakedQuad> getBlockQuadsForText(String text)
    {
        CubeInputs cube = new CubeInputs();
        cube.textureName = ModModels.FONT_RESOURCE_STRING;
        GlyphInfo g = this.glyphArray['A'];
        cube.u0 = g.uInterpolatedMin;
        cube.u1 = g.uInterpolatedMax;
        cube.v0 = g.vInterpolatedMin;
        cube.v1 = g.vInterpolatedMax;
        cube.color = 0xFF808080;
        
        ArrayList<BakedQuad> result = new ArrayList<BakedQuad>();
        
        result.add(QuadBakery.createBakedQuad(cube.makeRawFace(EnumFacing.DOWN)));
        result.add(QuadBakery.createBakedQuad(cube.makeRawFace(EnumFacing.UP)));
        result.add(QuadBakery.createBakedQuad(cube.makeRawFace(EnumFacing.EAST)));
        result.add(QuadBakery.createBakedQuad(cube.makeRawFace(EnumFacing.WEST)));
        result.add(QuadBakery.createBakedQuad(cube.makeRawFace(EnumFacing.SOUTH)));
        result.add(QuadBakery.createBakedQuad(cube.makeRawFace(EnumFacing.NORTH)));
        
        return result;
    }
    
    /**
     * Generates raw quads to render the given chemical formula on a block face.  
     * The quads are oriented to be readable and are positioned in the top half of the block.
     * Assumes the quds will be rendered on a typical 1x1 square block face. 
     */
    public void formulaBlockQuadsToList(String formula, List<RawQuad> list)
    {
        
    }
}