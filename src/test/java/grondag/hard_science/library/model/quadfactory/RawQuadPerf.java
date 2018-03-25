package grondag.hard_science.library.model.quadfactory;

import java.util.Random;

import org.junit.Test;

import grondag.exotic_matter.render.RawQuad;
import grondag.hard_science.HardScience;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

public class RawQuadPerf
{

    @Test
    public void test()
    {
        Vec3d point;
        Vec3d direction;
        RawQuad quad;
        
        Random r = new Random(9);
        
        final int RUNS = 10000;
        
        boolean results[] = new boolean[RUNS];
        
        long start = System.nanoTime();
        
        for(int i = 0; i < RUNS; i++)
        {
            EnumFacing face = EnumFacing.HORIZONTALS[r.nextInt(4)];
            float x = r.nextFloat();
            float y = r.nextFloat();
            quad = new RawQuad().setupFaceQuad(face, x, y, x + r.nextFloat(), y + r.nextFloat(), r.nextFloat(), EnumFacing.UP);
            point = new Vec3d(r.nextDouble(), r.nextDouble(), r.nextDouble());
            direction = new Vec3d(r.nextDouble(), r.nextDouble(), r.nextDouble());
            results[i] = quad.intersectsWithRaySlow(point, direction); 
        }
        
        HardScience.INSTANCE.info("Slow way ns each: " + (System.nanoTime() - start) / RUNS);
        
        r = new Random(9);
        start = System.nanoTime();
        int diffCount = 0;
        
        for(int i = 0; i < RUNS; i++)
        {
            EnumFacing face = EnumFacing.HORIZONTALS[r.nextInt(4)];
            float x = r.nextFloat();
            float y = r.nextFloat();
            quad = new RawQuad().setupFaceQuad(face, x, y, x + r.nextFloat(), y + r.nextFloat(), r.nextFloat(), EnumFacing.UP);
            point = new Vec3d(r.nextDouble(), r.nextDouble(), r.nextDouble());
            direction = new Vec3d(r.nextDouble(), r.nextDouble(), r.nextDouble());
            if(results[i] != quad.intersectsWithRay(point, direction))
            {
                diffCount++;
                HardScience.INSTANCE.info("===========================================================");
                HardScience.INSTANCE.info("Quad " + quad.toString());
                HardScience.INSTANCE.info("point " + point.toString());
                HardScience.INSTANCE.info("intersection " + quad.intersectionOfRayWithPlane(point, direction).toString());
                HardScience.INSTANCE.info("direction " + direction.toString());
                HardScience.INSTANCE.info("slow " + quad.intersectsWithRay(point, direction));
                HardScience.INSTANCE.info("fast " + quad.intersectsWithRaySlow(point, direction));
            }
//            assert(results[i] == quad.intersectsWithRayFast(point, direction)); 
        }
        
        HardScience.INSTANCE.info("Diff % " + diffCount * 100 / RUNS);
        HardScience.INSTANCE.info("Fast way ns each: " + (System.nanoTime() - start) / RUNS);
    }

}