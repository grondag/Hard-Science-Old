package grondag.adversity.niceblock.newmodel;

import java.util.Collections;
import java.util.List;

import grondag.adversity.niceblock.model.IModelController;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;

import net.minecraft.client.Minecraft;
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

    public static class QuadInputs
    {
        // yes, this is ugly
        public Vertex v1;
        public Vertex v2;
        public Vertex v3;
        public Vertex v4;
        public EnumFacing side;
        public TextureAtlasSprite textureSprite;
        public Rotation rotation;
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
         * Use this for block models. Is faster and smaller than (Colored) UnpackedBakedQuads.
         */
        public BakedQuad createNormalQuad()
        {

            float shade = LightUtil.diffuseLight(this.side);

            int red = (int) (shade * 255f * ((this.color >> 16 & 0xFF) / 255f));
            int green = (int) (shade * 255f * ((this.color >> 8 & 0xFF) / 255f));
            int blue = (int) (shade * 255f * ((this.color & 0xFF) / 255f));
            int alpha = this.color >> 24 & 0xFF;

            int colorOut = red | green << 8 | blue << 16 | alpha << 24;

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
        float u0;
        float v0;
        float u1;
        float v1;
        TextureAtlasSprite textureSprite;
        int color = 0xFFFFFFFF;
        Rotation textureRotation = Rotation.ROTATE_NONE;
        boolean rotateBottom = false;
        boolean isOverlay = false;
        boolean isItem = false;
        
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