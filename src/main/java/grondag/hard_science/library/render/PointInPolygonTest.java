package grondag.hard_science.library.render;

import net.minecraft.util.math.Vec3d;

/**
 * Adapted from code that bears the notice reproduced below and
 * which can be found at http://geomalgorithms.com/a03-_inclusion.html
 *
 *    // Copyright 2000 softSurfer, 2012 Dan Sunday
 *    // This code may be freely used and modified for any purpose
 *    // providing that this copyright notice is included with it.
 *    // SoftSurfer makes no warranty for this code, and cannot be held
 *    // liable for any real or imagined damage resulting from its use.
 *    // Users of this code must verify correctness for their application.
 * 
 * 
 */
public class PointInPolygonTest
{

    public static class Point2d
    {
        public final double x;
        public final double y;

        public Point2d(double x, double y)
        {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * Tests if a point is Left|On|Right of an infinite line
     * @param   lineStart
     * @param   lineEnd
     * @param   point
     * @return  >0 for point left of the line <br>
     *          =0 for point  on the line <br>
     *          <0 for point  right of the line
     */
    private static double isLeft( Point2d lineStart, Point2d lineEnd, Point2d point )
    {
        return  (lineEnd.x - lineStart.x) * (point.y - lineStart.y)
                - (point.x -  lineStart.x) * (lineEnd.y - lineStart.y);
    }

    /**
     * Crossing number test for a point in a polygon.
     * This code is patterned after [Franklin, 2000]
     * @param point    point to be tested
     * @param vertices vertex points of a closed polygon V[n+1] with V[n]=V[0]
     * @return     true if inside
     */
    public static boolean isPointInPolyCrossingNumber( Point2d point, Point2d[] vertices)
    {
        int    cn = 0;    // the  crossing number counter

        //number of vertices is one less due to wrapped input array
        int size = vertices.length - 1;

        // loop through all edges of the polygon
        for (int i=0; i<size; i++) 
        {    // edge from V[i]  to V[i+1]
            if (((vertices[i].y <= point.y) && (vertices[i+1].y > point.y))     // an upward crossing
                    || ((vertices[i].y > point.y) && (vertices[i+1].y <=  point.y)))  // a downward crossing
            {
                // compute  the actual edge-ray intersect x-coordinate
                double vt = (point.y  - vertices[i].y) / (vertices[i+1].y - vertices[i].y);
                if (point.x <  vertices[i].x + vt * (vertices[i+1].x - vertices[i].x)) // P.x < intersect
                    ++cn;   // a valid crossing of y=P.y right of P.x
            }
        }
        return (cn & 1) == 1;    // 0 if even (out), and 1 if  odd (in)
    }

    /**
     * Winding number test for a point in a polygon
     * @param point    point to be tested
     * @param vertices vertex points of a closed polygon V[n+1] with V[n]=V[0]
     * @return         true if inside
     */
    public static boolean isPointInPolyWindingNumber( Point2d point, Point2d[] vertices)
    {
        int    wn = 0;    // the  winding number counter

        //number of vertices is one less due to wrapped input array
        int size = vertices.length - 1;

        // loop through all edges of the polygon
        for (int i=0; i< size; i++)    // edge from V[i] to  V[i+1]
        {
            if (vertices[i].y <= point.y)            // start y <= P.y
            {
                if (vertices[i+1].y  > point.y)      // an upward crossing
                    if (isLeft( vertices[i], vertices[i+1], point) > 0)  // P left of  edge
                        ++wn;            // have  a valid up intersect
            }
            else  // start y > P.y (no test needed)
            {
                if (vertices[i+1].y  <= point.y)     // a downward crossing
                    if (isLeft( vertices[i], vertices[i+1], point) < 0)  // P right of  edge
                        --wn;            // have  a valid down intersect
            }
        }
        return wn != 0;
    }

    public static boolean isPointInRawQuad(Vec3d point, RawQuad quad)
    {
        // faster to check in 2 dimensions, so throw away the orthogonalAxis 
        // that is most orthogonal to our plane
        DiscardAxis discardAxis = DiscardAxis.get(quad.getFaceNormal());

        Point2d testPoint = discardAxis.get2dPoint(point);

        int size = quad.getVertexCount();
        Point2d vertices[] = new Point2d[size + 1];
        {
            for(int i = 0; i < size; i++)
            {
                vertices[i] = discardAxis.get2dPoint(quad.getVertex(i));
            }
            // make array wrap to simplify usage
            vertices[size] = vertices[0];
        }

        return isPointInPolyWindingNumber(testPoint, vertices);
    }

    private static enum DiscardAxis
    {
        X,
        Y,
        Z;

        /** 
         * Returns the orthogonalAxis that is most orthogonal to the plane
         * identified by the given normal and thus should be ignored for PnP testing.
         */
        private static DiscardAxis get(Vec3d normal)
        {
            DiscardAxis result = X;
            double maxAbsoluteComponent = Math.abs(normal.x);

            double absY = Math.abs(normal.y);
            if(absY > maxAbsoluteComponent)
            {
                result = Y;
                maxAbsoluteComponent = absY;
            }

            if(Math.abs(normal.z) > maxAbsoluteComponent)
            {
                result = Z;
            }

            return result;
        }

        /**
         * Returns a 2d point with this orthogonalAxis discarded.
         */
        private Point2d get2dPoint(Vec3d pointIn)
        {
            switch(this)
            {
            case X:
                return new Point2d(pointIn.y, pointIn.z);

            case Y:
                return new Point2d(pointIn.x, pointIn.z);

            case Z:
                return new Point2d(pointIn.x, pointIn.y);

            default:
                // nonsense
                return null;

            }
        }
    }
}
