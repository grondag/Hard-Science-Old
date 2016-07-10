package grondag.adversity.library.model.quadfactory;

import java.util.ArrayList;
import java.util.List;

import grondag.adversity.library.Rotation;
import grondag.adversity.library.Useful;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.pipeline.LightUtil;

public class RawQuad
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

        public RawQuad()
        {
            super();
        }

        public RawQuad(RawQuad template)
        {
            super();
            this.copyProperties(template);
        }

        public RawQuad clone()
        {
            RawQuad retval = new RawQuad();
            retval.vertices = this.vertices.clone();
            retval.copyProperties(this);
            return retval;
        }
        
        public int vertexCount()
        {
            return 4;
        }

        protected void copyProperties(RawQuad fromObject)
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
        public RawTri[] split()
        {
            RawTri retVal[];

            if(this instanceof RawTri)
            {
                retVal = new RawTri[1];
                retVal[0] = (RawTri) this;
            }
            else
            {
                retVal = new RawTri[2];
                retVal[0] = new RawTri(this);
                retVal[0].vertices[0] = this.vertices[0];
                retVal[0].vertices[1] = this.vertices[1];
                retVal[0].vertices[2] = this.vertices[2];
                retVal[0].vertices[3] = this.vertices[2];

                retVal[1] = new RawTri(this);
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

            vertexIn0.x -= QuadFactory.EPSILON;
            vertexIn3.x -= QuadFactory.EPSILON;
            vertexIn0.y -= QuadFactory.EPSILON;
            vertexIn1.y -= QuadFactory.EPSILON;

            vertexIn2.x += QuadFactory.EPSILON;
            vertexIn1.x += QuadFactory.EPSILON;
            vertexIn3.y += QuadFactory.EPSILON;
            vertexIn2.y += QuadFactory.EPSILON;

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
                QuadFactory.rotateQuadUV(this.vertices[0], this.vertices[1], this.vertices[2], this.vertices[3]);
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
        
        public ClipResults clipToUp(EnumFacing face, RawQuad patchTemplate)
        {
            ClipResults retVal = new ClipResults();

            boolean hasAbove = false;
            boolean hasBelow = false;

            ArrayList<RawQuad> holes = new ArrayList<RawQuad>(3);

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
                    for(RawQuad tri : this.split())
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
            return Math.abs(this.getFaceNormal().dotProduct(new Vec3d(face.getDirectionVec()))) <= QuadFactory.EPSILON;
        }

        /**
         * @param face
         * @param holes
         * @return
         */
        private List<RawQuad> splitOnFace(EnumFacing face, List<RawQuad> holes) 
        {
            ArrayList<RawQuad> retVal = new ArrayList<RawQuad>(4);

            for(RawTri tri : this.split())
            {

                ArrayList<Vertex> vertexKeep = new ArrayList<Vertex>(4); 
                ArrayList<Vertex> vertexHoles = new ArrayList<Vertex>(4); 

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
                        Vertex splitVertex = this.vertices[iThisVertex].interpolate(this.vertices[iNextVertex], a / (a + b)); 
                        vertexKeep.add(splitVertex);
                        vertexHoles.add(splitVertex);                    
                    }
                }

                // create polys and add to collections
                if(vertexKeep.size() == 3 || vertexKeep.size() == 4)
                {
                    RawQuad keeper = vertexKeep.size() == 3 ? new RawTri(this) : new RawQuad(this);
                    for(int i = 0; i < vertexKeep.size(); i++)
                    {
                        keeper.setVertex(i, vertexKeep.get(i));
                    }
                    retVal.add(keeper);
                }

                if(vertexHoles.size() == 3 || vertexHoles.size() == 4)
                {
                    RawQuad hole = vertexHoles.size() == 3 ? new RawTri(this) : new RawQuad(this);
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


        private List<RawQuad> makePatches(EnumFacing face, RawQuad patchTemplate, List<RawQuad> holes) 
        {
            ArrayList<RawQuad> retVal = new ArrayList<RawQuad>(holes.size());

            for(RawQuad hole : holes)
            {
                RawQuad patch = hole instanceof RawTri ? new RawTri(patchTemplate) : new RawQuad(patchTemplate);

                for(int i = 0; i < hole.vertexCount(); i++)
                {
                    double x = hole.getVertex(i).xCoord;
                    double y = hole.getVertex(i).yCoord;
                    double z = hole.getVertex(i).zCoord;
                    double u = 0;
                    double v = 0;
                    switch(face)
                    {
                    case UP:
                        y = 1;
                        u = x * 16;
                        v = z * 16;
                        break;
                    case DOWN:
                        y = 0;
                        u = (1 - x) * 16;
                        v = z * 16;
                        break;
                    case EAST:
                        x = 1;
                        u = (1 - z) * 16;
                        v = (1 - y) * 16;
                        break;
                    case WEST:
                        x = 0;
                        u = z * 16;
                        v = (1 - y) * 16;
                        break;
                    case NORTH:
                        z = 0;
                        u = (1 - x) * 16;
                        v = (1 - y) * 16;
                        break;
                    case SOUTH:
                        z = 1;
                        u = x * 16;
                        v = (1 - y) * 16;
                        break;
                    default:
                        // make compiler shut up about unhandled case
                        break;
                    }
                    patch.setVertex(i, new Vertex(x, y, z, u, v, patch.color));
                }
                
                retVal.add(patch);
            }

            return retVal;
        }

        public BakedQuad createNormalQuad()
        {
            for (int r = 0; r < this.rotation.index; r++)
            {
                QuadFactory.rotateQuadUV(this.vertices[0], this.vertices[1], this.vertices[2], this.vertices[3]);
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