package grondag.adversity.niceblock.newmodel;

import java.util.Collections;
import java.util.List;

import grondag.adversity.Adversity;
import grondag.adversity.library.Useful;
import grondag.adversity.niceblock.model.IModelController;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;

public class QuadFactory
{

    /**
     * Takes a normal vanilla quad and recolors it to return a normal quad.
     */
    public static BakedQuad recolorVanillaQuad(BakedQuad quadIn, int color)
    {
        float shade = LightUtil.diffuseLight(quadIn.getFace());
        int colorOut = shadeColor(color, shade, true);

        int[] aint = quadIn.getVertexData();
        aint[3] = colorOut;
        aint[3 + 7] = colorOut;
        aint[3 + 14] = colorOut;
        aint[3 + 21] = colorOut;
                
        return new BakedQuad(aint, quadIn.getTintIndex(), quadIn.getFace());
    }
    
    public static int shadeColor(int color, float shade, boolean glOrder)
    {
        int red = (int) (shade * 255f * ((color >> 16 & 0xFF) / 255f));
        int green = (int) (shade * 255f * ((color >> 8 & 0xFF) / 255f));
        int blue = (int) (shade * 255f * ((color & 0xFF) / 255f));
        int alpha = color >> 24 & 0xFF;

        return glOrder ? red  | green << 8 | blue << 16 | alpha << 24 : red << 16 | green << 8 | blue | alpha << 24;
    }
    
    public static class QuadInputs
    {
        // yes, this is ugly
        public Vertex v1;
        public Vertex v2;
        public Vertex v3;
        public Vertex v4;
        public EnumFacing side;
        public TextureAtlasSprite textureSprite;
        public Rotation rotation = Rotation.ROTATE_NONE;
        public int color;
        
        public QuadInputs clone()
        {
            QuadInputs retval = new QuadInputs();
            retval.v1 = this.v1;
            retval.v2 = this.v2;
            retval.v3 = this.v3;
            retval.v4 = this.v4;
            retval.side = this.side;
            retval.textureSprite = this.textureSprite;
            retval.rotation = this.rotation;
            retval.color = this.color;
            return retval;
        }
        
