package grondag.adversity.library.model.quadfactory;

import static org.junit.Assert.*;

import org.junit.Test;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

public class RawQuadTest
{

    @Test
    public void test()
    {
        Vec3d direction;
        Vec3d point;
        RawQuad quad = new RawQuad().setupFaceQuad(EnumFacing.UP, 0, 0, 1, 1, 0.5, EnumFacing.NORTH);
        
        // point on plane
        point = new Vec3d(0.5, 0.5, 0.5);
        direction = new Vec3d(0, 123, 0);
        assertTrue(quad.containsPoint(point));
        
        // point on plane outside poly
        point = new Vec3d(-0.5, 0.5, 1.5);
        direction = new Vec3d(0, 123, 0);
        assertFalse(quad.containsPoint(point));
        
        // point with ray intersecting poly
        point = new Vec3d(0.5, 0.1, 0.5);
        direction = new Vec3d(0, 123, 0);
        assertTrue(quad.intersectsWithRay(point, direction));        
        
        // point with ray intersecting plane outside poly
        point = new Vec3d(-32, 0.2, 27);
        direction = new Vec3d(0, 123, 0);
        assertFalse(quad.containsPoint(point));
             
        // point with ray facing away from poly
        point = new Vec3d(0.5, 0.1, 0.5);
        direction = new Vec3d(0, -124535, 0);
        assertFalse(quad.intersectsWithRay(point, direction));
        
        
        //convexity & area tests
        quad = new RawQuad().setupFaceQuad(EnumFacing.UP, 0, 0, 1, 1, 0.5, EnumFacing.NORTH);
        assertTrue(quad.isConvex());
        assertTrue(Math.abs(quad.getArea() - 1.0) < QuadFactory.EPSILON);
        
        quad = new RawQuad(3).setupFaceQuad(EnumFacing.UP,
                new FaceVertex(0, 0, 0), 
                new FaceVertex(1, 0, 0), 
                new FaceVertex(1, 1, 0), 
                EnumFacing.NORTH);
        assertTrue(quad.isConvex());
        assertTrue(Math.abs(quad.getArea() - 0.5) < QuadFactory.EPSILON);
        
        quad = new RawQuad().setupFaceQuad(EnumFacing.UP,
                new FaceVertex(0, 0, 0), 
                new FaceVertex(1, 0, 0), 
                new FaceVertex(1, 1, 0), 
                new FaceVertex(0.9, 0.1, 0), 
                EnumFacing.NORTH);
        assertFalse(quad.isConvex());
        
    }

}
