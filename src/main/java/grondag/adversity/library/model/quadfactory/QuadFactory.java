package grondag.adversity.library.model.quadfactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import grondag.adversity.Adversity;
import grondag.adversity.library.Rotation;
import grondag.adversity.library.Useful;
import grondag.adversity.library.model.quadfactory.QuadFactory.Vertex.FaceTestResult;

import javax.print.attribute.standard.MediaSize.Other;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
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
        protected Vertex[] vertices = new Vertex[4];
        public EnumFacing side;
        public TextureAtlasSprite textureSprite;
        public Rotation rotation = Rotation.ROTATE_NONE;
        public int color;
        public LightingMode lightingMode = LightingMode.SHADED;
        public boolean lockUV = false;
        public boolean isItem = false;

        public static class Tri extends QuadInputs
        {
            public Tri()
            {
                super();
            }

            /**
             * Copies everything except vertices.
             */
            public Tri(QuadInputs template)
            {
                super();
                this.copyProperties(template);
            }

            @Override
            public int vertexCount()
            {
                return 3;
            }
            
            public void setupFaceQuad(EnumFacing side, FaceVertex tv0, FaceVertex tv1, FaceVertex tv2, EnumFacing topFace)
            {
                this.side = side;
                this.setupFaceQuad(tv0, tv1, tv2, tv2, topFace);
            }

            public void setupFaceQuad(FaceVertex tv0, FaceVertex tv1, FaceVertex tv2, EnumFacing topFace)
            {
                this.setupFaceQuad(tv0, tv1, tv2, tv2, topFace);
            }

            /** Using this instead of method on vertex 
             * ensures normals are set correctly for tris.
             */
            @Override
            public void setNormal(int index, Vec3d normalIn)
            {
                this.vertices[index].setNormal(normalIn);
                if(index == 2)
                {
                    this.vertices[3].setNormal(normalIn);
                }
            }

            /** Using this instead of referencing vertex array directly
             * ensures correct handling for tris.
             */
            @Override
            public void setVertex(int index, Vertex vertexIn)
            {
                this.vertices[index] = vertexIn;
                if(index == 2)
                {
                    this.vertices[3] = vertexIn;
                }
            }

            public Tri clone()
            {
                Tri retval = new Tri();
                retval.vertices = this.vertices.clone();
                retval.copyProperties(this);
                return retval;
            }

        }

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

        public QuadInputs()
        {
            super();
        }

        public QuadInputs(QuadInputs template)
        {
            super();
            this.copyProperties(template);
        }

        public QuadInputs clone()
        {
            QuadInputs retval = new QuadInputs();
            retval.vertices = this.vertices.clone();
            retval.copyProperties(this);
            return retval;
        }
        
        public int vertexCount()
        {
            return 4;
        }

        public void copyProperties(QuadInputs fromObject)
        {
            this.side = fromObject.side;
            this.textureSprite = fromObject.textureSprite;
            this.rotation = fromObject.rotation;
            this.color = fromObject.color;
            this.lightingMode = fromObject.lightingMode;
            this.lockUV = fromObject.lockUV;
            this.isItem = fromObject.isItem;
        }

        /** 
         * If this is a quad, returns two tris.
         * If is already a tri, returns self.
         */
        public QuadInputs.Tri[] split()
        {
            QuadInputs.Tri retVal[];

            if(this instanceof Tri)
            {
                retVal = new QuadInputs.Tri[1];
                retVal[0] = (Tri) this;
            }
            else
            {
                retVal = new QuadInputs.Tri[2];
                retVal[0] = new Tri(this);
                retVal[0].vertices[0] = this.vertices[0];
                retVal[0].vertices[1] = this.vertices[1];
                retVal[0].vertices[2] = this.vertices[2];
                retVal[0].vertices[3] = this.vertices[2];

                retVal[1] = new Tri(this);
                retVal[1].vertices[0] = this.vertices[0];
                retVal[1].vertices[1] = this.vertices[2];
                retVal[1].vertices[2] = this.vertices[3];
                retVal[1].vertices[3] = this.vertices[3];
            }

            return retVal;
        }


        /** 
         * Sets up a quad with standard semantics.  
         * Vertices should be given counter-clockwise from lower left.
         * Ordering of vertices is maintained.
         * 
         * topFace establishes a reference for "up" in these semantics.
         * Depth represents how far recessed into the surface of the face the quad should be.
         * lockUV means UV coordinates means the texture doesn't appear rotated, which in practice
         * means the UV coordinates *are* rotated so that a different part of the texture shows through.
         */
        public void setupFaceQuad(FaceVertex vertexIn0, FaceVertex vertexIn1, FaceVertex vertexIn2, FaceVertex vertexIn3, EnumFacing topFace)
        {

            EnumFacing defaultTop = Useful.defaultTopOf(this.side);
            FaceVertex rv0;
            FaceVertex rv1;
            FaceVertex rv2;
            FaceVertex rv3;
            int uvRotationCount = 0;

            if(topFace == defaultTop)
            {
                rv0 = vertexIn0.clone();
                rv1 = vertexIn1.clone();
                rv2 = vertexIn2.clone();
                rv3 = vertexIn3.clone();
            }
            else if(topFace == Useful.rightOf(this.side, defaultTop))
            {
                rv0 = new FaceVertex.Colored(vertexIn0.y, 1.0 - vertexIn0.x, vertexIn0.depth, vertexIn0.getColor(this.color));
                rv1 = new FaceVertex.Colored(vertexIn1.y, 1.0 - vertexIn1.x, vertexIn1.depth, vertexIn1.getColor(this.color));
                rv2 = new FaceVertex.Colored(vertexIn2.y, 1.0 - vertexIn2.x, vertexIn2.depth, vertexIn2.getColor(this.color));
                rv3 = new FaceVertex.Colored(vertexIn3.y, 1.0 - vertexIn3.x, vertexIn3.depth, vertexIn3.getColor(this.color));
                uvRotationCount = lockUV ? 0 : 1;
            }
            else if(topFace == Useful.bottomOf(this.side, defaultTop))
            {
                rv0 = new FaceVertex.Colored(1.0 - vertexIn0.x, 1.0 - vertexIn0.y, vertexIn0.depth, vertexIn0.getColor(this.color));
                rv1 = new FaceVertex.Colored(1.0 - vertexIn1.x, 1.0 - vertexIn1.y, vertexIn1.depth, vertexIn1.getColor(this.color));
                rv2 = new FaceVertex.Colored(1.0 - vertexIn2.x, 1.0 - vertexIn2.y, vertexIn2.depth, vertexIn2.getColor(this.color));
                rv3 = new FaceVertex.Colored(1.0 - vertexIn3.x, 1.0 - vertexIn3.y, vertexIn3.depth, vertexIn3.getColor(this.color));
                uvRotationCount = lockUV ? 0 : 2;
            }
            else // left of
            {
                rv0 = new FaceVertex.Colored(1.0 - vertexIn0.y, vertexIn0.x, vertexIn0.depth, vertexIn0.getColor(this.color));
                rv1 = new FaceVertex.Colored(1.0 - vertexIn1.y, vertexIn1.x, vertexIn1.depth, vertexIn1.getColor(this.color));
                rv2 = new FaceVertex.Colored(1.0 - vertexIn2.y, vertexIn2.x, vertexIn2.depth, vertexIn2.getColor(this.color));
                rv3 = new FaceVertex.Colored(1.0 - vertexIn3.y, vertexIn3.x, vertexIn3.depth, vertexIn3.getColor(this.color));
                uvRotationCount = lockUV ? 0 : 3;
            }

            if(this.lockUV)
            {
                vertexIn0 = rv0;
                vertexIn1 = rv1;
                vertexIn2 = rv2;
                vertexIn3 = rv3;
            }

            vertexIn0.x -= EPSILON;
            vertexIn3.x -= EPSILON;
            vertexIn0.y -= EPSILON;
            vertexIn1.y -= EPSILON;

            vertexIn2.x += EPSILON;
            vertexIn1.x += EPSILON;
            vertexIn3.y += EPSILON;
            vertexIn2.y += EPSILON;

            switch(this.side)
            {
            case UP:
                this.vertices[0] = new Vertex(rv0.x, 1-rv0.depth, 1-rv0.y, vertexIn0.x * 16.0, (1-vertexIn0.y) * 16.0, rv0.getColor(this.color));
                this.vertices[1] = new Vertex(rv1.x, 1-rv1.depth, 1-rv1.y, vertexIn1.x * 16.0, (1-vertexIn1.y) * 16.0, rv1.getColor(this.color));
                this.vertices[2] = new Vertex(rv2.x, 1-rv2.depth, 1-rv2.y, vertexIn2.x * 16.0, (1-vertexIn2.y) * 16.0, rv2.getColor(this.color));
                this.vertices[3] = new Vertex(rv3.x, 1-rv3.depth, 1-rv3.y, vertexIn3.x * 16.0, (1-vertexIn3.y) * 16.0, rv3.getColor(this.color));
                break;

            case DOWN:     
                this.vertices[0] = new Vertex(rv0.x, rv0.depth, rv0.y, (1.0-vertexIn0.x) * 16.0, vertexIn0.y * 16.0, rv0.getColor(this.color));
                this.vertices[1] = new Vertex(rv1.x, rv1.depth, rv1.y, (1.0-vertexIn1.x) * 16.0, vertexIn1.y * 16.0, rv1.getColor(this.color));
                this.vertices[2] = new Vertex(rv2.x, rv2.depth, rv2.y, (1.0-vertexIn2.x) * 16.0, vertexIn2.y * 16.0, rv2.getColor(this.color));
                this.vertices[3] = new Vertex(rv3.x, rv3.depth, rv3.y, (1.0-vertexIn3.x) * 16.0, vertexIn3.y * 16.0, rv3.getColor(this.color));
                break;

            case EAST:


                this.vertices[0] = new Vertex(1-rv0.depth, rv0.y, 1-rv0.x, vertexIn0.x * 16.0, (1-vertexIn0.y) * 16.0, rv0.getColor(this.color));
                this.vertices[1] = new Vertex(1-rv1.depth, rv1.y, 1-rv1.x, vertexIn1.x * 16.0, (1-vertexIn1.y) * 16.0, rv1.getColor(this.color));
                this.vertices[2] = new Vertex(1-rv2.depth, rv2.y, 1-rv2.x, vertexIn2.x * 16.0, (1-vertexIn2.y) * 16.0, rv2.getColor(this.color));
                this.vertices[3] = new Vertex(1-rv3.depth, rv3.y, 1-rv3.x, vertexIn3.x * 16.0, (1-vertexIn3.y) * 16.0, rv3.getColor(this.color));
                break;

            case WEST:
                this.vertices[0] = new Vertex(rv0.depth, rv0.y, rv0.x, vertexIn0.x * 16.0, (1-vertexIn0.y) * 16.0, rv0.getColor(this.color));
                this.vertices[1] = new Vertex(rv1.depth, rv1.y, rv1.x, vertexIn1.x * 16.0, (1-vertexIn1.y) * 16.0, rv1.getColor(this.color));
                this.vertices[2] = new Vertex(rv2.depth, rv2.y, rv2.x, vertexIn2.x * 16.0, (1-vertexIn2.y) * 16.0, rv2.getColor(this.color));
                this.vertices[3] = new Vertex(rv3.depth, rv3.y, rv3.x, vertexIn3.x * 16.0, (1-vertexIn3.y) * 16.0, rv3.getColor(this.color));
                break;

            case NORTH:
                this.vertices[0] = new Vertex(1-rv0.x, rv0.y, rv0.depth, rv0.x * 16.0, (1-rv0.y) * 16.0, rv0.getColor(this.color));
                this.vertices[1] = new Vertex(1-rv1.x, rv1.y, rv1.depth, rv1.x * 16.0, (1-rv1.y) * 16.0, rv1.getColor(this.color));
                this.vertices[2] = new Vertex(1-rv2.x, rv2.y, rv2.depth, rv2.x * 16.0, (1-rv2.y) * 16.0, rv2.getColor(this.color));
                this.vertices[3] = new Vertex(1-rv3.x, rv3.y, rv3.depth, rv3.x * 16.0, (1-rv3.y) * 16.0, rv3.getColor(this.color));
                break;

            case SOUTH:
                this.vertices[0] = new Vertex(rv0.x, rv0.y, 1-rv0.depth, rv0.x * 16.0, (1-rv0.y) * 16.0, rv0.getColor(this.color));
                this.vertices[1] = new Vertex(rv1.x, rv1.y, 1-rv1.depth, rv1.x * 16.0, (1-rv1.y) * 16.0, rv1.getColor(this.color));
                this.vertices[2] = new Vertex(rv2.x, rv2.y, 1-rv2.depth, rv2.x * 16.0, (1-rv2.y) * 16.0, rv2.getColor(this.color));
                this.vertices[3] = new Vertex(rv3.x, rv3.y, 1-rv3.depth, rv3.x * 16.0, (1-rv3.y) * 16.0, rv3.getColor(this.color));
                break;
            }

            for (int r = 0; r < uvRotationCount; r++)
            {
                rotateQuadUV(this.vertices[0], this.vertices[1], this.vertices[2], this.vertices[3]);
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

        /** Using this instead of method on vertex 
         * ensures normals are set correctly for tris.
         */
        public void setNormal(int index, Vec3d normalIn)
        {
            this.vertices[index].setNormal(normalIn);
        }

        /** Using this instead of referencing vertex array directly
         * ensures correctl handling for tris.
         */
        public void setVertex(int index, Vertex vertexIn)
        {
            this.vertices[index] = vertexIn;
        }

        public Vertex getVertex(int index)
        {
            return this.vertices[index];
        }
        
        public ClipResults clipToUp(EnumFacing face, QuadInputs patchTemplate)
        {
            ClipResults retVal = new ClipResults();

            boolean hasAbove = false;
            boolean hasBelow = false;

            ArrayList<QuadInputs> holes = new ArrayList<QuadInputs>(3);

            for(Vertex v : this.vertices)
            {
                hasAbove = hasAbove || v.yCoord > (1 + QuadFactory.EPSILON);
                hasBelow = hasBelow || v.yCoord < (1 + QuadFactory.EPSILON);
            }

            if(hasAbove)
            {
                if(hasBelow) // yes above, yes below
                {
                    // add portion below plane to output 
                    // and add to hole generation if not orthogonal
                    for(QuadInputs tri : this.split())
                    {
                        retVal.clippedQuads.addAll(tri.splitOnFace(face, holes));
                    }
                }
                else // yes above, no below
                {
                    //create a patch for whole poly if it isn't orthogonal
                    if(!this.isOrthogonalTo(face))
                    {
                        holes.add(this);
                    }
                }
            }
            else
            {
                retVal.clippedQuads.add(this);
            }

            retVal.facePatches = makePatches(face, patchTemplate, holes);

            // compute uv in addition to xyz
            // interpolate normals
            // interpolate color

            return retVal;
        }

        private boolean isOrthogonalTo(EnumFacing face)
        {
            return Math.abs(this.getFaceNormal().dotProduct(new Vec3d(face.getDirectionVec()))) <= EPSILON;
        }

        /**
         * @param face
         * @param holes
         * @return
         */
        private List<QuadInputs> splitOnFace(EnumFacing face, List<QuadInputs> holes) 
        {
            ArrayList<QuadInputs> retVal = new ArrayList<QuadInputs>(4);

            for(QuadInputs.Tri tri : this.split())
            {

                ArrayList<QuadFactory.Vertex> vertexKeep = new ArrayList<QuadFactory.Vertex>(4); 
                ArrayList<QuadFactory.Vertex> vertexHoles = new ArrayList<QuadFactory.Vertex>(4); 

                // precalc to avoid calling twice in loop below
                FaceTestResult testResults[] = new FaceTestResult[3];
                testResults[0] = this.vertices[0].faceTest(face);
                testResults[1] = this.vertices[1].faceTest(face);
                testResults[2] = this.vertices[2].faceTest(face);

                for (int iThisVertex = 0; iThisVertex < 3; iThisVertex++) 
                {
                    int iNextVertex = (iThisVertex + 1) % 3;
                    boolean isSplitNeeded = false;
                    switch(testResults[iThisVertex])
                    {
                    case COPLANAR:
                        vertexKeep.add(this.vertices[iThisVertex]);
                        vertexHoles.add(this.vertices[iThisVertex]);
                        break;

                    case FRONT:
                        vertexHoles.add(this.vertices[iThisVertex]);
                        isSplitNeeded = testResults[iNextVertex] == FaceTestResult.BACK;
                        break;

                    case BACK:
                        vertexKeep.add(this.vertices[iThisVertex]);
                        isSplitNeeded = testResults[iNextVertex] == FaceTestResult.FRONT;
                        break;
                    }

                    if(isSplitNeeded)
                    {
                        double a = Math.abs(this.vertices[iThisVertex].distanceToFacePlane(face));
                        double b = Math.abs(this.vertices[iNextVertex].distanceToFacePlane(face));
                        QuadFactory.Vertex splitVertex = this.vertices[iThisVertex].interpolate(this.vertices[iNextVertex], a / (a + b)); 
                        vertexKeep.add(splitVertex);
                        vertexHoles.add(splitVertex);                    
                    }
                }

                // create polys and add to collections
                if(vertexKeep.size() == 3 || vertexKeep.size() == 4)
                {
                    QuadInputs keeper = vertexKeep.size() == 3 ? new Tri(this) : new QuadInputs(this);
                    for(int i = 0; i < vertexKeep.size(); i++)
                    {
                        keeper.setVertex(i, vertexKeep.get(i));
                    }
                    retVal.add(keeper);
                }

                if(vertexHoles.size() == 3 || vertexHoles.size() == 4)
                {
                    QuadInputs hole = vertexHoles.size() == 3 ? new Tri(this) : new QuadInputs(this);
                    for(int i = 0; i < vertexHoles.size(); i++)
                    {
                        hole.setVertex(i, vertexHoles.get(i));
                    }
                    if(!hole.isOrthogonalTo(face))
                    {
                        holes.add(hole);
                    }
                }
            }           

            return retVal;
        }     


        private List<QuadInputs> makePatches(EnumFacing face, QuadInputs patchTemplate, List<QuadInputs> holes) 
        {
            ArrayList<QuadInputs> retVal = new ArrayList<QuadInputs>(holes.size());

            for(QuadInputs hole : holes)
            {
                FaceVertex fvHole[] = new FaceVertex[4];
                
                for(int i = 0; i < hole.vertexCount(); i++)
                {
                    double x = hole.getVertex(i).xCoord;
                    double y = hole.getVertex(i).yCoord;
                    double z = hole.getVertex(i).zCoord;
                    double u;
                    double v;
                    switch(face)
                    {
                    case DOWN:
                        y = 0;
                        u = x;
                        v = z;
                        break;
                    case EAST:
                        x = 1;
                        break;
                    case NORTH:
                        z = 0;
                        break;
                    case SOUTH:
                        z = 1;
                        break;
                    case UP:
                        y = 1;
                        break;
                    case WEST:
                        x = 0;
                        break;
                    default:
                        break;
                    }
                  //  fvHole[i](i, vertexHoles.get(i));
                }
                QuadInputs patch = hole instanceof Tri ? new Tri(patchTemplate) : new QuadInputs(patchTemplate);

                retVal.add(patch);

            }
            //TODO:  do the thing
//            The projection of a point q = (x, y, z) onto a plane given by a point p = (a, b, c) and a normal n = (d, e, f) is
//
//                    q_proj = q - dot(q - p, n) * n
//                    This calculation assumes that n is a unit vector.

            return retVal;
        }

        public BakedQuad createNormalQuad()
        {
            for (int r = 0; r < this.rotation.index; r++)
            {
                rotateQuadUV(this.vertices[0], this.vertices[1], this.vertices[2], this.vertices[3]);
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
                        LightUtil.pack(vertices[v].xyzToFloatArray(), vertexData, format, v, e);
                        break;

                    case NORMAL: 
                        LightUtil.pack(vertices[v].hasNormal() ? vertices[v].normalToFloatArray() : faceNormal, vertexData, format, v, e);
                        break;

                    case COLOR:
                        float[] colorRGBA = new float[4];
                        colorRGBA[0] = ((float) (vertices[v].color >> 16 & 0xFF)) / 255f;
                        colorRGBA[1] = ((float) (vertices[v].color >> 8 & 0xFF)) / 255f;
                        colorRGBA[2] = ((float) (vertices[v].color  & 0xFF)) / 255f;
                        colorRGBA[3] = ((float) (vertices[v].color >> 24 & 0xFF)) / 255f;
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
                            uvData[0] = this.textureSprite.getInterpolatedU(vertices[v].u);
                            uvData[1] = this.textureSprite.getInterpolatedV(vertices[v].v);
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
            return vertices[2].subtract(vertices[0]).crossProduct(vertices[3].subtract(vertices[1])).normalize();
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

        protected Vertex setNormal(Vec3d normalIn)
        {
            this.normal = normalIn;
            return this;
        }

        public boolean hasNormal()
        {
            return this.normal != null;
        }

        /**
         * Returns a new, linearly interpolated vertex based on this vertex
         * and the other vertex provided.  Neither vertex is changed.
         * Factor 0 returns this vertex. Factor 1 return other vertex, 
         * with values in between returning a weighted average.
         */
        public Vertex interpolate(Vertex otherVertex, double otherWeight)
        {
            Vec3d newPos = this.add(otherVertex.subtract(this).scale(otherWeight));
            Vec3d newNorm = this.normal.add(otherVertex.normal.subtract(this.normal).scale(otherWeight));
            double newU = this.u + (otherVertex.u - this.u) * otherWeight;
            double newV = this.v + (otherVertex.v - this.v) * otherWeight;

            int newColor = (int) ((this.color & 0xFF) + ((otherVertex.color & 0xFF) - (this.color & 0xFF)) * otherWeight);
            newColor |= (int) ((this.color & 0xFF00) + ((otherVertex.color & 0xFF00) - (this.color & 0xFF00)) * otherWeight);
            newColor |= (int) ((this.color & 0xFF0000) + ((otherVertex.color & 0xFF0000) - (this.color & 0xFF0000)) * otherWeight);
            newColor |= (int) ((this.color & 0xFF000000) + ((otherVertex.color & 0xFF000000) - (this.color & 0xFF000000)) * otherWeight);

            return new Vertex(newPos.xCoord, newPos.yCoord, newPos.zCoord, newU, newV, newColor, newNorm);
        }

        public enum FaceTestResult
        {
            FRONT,
            BACK,
            COPLANAR
        }

        /**
         * Tests whether vertex is inside, on or outside the plane of 
         * the given face for a standard block with bounds 0,0,0 to 1,1,1.
         */
        public FaceTestResult faceTest(EnumFacing face)
        {
            double distance = distanceToFacePlane(face);
            return (distance < -EPSILON) ? FaceTestResult.BACK : (distance > EPSILON) ? FaceTestResult.FRONT : FaceTestResult.COPLANAR;
        }

        /**
         * Returns a signed distance to the plane of the given face.
         * Positive numbers mean in front of face, negative numbers in back.
         */
        public double distanceToFacePlane(EnumFacing face)
        {
            int offset = face.getAxisDirection() == AxisDirection.POSITIVE ? 1 : 0;
            return new Vec3d(face.getDirectionVec()).dotProduct(this) - offset;
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
                qi.vertices[0] = new Vertex(minBound, maxBound, minBound, u0, v0, this.color);
                qi.vertices[1] = new Vertex(minBound, maxBound, maxBound, u0, v1, this.color);
                qi.vertices[2] = new Vertex(maxBound, maxBound, maxBound, u1, v1, this.color);
                qi.vertices[3] = new Vertex(maxBound, maxBound, minBound, u1, v0, this.color);
                break;

            case DOWN:     
                qi.vertices[0] = new Vertex(maxBound, minBound, maxBound, u0, v1, this.color);
                qi.vertices[1] = new Vertex(minBound, minBound, maxBound, u1, v1, this.color); 
                qi.vertices[2] = new Vertex(minBound, minBound, minBound, u1, v0, this.color); 
                qi.vertices[3] = new Vertex(maxBound, minBound, minBound, u0, v0, this.color);
                break;

            case WEST:
                qi.vertices[0] = new Vertex(minBound, minBound, minBound, u0, v1, this.color);
                qi.vertices[1] = new Vertex(minBound, minBound, maxBound, u1, v1, this.color);
                qi.vertices[2] = new Vertex(minBound, maxBound, maxBound, u1, v0, this.color);
                qi.vertices[3] = new Vertex(minBound, maxBound, minBound, u0, v0, this.color);
                break;

            case EAST:
                qi.vertices[0] = new Vertex(maxBound, minBound, minBound, u1, v1, this.color);
                qi.vertices[1] = new Vertex(maxBound, maxBound, minBound, u1, v0, this.color);
                qi.vertices[2] = new Vertex(maxBound, maxBound, maxBound, u0, v0, this.color);
                qi.vertices[3] = new Vertex(maxBound, minBound, maxBound, u0, v1, this.color);
                break;

            case NORTH:
                qi.vertices[0] = new Vertex(minBound, minBound, minBound, u1, v1, this.color);
                qi.vertices[1] = new Vertex(minBound, maxBound, minBound, u1, v0, this.color);
                qi.vertices[2] = new Vertex(maxBound, maxBound, minBound, u0, v0, this.color);
                qi.vertices[3] = new Vertex(maxBound, minBound, minBound, u0, v1, this.color);
                break;

            case SOUTH:
                qi.vertices[0] = new Vertex(minBound, minBound, maxBound, u0, v1, this.color);
                qi.vertices[1] = new Vertex(maxBound, minBound, maxBound, u1, v1, this.color);
                qi.vertices[2] = new Vertex(maxBound, maxBound, maxBound, u1, v0, this.color);
                qi.vertices[3] = new Vertex(minBound, maxBound, maxBound, u0, v0, this.color);
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

    public static class ClipResults
    {
        public List<QuadInputs> facePatches = new ArrayList<QuadInputs>(4);
        public List<QuadInputs> clippedQuads = new ArrayList<QuadInputs>(4);
    }

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