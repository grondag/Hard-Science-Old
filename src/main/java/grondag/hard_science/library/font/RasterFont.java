package grondag.hard_science.library.font;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import grondag.hard_science.init.ModModels;
import grondag.hard_science.library.render.CubeInputs;
import grondag.hard_science.library.render.FaceVertex;
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
        public final int positionX;

        /**
         * Character's y position within texture
         */
        public final int positionY;
        
        /** texture coordinate relative to sprite, scaled 0-16 */
        public final float uMinMinecraft;
        /** texture coordinate relative to sprite, scaled 0-16 */
        public final float vMinMinecraft;
        /** texture coordinate relative to sprite, scaled 0-16 */
        public final float uMaxMinecraft;
        /** texture coordinate relative to sprite, scaled 0-16 */
        public final float vMaxMinecraft;
        
        /** texture coordinate relative to sprite, scaled 0-1 */
        public final float uMinNormal;
        /** texture coordinate relative to sprite, scaled 0-1 */
        public final float vMinNormal;
        /** texture coordinate relative to sprite, scaled 0-1 */
        public final float uMaxNormal;
        /** texture coordinate relative to sprite, scaled 0-1 */
        public final float vMaxNormal;

        /** texture coordinate within OpenGL texture, scaled 0-1  */
        public final float interpolatedMinU;
        /** texture coordinate within OpenGL texture, scaled 0-1  */
        public final float interpolatedMinV;
        /** texture coordinate within OpenGL texture, scaled 0-1  */
        public final float interpolatedMaxU;
        /** texture coordinate within OpenGL texture, scaled 0-1  */
        public final float interpolatedMaxV;
        
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
            
            
            this.uMinNormal = (float) positionX / size;
            this.uMaxNormal = (float) (positionX + width) / size;
            this.vMinNormal = (float) positionY / size;
            this.vMaxNormal = (float) (positionY + height) / size;

            this.uMinMinecraft = 16f * uMinNormal;
            this.uMaxMinecraft = 16f * uMaxNormal;
            this.vMinMinecraft = 16f * vMinNormal;
            this.vMaxMinecraft = 16f * vMaxNormal;
            
            this.interpolatedMinU = sprite.getInterpolatedU(this.uMinMinecraft);
            this.interpolatedMaxU = sprite.getInterpolatedU(this.uMaxMinecraft);
            this.interpolatedMinV = sprite.getInterpolatedV(this.vMinMinecraft);
            this.interpolatedMaxV = sprite.getInterpolatedV(this.vMaxMinecraft);
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

        buffer.pos(xLeft, yTop, 0).color(red, green, blue, alpha).tex(glyph.interpolatedMinU, glyph.interpolatedMinV).lightmap(0x00f0, 0x00f0).endVertex();
        buffer.pos(xLeft, yBottom, 0).color(red, green, blue, alpha).tex(glyph.interpolatedMinU, glyph.interpolatedMaxV).lightmap(0x00f0, 0x00f0).endVertex();
        buffer.pos(xRight, yBottom, 0).color(red, green, blue, alpha).tex(glyph.interpolatedMaxU, glyph.interpolatedMaxV).lightmap(0x00f0, 0x00f0).endVertex();
        buffer.pos(xRight, yTop, 0).color(red, green, blue, alpha).tex(glyph.interpolatedMaxU, glyph.interpolatedMinV).lightmap(0x00f0, 0x00f0).endVertex();
    }
    
    public List<BakedQuad> getBlockQuadsForText(String text)
    {
        CubeInputs cube = new CubeInputs();
        cube.textureName = ModModels.FONT_RESOURCE_STRING;
        GlyphInfo g = this.glyphArray['A'];
        cube.u0 = g.interpolatedMinU;
        cube.u1 = g.interpolatedMaxU;
        cube.v0 = g.interpolatedMinV;
        cube.v1 = g.interpolatedMaxV;
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
     * Generates quads to render the given chemical formula on all faces of a block.  
     * The quads are oriented to be readable and are positioned in the top half of the block.
     * Assumes the quds will be rendered on a typical 1x1 square block face. 
     */
    public void formulaBlockQuadsToList(String formula, int color, List<BakedQuad> list)
    {
        RawQuad template = new RawQuad();
        template.textureName = ModModels.FONT_RESOURCE_STRING;
        template.color = color;
        template.lockUV = false;
        template.shouldContractUVs = false;
        
        int pixelWidth = this.getWidth(formula);
        
        // try fitting to height first
        float height = 0.5f;
        float width = height * pixelWidth / this.fontHeight;
        
        if(width > 1.0f)
        {
            // too wide, so justify to width instead
            width = 1.0f;
            height = pixelWidth / this.fontHeight;
        }
                
        float scaleFactor = height / this.fontHeight;
        float left = (1 - width) / 2;

        for(char c : formula.toCharArray())
        {
            GlyphInfo g = this.getGlyphInfo(c);
            if(g != null)
            {
                float glyphWidth = g.pixelWidth * scaleFactor;
                FaceVertex[] fv = makeFaceVertices(g, left, 1, glyphWidth, height);
                left += glyphWidth;
                
                for(EnumFacing face : EnumFacing.VALUES)
                {
                    RawQuad quad = template.clone();
                    quad.setupFaceQuad(face, fv[0], fv[1], fv[2], fv[3], null);
                    quad.scaleFromBlockCenter(1.02);
                    list.add(QuadBakery.createBakedQuad(quad));
                }
                
            }
        }

    }
    
    private FaceVertex[] makeFaceVertices(GlyphInfo glyph, float left, float top, float width, float height)
    {
        float bottom = top - height;
        float right = left + width;
        
        FaceVertex[] result = new FaceVertex[4];
        
        result[0] = new FaceVertex.UV(left, bottom, 0, glyph.uMinNormal, glyph.vMaxNormal);
        result[1] = new FaceVertex.UV(right, bottom, 0, glyph.uMaxNormal, glyph.vMaxNormal);
        result[2] = new FaceVertex.UV(right, top, 0, glyph.uMaxNormal, glyph.vMinNormal);
        result[3] = new FaceVertex.UV(left, top, 0, glyph.uMinNormal, glyph.vMinNormal);
        
        return result;
    }
    

}