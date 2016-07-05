package grondag.adversity.library.model;

import java.util.List;

import grondag.adversity.library.Rotation;
import grondag.adversity.library.Useful;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.pipeline.LightUtil;
import com.google.common.collect.ImmutableList;

public class QuadFactory
{
    private static final double EPSILON = 0.0000005;


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
        public Vertex[] vertex = new Vertex[4];
//        public Vertex v2;
//        public Vertex v3;
//        public Vertex v4;
        public EnumFacing side;
        public TextureAtlasSprite textureSprite;
        public Rotation rotation = Rotation.ROTATE_NONE;
        public int color;
        public LightingMode lightingMode = LightingMode.SHADED;
        public boolean lockUV = false;
        public boolean isItem = false;
        
        public static enum LightingMode
        {
            FLAT,
            SHADED,
            FULLBRIGHT;
            
            public VertexFormat vertexFormat = DefaultVertexFormats.ITEM;
            
            static
            {
                FULLBRIGHT.vertexFormat = DefaultVertexFormats.BLOCK;
            }
        }
        
        public QuadInputs clone()
        {
            QuadInputs retval = new QuadInputs();
            retval.vertex = this.vertex.clone();
            retval.side = this.side;
            retval.textureSprite = this.textureSprite;
            retval.rotation = this.rotation;
            retval.color = this.color;
            retval.lightingMode = this.lightingMode;
            retval.lockUV = this.lockUV;
            retval.isItem = this.isItem;
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
        public void setupFaceQuad(FaceVertex tv0, FaceVertex tv1, FaceVertex tv2, FaceVertex tv3, EnumFacing topFace)
        {
            //Note: used to apply face shading here but no longer seems necessary for block models.
//            float shade = 1; //isShaded ? LightUtil.diffuseLight(this.side) : 1;

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
                rv0 = new FaceVertex.Colored(tv1.y, 1.0 - tv1.x, tv1.depth, tv1.getColor(this.color));
                rv1 = new FaceVertex.Colored(tv2.y, 1.0 - tv2.x, tv2.depth, tv2.getColor(this.color));
                rv2 = new FaceVertex.Colored(tv3.y, 1.0 - tv3.x, tv3.depth, tv3.getColor(this.color));
                rv3 = new FaceVertex.Colored(tv0.y, 1.0 - tv0.x, tv0.depth, tv0.getColor(this.color));
                uvRotationCount = lockUV ? 0 : 1;
            }
            else if(topFace == Useful.bottomOf(this.side, defaultTop))
            {
                rv0 = new FaceVertex.Colored(1.0 - tv2.x, 1.0 - tv2.y, tv2.depth, tv2.getColor(this.color));
                rv1 = new FaceVertex.Colored(1.0 - tv3.x, 1.0 - tv3.y, tv3.depth, tv3.getColor(this.color));
                rv2 = new FaceVertex.Colored(1.0 - tv0.x, 1.0 - tv0.y, tv0.depth, tv0.getColor(this.color));
                rv3 = new FaceVertex.Colored(1.0 - tv1.x, 1.0 - tv1.y, tv1.depth, tv1.getColor(this.color));
                uvRotationCount = lockUV ? 0 : 2;
            }
            else // left of
            {
                rv0 = new FaceVertex.Colored(1.0 - tv3.y, tv3.x, tv3.depth, tv3.getColor(this.color));
                rv1 = new FaceVertex.Colored(1.0 - tv0.y, tv0.x, tv0.depth, tv0.getColor(this.color));
                rv2 = new FaceVertex.Colored(1.0 - tv1.y, tv1.x, tv1.depth, tv1.getColor(this.color));
                rv3 = new FaceVertex.Colored(1.0 - tv2.y, tv2.x, tv2.depth, tv2.getColor(this.color));
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
                this.vertex[0] = new Vertex(rv0.x, 1-rv0.depth, 1-rv0.y, tv0.x * 16.0, (1-tv0.y) * 16.0, rv0.getColor(this.color));
                this.vertex[1] = new Vertex(rv1.x, 1-rv1.depth, 1-rv1.y, tv1.x * 16.0, (1-tv1.y) * 16.0, rv1.getColor(this.color));
                this.vertex[2] = new Vertex(rv2.x, 1-rv2.depth, 1-rv2.y, tv2.x * 16.0, (1-tv2.y) * 16.0, rv2.getColor(this.color));
                this.vertex[3] = new Vertex(rv3.x, 1-rv3.depth, 1-rv3.y, tv3.x * 16.0, (1-tv3.y) * 16.0, rv3.getColor(this.color));
                break;

            case DOWN:     
                this.vertex[0] = new Vertex(rv0.x, rv0.depth, rv0.y, (1.0-tv0.x) * 16.0, tv0.y * 16.0, rv0.getColor(this.color));
                this.vertex[1] = new Vertex(rv1.x, rv1.depth, rv1.y, (1.0-tv1.x) * 16.0, tv1.y * 16.0, rv1.getColor(this.color));
                this.vertex[2] = new Vertex(rv2.x, rv2.depth, rv2.y, (1.0-tv2.x) * 16.0, tv2.y * 16.0, rv2.getColor(this.color));
                this.vertex[3] = new Vertex(rv3.x, rv3.depth, rv3.y, (1.0-tv3.x) * 16.0, tv3.y * 16.0, rv3.getColor(this.color));
                break;

            case EAST:
                
                
                this.vertex[0] = new Vertex(1-rv0.depth, rv0.y, 1-rv0.x, tv0.x * 16.0, (1-tv0.y) * 16.0, rv0.getColor(this.color));
                this.vertex[1] = new Vertex(1-rv1.depth, rv1.y, 1-rv1.x, tv1.x * 16.0, (1-tv1.y) * 16.0, rv1.getColor(this.color));
                this.vertex[2] = new Vertex(1-rv2.depth, rv2.y, 1-rv2.x, tv2.x * 16.0, (1-tv2.y) * 16.0, rv2.getColor(this.color));
                this.vertex[3] = new Vertex(1-rv3.depth, rv3.y, 1-rv3.x, tv3.x * 16.0, (1-tv3.y) * 16.0, rv3.getColor(this.color));
                break;

            case WEST:
                this.vertex[0] = new Vertex(rv0.depth, rv0.y, rv0.x, tv0.x * 16.0, (1-tv0.y) * 16.0, rv0.getColor(this.color));
                this.vertex[1] = new Vertex(rv1.depth, rv1.y, rv1.x, tv1.x * 16.0, (1-tv1.y) * 16.0, rv1.getColor(this.color));
                this.vertex[2] = new Vertex(rv2.depth, rv2.y, rv2.x, tv2.x * 16.0, (1-tv2.y) * 16.0, rv2.getColor(this.color));
                this.vertex[3] = new Vertex(rv3.depth, rv3.y, rv3.x, tv3.x * 16.0, (1-tv3.y) * 16.0, rv3.getColor(this.color));
                break;

            case NORTH:
                this.vertex[0] = new Vertex(1-rv0.x, rv0.y, rv0.depth, rv0.x * 16.0, (1-rv0.y) * 16.0, rv0.getColor(this.color));
                this.vertex[1] = new Vertex(1-rv1.x, rv1.y, rv1.depth, rv1.x * 16.0, (1-rv1.y) * 16.0, rv1.getColor(this.color));
                this.vertex[2] = new Vertex(1-rv2.x, rv2.y, rv2.depth, rv2.x * 16.0, (1-rv2.y) * 16.0, rv2.getColor(this.color));
                this.vertex[3] = new Vertex(1-rv3.x, rv3.y, rv3.depth, rv3.x * 16.0, (1-rv3.y) * 16.0, rv3.getColor(this.color));
                break;

            case SOUTH:
                this.vertex[0] = new Vertex(rv0.x, rv0.y, 1-rv0.depth, rv0.x * 16.0, (1-rv0.y) * 16.0, rv0.getColor(this.color));
                this.vertex[1] = new Vertex(rv1.x, rv1.y, 1-rv1.depth, rv1.x * 16.0, (1-rv1.y) * 16.0, rv1.getColor(this.color));
                this.vertex[2] = new Vertex(rv2.x, rv2.y, 1-rv2.depth, rv2.x * 16.0, (1-rv2.y) * 16.0, rv2.getColor(this.color));
                this.vertex[3] = new Vertex(rv3.x, rv3.y, 1-rv3.depth, rv3.x * 16.0, (1-rv3.y) * 16.0, rv3.getColor(this.color));
                break;
            }
            
            for (int r = 0; r < uvRotationCount; r++)
            {
                rotateQuadUV(this.vertex[0], this.vertex[1], this.vertex[2], this.vertex[3]);
            }
        }
        
