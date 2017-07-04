package grondag.hard_science.library.render;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class CSGBounds extends AxisAlignedBB
{

    public CSGBounds(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        super(x1, y1, z1, x2, y2, z2);
    }

    public CSGBounds(Vec3d min, Vec3d max)
    {
        super(min.xCoord, min.yCoord, min.zCoord, max.xCoord, max.yCoord, max.zCoord);
    }
    
    /**
     * For CSG operations we consider a point on the edge to be intersecting.
     */
    public boolean intersectsWith(AxisAlignedBB other)
    {
        return this.intersects(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
    }

    /**
     * For CSG operations we consider a point on the edge to be intersecting.  
     */
    public boolean intersects(double xMin, double yMin, double zMin, double xMax, double yMax, double zMax)
    {

        return this.minX <= xMax && this.maxX >= xMin && this.minY <= yMax && this.maxY >= yMin && this.minZ <= zMax && this.maxZ >= zMin;
    }


}
