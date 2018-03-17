package grondag.hard_science.library.model.quadfactory;

import static org.junit.Assert.*;

import org.junit.Test;

import grondag.exotic_matter.render.Vertex;
import net.minecraft.util.math.Vec3d;

public class VertexTest
{

    @Test
    public void test()
    {
        Vertex testPoint = new Vertex(.5, .5, .5, .5, .5, 0);
        
        assertTrue(testPoint.isOnLine(new Vec3d(0, 0, 0), new Vec3d(1, 1, 1)));
        assertTrue(testPoint.isOnLine(new Vec3d(.5, 0, .5), new Vec3d(.5, 1, .5)));
        assertFalse(testPoint.isOnLine(new Vec3d(.7, 2, .1), new Vec3d(0, -1, .25)));
        
        testPoint = new Vertex(.6, .4, .5333333333, .4, .5333333333, 0);
        assertTrue(testPoint.isOnLine(new Vec3d(0.6, 0.4, 0.4), new Vec3d(0.6, .4, .6)));

    }

}