        public void setupFaceQuad(EnumFacing side, FaceVertex tv0, FaceVertex tv1, FaceVertex tv2, FaceVertex tv3, EnumFacing topFace)
        {
            this.side = side;
            this.setupFaceQuad(tv0, tv1, tv2, tv3, topFace);
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
                    new FaceVertex(x0, y0, depth),
                    new FaceVertex(x1, y0, depth),
                    new FaceVertex(x1, y1, depth),
                    new FaceVertex(x0, y1, depth), 
                    topFace);
        }
        
        public void setupFaceQuad(EnumFacing face, double x0, double y0, double x1, double y1, double depth, EnumFacing topFace)
        {
            this.side = face;
            this.setupFaceQuad(x0, y0, x1, y1, depth, topFace);
        }



        public BakedQuad createNormalQuad()
        {
            for (int r = 0; r < this.rotation.index; r++)
            {
                rotateQuadUV(this.vertex[0], this.vertex[1], this.vertex[2], this.vertex[3]);
            }

            int[] vertexData = new int[28];
            
            VertexFormat format = this.isItem ? DefaultVertexFormats.ITEM : lightingMode.vertexFormat;
            
            float[] faceNormal = this.getFaceNormalArray();
            
            if(this.isItem && this.lightingMode == LightingMode.FULLBRIGHT)
            {
                //TODO: this is really a hack - doesn't work well if item is at angle to Y axis
                //There does not yet appear to be a supported method for full-bright items
                faceNormal[0] = 0;
                faceNormal[1] = 1;
                faceNormal[2] = 0;
            }
            
            for(int v = 0; v < 4; v++)
            {
                
                for(int e = 0; e < format.getElementCount(); e++)
                {
                    switch(format.getElement(e).getUsage())
                    {
                        case POSITION:
                            LightUtil.pack(vertex[v].xyzToFloatArray(), vertexData, format, v, e);
                            break;
                            
                        case NORMAL: 
                            LightUtil.pack(vertex[v].hasNormal() ? vertex[v].normalToFloatArray() : faceNormal, vertexData, format, v, e);
                            break;
                        
                        case COLOR:
                            float[] colorRGBA = new float[4];
                            colorRGBA[0] = ((float) (vertex[v].color >> 16 & 0xFF)) / 255f;
                            colorRGBA[1] = ((float) (vertex[v].color >> 8 & 0xFF)) / 255f;
                            colorRGBA[2] = ((float) (vertex[v].color  & 0xFF)) / 255f;
                            colorRGBA[3] = ((float) (vertex[v].color >> 24 & 0xFF)) / 255f;
                            LightUtil.pack(colorRGBA, vertexData, format, v, e);
                            break;

                        case UV: 
                            if(format.getElement(e).getIndex() == 1)
                            {
                                float[] fullBright = new float[2];
                                
                                //Don't really understand how brightness format works, but this does the job
                                fullBright[0] = ((float)((15 >> 0x04) & 0xF) * 0x20) / 0xFFFF;
                                fullBright[1] = ((float)((15 >> 0x14) & 0xF) * 0x20) / 0xFFFF;

                                LightUtil.pack(fullBright, vertexData, format, v, e);
                            }
                            else
                            {
                                float[] uvData = new float[2];
                                uvData[0] = this.textureSprite.getInterpolatedU(vertex[v].u);
                                uvData[1] = this.textureSprite.getInterpolatedV(vertex[v].v);
                                LightUtil.pack(uvData, vertexData, format, v, e);
                            }
                            break;
                            
                        default:
                        // NOOP, padding or weirdness
                    }
                }
            }

//            int[] aint = Ints.concat(vertexToInts(this.v1.xCoord, this.v1.yCoord, this.v1.zCoord, this.v1.u, this.v1.v, v1.color, this.textureSprite),
//                    vertexToInts(this.v2.xCoord, this.v2.yCoord, this.v2.zCoord, this.v2.u, this.v2.v, v2.color, this.textureSprite),
//                    vertexToInts(this.v3.xCoord, this.v3.yCoord, this.v3.zCoord, this.v3.u, this.v3.v, v3.color, this.textureSprite),
//                    vertexToInts(this.v4.xCoord, this.v4.yCoord, this.v4.zCoord, this.v4.u, this.v4.v, v4.color, this.textureSprite));


            return new BakedQuad(vertexData, color, side, textureSprite, lightingMode == LightingMode.SHADED, format);

        }
     
        public Vec3d getFaceNormal()
        {
            return vertex[2].subtract(vertex[0]).crossProduct(vertex[3].subtract(vertex[1])).normalize();
        }
        
        private float[] getFaceNormalArray()
        {
            Vec3d normal = getFaceNormal();
            
            float[] retval = new float[3];
            
            retval[0] = (float)(normal.xCoord);
            retval[1] = (float)(normal.yCoord);
            retval[2] = (float)(normal.zCoord);
            return retval;
        }
    }
    
    

    public static class Vertex extends Vec3d
    {
        protected double u;
        protected double v;
        protected int color;
        protected Vec3d normal;

        public Vertex(double x, double y, double z, double u, double v, int color)
        {
            super(x, y, z);
            this.u = u;
            this.v = v;
            this.color = color;
        }

        public Vertex(double x, double y, double z, double u, double v, int color, Vec3d normal)
        {
            this(x, y, z, u, v, color);
            this.normal = normal;
        }
        
        public void setNormal(Vec3d normalIn)
        {
            this.normal = normalIn;
        }

        public boolean hasNormal()
        {
            return this.normal != null;
        }
        
        public Vertex clone()
        {
            return new Vertex(this.xCoord, this.yCoord, this.zCoord, this.u, this.v, this.color, this.normal);
        }
        
        public Vertex transform(Matrix4f matrix, boolean rescaleToUnitCube)
        {

            Vector4f tmp = new Vector4f((float) xCoord, (float) yCoord, (float) zCoord, 1f);
            
            matrix.transform(tmp);
            if (rescaleToUnitCube && Math.abs(tmp.w - 1f) > 1e-5)
            {
                tmp.scale(1f / tmp.w);
            }
            
            if(this.hasNormal())
            {
                Vector4f tmpNormal = new Vector4f((float)this.normal.xCoord, (float)this.normal.yCoord, (float)this.normal.zCoord, 1f);
                matrix.transform(tmp);
                Vec3d newNormal = new Vec3d(tmpNormal.x, tmpNormal.y, tmpNormal.z);
                newNormal.normalize();
                return new Vertex(tmp.x, tmp.y, tmp.z, u, v, color, newNormal);
            }
            else
            {
                return new Vertex(tmp.x, tmp.y, tmp.z, u, v, color);
            }
            
        }
        
        public float[] xyzToFloatArray()
        {
            float[] retVal = new float[3];
            retVal[0] = (float)this.xCoord;
            retVal[1] = (float)this.yCoord;
            retVal[2] = (float)this.zCoord;
            return retVal;
        }
        
        public float[] normalToFloatArray()
        {
            float[] retVal = null;
            if(this.hasNormal())
            {
                retVal = new float[3];
                retVal[0] = (float) this.normal.xCoord;
                retVal[1] = (float) this.normal.yCoord;
                retVal[2] = (float) this.normal.zCoord;
            }
            return retVal;
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
            
            double minBound = this.isOverlay ? -0.0002 : 0.0;
            double maxBound = this.isOverlay ? 1.0002 : 1.0;
            qi.side = side;

            ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
            
            switch(side)
            {
            case UP:
                qi.vertex[0] = new Vertex(minBound, maxBound, minBound, u0, v0, this.color);
                qi.vertex[1] = new Vertex(minBound, maxBound, maxBound, u0, v1, this.color);
                qi.vertex[2] = new Vertex(maxBound, maxBound, maxBound, u1, v1, this.color);
                qi.vertex[3] = new Vertex(maxBound, maxBound, minBound, u1, v0, this.color);
                break;
    
            case DOWN:     
                qi.vertex[0] = new Vertex(maxBound, minBound, maxBound, u0, v1, this.color);
                qi.vertex[1] = new Vertex(minBound, minBound, maxBound, u1, v1, this.color); 
                qi.vertex[2] = new Vertex(minBound, minBound, minBound, u1, v0, this.color); 
                qi.vertex[3] = new Vertex(maxBound, minBound, minBound, u0, v0, this.color);
                break;
    
            case WEST:
                qi.vertex[0] = new Vertex(minBound, minBound, minBound, u0, v1, this.color);
                qi.vertex[1] = new Vertex(minBound, minBound, maxBound, u1, v1, this.color);
                qi.vertex[2] = new Vertex(minBound, maxBound, maxBound, u1, v0, this.color);
                qi.vertex[3] = new Vertex(minBound, maxBound, minBound, u0, v0, this.color);
                break;
                
            case EAST:
                qi.vertex[0] = new Vertex(maxBound, minBound, minBound, u1, v1, this.color);
                qi.vertex[1] = new Vertex(maxBound, maxBound, minBound, u1, v0, this.color);
                qi.vertex[2] = new Vertex(maxBound, maxBound, maxBound, u0, v0, this.color);
                qi.vertex[3] = new Vertex(maxBound, minBound, maxBound, u0, v1, this.color);
                break;
    
            case NORTH:
                qi.vertex[0] = new Vertex(minBound, minBound, minBound, u1, v1, this.color);
                qi.vertex[1] = new Vertex(minBound, maxBound, minBound, u1, v0, this.color);
                qi.vertex[2] = new Vertex(maxBound, maxBound, minBound, u0, v0, this.color);
                qi.vertex[3] = new Vertex(maxBound, minBound, minBound, u0, v1, this.color);
                break;
    
            case SOUTH:
                qi.vertex[0] = new Vertex(minBound, minBound, maxBound, u0, v1, this.color);
                qi.vertex[1] = new Vertex(maxBound, minBound, maxBound, u1, v1, this.color);
                qi.vertex[2] = new Vertex(maxBound, maxBound, maxBound, u1, v0, this.color);
                qi.vertex[3] = new Vertex(minBound, maxBound, maxBound, u0, v0, this.color);
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



//    private static int[] vertexToInts(double x, double y, double z, double u, double v, int color, TextureAtlasSprite sprite)
//    {
//
//        return new int[] { Float.floatToRawIntBits((float) x), Float.floatToRawIntBits((float) y), Float.floatToRawIntBits((float) z), color,
//                Float.floatToRawIntBits(sprite.getInterpolatedU(u)), Float.floatToRawIntBits(sprite.getInterpolatedV(v)), 0 };
//    }

    public static class FaceVertex
    {
        public double x;
        public double y;
        public double u;
        public double v;
        public double depth;
        
        public FaceVertex(double x, double y, double depth)
        {
            this.x = x;
            this.y = y;
            this.u = x;
            this.v = y;
            this.depth = depth;
        }
        
        public FaceVertex(double x, double y, double depth, double u, double v)
        {
            this.x = x;
            this.y = y;
            this.u = u;
            this.v = v;
            this.depth = depth;
        }
        
        public void rotateFacing(EnumFacing onFace, EnumFacing toFace, EnumFacing fromFace, boolean includeUV)
        {
            if(toFace == fromFace)
            {
                //   NOOP
            }
            else if(toFace == Useful.rightOf(onFace, fromFace))
            {
                double oldX = this.x;
                double oldY = this.y;
                this.x = oldY;
                this.y = 1.0 - oldX;
                
                if(includeUV)
                {
                    double oldU = this.u;
                    double oldV = this.v;
                    this.u = oldV;
                    this.v = 1.0 - oldU;
                }
            }
            else if(toFace == Useful.bottomOf(onFace, fromFace))
            {
                double oldX = this.x;
                double oldY = this.y;
                this.x = 1.0 - oldX;
                this.y = 1.0 - oldY;
                
                if(includeUV)
                {
                    double oldU = this.u;
                    double oldV = this.v;
                    this.u = 1.0 - oldU;
                    this.v = 1.0 - oldV;
                }
            }
            else // left of
            {
                double oldX = this.x;
                double oldY = this.y;
                this.x = 1.0 - oldY;
                this.y = oldX;
                
                if(includeUV)
                {
                    double oldU = this.u;
                    double oldV = this.v;
                    this.u = 1.0 - oldV;
                    this.v = oldU;
                }
            }
        }
        
        public FaceVertex clone()
        {
            return new FaceVertex(x, y, depth, u, v);
        }
        
        public int getColor(int defaultColor)
        {
            return defaultColor;
        }
        
        public static class Colored extends FaceVertex
        {
            private int color = 0xFFFFFFFF;
            
            public Colored(double x, double y, double depth, int color)
            {
                super(x, y, depth);
                this.color = color;
            }
            
            public Colored(double x, double y, double depth, double u, double v, int color)
            {
                super(x, y, depth, u, v);
                this.color = color;
            }
            
            @Override
            public FaceVertex clone()
            {
                return new FaceVertex.Colored(x, y, depth, u, v, color);
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