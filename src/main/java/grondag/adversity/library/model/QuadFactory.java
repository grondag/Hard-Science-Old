package grondag.adversity.library.model;

import java.util.List;

import grondag.adversity.library.Rotation;
import grondag.adversity.library.Useful;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.pipeline.LightUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;

public class QuadFactory
{

	public static final List<BakedQuad> EMPTY_QUAD_LIST = new ImmutableList.Builder<BakedQuad>().build();
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
                
        return new BakedQuad(aint, colorOut, quadIn.getFace(), quadIn.getSprite(), quadIn.shouldApplyDiffuseLighting(), quadIn.getFormat());
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
        public boolean isShaded = true;
        public boolean lockUV = false;
        
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
            retval.isShaded = this.isShaded;
            retval.lockUV = this.lockUV;
            return retval;
        }

        /** 
         * Sets up a quad with standard semantics.  
         * Vertices should be given counter-clockwise from lower left.
         * 
         * topFace establishes a reference for "up" in these semantics.
         * Depth represents how far recessed into the surface of the face the quad should be.
         * lockUV means UV coordinates means the texture doesn't appear rotated, which in practice
         * means the UV coordinates *are* rotated so that a different part of the texture shows through.
         */
        public void setupFaceQuad(FaceVertex tv0, FaceVertex tv1, FaceVertex tv2, FaceVertex tv3, double depth, EnumFacing topFace)
        {
            final double EPSILON = 0.0000005;
            float shade = isShaded ? LightUtil.diffuseLight(this.side) : 1;

            EnumFacing defaultTop = Useful.defaultTopOf(this.side);
            FaceVertex rv0;
            FaceVertex rv3;
            FaceVertex rv2;
            FaceVertex rv1;
            int uvRotationCount = 0;
            
            if(topFace == defaultTop)
            {
                rv0 = tv0.clone();
                rv1 = tv1.clone();
                rv2 = tv2.clone();
                rv3 = tv3.clone();
            }
            else if(topFace == Useful.rightOf(this.side, defaultTop))
            {
                rv0 = new FaceVertex.Colored(tv1.y, 1.0 - tv1.x, tv1.getColor(this.color));
                rv1 = new FaceVertex.Colored(tv2.y, 1.0 - tv2.x, tv2.getColor(this.color));
                rv2 = new FaceVertex.Colored(tv3.y, 1.0 - tv3.x, tv3.getColor(this.color));
                rv3 = new FaceVertex.Colored(tv0.y, 1.0 - tv0.x, tv0.getColor(this.color));
                uvRotationCount = lockUV ? 0 : 1;
            }
            else if(topFace == Useful.bottomOf(this.side, defaultTop))
            {
                rv0 = new FaceVertex.Colored(1.0 - tv2.x, 1.0 - tv2.y, tv2.getColor(this.color));
                rv1 = new FaceVertex.Colored(1.0 - tv3.x, 1.0 - tv3.y, tv3.getColor(this.color));
                rv2 = new FaceVertex.Colored(1.0 - tv0.x, 1.0 - tv0.y, tv0.getColor(this.color));
                rv3 = new FaceVertex.Colored(1.0 - tv1.x, 1.0 - tv1.y, tv1.getColor(this.color));
                uvRotationCount = lockUV ? 0 : 2;
            }
            else // left of
            {
                rv0 = new FaceVertex.Colored(1.0 - tv3.y, tv3.x, tv3.getColor(this.color));
                rv1 = new FaceVertex.Colored(1.0 - tv0.y, tv0.x, tv0.getColor(this.color));
                rv2 = new FaceVertex.Colored(1.0 - tv1.y, tv1.x, tv1.getColor(this.color));
                rv3 = new FaceVertex.Colored(1.0 - tv2.y, tv2.x, tv2.getColor(this.color));
                uvRotationCount = lockUV ? 0 : 3;
            }
            
            if(this.lockUV)
            {
                tv0 = rv0;
                tv1 = rv1;
                tv2 = rv2;
                tv3 = rv3;
            }
            
            tv0.x -= EPSILON;
            tv3.x -= EPSILON;
            tv0.y -= EPSILON;
            tv1.y -= EPSILON;
            
            tv2.x += EPSILON;
            tv1.x += EPSILON;
            tv3.y += EPSILON;
            tv2.y += EPSILON;
            
            switch(this.side)
            {
            case UP:
                this.v1 = new Vertex(rv0.x, 1-depth, 1-rv0.y, tv0.x * 16.0, (1-tv0.y) * 16.0, shadeColor(rv0.getColor(this.color), shade, true));
                this.v2 = new Vertex(rv1.x, 1-depth, 1-rv1.y, tv1.x * 16.0, (1-tv1.y) * 16.0, shadeColor(rv1.getColor(this.color), shade, true));
                this.v3 = new Vertex(rv2.x, 1-depth, 1-rv2.y, tv2.x * 16.0, (1-tv2.y) * 16.0, shadeColor(rv2.getColor(this.color), shade, true));
                this.v4 = new Vertex(rv3.x, 1-depth, 1-rv3.y, tv3.x * 16.0, (1-tv3.y) * 16.0, shadeColor(rv3.getColor(this.color), shade, true));
                break;

            case DOWN:     
                this.v1 = new Vertex(rv0.x, depth, rv0.y, (1.0-tv0.x) * 16.0, tv0.y * 16.0, shadeColor(rv0.getColor(this.color), shade, true));
                this.v2 = new Vertex(rv1.x, depth, rv1.y, (1.0-tv1.x) * 16.0, tv1.y * 16.0, shadeColor(rv1.getColor(this.color), shade, true));
                this.v3 = new Vertex(rv2.x, depth, rv2.y, (1.0-tv2.x) * 16.0, tv2.y * 16.0, shadeColor(rv2.getColor(this.color), shade, true));
                this.v4 = new Vertex(rv3.x, depth, rv3.y, (1.0-tv3.x) * 16.0, tv3.y * 16.0, shadeColor(rv3.getColor(this.color), shade, true));
                break;

            case EAST:
                
                
                this.v1 = new Vertex(1-depth, rv0.y, 1-rv0.x, tv0.x * 16.0, (1-tv0.y) * 16.0, shadeColor(rv0.getColor(this.color), shade, true));
                this.v2 = new Vertex(1-depth, rv1.y, 1-rv1.x, tv1.x * 16.0, (1-tv1.y) * 16.0, shadeColor(rv1.getColor(this.color), shade, true));
                this.v3 = new Vertex(1-depth, rv2.y, 1-rv2.x, tv2.x * 16.0, (1-tv2.y) * 16.0, shadeColor(rv2.getColor(this.color), shade, true));
                this.v4 = new Vertex(1-depth, rv3.y, 1-rv3.x, tv3.x * 16.0, (1-tv3.y) * 16.0, shadeColor(rv3.getColor(this.color), shade, true));
                break;

            case WEST:
                this.v1 = new Vertex(depth, rv0.y, rv0.x, tv0.x * 16.0, (1-tv0.y) * 16.0, shadeColor(rv0.getColor(this.color), shade, true));
                this.v2 = new Vertex(depth, rv1.y, rv1.x, tv1.x * 16.0, (1-tv1.y) * 16.0, shadeColor(rv1.getColor(this.color), shade, true));
                this.v3 = new Vertex(depth, rv2.y, rv2.x, tv2.x * 16.0, (1-tv2.y) * 16.0, shadeColor(rv2.getColor(this.color), shade, true));
                this.v4 = new Vertex(depth, rv3.y, rv3.x, tv3.x * 16.0, (1-tv3.y) * 16.0, shadeColor(rv3.getColor(this.color), shade, true));
                break;

            case NORTH:
                this.v1 = new Vertex(1-rv0.x, rv0.y, depth, rv0.x * 16.0, (1-rv0.y) * 16.0, shadeColor(rv0.getColor(this.color), shade, true));
                this.v2 = new Vertex(1-rv1.x, rv1.y, depth, rv1.x * 16.0, (1-rv1.y) * 16.0, shadeColor(rv1.getColor(this.color), shade, true));
                this.v3 = new Vertex(1-rv2.x, rv2.y, depth, rv2.x * 16.0, (1-rv2.y) * 16.0, shadeColor(rv2.getColor(this.color), shade, true));
                this.v4 = new Vertex(1-rv3.x, rv3.y, depth, rv3.x * 16.0, (1-rv3.y) * 16.0, shadeColor(rv3.getColor(this.color), shade, true));
                break;

            case SOUTH:
                this.v1 = new Vertex(rv0.x, rv0.y, 1-depth, rv0.x * 16.0, (1-rv0.y) * 16.0, shadeColor(rv0.getColor(this.color), shade, true));
                this.v2 = new Vertex(rv1.x, rv1.y, 1-depth, rv1.x * 16.0, (1-rv1.y) * 16.0, shadeColor(rv1.getColor(this.color), shade, true));
                this.v3 = new Vertex(rv2.x, rv2.y, 1-depth, rv2.x * 16.0, (1-rv2.y) * 16.0, shadeColor(rv2.getColor(this.color), shade, true));
                this.v4 = new Vertex(rv3.x, rv3.y, 1-depth, rv3.x * 16.0, (1-rv3.y) * 16.0, shadeColor(rv3.getColor(this.color), shade, true));
                break;
            }
            
            for (int r = 0; r < uvRotationCount; r++)
            {
                rotateQuadUV(this.v1, this.v2, this.v3, this.v4);
            }
        }
        
        public void setupFaceQuad(EnumFacing side, FaceVertex tv0, FaceVertex tv1, FaceVertex tv2, FaceVertex tv3, double depth, EnumFacing topFace)
        {
            this.side = side;
            this.setupFaceQuad(tv0, tv1, tv2, tv3, depth, topFace);
        }
        
        /** 
         * Sets up a quad with standard semantics.  
         * x0,y0 are at lower left and x1, y1 are top right.
         * topFace establishes a reference for "up" in these semantics.
         * Depth represents how far recessed into the surface of the face the quad should be.
         * lockUV means UV coordinates means the texture doesn't appear rotated, which in practice
         * means the UV coordinates *are* rotated so that a different part of the texture shows through.
         */
        public void setupFaceQuad(double x0, double y0, double x1, double y1, double depth, EnumFacing topFace)
        {
            this.setupFaceQuad(
                    new FaceVertex(x0, y0),
                    new FaceVertex(x1, y0),
                    new FaceVertex(x1, y1),
                    new FaceVertex(x0, y1), 
                    depth, topFace);
        }
        
        public void setupFaceQuad(EnumFacing face, double x0, double y0, double x1, double y1, double depth, EnumFacing topFace)
        {
            this.side = face;
            this.setupFaceQuad(x0, y0, x1, y1, depth, topFace);
        }


        /**
         * Use this for block models. Is faster and smaller than (Colored) UnpackedBakedQuads.
         */
        public BakedQuad createNormalQuad()
        {
            for (int r = 0; r < this.rotation.index; r++)
            {
                rotateQuadUV(this.v1, this.v2, this.v3, this.v4);
            }

            int[] aint = Ints.concat(vertexToInts(this.v1.xCoord, this.v1.yCoord, this.v1.zCoord, this.v1.u, this.v1.v, v1.color, this.textureSprite),
                    vertexToInts(this.v2.xCoord, this.v2.yCoord, this.v2.zCoord, this.v2.u, this.v2.v, v2.color, this.textureSprite),
                    vertexToInts(this.v3.xCoord, this.v3.yCoord, this.v3.zCoord, this.v3.u, this.v3.v, v3.color, this.textureSprite),
                    vertexToInts(this.v4.xCoord, this.v4.yCoord, this.v4.zCoord, this.v4.u, this.v4.v, v4.color, this.textureSprite));

            // necessary to support forge lighting model
            net.minecraftforge.client.ForgeHooksClient.fillNormal(aint, this.side);

            return new BakedQuad(aint, color, side, textureSprite, isShaded, DefaultVertexFormats.ITEM);

        }
        
    }
    
    public static class Vertex extends Vec3d
    {
        protected double u;
        protected double v;
        protected int color;

        public Vertex(double x, double y, double z, double u, double v, int color)
        {
            super(x, y, z);
            this.u = u;
            this.v = v;
            this.color = color;
        }

        public Vertex transform(Matrix4f matrix, boolean rescaleToUnitCube)
        {

            Vector4f tmp = new Vector4f((float) xCoord, (float) yCoord, (float) zCoord, 1f);
            matrix.transform(tmp);
            if (rescaleToUnitCube && Math.abs(tmp.w - 1f) > 1e-5)
            {
                tmp.scale(1f / tmp.w);
            }
            return new Vertex(tmp.x, tmp.y, tmp.z, u, v, color);
        }
    }

    public static class CubeInputs{
        public double u0;
        public double v0;
        public double u1;
        public double v1;
        public TextureAtlasSprite textureSprite;
        public int color = 0xFFFFFFFF;
        public Rotation textureRotation = Rotation.ROTATE_NONE;
        public boolean rotateBottom = false;
        public boolean isOverlay = false;
        public boolean isItem = false;
        public boolean isShaded = true;
        
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
            
            //int shadedColor = shadeColor(this.color, this.isShaded ? LightUtil.diffuseLight(side) : 1.0F, true);
            // quads have diffuse lighting applied later it seems
            int shadedColor = shadeColor(this.color, 1.0F, true);
            
            double minBound = this.isOverlay ? -0.0002 : 0.0;
            double maxBound = this.isOverlay ? 1.0002 : 1.0;
            qi.side = side;

            ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
            
            switch(side)
            {
            case UP:
                qi.v1 = new Vertex(minBound, maxBound, minBound, u0, v0, shadedColor);
                qi.v2 = new Vertex(minBound, maxBound, maxBound, u0, v1, shadedColor);
                qi.v3 = new Vertex(maxBound, maxBound, maxBound, u1, v1, shadedColor);
                qi.v4 = new Vertex(maxBound, maxBound, minBound, u1, v0, shadedColor);
                break;
    
            case DOWN:     
                qi.v1 = new Vertex(maxBound, minBound, maxBound, u0, v1, shadedColor);
                qi.v2 = new Vertex(minBound, minBound, maxBound, u1, v1, shadedColor); 
                qi.v3 = new Vertex(minBound, minBound, minBound, u1, v0, shadedColor); 
                qi.v4 = new Vertex(maxBound, minBound, minBound, u0, v0, shadedColor);
                break;
    
            case WEST:
                qi.v1 = new Vertex(minBound, minBound, minBound, u0, v1, shadedColor);
                qi.v2 = new Vertex(minBound, minBound, maxBound, u1, v1, shadedColor);
                qi.v3 = new Vertex(minBound, maxBound, maxBound, u1, v0, shadedColor);
                qi.v4 = new Vertex(minBound, maxBound, minBound, u0, v0, shadedColor);
                break;
                
            case EAST:
                qi.v1 = new Vertex(maxBound, minBound, minBound, u1, v1, shadedColor);
                qi.v2 = new Vertex(maxBound, maxBound, minBound, u1, v0, shadedColor);
                qi.v3 = new Vertex(maxBound, maxBound, maxBound, u0, v0, shadedColor);
                qi.v4 = new Vertex(maxBound, minBound, maxBound, u0, v1, shadedColor);
                break;
    
            case NORTH:
                qi.v1 = new Vertex(minBound, minBound, minBound, u1, v1, shadedColor);
                qi.v2 = new Vertex(minBound, maxBound, minBound, u1, v0, shadedColor);
                qi.v3 = new Vertex(maxBound, maxBound, minBound, u0, v0, shadedColor);
                qi.v4 = new Vertex(maxBound, minBound, minBound, u0, v1, shadedColor);
                break;
    
            case SOUTH:
                qi.v1 = new Vertex(minBound, minBound, maxBound, u0, v1, shadedColor);
                qi.v2 = new Vertex(maxBound, minBound, maxBound, u1, v1, shadedColor);
                qi.v3 = new Vertex(maxBound, maxBound, maxBound, u1, v0, shadedColor);
                qi.v4 = new Vertex(minBound, maxBound, maxBound, u0, v0, shadedColor);
                break;
            }
 
 
            builder.add(qi.createNormalQuad()).build();
 
            return builder.build();
        }
    }
    
    /**
     * Rotates face texture 90deg clockwise
     */
    private static void rotateQuadUV(Vertex v1, Vertex v2, Vertex v3, Vertex v4)
    {
        double swapU = v1.u;
        double swapV = v1.v;
        v1.u = v2.u;
        v1.v = v2.v;
        v2.u = v3.u;
        v2.v = v3.v;
        v3.u = v4.u;
        v3.v = v4.v;
        v4.u = swapU;
        v4.v = swapV;
    }



    private static int[] vertexToInts(double x, double y, double z, double u, double v, int color, TextureAtlasSprite sprite)
    {

        return new int[] { Float.floatToRawIntBits((float) x), Float.floatToRawIntBits((float) y), Float.floatToRawIntBits((float) z), color,
                Float.floatToRawIntBits(sprite.getInterpolatedU(u)), Float.floatToRawIntBits(sprite.getInterpolatedV(v)), 0 };
    }

    public static class FaceVertex
    {
        public double x;
        public double y;
        public double u;
        public double v;
        
        public FaceVertex(double x, double y)
        {
            this.x = x;
            this.y = y;
            this.u = x;
            this.v = y;
        }
        
        public FaceVertex(double x, double y, double u, double v)
        {
            this.x = x;
            this.y = y;
            this.u = u;
            this.v = v;
        }
        
        public FaceVertex clone()
        {
            return new FaceVertex(x, y, u, v);
        }
        
        public int getColor(int defaultColor)
        {
            return defaultColor;
        }
        
        public static class Colored extends FaceVertex
        {
            private int color = 0xFFFFFFFF;
            
            public Colored(double x, double y, int color)
            {
                super(x, y);
                this.color = color;
            }
            
            public Colored(double x, double y, double u, double v, int color)
            {
                super(x, y, u, v);
                this.color = color;
            }
            
            @Override
            public FaceVertex clone()
            {
                return new FaceVertex.Colored(x, y, u, v, color);
            }
            
            @Override
            public int getColor(int defaultColor)
            {
                return color;
            }
        }
    }
    
    public static class SimpleQuadBounds
    {
        public EnumFacing face;
        public double x0;
        public double y0;
        public double x1;
        public double y1;
        public double depth;
        public EnumFacing topFace;
        
        public SimpleQuadBounds(EnumFacing face, double x0, double y0, double x1, double y1, double depth, EnumFacing topFace)
        {
            this.face = face;
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
            this.depth = depth;
            this.topFace = topFace;
        }
    }

}