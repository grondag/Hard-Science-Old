package grondag.adversity.library.model.quadfactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import grondag.adversity.Adversity;
import grondag.adversity.library.Rotation;
import grondag.adversity.library.Useful;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.pipeline.LightUtil;

public class RawQuad
    {
        // yes, this is ugly
        protected Vertex[] vertices = new Vertex[4];
        public EnumFacing face;
        public TextureAtlasSprite textureSprite;
        public Rotation rotation = Rotation.ROTATE_NONE;
        public int color;
        public LightingMode lightingMode = LightingMode.SHADED;
        public boolean lockUV = false;
        public boolean isItem = false;

        protected static AtomicLong nextQuadID = new AtomicLong(1);
        protected static long IS_AN_ANCESTOR = -1;
        protected static long NO_ID = 0;

        protected boolean isInverted = false;
        protected long quadID = NO_ID;
        protected long ancestorQuadID = NO_ID;
        protected long[] lineID = new long[4];
        
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
            retval.copyProperties(this);
            retval.copyVertices(this);
            return retval;
        }
        
        protected void copyVertices(RawQuad template)
        {
            for(int i = 0; i < 4; i++)
            {
                this.vertices[i] = template.vertices[i] == null ? null : template.vertices[i].clone();
                this.lineID[i] = template.lineID[i];
            }

        }
        public int vertexCount()
        {
            return 4;
        }
        
        protected void copyProperties(RawQuad fromObject)
        {
            this.face = fromObject.face;
            this.textureSprite = fromObject.textureSprite;
            this.rotation = fromObject.rotation;
            this.color = fromObject.color;
            this.lightingMode = fromObject.lightingMode;
            this.lockUV = fromObject.lockUV;
            this.isItem = fromObject.isItem;
            this.quadID = fromObject.quadID;
            this.ancestorQuadID = fromObject.ancestorQuadID;
            this.isInverted = fromObject.isInverted;
        }

        /** 
         * If this is a quad, returns two tris.
         * If is already a tri, returns copy of self.
         */
        public RawTri[] split()
        {
            RawTri retVal[] = new RawTri[2];
            
            long splitLineID = CSGPlane.nextLineID.getAndIncrement();
            
            retVal[0] = new RawTri(this);
            retVal[0].setVertex(0, this.getVertex(0).clone());
            retVal[0].setVertex(1, this.getVertex(1).clone());
            retVal[0].setVertex(2, this.getVertex(2).clone());
            retVal[0].setLineID(0, this.getLineID(0));
            retVal[0].setLineID(1, this.getLineID(1));
            retVal[0].setLineID(2, splitLineID);
            retVal[0].ancestorQuadID = this.getAncestorQuadIDForDescendant();
            retVal[0].quadID = nextQuadID.incrementAndGet();

            retVal[1] = new RawTri(this);
            retVal[1].setVertex(0, this.getVertex(0).clone());
            retVal[1].setVertex(1, this.getVertex(2).clone());
            retVal[1].setVertex(2, this.getVertex(3).clone());
            retVal[1].setLineID(0, splitLineID);
            retVal[1].setLineID(1, this.getLineID(2));
            retVal[1].setLineID(2, this.getLineID(3));
            retVal[1].ancestorQuadID = this.getAncestorQuadIDForDescendant();
            retVal[1].quadID = nextQuadID.incrementAndGet();
 
            return retVal;
        }

        /**
         * Reverses winding order of this quad and returns itself
         */
        public RawQuad invert()
        {
            for(int i = 0; i < 4; i++)
            {
                if(vertices[i] != null && vertices[i].normal != null)
                {
                    vertices[i].normal = vertices[i].normal.scale(-1);
                }
            }
            
            Vertex swapVertex = getVertex(0);
            setVertex(0, getVertex(2));
            setVertex(2, swapVertex);
            
            long swapLine = lineID[0];
            lineID[0] = lineID[1];
            lineID[1] = swapLine;
            swapLine = lineID[2];
            lineID[2] = lineID[3];
            lineID[3] = swapLine;
            
            this.isInverted = !this.isInverted;

            return this;
        }
        
        /** 
         * Sets up a quad with human-friendly semantics.  
         * 
         * topFace establishes a reference for "up" in these semantics.
         * Depth represents how far recessed into the surface of the face the quad should be.
         * lockUV means UV coordinates means the texture doesn't appear rotated, which in practice
         * means the UV coordinates *are* rotated so they aren't affected by the order of vertices.
         * 
         * Vertices should be given counter-clockwise.
         * Ordering of vertices is maintained for future references.
         * (First vertex passed in will be vertex 0, for example.)
         */
        public RawQuad setupFaceQuad(FaceVertex vertexIn0, FaceVertex vertexIn1, FaceVertex vertexIn2, FaceVertex vertexIn3, EnumFacing topFace)
        {
            
            EnumFacing defaultTop = Useful.defaultTopOf(this.face);
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
            else if(topFace == Useful.rightOf(this.face, defaultTop))
            {
                rv0 = new FaceVertex.Colored(vertexIn0.y, 1.0 - vertexIn0.x, vertexIn0.depth, vertexIn0.getColor(this.color));
                rv1 = new FaceVertex.Colored(vertexIn1.y, 1.0 - vertexIn1.x, vertexIn1.depth, vertexIn1.getColor(this.color));
                rv2 = new FaceVertex.Colored(vertexIn2.y, 1.0 - vertexIn2.x, vertexIn2.depth, vertexIn2.getColor(this.color));
                rv3 = new FaceVertex.Colored(vertexIn3.y, 1.0 - vertexIn3.x, vertexIn3.depth, vertexIn3.getColor(this.color));
                uvRotationCount = lockUV ? 0 : 1;
            }
            else if(topFace == Useful.bottomOf(this.face, defaultTop))
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

            //TODO: move this to baked quad creation - causes problems with CSG if done before
//            vertexIn0.x -= QuadFactory.EPSILON;
//            vertexIn3.x -= QuadFactory.EPSILON;
//            vertexIn0.y -= QuadFactory.EPSILON;
//            vertexIn1.y -= QuadFactory.EPSILON;
//
//            vertexIn2.x += QuadFactory.EPSILON;
//            vertexIn1.x += QuadFactory.EPSILON;
//            vertexIn3.y += QuadFactory.EPSILON;
//            vertexIn2.y += QuadFactory.EPSILON;

            switch(this.face)
            {
            case UP:
                setVertex(0, new Vertex(rv0.x, 1-rv0.depth, 1-rv0.y, vertexIn0.x * 16.0, (1-vertexIn0.y) * 16.0, rv0.getColor(this.color)));
                setVertex(1, new Vertex(rv1.x, 1-rv1.depth, 1-rv1.y, vertexIn1.x * 16.0, (1-vertexIn1.y) * 16.0, rv1.getColor(this.color)));
                setVertex(2, new Vertex(rv2.x, 1-rv2.depth, 1-rv2.y, vertexIn2.x * 16.0, (1-vertexIn2.y) * 16.0, rv2.getColor(this.color)));
                setVertex(3, new Vertex(rv3.x, 1-rv3.depth, 1-rv3.y, vertexIn3.x * 16.0, (1-vertexIn3.y) * 16.0, rv3.getColor(this.color)));
                break;

            case DOWN:     
                setVertex(0, new Vertex(rv0.x, rv0.depth, rv0.y, (1.0-vertexIn0.x) * 16.0, vertexIn0.y * 16.0, rv0.getColor(this.color)));
                setVertex(1, new Vertex(rv1.x, rv1.depth, rv1.y, (1.0-vertexIn1.x) * 16.0, vertexIn1.y * 16.0, rv1.getColor(this.color)));
                setVertex(2, new Vertex(rv2.x, rv2.depth, rv2.y, (1.0-vertexIn2.x) * 16.0, vertexIn2.y * 16.0, rv2.getColor(this.color)));
                setVertex(3, new Vertex(rv3.x, rv3.depth, rv3.y, (1.0-vertexIn3.x) * 16.0, vertexIn3.y * 16.0, rv3.getColor(this.color)));
                break;

            case EAST:


                setVertex(0, new Vertex(1-rv0.depth, rv0.y, 1-rv0.x, vertexIn0.x * 16.0, (1-vertexIn0.y) * 16.0, rv0.getColor(this.color)));
                setVertex(1, new Vertex(1-rv1.depth, rv1.y, 1-rv1.x, vertexIn1.x * 16.0, (1-vertexIn1.y) * 16.0, rv1.getColor(this.color)));
                setVertex(2, new Vertex(1-rv2.depth, rv2.y, 1-rv2.x, vertexIn2.x * 16.0, (1-vertexIn2.y) * 16.0, rv2.getColor(this.color)));
                setVertex(3, new Vertex(1-rv3.depth, rv3.y, 1-rv3.x, vertexIn3.x * 16.0, (1-vertexIn3.y) * 16.0, rv3.getColor(this.color)));
                break;

            case WEST:
                setVertex(0, new Vertex(rv0.depth, rv0.y, rv0.x, vertexIn0.x * 16.0, (1-vertexIn0.y) * 16.0, rv0.getColor(this.color)));
                setVertex(1, new Vertex(rv1.depth, rv1.y, rv1.x, vertexIn1.x * 16.0, (1-vertexIn1.y) * 16.0, rv1.getColor(this.color)));
                setVertex(2, new Vertex(rv2.depth, rv2.y, rv2.x, vertexIn2.x * 16.0, (1-vertexIn2.y) * 16.0, rv2.getColor(this.color)));
                setVertex(3, new Vertex(rv3.depth, rv3.y, rv3.x, vertexIn3.x * 16.0, (1-vertexIn3.y) * 16.0, rv3.getColor(this.color)));
                break;

            case NORTH:
                setVertex(0, new Vertex(1-rv0.x, rv0.y, rv0.depth, rv0.x * 16.0, (1-rv0.y) * 16.0, rv0.getColor(this.color)));
                setVertex(1, new Vertex(1-rv1.x, rv1.y, rv1.depth, rv1.x * 16.0, (1-rv1.y) * 16.0, rv1.getColor(this.color)));
                setVertex(2, new Vertex(1-rv2.x, rv2.y, rv2.depth, rv2.x * 16.0, (1-rv2.y) * 16.0, rv2.getColor(this.color)));
                setVertex(3, new Vertex(1-rv3.x, rv3.y, rv3.depth, rv3.x * 16.0, (1-rv3.y) * 16.0, rv3.getColor(this.color)));
                break;

            case SOUTH:
                setVertex(0, new Vertex(rv0.x, rv0.y, 1-rv0.depth, rv0.x * 16.0, (1-rv0.y) * 16.0, rv0.getColor(this.color)));
                setVertex(1, new Vertex(rv1.x, rv1.y, 1-rv1.depth, rv1.x * 16.0, (1-rv1.y) * 16.0, rv1.getColor(this.color)));
                setVertex(2, new Vertex(rv2.x, rv2.y, 1-rv2.depth, rv2.x * 16.0, (1-rv2.y) * 16.0, rv2.getColor(this.color)));
                setVertex(3, new Vertex(rv3.x, rv3.y, 1-rv3.depth, rv3.x * 16.0, (1-rv3.y) * 16.0, rv3.getColor(this.color)));
                break;
            }

            for (int r = 0; r < uvRotationCount; r++)
            {
                QuadFactory.rotateQuadUV(this.vertices[0], this.vertices[1], this.vertices[2], this.vertices[3]);
            }
            
            return this;
        }

        public RawQuad setupFaceQuad(EnumFacing side, FaceVertex tv0, FaceVertex tv1, FaceVertex tv2, FaceVertex tv3, EnumFacing topFace)
        {
            this.face = side;
            return this.setupFaceQuad(tv0, tv1, tv2, tv3, topFace);
        }

        /** 
         * Sets up a quad with standard semantics.  
         * x0,y0 are at lower left and x1, y1 are top right.
         * topFace establishes a reference for "up" in these semantics.
         * Depth represents how far recessed into the surface of the face the quad should be.
         * lockUV means UV coordinates means the texture doesn't appear rotated, which in practice
         * means the UV coordinates *are* rotated so that a different part of the texture shows through.
         * 
         * Returns self for convenience.
         */
        public RawQuad setupFaceQuad(double x0, double y0, double x1, double y1, double depth, EnumFacing topFace)
        {
            this.setupFaceQuad(
                    new FaceVertex(x0, y0, depth),
                    new FaceVertex(x1, y0, depth),
                    new FaceVertex(x1, y1, depth),
                    new FaceVertex(x0, y1, depth), 
                    topFace);
            
            return this;
        }


        public RawQuad setupFaceQuad(EnumFacing face, double x0, double y0, double x1, double y1, double depth, EnumFacing topFace)
        {
            this.face = face;
            return this.setupFaceQuad(x0, y0, x1, y1, depth, topFace);
        }

        /** Using this instead of method on vertex 
         * ensures normals are set correctly for tris.
         */
        public void setNormal(int index, Vec3d normalIn)
        {
            this.vertices[index].setNormal(normalIn);
        }

        /**
         * Changes all vertices and quad color to new color and returns itself
         */
        public RawQuad recolor(int color)
        {
            this.color = color;
            if(vertices[0] != null) vertices[0].color = color;
            if(vertices[1] != null) vertices[1].color = color;
            if(vertices[2] != null) vertices[2].color = color;
            if(vertices[3] != null) vertices[3].color = color;
            return this;
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

        public void setLineID(int index, long lineID)
        {
            this.lineID[index] = lineID;
        }
        
        public long getLineID(int index)
        {
            return this.lineID[index];
        }
        
        public static int LINE_NOT_FOUND = -1;
        /**
         * Returns the index of the edge with the given line ID.
         * Returns LINE_NOT_FOUND if, uh, you know, it wasn't found.
         */
        public int findLineIndex(long lineID)
        {
            for(int i = 0; i < this.vertexCount(); i++)
            {
                if(this.getLineID(i) == lineID)
                {
                    return i;
                }
            }
            return LINE_NOT_FOUND;
        }
        
        public long getAncestorQuadID()
        {
            return this.ancestorQuadID;
        }
        
        public long getAncestorQuadIDForDescendant()
        {
            return this.ancestorQuadID == IS_AN_ANCESTOR ? this.quadID : this.ancestorQuadID;
        }
        
        public RawQuad initCsg()
        {
            this.ancestorQuadID = IS_AN_ANCESTOR;
            this.quadID = nextQuadID.getAndIncrement();
            for(int i = 0; i < 4; i++)
            {
                if(vertices[i] != null)
                {
                    if(i == 3 && this instanceof RawTri)
                    {
                        vertices[3].setVertexID(vertices[2].getVertexID());
                        this.setLineID(3, this.getLineID(2));
                    }
                    else
                    {
                        this.setLineID(i, CSGPlane.nextLineID.getAndIncrement());
                    }
                }
            }
            return this;
        }
        
//        public List<RawQuad> clipToFace(EnumFacing face, RawQuad patchTemplate)
//        {
//            LinkedList<RawQuad> retVal = new LinkedList<RawQuad>();
//            for(RawTri tri : this.split())
//            {
//                retVal.addAll(tri.splitOnFace(face, patchTemplate));
//            }
//            return retVal;
//        }

        /**
         * Returns true if this polygon is convex.
         * All Tris must be.  
         * For quads, confirms that each turn around the quad 
         * goes same way by comparing cross products of edges.
         */
        public boolean isConvex()
        {
            if(this instanceof RawTri) return true;
            
            Vec3d testVector = null;
            
            for(int thisVertex = 0; thisVertex < this.vertexCount(); thisVertex++)
            {
                int nextVertex = thisVertex + 1;
                if(nextVertex == this.vertexCount()) nextVertex = 0;

                int priorVertex = thisVertex - 1;
                if(priorVertex == -1) priorVertex = this.vertexCount() - 1;

                Vec3d lineA = vertices[thisVertex].subtract(vertices[priorVertex]);
                Vec3d lineB = vertices[nextVertex].subtract(vertices[thisVertex]);
                
                if(testVector == null)
                {
                    testVector = lineA.crossProduct(lineB);
                }
                else if(testVector.dotProduct(lineA.crossProduct(lineB)) < 0)
                {
                    return false;
                }
            }
            return true;
        }
        
        protected boolean isOrthogonalTo(EnumFacing face)
        {
            return Math.abs(this.getFaceNormal().dotProduct(new Vec3d(face.getDirectionVec()))) <= QuadFactory.EPSILON;
        }

        public boolean isOnFace(EnumFacing face)
        {
            return vertices[0].isOnFacePlane(face)
                && vertices[1].isOnFacePlane(face)
                && vertices[2].isOnFacePlane(face)
                && vertices[3].isOnFacePlane(face);
        }
        
        public boolean intersectsWithRay(Vec3d origin, Vec3d direction)
        {
            Vec3d normal = this.getFaceNormal();

            double directionDotNormal = normal.dotProduct(direction);
            if (Math.abs(directionDotNormal) < QuadFactory.EPSILON) 
            { 
                // parallel
                return false;
            }

            double distanceToPlane = -normal.dotProduct((origin.subtract(vertices[0]))) / directionDotNormal;
            // facing away from plane
            if(distanceToPlane < -QuadFactory.EPSILON) return false;
            
            Vec3d intersection = origin.add(direction.scale(distanceToPlane));

            // now we just need to test if point is inside this polygon
            return containsPoint(intersection);
        }
        
        /**
         * Assumes the given point is on the plane of the polygon.
         * 
         * For each side, find a vector in the plane of the 
         * polygon orthogonal to the line formed by the two vertices of the edge.
         * Then take the dot product with vector formed by the first vertex and the point.
         * If the point is inside the polygon, the sign should be the same for all
         * edges, or the dot product should be very small, meaning the point is on the edge.
         */
        public boolean containsPoint(Vec3d point)
        {
 
            double lastSignum = 0;
            Vec3d faceNormal = this.getFaceNormal();
            int vertexCount = this instanceof RawTri ? 3 : 4;
            
            for(int i = 0; i < vertexCount; i++)
            {
                int nextVertex = i + 1;
                if(nextVertex == vertexCount) nextVertex = 0;
                
                Vec3d line = vertices[nextVertex].subtract(vertices[i]);
                Vec3d normalInPlane = faceNormal.crossProduct(line);
                
                double sign = normalInPlane.dotProduct(point.subtract(vertices[i]));
                
                if(lastSignum == 0)
                {
                    lastSignum = Math.signum(sign);
                }
                else if(Math.signum(sign) != lastSignum)
                {
                    return false;
                }
            }
            return true;
        }
        
        
        public AxisAlignedBB getAABB()
        {
            double minX = Math.min(Math.min(vertices[0].xCoord, vertices[1].xCoord), Math.min(vertices[2].xCoord, vertices[3].xCoord));
            double minY = Math.min(Math.min(vertices[0].yCoord, vertices[1].yCoord), Math.min(vertices[2].yCoord, vertices[3].yCoord));
            double minZ = Math.min(Math.min(vertices[0].zCoord, vertices[1].zCoord), Math.min(vertices[2].zCoord, vertices[3].zCoord));
         
            double maxX = Math.max(Math.max(vertices[0].xCoord, vertices[1].xCoord), Math.max(vertices[2].xCoord, vertices[3].xCoord));
            double maxY = Math.max(Math.max(vertices[0].yCoord, vertices[1].yCoord), Math.max(vertices[2].yCoord, vertices[3].yCoord));
            double maxZ = Math.max(Math.max(vertices[0].zCoord, vertices[1].zCoord), Math.max(vertices[2].zCoord, vertices[3].zCoord));

            return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
        }
        
        public BakedQuad createBakedQuad()
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


            return new BakedQuad(vertexData, color, face, textureSprite, lightingMode == LightingMode.SHADED, format);

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