        /** 
         * Sets up a quad with standard semantics.  
         * x0,y0 are at lower left and x1, y1 are top right.
         * topFace establishes a reference for "up" in these semantics.
         * Depth represents how far recessed into the surface of the face the quad should be.
         * lockUV means UV coordinates means the texture doesn't appear rotated, which in practice
         * means the UV coordinates *are* rotated so that a different part of the texture shows through.
         */
        public void setupFaceQuad(EnumFacing face, float x0, float y0, float x1, float y1, float depth, EnumFacing topFace, boolean lockUV)
        {
            
            this.side = face;
            EnumFacing defaultTop = Useful.defaultTopOf(face);
            float rx0;
            float rx1;
            float ry0;
            float ry1;
            int uvRotationCount = 0;
            
            if(topFace == defaultTop)
            {
                rx0 = x0;
                ry0 = y0;
                rx1 = x1;
                ry1 = y1;
            }
            else if(topFace == Useful.rightOf(face, defaultTop))
            {
                rx0 = y0;
                ry0 = 1-x1;
                rx1 = y1;
                ry1 = 1-x0;
                uvRotationCount = lockUV ? 0 : 1;
            }
            else if(topFace == Useful.bottomOf(face, defaultTop))
            {
                rx0 = 1-x1;
                ry0 = 1-y1;
                rx1 = 1-x0;
                ry1 = 1-y0;
                uvRotationCount = lockUV ? 0 : 2;
            }
            else // left of
            {
                rx0 = 1-y1;
                ry0 = x0;
                rx1 = 1-y0;
                ry1 = x1;
                uvRotationCount = lockUV ? 0 : 3;
            }
            
            if(lockUV)
            {
                x0 = rx0;
                x1 = rx1;
                y0 = ry0;
                y1 = ry1;
            }
            
            switch(face)
            {
            case UP:
                this.v1 = new Vertex(rx0, 1-depth, 1-ry0, x0 * 16.0F, (1-y0) * 16.0F);
                this.v2 = new Vertex(rx1, 1-depth, 1-ry0, x1 * 16.0F, (1-y0) * 16.0F);
                this.v3 = new Vertex(rx1, 1-depth, 1-ry1, x1 * 16.0F, (1-y1) * 16.0F);
                this.v4 = new Vertex(rx0, 1-depth, 1-ry1, x0 * 16.0F, (1-y1) * 16.0F);
                break;

            case DOWN:     
                this.v1 = new Vertex(rx0, depth, ry0, x0 * 16.0F, y0 * 16.0F); 
                this.v2 = new Vertex(rx1, depth, ry0, x1 * 16.0F, y0 * 16.0F);
                this.v3 = new Vertex(rx1, depth, ry1, x1 * 16.0F, y1 * 16.0F);
                this.v4 = new Vertex(rx0, depth, ry1, x0 * 16.0F, y1 * 16.0F); 
                break;

            case EAST:
                this.v1 = new Vertex(1-depth, ry0, 1-rx0, (x0) * 16.0F, (1-y0) * 16.0F);
                this.v2 = new Vertex(1-depth, ry0, 1-rx1, (x1) * 16.0F, (1-y0) * 16.0F);
                this.v3 = new Vertex(1-depth, ry1, 1-rx1, (x1) * 16.0F, (1-y1) * 16.0F);
                this.v4 = new Vertex(1-depth, ry1, 1-rx0, (x0) * 16.0F, (1-y1) * 16.0F);
                break;

            case WEST:
                this.v1 = new Vertex(depth, ry0, rx0, x0 * 16.0F, (1-y0) * 16.0F);
                this.v2 = new Vertex(depth, ry0, rx1, x1 * 16.0F, (1-y0) * 16.0F);
                this.v3 = new Vertex(depth, ry1, rx1, x1 * 16.0F, (1-y1) * 16.0F);
                this.v4 = new Vertex(depth, ry1, rx0, x0 * 16.0F, (1-y1) * 16.0F);
                break;

            case NORTH:
                this.v1 = new Vertex(1-rx0, ry0, depth, (x0) * 16.0F, (1-y0) * 16.0F);
                this.v2 = new Vertex(1-rx1, ry0, depth, (x1) * 16.0F, (1-y0) * 16.0F);
                this.v3 = new Vertex(1-rx1, ry1, depth, (x1) * 16.0F, (1-y1) * 16.0F);
                this.v4 = new Vertex(1-rx0, ry1, depth, (x0) * 16.0F, (1-y1) * 16.0F);
                break;

            case SOUTH:
                this.v1 = new Vertex(rx0, ry0, 1-depth, x0 * 16.0F, (1-y0) * 16.0F);
                this.v2 = new Vertex(rx1, ry0, 1-depth, x1 * 16.0F, (1-y0) * 16.0F);
                this.v3 = new Vertex(rx1, ry1, 1-depth, x1 * 16.0F, (1-y1) * 16.0F);
                this.v4 = new Vertex(rx0, ry1, 1-depth, x0 * 16.0F, (1-y1) * 16.0F);
                break;
            }
            
            for (int r = 0; r < uvRotationCount; r++)
            {
                rotateQuadUV(this.v1, this.v2, this.v3, this.v4);
            }
        }
        
        /**
         * Use this for block models. Is faster and smaller than (Colored) UnpackedBakedQuads.
         */
        public BakedQuad createNormalQuad()
        {

            float shade = LightUtil.diffuseLight(this.side);
            int colorOut = shadeColor(color, shade, true);

            for (int r = 0; r < this.rotation.index; r++)
            {
                rotateQuadUV(this.v1, this.v2, this.v3, this.v4);
            }

            int[] aint = Ints.concat(vertexToInts(this.v1.xCoord, this.v1.yCoord, this.v1.zCoord, this.v1.u, this.v1.v, colorOut, this.textureSprite),
                    vertexToInts(this.v2.xCoord, this.v2.yCoord, this.v2.zCoord, this.v2.u, this.v2.v, colorOut, this.textureSprite),
                    vertexToInts(this.v3.xCoord, this.v3.yCoord, this.v3.zCoord, this.v3.u, this.v3.v, colorOut, this.textureSprite),
                    vertexToInts(this.v4.xCoord, this.v4.yCoord, this.v4.zCoord, this.v4.u, this.v4.v, colorOut, this.textureSprite));

            // necessary to support forge lighting model
            net.minecraftforge.client.ForgeHooksClient.fillNormal(aint, this.side);

            return new BakedQuad(aint, -1, this.side);

        }
        
