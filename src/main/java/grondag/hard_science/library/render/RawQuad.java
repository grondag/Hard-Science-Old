package grondag.hard_science.library.render;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4d;

import grondag.hard_science.Log;
import grondag.hard_science.library.varia.Color;
import grondag.hard_science.library.world.Rotation;
import grondag.hard_science.superblock.model.state.Surface;
import grondag.hard_science.superblock.model.state.SurfaceTopology;
import grondag.hard_science.superblock.model.state.SurfaceType;
import grondag.hard_science.superblock.model.state.Surface.SurfaceInstance;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class RawQuad
{
    private static final SurfaceInstance NO_SURFACE = new Surface(SurfaceType.MAIN, SurfaceTopology.CUBIC).unitInstance;
    
    private Vertex[] vertices;
    private Vec3d faceNormal = null;
    private final int vertexCount;

    protected EnumFacing face;
    public String textureName;
    
    /** 
     * Causes texture to appear rotated within the frame
     * of this texture. Relies on UV coordinates
     * being in the range 0-16. <br><br>
     * 
     * Rotation happens during quad bake.
     * If lockUV is true, rotation happens after UV
     * coordinates are derived.
     */
    public Rotation rotation = Rotation.ROTATE_NONE;
    
    public int color = Color.WHITE;
    
    public boolean isFullBrightness = false;
    
    /** 
     * If true then UV coordinates will be ignored and instead set
     * based on projection of vertices onto the given nominal face.
     */
    public boolean lockUV = false;
    

    public boolean shouldContractUVs = true;
    
    public BlockRenderLayer renderLayer = BlockRenderLayer.SOLID;
    public SurfaceInstance surfaceInstance = NO_SURFACE;

    public float minU = 0;
    public float maxU = 16;
    public float minV = 0;
    public float maxV = 16;

    protected static AtomicLong nextQuadID = new AtomicLong(1);
    protected static long IS_AN_ANCESTOR = -1;
    protected static long NO_ID = 0;

    protected boolean isInverted = false;
    protected long quadID = nextQuadID.incrementAndGet();
    protected long ancestorQuadID = NO_ID;
    protected long[] lineID;

//    private static final Vec3d LIGHTING_NORMAL = new Vec3d(0, 1, 0.35).normalize();

    public RawQuad()
    {
        this(4);
    }

    public RawQuad(RawQuad template)
    {
        this(template, template.getVertexCount());
    }

    public RawQuad(int vertexCount)
    {
        super();
        this.vertexCount = Math.max(3, vertexCount);
        this.vertices = new Vertex[vertexCount];
        this.lineID = new long[vertexCount];
    }

    public RawQuad(RawQuad template, int vertexCount)
    {
        this(vertexCount);
        this.copyProperties(template);
    }

    public RawQuad clone()
    {
        RawQuad retval = new RawQuad(this);
        retval.copyVertices(this);
        return retval;
    }

    protected void copyVertices(RawQuad template)
    {
        for(int i = 0; i < this.getVertexCount(); i++)
        {
            this.setVertex(i, template.getVertex(i) == null ? null : template.getVertex(i).clone());
            this.lineID[i] = template.lineID[i];
        }
    }

    public int getVertexCount()
    {
        return this.vertexCount;
    }

    private void copyProperties(RawQuad fromObject)
    {
        this.setFace(fromObject.getNominalFace());
        this.textureName = fromObject.textureName;
        this.rotation = fromObject.rotation;
        this.color = fromObject.color;
        this.isFullBrightness = fromObject.isFullBrightness;
        this.lockUV = fromObject.lockUV;
        this.ancestorQuadID = fromObject.ancestorQuadID;
        this.isInverted = fromObject.isInverted;
        this.faceNormal = fromObject.faceNormal;
        this.shouldContractUVs = fromObject.shouldContractUVs;
        this.minU = fromObject.minU;
        this.maxU = fromObject.maxU;
        this.minV = fromObject.minV;
        this.maxV = fromObject.maxV;
        this.renderLayer = fromObject.renderLayer;
        this.surfaceInstance = fromObject.surfaceInstance;
    }

    public List<RawQuad> toQuads()
    {
        ArrayList<RawQuad> retVal = new ArrayList<RawQuad>();

        if(this.vertexCount <= 4)
        {
            retVal.add(this);
        }
        else
        {
            int head = vertexCount - 1;
            int tail = 2;
            RawQuad work = new RawQuad(this, 4);
            work.setVertex(0, this.getVertex(head));
            work.setVertex(1, this.getVertex(0));
            work.setVertex(2, this.getVertex(1));
            work.setVertex(3, this.getVertex(tail));
            retVal.add(work);

            while(head - tail > 1)
            {
                work = new RawQuad(this, head - tail == 2 ? 3 : 4);
                work.setVertex(0, this.getVertex(head));
                work.setVertex(1, this.getVertex(tail));
                work.setVertex(2, this.getVertex(++tail));
                if(head - tail > 1)
                {
                    work.setVertex(3, this.getVertex(--head));
                }
                retVal.add(work);
            }
        }
        return retVal;
    }

    /** 
     * If this is a quad, returns two tris.
     * If is already a tri, returns copy of self.
     */
    public List<RawQuad> toTris()
    {
        ArrayList<RawQuad>  retVal= new ArrayList<RawQuad>(this.getVertexCount()-2);

        if(this.getVertexCount() == 3)
        {
            retVal.add(this.clone());
        }
        //            else if(this.getVertexCount() == 4)
        //            {
        //                long splitLineID = CSGPlane.nextInsideLineID.getAndIncrement();
        //                
        //                RawQuad work = new RawQuad(this, 3);
        //               work.setVertex(0, this.getVertex(0));
        //               work.setVertex(1, this.getVertex(1));
        //               work.setVertex(2, this.getVertex(2));
        //               work.setLineID(0, this.getLineID(0));
        //               work.setLineID(1, this.getLineID(1));
        //               work.setLineID(2, splitLineID);
        //               work.ancestorQuadID = this.getAncestorQuadIDForDescendant();
        //               retVal.add(work);
        //
        //               work = new RawQuad(this, 3);
        //               work.setVertex(0, this.getVertex(0));
        //               work.setVertex(1, this.getVertex(2));
        //               work.setVertex(2, this.getVertex(3));
        //               work.setLineID(0, splitLineID);
        //               work.setLineID(1, this.getLineID(2));
        //               work.setLineID(2, this.getLineID(3));
        //               work.ancestorQuadID = this.getAncestorQuadIDForDescendant();
        //               retVal.add(work);
        //
        //            }
        else
        {
            long splitLineID = CSGPlane.nextInsideLineID.getAndIncrement();
            int head = vertexCount - 1;
            int tail = 1;

            RawQuad work = new RawQuad(this, 3);
            work.setVertex(0, this.getVertex(head));
            work.setLineID(0, this.getLineID(head));
            work.setVertex(1, this.getVertex(0));
            work.setLineID(1, this.getLineID(0));
            work.setVertex(2, this.getVertex(tail));
            work.setLineID(2, splitLineID);
            work.ancestorQuadID = this.getAncestorQuadIDForDescendant();
            retVal.add(work);

            while(head - tail > 1)
            {
                work = new RawQuad(this, 3);
                work.setVertex(0, this.getVertex(head));
                work.setLineID(0, splitLineID);
                work.setVertex(1, this.getVertex(tail));
                work.setLineID(1, this.getLineID(tail));
                splitLineID = CSGPlane.nextInsideLineID.getAndIncrement();
                work.setVertex(2, this.getVertex(++tail));
                work.setLineID(2, head - tail == 1 ? this.getLineID(tail): splitLineID);
                work.ancestorQuadID = this.getAncestorQuadIDForDescendant();
                retVal.add(work);

                if(head - tail > 1)
                {
                    work = new RawQuad(this, 3);
                    work.setVertex(0, this.getVertex(head));
                    work.setLineID(0, splitLineID);
                    splitLineID = CSGPlane.nextInsideLineID.getAndIncrement();
                    work.setVertex(1, this.getVertex(tail));
                    work.setLineID(1, head - tail == 1 ? this.getLineID(tail): splitLineID);
                    work.setVertex(2, this.getVertex(--head).clone());
                    work.setLineID(2, this.getLineID(head));
                    work.ancestorQuadID = this.getAncestorQuadIDForDescendant();
                    retVal.add(work);
                }
            }
        }

        return retVal;
    }

    /**
     * Reverses winding order of this quad and returns itself
     */
    public RawQuad invert()
    {

        for(int i = 0; i < vertices.length; i++)
        {
            if(getVertex(i) != null && getVertex(i).normal != null)
            {
                setVertexNormal(i, getVertex(i).normal.scale(-1));
            }
        }            

        //reverse order of vertices
        for (int i = 0, mid = vertices.length / 2, j = vertices.length - 1; i < mid; i++, j--)
        {
            Vertex swapVertex = vertices[i];
            vertices[i] = vertices[j];
            vertices[j] = swapVertex;
        }

        // last edge is still always the last, and isn't sorted  (draw it to see)
        for (int i = 0, mid = (vertices.length - 1) / 2, j = vertices.length - 2; i < mid; i++, j--)
        {
            long swapLineID = lineID[i];
            lineID[i] = lineID[j];
            lineID[j] = swapLineID;
        }

        if(this.faceNormal != null) this.faceNormal = faceNormal.scale(-1);

        this.isInverted = !this.isInverted;

        return this;
    }

    /** 
     * Sets up a quad with human-friendly semantics. <br><br>
     * 
     * topFace establishes a reference for "up" in these semantics.
     * Depth represents how far recessed into the surface of the face the quad should be. <br><br>
     * 
     * Vertices should be given counter-clockwise.
     * Ordering of vertices is maintained for future references.
     * (First vertex passed in will be vertex 0, for example.) <br><br>
     * 
     * UV coordinates always based on where rotated vertices project onto the nominal 
     * face for this quad. (Do not use this unless lockUV is on.)
     */
    public RawQuad setupFaceQuad(FaceVertex vertexIn0, FaceVertex vertexIn1, FaceVertex vertexIn2, FaceVertex vertexIn3, EnumFacing topFace)
    {
        if(!this.lockUV) Log.warn("RawQuad faceQuad used when lockUV is false. Quad semantics may be invalid.");
        
        EnumFacing defaultTop = QuadHelper.defaultTopOf(this.getNominalFace());
        FaceVertex rv0;
        FaceVertex rv1;
        FaceVertex rv2;
        FaceVertex rv3;

        if(topFace == defaultTop)
        {
            rv0 = vertexIn0.clone();
            rv1 = vertexIn1.clone();
            rv2 = vertexIn2.clone();
            rv3 = vertexIn3.clone();
        }
        else if(topFace == QuadHelper.rightOf(this.getNominalFace(), defaultTop))
        {
            rv0 = new FaceVertex.Colored(vertexIn0.y, 1.0 - vertexIn0.x, vertexIn0.depth, vertexIn0.getColor(this.color));
            rv1 = new FaceVertex.Colored(vertexIn1.y, 1.0 - vertexIn1.x, vertexIn1.depth, vertexIn1.getColor(this.color));
            rv2 = new FaceVertex.Colored(vertexIn2.y, 1.0 - vertexIn2.x, vertexIn2.depth, vertexIn2.getColor(this.color));
            rv3 = new FaceVertex.Colored(vertexIn3.y, 1.0 - vertexIn3.x, vertexIn3.depth, vertexIn3.getColor(this.color));
        }
        else if(topFace == QuadHelper.bottomOf(this.getNominalFace(), defaultTop))
        {
            rv0 = new FaceVertex.Colored(1.0 - vertexIn0.x, 1.0 - vertexIn0.y, vertexIn0.depth, vertexIn0.getColor(this.color));
            rv1 = new FaceVertex.Colored(1.0 - vertexIn1.x, 1.0 - vertexIn1.y, vertexIn1.depth, vertexIn1.getColor(this.color));
            rv2 = new FaceVertex.Colored(1.0 - vertexIn2.x, 1.0 - vertexIn2.y, vertexIn2.depth, vertexIn2.getColor(this.color));
            rv3 = new FaceVertex.Colored(1.0 - vertexIn3.x, 1.0 - vertexIn3.y, vertexIn3.depth, vertexIn3.getColor(this.color));
        }
        else // left of
        {
            rv0 = new FaceVertex.Colored(1.0 - vertexIn0.y, vertexIn0.x, vertexIn0.depth, vertexIn0.getColor(this.color));
            rv1 = new FaceVertex.Colored(1.0 - vertexIn1.y, vertexIn1.x, vertexIn1.depth, vertexIn1.getColor(this.color));
            rv2 = new FaceVertex.Colored(1.0 - vertexIn2.y, vertexIn2.x, vertexIn2.depth, vertexIn2.getColor(this.color));
            rv3 = new FaceVertex.Colored(1.0 - vertexIn3.y, vertexIn3.x, vertexIn3.depth, vertexIn3.getColor(this.color));
        }

        // NOTE: the UVs that are populated here will be replaced at quad bake.
        // They are leftover from a prior approach.
        // Left in because we need to put in some value anyway and they do no harm.
        
        switch(this.getNominalFace())
        {
        case UP:
            setVertex(0, new Vertex(rv0.x, 1-rv0.depth, 1-rv0.y, rv0.x * 16.0, (1-rv0.y) * 16.0, rv0.getColor(this.color)));
            setVertex(1, new Vertex(rv1.x, 1-rv1.depth, 1-rv1.y, rv1.x * 16.0, (1-rv1.y) * 16.0, rv1.getColor(this.color)));
            setVertex(2, new Vertex(rv2.x, 1-rv2.depth, 1-rv2.y, rv2.x * 16.0, (1-rv2.y) * 16.0, rv2.getColor(this.color)));
            setVertex(3, new Vertex(rv3.x, 1-rv3.depth, 1-rv3.y, rv3.x * 16.0, (1-rv3.y) * 16.0, rv3.getColor(this.color)));
            break;

        case DOWN:     
            setVertex(0, new Vertex(rv0.x, rv0.depth, rv0.y, (1.0-rv0.x) * 16.0, rv0.y * 16.0, rv0.getColor(this.color)));
            setVertex(1, new Vertex(rv1.x, rv1.depth, rv1.y, (1.0-rv1.x) * 16.0, rv1.y * 16.0, rv1.getColor(this.color)));
            setVertex(2, new Vertex(rv2.x, rv2.depth, rv2.y, (1.0-rv2.x) * 16.0, rv2.y * 16.0, rv2.getColor(this.color)));
            setVertex(3, new Vertex(rv3.x, rv3.depth, rv3.y, (1.0-rv3.x) * 16.0, rv3.y * 16.0, rv3.getColor(this.color)));
            break;

        case EAST:
            setVertex(0, new Vertex(1-rv0.depth, rv0.y, 1-rv0.x, rv0.x * 16.0, (1-rv0.y) * 16.0, rv0.getColor(this.color)));
            setVertex(1, new Vertex(1-rv1.depth, rv1.y, 1-rv1.x, rv1.x * 16.0, (1-rv1.y) * 16.0, rv1.getColor(this.color)));
            setVertex(2, new Vertex(1-rv2.depth, rv2.y, 1-rv2.x, rv2.x * 16.0, (1-rv2.y) * 16.0, rv2.getColor(this.color)));
            setVertex(3, new Vertex(1-rv3.depth, rv3.y, 1-rv3.x, rv3.x * 16.0, (1-rv3.y) * 16.0, rv3.getColor(this.color)));
            break;

        case WEST:
            setVertex(0, new Vertex(rv0.depth, rv0.y, rv0.x, rv0.x * 16.0, (1-rv0.y) * 16.0, rv0.getColor(this.color)));
            setVertex(1, new Vertex(rv1.depth, rv1.y, rv1.x, rv1.x * 16.0, (1-rv1.y) * 16.0, rv1.getColor(this.color)));
            setVertex(2, new Vertex(rv2.depth, rv2.y, rv2.x, rv2.x * 16.0, (1-rv2.y) * 16.0, rv2.getColor(this.color)));
            setVertex(3, new Vertex(rv3.depth, rv3.y, rv3.x, rv3.x * 16.0, (1-rv3.y) * 16.0, rv3.getColor(this.color)));
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

        return this;
    }

    /**
     * Same as {@link #setupFaceQuad(FaceVertex, FaceVertex, FaceVertex, FaceVertex, EnumFacing)}
     * except also sets nominal face to the given face in the first parameter. 
     * Returns self for convenience.
     */
    public RawQuad setupFaceQuad(EnumFacing side, FaceVertex tv0, FaceVertex tv1, FaceVertex tv2, FaceVertex tv3, EnumFacing topFace)
    {
        assert(this.getVertexCount() == 4);
        this.setFace(side);
        return this.setupFaceQuad(tv0, tv1, tv2, tv3, topFace);
    }

    /** 
     * Sets up a quad with standard semantics.  
     * x0,y0 are at lower left and x1, y1 are top right.
     * topFace establishes a reference for "up" in these semantics.
     * Depth represents how far recessed into the surface of the face the quad should be.<br><br>
     * 
     * Returns self for convenience.<br><br>
     * 
     * @see #setupFaceQuad(FaceVertex, FaceVertex, FaceVertex, FaceVertex, EnumFacing)
     */
    public RawQuad setupFaceQuad(double x0, double y0, double x1, double y1, double depth, EnumFacing topFace)
    {
        assert(this.getVertexCount() == 4);
        this.setupFaceQuad(
                new FaceVertex(x0, y0, depth),
                new FaceVertex(x1, y0, depth),
                new FaceVertex(x1, y1, depth),
                new FaceVertex(x0, y1, depth), 
                topFace);
        return this;
    }


    /**
     * Same as {@link #setupFaceQuad(double, double, double, double, double, EnumFacing)}
     * but also sets nominal face with given face in first parameter.  
     * Returns self as convenience.
     */
    public RawQuad setupFaceQuad(EnumFacing face, double x0, double y0, double x1, double y1, double depth, EnumFacing topFace)
    {
        assert(this.getVertexCount() == 4);
        this.setFace(face);
        return this.setupFaceQuad(x0, y0, x1, y1, depth, topFace);
    }

    /**
     * Triangular version of {@link #setupFaceQuad(EnumFacing, FaceVertex, FaceVertex, FaceVertex, EnumFacing)}
     */
    public RawQuad setupFaceQuad(EnumFacing side, FaceVertex tv0, FaceVertex tv1, FaceVertex tv2, EnumFacing topFace)
    {
        assert(this.getVertexCount() == 3);
        this.setFace(side);
        return this.setupFaceQuad(tv0, tv1, tv2, tv2, topFace);
    }

    /**
     * Triangular version of {@link #setupFaceQuad(FaceVertex, FaceVertex, FaceVertex, FaceVertex, EnumFacing)}
     */
    public RawQuad setupFaceQuad(FaceVertex tv0, FaceVertex tv1, FaceVertex tv2, EnumFacing topFace)
    {
        assert(this.getVertexCount() == 3);
        return this.setupFaceQuad(tv0, tv1, tv2, tv2, topFace);
    }

    /** Using this instead of method on vertex 
     * ensures normals are set correctly for tris.
     */
    public void setVertexNormal(int index, Vec3d normalIn)
    {
        if(index < this.vertexCount)
        {
            this.setVertex(index, this.getVertex(index).withNormal(normalIn));
        }
    }

    /**
     * Changes all vertices and quad color to new color and returns itself
     */
    public RawQuad replaceColor(int color)
    {
        this.color = color;
        for(int i = 0; i < this.getVertexCount(); i++)
        {
            if(getVertex(i) != null) setVertex(i, getVertex(i).withColor(color));
        }
        return this;
    }
    

    /**
     * Multiplies all vertex color by given color and returns itself
     */
    public RawQuad multiplyColor(int color)
    {
        this.color = QuadHelper.multiplyColor(this.color, color);
        for(int i = 0; i < this.getVertexCount(); i++)
        {
            Vertex v = this.getVertex(i);
            if(v != null)
            {
                int vColor = QuadHelper.multiplyColor(color, v.color);
                this.setVertex(i, v.withColor(vColor));
            }
        }
        return this;
    }

    /** 
     * Using this instead of referencing vertex array directly.
     */
    public void setVertex(int index, Vertex vertexIn)
    {
        if(index < this.vertexCount)
        {
            this.vertices[index] = vertexIn;
            this.faceNormal = null;
        }
    }

    public Vertex getVertex(int index)
    {
        if(this.getVertexCount() == 3 && index == 3) return this.vertices[2];
        return this.vertices[index];
    }

    public void setLineID(int index, long lineID)
    {
        if(index < this.vertexCount)
        {
            this.lineID[index] = lineID;
        }
    }

    public long getLineID(int index)
    {
        if(this.getVertexCount() == 3 && index == 3) return this.lineID[2];
        return this.lineID[index];
    }

    public long getQuadID()
    {
        return this.quadID;
    }
    
    public static int LINE_NOT_FOUND = -1;
    /**
     * Returns the index of the edge with the given line ID.
     * Returns LINE_NOT_FOUND if, uh, you know, it wasn't found.
     */
    public int findLineIndex(long lineID)
    {
        for(int i = 0; i < this.getVertexCount(); i++)
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
        for(int i = 0; i < this.getVertexCount(); i++)
        {
            this.lineID[i] = CSGPlane.nextOutsideLineID.getAndDecrement();
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
        if(this.getVertexCount() == 3) return true;

        Vec3d testVector = null;

        for(int thisVertex = 0; thisVertex < this.getVertexCount(); thisVertex++)
        {
            int nextVertex = thisVertex + 1;
            if(nextVertex == this.getVertexCount()) nextVertex = 0;

            int priorVertex = thisVertex - 1;
            if(priorVertex == -1) priorVertex = this.getVertexCount() - 1;

            Vec3d lineA = getVertex(thisVertex).subtract(getVertex(priorVertex));
            Vec3d lineB = getVertex(nextVertex).subtract(getVertex(thisVertex));

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
        return Math.abs(this.getFaceNormal().dotProduct(new Vec3d(face.getDirectionVec()))) <= QuadHelper.EPSILON;
    }

    public boolean isOnSinglePlane()
    {
        if(this.getVertexCount() == 3) return true;

        Vec3d fn = this.getFaceNormal();
        if(fn == null) return false;

        for(int i = 3; i < this.getVertexCount(); i++)
        {
            Vertex v = this.getVertex(i);
            if(v == null) return false;

            if(Math.abs(v.subtract(this.getVertex(0)).dotProduct(fn)) > QuadHelper.EPSILON) return false;
        }

        return true;
    }

    public boolean isOnFace(EnumFacing face, double tolerance)
    {
        if(face == null) return false;
        boolean retVal = true;
        for(int i = 0; i < this.vertexCount; i++)
        {
            retVal = retVal && getVertex(i).isOnFacePlane(face, tolerance);
        }
        return retVal;
    }

    /** 
     * Returns intersection point of given ray with the plane of this quad.
     * Return null if parallel or facing away.
     */
    public Vec3d intersectionOfRayWithPlane(Vec3d origin, Vec3d direction)
    {
        Vec3d normal = this.getFaceNormal();

        double directionDotNormal = normal.dotProduct(direction);
        if (Math.abs(directionDotNormal) < QuadHelper.EPSILON) 
        { 
            // parallel
            return null;
        }

        double distanceToPlane = -normal.dotProduct((origin.subtract(getVertex(0)))) / directionDotNormal;
        // facing away from plane
        if(distanceToPlane < -QuadHelper.EPSILON) return null;

        return origin.add(direction.scale(distanceToPlane));
    }
    
    /**
     * Keeping for convenience in case discover any problems with the fast version.
     * Unit tests indicate identical results.
     */
    public boolean intersectsWithRaySlow(Vec3d origin, Vec3d direction)
    {
        Vec3d intersection = this.intersectionOfRayWithPlane(origin, direction);
        
        // now we just need to test if point is inside this polygon
        return intersection == null ? false : containsPointSlow(intersection);
        
    }

    public boolean intersectsWithRay(Vec3d origin, Vec3d direction)
    {
        Vec3d intersection = this.intersectionOfRayWithPlane(origin, direction);

        // now we just need to test if point is inside this polygon
        return intersection == null ? false : containsPoint(intersection);
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
        return PointInPolygonTest.isPointInRawQuad(point, this);
    }
    

    /**
     * Keeping for convenience in case discover any problems with the fast version.
     * Unit tests indicate identical results.
     */
    public boolean containsPointSlow(Vec3d point)
    {
        double lastSignum = 0;
        Vec3d faceNormal = this.getFaceNormal();

        for(int i = 0; i < this.getVertexCount(); i++)
        {
            int nextVertex = i + 1;
            if(nextVertex == this.getVertexCount()) nextVertex = 0;

            Vec3d line = getVertex(nextVertex).subtract(getVertex(i));
            Vec3d normalInPlane = faceNormal.crossProduct(line);

            double sign = normalInPlane.dotProduct(point.subtract(getVertex(i)));

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
        double minX = Math.min(Math.min(getVertex(0).x, getVertex(1).x), Math.min(getVertex(2).x, getVertex(3).x));
        double minY = Math.min(Math.min(getVertex(0).y, getVertex(1).y), Math.min(getVertex(2).y, getVertex(3).y));
        double minZ = Math.min(Math.min(getVertex(0).z, getVertex(1).z), Math.min(getVertex(2).z, getVertex(3).z));

        double maxX = Math.max(Math.max(getVertex(0).x, getVertex(1).x), Math.max(getVertex(2).x, getVertex(3).x));
        double maxY = Math.max(Math.max(getVertex(0).y, getVertex(1).y), Math.max(getVertex(2).y, getVertex(3).y));
        double maxZ = Math.max(Math.max(getVertex(0).z, getVertex(1).z), Math.max(getVertex(2).z, getVertex(3).z));

        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * Assigns UV coordinates to each vertex by projecting vertex
     * onto plane of the quad's face. If the quad is not rotated,
     * then semantics of vertex coordinates matches those of setupFaceQuad.
     * For example, on NSEW faces, "up" (+y) corresponds to the top of the texture.
     * 
     * Assigned values are in the range 0-16, as is conventional for MC.
     */
    public void assignLockedUVCoordinates()
    {
        EnumFacing face = getNominalFace();
        if(face == null)
        {
            if(Log.DEBUG_MODE)
                Log.warn("RawQuad.assignLockedUVCoordinates encountered null nominal face.  Should not occur.  Using normal face instead.");
            face = getNormalFace();
        }

        for(int i = 0; i < this.getVertexCount(); i++)
        {
            Vertex v = getVertex(i);
            
            switch(face)
            {
            case EAST:
                this.setVertex(i, v.withUV((1.0 - v.z) * 16, (1.0 - v.y) * 16));
                break;
                
            case WEST:
                this.setVertex(i, v.withUV(v.z * 16, (1.0 - v.y) * 16));
                break;
                
            case NORTH:
                this.setVertex(i, v.withUV((1.0 - v.x) * 16, (1.0 - v.y) * 16));
                break;
                
            case SOUTH:
                this.setVertex(i, v.withUV(v.x * 16, (1.0 - v.y) * 16));
                break;
                
            case DOWN:
                this.setVertex(i, v.withUV(v.x * 16, (1.0 - v.z) * 16));
                break;
                
            case UP:
                // our default semantic for UP is different than MC
                // "top" is north instead of south
                this.setVertex(i, v.withUV(v.x * 16, v.z * 16));
                break;
            
            }
        }
        
    }
     /**
      * Multiplies uvMin/Max by the given factors.
      */
    public void scaleQuadUV(double uScale, double vScale)
    {
        this.minU *= uScale;
        this.maxU *= uScale;
        this.minV *= vScale;
        this.maxV *= vScale;
    }
    
    /** 
     * Simple scale transformation of all vertex coordinates 
     * using block center (0.5, 0.5, 0.5) as origin.
     */
    public void scaleFromBlockCenter(double scale)
    {
        double c = 0.5 * (1-scale);
        
        for(int i = 0; i < this.getVertexCount(); i++)
        {
            Vertex v = getVertex(i);
            this.setVertex(i, v.withXYZ(v.x * scale + c, v.y * scale + c, v.z * scale + c));
        }
    }

    public Vec3d getFaceNormal()
    {
        if(faceNormal == null && getVertex(0) != null && getVertex(1) != null && getVertex(2) != null && getVertex(3) != null)
        {
            faceNormal = computeFaceNormal();
        }
        return faceNormal;
    }
    
    private Vec3d computeFaceNormal()
    {
        return getVertex(2).subtract(getVertex(0)).crossProduct(getVertex(3).subtract(getVertex(1))).normalize();
    }

    public float[] getFaceNormalArray()
    {
        Vec3d normal = getFaceNormal();

        float[] retval = new float[3];

        retval[0] = (float)(normal.x);
        retval[1] = (float)(normal.y);
        retval[2] = (float)(normal.z);
        return retval;
    }

    public double getArea()
    {
        if(this.getVertexCount() == 3)
        {
            return Math.abs(getVertex(1).subtract(getVertex(0)).crossProduct(getVertex(2).subtract(getVertex(0))).lengthVector()) / 2.0;

        }
        else if(this.getVertexCount() == 4) //quad
        {
            return Math.abs(getVertex(2).subtract(getVertex(0)).crossProduct(getVertex(3).subtract(getVertex(1))).lengthVector()) / 2.0;
        }
        else
        {
            double area = 0;
            for(RawQuad q : this.toQuads())
            {
                area += q.getArea();
            }
            return area;
        }
    }

    @Override
    public String toString()
    {
        String result = "id: " + this.quadID + " face: " + this.getNominalFace();
        for(int i = 0; i < getVertexCount(); i++)
        {
            result += " v" + i + ": " + this.getVertex(i).toString();
            result += " l" + i + ": " + this.getLineID(i);
        }
        return result;
    }

    /**
     * Gets the face to be used for setupFace semantics.  
     * Is a general facing but does NOT mean poly is actually on that face.
     */
    public EnumFacing getNominalFace()
    {
        return face;
    }

    /** 
     * Face to use for shading testing.
     * Based on which way face points. 
     * Never null
     */
    public EnumFacing getNormalFace()
    {
        return QuadHelper.computeFaceForNormal(this.getFaceNormal());
    }
    
    /** 
     * Face to use for occlusion testing.
     * Null if not fully on one of the faces.
     * Fudges a bit because painted quads can be slightly offset from the plane.
     */
    public EnumFacing getActualFace()
    {
        // semantic face will be right most of the time
        if(this.isOnFace(this.face, QuadHelper.EPSILON)) return face;

        for(EnumFacing f : EnumFacing.values())
        {
            if(f != face && this.isOnFace(f, QuadHelper.EPSILON)) return f;
        }
        return null;
    }

    /**
     * Sets the face to be used for setupFace semantics
     */
    public EnumFacing setFace(EnumFacing face)
    {
        this.face = face;
        return face;
    }
    
    /** convenience method - sets surface value and returns self */
    public RawQuad setSurfaceInstance(SurfaceInstance surfaceInstance)
    {
        this.surfaceInstance = surfaceInstance;
        return this;
    }
    
    /** returns a copy of this quad with the given transformation applied */
    public RawQuad transform(Matrix4f m)
    {
        return this.transform(new Matrix4d(m));
    }
    
    /** returns a copy of this quad with the given transformation applied */
    public RawQuad transform(Matrix4d matrix)
    {
        RawQuad result = this.clone();
        
        // transform vertices
        for(int i = 0; i < result.vertexCount; i++)
        {
            Vertex vertex = result.getVertex(i);
            Vector4d temp = new Vector4d(vertex.x, vertex.y, vertex.z, 1.0);
            matrix.transform(temp);
            if(Math.abs(temp.w - 1.0) > 1e-5) temp.scale(1.0 / temp.w);
            result.setVertex(i, vertex.withXYZ(temp.x, temp.y, temp.z));
        }
        
        // transform nominal face
        // our matrix transform has block center as its origin,
        // so need to translate face vectors to/from block center 
        // origin before/applying matrix.
        if(this.face != null)
        {
            Vec3i curNorm = this.face.getDirectionVec();
            Vector4d newFaceVec = new Vector4d(curNorm.getX() + 0.5, curNorm.getY() + 0.5, curNorm.getZ() + 0.5, 1.0);
            matrix.transform(newFaceVec);
            newFaceVec.x -= 0.5;
            newFaceVec.y -= 0.5;
            newFaceVec.z -= 0.5;
            result.setFace(QuadHelper.computeFaceForNormal(newFaceVec));
        }
        
        return result;
    }
}