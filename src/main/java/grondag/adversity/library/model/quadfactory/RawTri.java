package grondag.adversity.library.model.quadfactory;

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

    public RawTri clone()
    {
        RawTri retval = new RawTri();
        retval.vertices = this.vertices.clone();
        retval.copyProperties(this);
        return retval;
    }

}