        /**
         * Use this for item models. Supports coloring unlike vanilla quads but has a slight performance hit at render time.
         */
        public BakedQuad createColoredQuad()
        {

            for (int r = 0; r < this.rotation.index; r++)
            {
                rotateQuadUV(this.v1, this.v2, this.v3, this.v4);
            }

            Vec3 faceNormal = this.v1.subtract(this.v3).crossProduct(this.v3.subtract(this.v4));
            faceNormal.normalize();

            UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(DefaultVertexFormats.ITEM);
            builder.setQuadOrientation(EnumFacing.getFacingFromVector((float) faceNormal.xCoord, (float) faceNormal.yCoord, (float) faceNormal.zCoord));
            builder.setQuadColored();
            putVertexData(builder, this.v1, this.color, this.side, faceNormal, this.textureSprite);
            putVertexData(builder, this.v2, this.color, this.side, faceNormal, this.textureSprite);
            putVertexData(builder, this.v3, this.color, this.side, faceNormal, this.textureSprite);
            putVertexData(builder, this.v4, this.color, this.side, faceNormal, this.textureSprite);
            return builder.build();
        }
    }

    public static class Vertex extends Vec3
    {
        protected float u;
        protected float v;

        public Vertex(float x, float y, float z, float u, float v)
        {
            super(x, y, z);
            this.u = u;
            this.v = v;
        }

        public Vertex transform(Matrix4f matrix, boolean rescaleToUnitCube)
        {

            Vector4f tmp = new Vector4f((float) xCoord, (float) yCoord, (float) zCoord, 1f);
            matrix.transform(tmp);
            if (rescaleToUnitCube && Math.abs(tmp.w - 1f) > 1e-5)
            {
                tmp.scale(1f / tmp.w);
            }
            return new Vertex(tmp.x, tmp.y, tmp.z, u, v);
        }

    }

    public static class CubeInputs{
        public float u0;
        public float v0;
        public float u1;
        public float v1;
        public TextureAtlasSprite textureSprite;
        public int color = 0xFFFFFFFF;
        public Rotation textureRotation = Rotation.ROTATE_NONE;
        public boolean rotateBottom = false;
        public boolean isOverlay = false;
        public boolean isItem = false;
        
        public CubeInputs()
        {
            //NOOP
        }
        public CubeInputs(int color, Rotation textureRotation, TextureAtlasSprite textureSprite, boolean flipU, boolean flipV, boolean isOverlay, boolean isItem)
        {
            this.color = color;
            this.textureRotation = textureRotation;
            this.textureSprite = textureSprite;
            this.isOverlay = isOverlay;
            this.isItem = isItem;
            this.u0 = flipU ? 16 : 0;
            this.v0 = flipV ? 16 : 0;
            this.u1 = flipU ? 0 : 16;
            this.v1 = flipV ? 0 : 16;
            this.rotateBottom = true;
        }
        
