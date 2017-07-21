package grondag.hard_science.library.model.quadfactory;

import java.util.Random;

import org.junit.Test;

import grondag.hard_science.Log;
import grondag.hard_science.library.render.RawQuad;
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
        
        Log.info("Slow way ns each: " + (System.nanoTime() - start) / RUNS);
        
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
                Log.info("===========================================================");
                Log.info("Quad " + quad.toString());
                Log.info("point " + point.toString());
                Log.info("intersection " + quad.intersectionOfRayWithPlane(point, direction).toString());
                Log.info("direction " + direction.toString());
                Log.info("slow " + quad.intersectsWithRay(point, direction));
                Log.info("fast " + quad.intersectsWithRaySlow(point, direction));
            }
//            assert(results[i] == quad.intersectsWithRayFast(point, direction)); 
        }
        
        Log.info("Diff % " + diffCount * 100 / RUNS);
        Log.info("Fast way ns each: " + (System.nanoTime() - start) / RUNS);
    }

}
