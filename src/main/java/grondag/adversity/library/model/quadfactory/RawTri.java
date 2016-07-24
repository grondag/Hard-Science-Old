package grondag.adversity.library.model.quadfactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import grondag.adversity.Adversity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

public class RawTri extends RawQuad
{
    public RawTri()
    {
        super();
    }

    /**
     * Copies everything except vertices.
     */
    public RawTri(RawQuad template)
    {
        super();
        this.copyProperties(template);
    }

    @Override
    public int vertexCount()
    {
        return 3;
    }
    
    public RawTri setupFaceQuad(EnumFacing side, FaceVertex tv0, FaceVertex tv1, FaceVertex tv2, EnumFacing topFace)
    {
        this.face = side;
        return (RawTri) this.setupFaceQuad(tv0, tv1, tv2, tv2, topFace);
    }

    public RawTri setupFaceQuad(FaceVertex tv0, FaceVertex tv1, FaceVertex tv2, EnumFacing topFace)
    {
        return (RawTri) this.setupFaceQuad(tv0, tv1, tv2, tv2, topFace);
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

    /** Using this instead of referencing array directly
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
    /** Using this instead of referencing array directly
     * ensures correct handling for tris.
     */
    @Override
    public void setLineID(int index, long lineID)
    {
        this.lineID[index] = lineID;
        if(index == 2)
        {
            this.lineID[3] = lineID;
        }
    
    }
    
    public RawTri clone()
    {
        RawTri retval = new RawTri();
        retval.copyVertices(this);
        retval.copyProperties(this);
        return retval;
    }

    /** 
     * If this is a quad, returns two tris.
     * If is already a tri, returns copy of self.
     */
    @Override
    public RawTri[] split()
    {
        RawTri retVal[] = new RawTri[1];
        retVal[0] = (RawTri) this.clone();
        return retVal;
    }
    
    /**
     * Reverses winding order of this quad and returns itself
     */
    @Override
    public RawQuad invert()
    {
        for(int i = 0; i < 4; i++)
        {
            if(vertices[i] != null && vertices[i].normal != null)
            {
                vertices[i].normal = vertices[i].normal.scale(-1);
            }
        }

        Vertex swapVertex = vertices[0];
        vertices[0] = vertices[2];
        vertices[2] = swapVertex;
        vertices[3] = swapVertex;
        
        long swapLine = lineID[0];
        lineID[0] = lineID[1];
        lineID[1] = swapLine;
        // third leg is same line, just different vertex order
        // and no forth leg on a tri
        
        this.isInverted = !this.isInverted;
        
        return this;
    }
    
//    /**
//     * Returns portion of this tri that is inside (below) the plane
//     * of the given face.  Uses the template quad to patch any holes created.
//     */
//    List<RawQuad> splitOnFace(EnumFacing face, RawQuad patchTemplate) 
//    {
//        LinkedList<RawQuad> retVal = new LinkedList<RawQuad>();
//        
//        final int COPLANAR = 0;
//        final int FRONT = 1;
//        final int BACK = 2;
//
//        int vertexFlags = 0;
//        // precalc to avoid calling twice in loop below
//        int testResults[] = new int[3];
//        
//        for(int i = 0; i < 3 ; i++)
//        {
//            double distance = this.vertices[i].distanceToFacePlane(face);
//            testResults[i] = (distance < -QuadFactory.EPSILON) ? BACK : (distance > QuadFactory.EPSILON) ? FRONT : COPLANAR;
//            vertexFlags |= testResults[i];
//        }
//
//        // nothing to do if all vertices are coplanar or inside face
//        if(vertexFlags == COPLANAR || vertexFlags == BACK) 
//        {
//            retVal.add(this);
//            return retVal;
//        }
//        
//        /**
//         * If > 0, patches should cover clipped portion.
//         * If < 0, patches should cover kept portion.
//         * if = 0, no patches needed because quad is orthogonal to plane.
//         */
//        double patchTest = this.getFaceNormal().dotProduct(new Vec3d(face.getDirectionVec()));
//        if(patchTest > -QuadFactory.EPSILON && patchTest < QuadFactory.EPSILON) patchTest = 0;
//        
//        // clip entire poly if all vertices are coplanar or outside face
//        if(vertexFlags == FRONT)
//        {
//            if(patchTest > 0) retVal.add(makePatch(face, patchTemplate, this));
//            return retVal;
//        }
//        
//        // if we get here, means we need to split the poly
//        ArrayList<Vertex> vertexKeep = new ArrayList<Vertex>(4); 
//        ArrayList<Vertex> vertexClip = new ArrayList<Vertex>(4); 
//
//        for (int iThisVertex = 0; iThisVertex < 3; iThisVertex++) 
//        {
//            int iNextVertex = (iThisVertex + 1) % 3;
//            boolean isSplitNeeded = false;
//            switch(testResults[iThisVertex])
//            {
//            case COPLANAR:
//                vertexKeep.add(this.vertices[iThisVertex]);
//                vertexClip.add(this.vertices[iThisVertex]);
//                break;
//
//            case FRONT:
//                vertexClip.add(this.vertices[iThisVertex]);
//                isSplitNeeded = testResults[iNextVertex] == BACK;
//                break;
//
//            case BACK:
//                vertexKeep.add(this.vertices[iThisVertex]);
//                isSplitNeeded = testResults[iNextVertex] == FRONT;
//                break;
//            }
//
//            if(isSplitNeeded)
//            {
//                double a = Math.abs(this.vertices[iThisVertex].distanceToFacePlane(face));
//                double b = Math.abs(this.vertices[iNextVertex].distanceToFacePlane(face));
//                Vertex splitVertex = this.vertices[iThisVertex].interpolate(this.vertices[iNextVertex], a / (a + b)); 
//                vertexKeep.add(splitVertex);
//                vertexClip.add(splitVertex);                    
//            }
//        }
//
//        // create polys as appropriate
//        if(vertexKeep.size() == 3 || vertexKeep.size() == 4)
//        {
//            RawQuad keeper = vertexKeep.size() == 3 ? new RawTri(this) : new RawQuad(this);
//            for(int i = 0; i < vertexKeep.size(); i++)
//            {
//                keeper.setVertex(i, vertexKeep.get(i));
//            }
//            retVal.add(keeper);
//            
//            // create patch for kept portion if facing opposite the clip face
//            if(patchTest < 0) retVal.add(makePatch(face, patchTemplate, keeper));
//
//        }
//
//        // create patch for clipped portion if facing same way as clip face
//        if(patchTest > 0 && (vertexClip.size() == 3 || vertexClip.size() == 4))
//        {
//            RawQuad hole = vertexClip.size() == 3 ? new RawTri(this) : new RawQuad(this);
//            for(int i = 0; i < vertexClip.size(); i++)
//            {
//                hole.setVertex(i, vertexClip.get(i));
//            }
//            
//            retVal.add(makePatch(face, patchTemplate, hole));
//        }
//
//        return retVal;
//    }     
//
//    private RawQuad makePatch(EnumFacing face, RawQuad patchTemplate, RawQuad hole) 
//    {
//
//        RawQuad patch = hole instanceof RawTri ? new RawTri(patchTemplate) : new RawQuad(patchTemplate);
//        patch.face = face;
//
//        for(int i = 0; i < hole.vertexCount(); i++)
//        {
//            double x = hole.getVertex(i).xCoord;
//            double y = hole.getVertex(i).yCoord;
//            double z = hole.getVertex(i).zCoord;
//            double u = 0;
//            double v = 0;
//            switch(face)
//            {
//            case UP:
//                y = 1;
//                u = x * 16;
//                v = z * 16;
//                break;
//            case DOWN:
//                y = 0;
//                u = (1 - x) * 16;
//                v = z * 16;
//                break;
//            case EAST:
//                x = 1;
//                u = (1 - z) * 16;
//                v = (1 - y) * 16;
//                break;
//            case WEST:
//                x = 0;
//                u = z * 16;
//                v = (1 - y) * 16;
//                break;
//            case NORTH:
//                z = 0;
//                u = (1 - x) * 16;
//                v = (1 - y) * 16;
//                break;
//            case SOUTH:
//                z = 1;
//                u = x * 16;
//                v = (1 - y) * 16;
//                break;
//            default:
//                // make compiler shut up about unhandled case
//                break;
//            }
//            patch.setVertex(i, new Vertex(x, y, z, u, v, patch.color));
//        }
//        
//        if(hole.getFaceNormal().dotProduct(new Vec3d(face.getDirectionVec())) < -QuadFactory.EPSILON) patch.invert();
//
//        return patch;
//    }
}