        public List<BakedQuad> makeFace(EnumFacing side){
            
            QuadInputs qi = new QuadInputs();
            qi.color = this.color;
            qi.rotation = (rotateBottom && side == EnumFacing.DOWN) ? this.textureRotation.clockwise().clockwise() : this.textureRotation;
            qi.textureSprite = this.textureSprite;
            
            float minBound = (float) (this.isOverlay ? -0.0002 : 0.0);
            float maxBound = (float) (this.isOverlay ? 1.0002 : 1.0);

            ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
            
            switch(side)
            {
            case UP:
                qi.v1 = new Vertex(minBound, maxBound, minBound, u0, v0);
                qi.v2 = new Vertex(minBound, maxBound, maxBound, u0, v1);
                qi.v3 = new Vertex(maxBound, maxBound, maxBound, u1, v1);
                qi.v4 = new Vertex(maxBound, maxBound, minBound, u1, v0);
                qi.side = EnumFacing.UP;
                break;
    
            case DOWN:     
                qi.v1 = new Vertex(maxBound, minBound, maxBound, u0, v1);
                qi.v2 = new Vertex(minBound, minBound, maxBound, u1, v1); 
                qi.v3 = new Vertex(minBound, minBound, minBound, u1, v0); 
                qi.v4 = new Vertex(maxBound, minBound, minBound, u0, v0);
                qi.side = EnumFacing.DOWN;
                break;
    
            case WEST:
                qi.v1 = new Vertex(minBound, minBound, minBound, u0, v1);
                qi.v2 = new Vertex(minBound, minBound, maxBound, u1, v1);
                qi.v3 = new Vertex(minBound, maxBound, maxBound, u1, v0);
                qi.v4 = new Vertex(minBound, maxBound, minBound, u0, v0);
                qi.side = EnumFacing.WEST;
                break;
                
            case EAST:
                qi.v1 = new Vertex(maxBound, minBound, minBound, u1, v1);
                qi.v2 = new Vertex(maxBound, maxBound, minBound, u1, v0);
                qi.v3 = new Vertex(maxBound, maxBound, maxBound, u0, v0);
                qi.v4 = new Vertex(maxBound, minBound, maxBound, u0, v1);
                qi.side = EnumFacing.EAST;
                break;
    
            case NORTH:
                qi.v1 = new Vertex(minBound, minBound, minBound, u1, v1);
                qi.v2 = new Vertex(minBound, maxBound, minBound, u1, v0);
                qi.v3 = new Vertex(maxBound, maxBound, minBound, u0, v0);
                qi.v4 = new Vertex(maxBound, minBound, minBound, u0, v1);
                qi.side = EnumFacing.NORTH;
                break;
    
            case SOUTH:
                qi.v1 = new Vertex(minBound, minBound, maxBound, u0, v1);
                qi.v2 = new Vertex(maxBound, minBound, maxBound, u1, v1);
                qi.v3 = new Vertex(maxBound, maxBound, maxBound, u1, v0);
                qi.v4 = new Vertex(minBound, maxBound, maxBound, u0, v0);
                qi.side = EnumFacing.SOUTH;
                break;
            }
 
            if(this.isItem){
                builder.add(qi.createColoredQuad()).build();
            }
            else 
            {
                builder.add(qi.createNormalQuad()).build();
            }
 
            return builder.build();
        }
    }
    
    /**
     * Rotates face texture 90deg clockwise
     */
    private static void rotateQuadUV(Vertex v1, Vertex v2, Vertex v3, Vertex v4)
    {
        float swapU = v1.u;
        float swapV = v1.v;
        v1.u = v2.u;
        v1.v = v2.v;
        v2.u = v3.u;
        v2.v = v3.v;
        v3.u = v4.u;
        v3.v = v4.v;
        v4.u = swapU;
        v4.v = swapV;
    }



    private static int[] vertexToInts(double x, double y, double z, float u, float v, int color, TextureAtlasSprite sprite)
    {

        return new int[] { Float.floatToRawIntBits((float) x), Float.floatToRawIntBits((float) y), Float.floatToRawIntBits((float) z), color,
                Float.floatToRawIntBits(sprite.getInterpolatedU(u)), Float.floatToRawIntBits(sprite.getInterpolatedV(v)), 0 };
    }



    private static void putVertexData(UnpackedBakedQuad.Builder builder, QuadFactory.Vertex vertexIn, int colorIn, EnumFacing side, Vec3 faceNormal,
            TextureAtlasSprite sprite)
    {
        for (int e = 0; e < DefaultVertexFormats.ITEM.getElementCount(); e++)
        {
            switch (DefaultVertexFormats.ITEM.getElement(e).getUsage())
            {
            case POSITION:
                builder.put(e, (float) vertexIn.xCoord, (float) vertexIn.yCoord, (float) vertexIn.zCoord, 1);
                break;
            case COLOR:
                float shade = LightUtil.diffuseLight((float) faceNormal.xCoord, (float) faceNormal.yCoord, (float) faceNormal.zCoord);

                float red = shade * ((colorIn >> 16 & 0xFF) / 255f);
                float green = shade * ((colorIn >> 8 & 0xFF) / 255f);
                float blue = shade * ((colorIn & 0xFF) / 255f);
                float alpha = (colorIn >> 24 & 0xFF) / 255f;

                builder.put(e, red, green, blue, alpha);
                break;

            case UV:
                builder.put(e, sprite.getInterpolatedU(vertexIn.u), sprite.getInterpolatedV(vertexIn.v), 0, 1);
                break;

            case NORMAL:
                builder.put(e, (float) faceNormal.xCoord, (float) faceNormal.yCoord, (float) faceNormal.zCoord, 0);
                break;

            default:
                builder.put(e);
            }
        }
    }
}