package grondag.adversity.library.model.quadfactory;


import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;

import grondag.adversity.Adversity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

public class Vertex extends Vec3d
{
    protected final double u;
    protected final double v;
    public final int color;
    protected final Vec3d normal;
    
    
    public Vertex(double x, double y, double z, double u, double v, int color)
    {
        this(x, y, z, u, v, color, null);
    }

    public Vertex(double x, double y, double z, double u, double v, int color, Vec3d normal)
    {
        super(x, y, z);
        this.u = u;
        this.v = v;
        this.color = color;
        this.normal = normal;
    }

    protected Vertex withNormal(Vec3d normalIn)
    {
        return new Vertex(this.xCoord, this.yCoord, this.zCoord, this.u, this.v, this.color, normalIn);
    }

    protected Vertex withColor(int colorIn)
    {
        return new Vertex(this.xCoord, this.yCoord, this.zCoord, this.u, this.v, colorIn, this.normal);
    }

    public boolean hasNormal()
    {
        return this.normal != null;
    }

    public Vec3d getNormal()
    {
        return this.normal;
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
        Vec3d newNorm = null;

        if(this.normal != null && otherVertex.normal != null)
        {
            newNorm = this.normal.add(otherVertex.normal.subtract(this.normal).scale(otherWeight));
        }
        double newU = this.u + (otherVertex.u - this.u) * otherWeight;
        double newV = this.v + (otherVertex.v - this.v) * otherWeight;

        int newColor = (int) ((this.color & 0xFF) + ((otherVertex.color & 0xFF) - (this.color & 0xFF)) * otherWeight);
        newColor |= (int) ((this.color & 0xFF00) + ((otherVertex.color & 0xFF00) - (this.color & 0xFF00)) * otherWeight);
        newColor |= (int) ((this.color & 0xFF0000) + ((otherVertex.color & 0xFF0000) - (this.color & 0xFF0000)) * otherWeight);
        newColor |= (int) ((this.color & 0xFF000000) + ((otherVertex.color & 0xFF000000) - (this.color & 0xFF000000)) * otherWeight);

        return new Vertex(newPos.xCoord, newPos.yCoord, newPos.zCoord, newU, newV, newColor, newNorm);
    }

    /**
     * Returns a signed distance to the plane of the given face.
     * Positive numbers mean in front of face, negative numbers in back.
     */
//    public double distanceToFacePlane(EnumFacing face)
//    {
//        int offset = face.getAxisDirection() == AxisDirection.POSITIVE ? 1 : 0;
//        return new Vec3d(face.getDirectionVec()).dotProduct(this) - offset;
//    }

    /**
     * Returns a signed distance to the plane of the given face.
     * Positive numbers mean in front of face, negative numbers in back.
     */
    public double distanceToFacePlane(EnumFacing face)
    {
        // could use dot product, but exploiting special case for less math
        switch(face)
        {
        case UP:
            return this.yCoord - 1;

        case DOWN:
            return - this.yCoord;
            
        case EAST:
            return this.xCoord - 1;

        case WEST:
            return -this.xCoord;

        case NORTH:
            return -this.zCoord;
            
        case SOUTH:
            return this.zCoord - 1;

        default:
            // make compiler shut up about unhandled case
            return 0;
        }
    }
    
    public boolean isOnFacePlane(EnumFacing face)
    {
        return Math.abs(this.distanceToFacePlane(face)) < QuadFactory.EPSILON;
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

    /** 
     * True if both vertices are at the same point. 
     */
    public boolean isCsgEqual(Vertex vertexIn)
    {
        return Math.abs(vertexIn.xCoord - this.xCoord) < QuadFactory.EPSILON
            && Math.abs(vertexIn.yCoord - this.yCoord) < QuadFactory.EPSILON
            && Math.abs(vertexIn.zCoord - this.zCoord) < QuadFactory.EPSILON;

    }

    /**
     * True if this point is on the line formed by the two given points.
     */
    public boolean isOnLine(Vec3d pointA, Vec3d pointB)
    {
        return(Math.abs(pointA.distanceTo(pointB) - pointB.distanceTo(this) - pointA.distanceTo(this)) < QuadFactory.EPSILON);
    }
}