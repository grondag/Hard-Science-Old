package grondag.hard_science.library.varia;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.vecmath.Matrix4f;

import org.lwjgl.util.vector.Vector3f;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import grondag.hard_science.library.render.RawQuad;
import grondag.hard_science.library.world.PackedBlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;


/**
 * Random utilities that have not yet found a more appropriate home.
 */
public class Useful 
{
    
    private static final Vec3i DISTANCE_SORTED_CIRCULAR_OFFSETS[];
    
    static
    {
        // need to use a hash bc fill2dCircleInPlaneXZ does not guarantee uniqueness.
        HashSet<Vec3i> offsets = new HashSet<Vec3i>();

        
        for(long packed : Useful.fill2dCircleInPlaneXZ(64).toArray())
        {
            int x = PackedBlockPos.getX(packed);
            int z = PackedBlockPos.getZ(packed);
            offsets.add(new Vec3i(x, Math.sqrt(x * x + z * z), z));
        }
        
        ArrayList<Vec3i> offsetList = new ArrayList<Vec3i>(offsets);
        offsetList.sort(new Comparator<Vec3i>() {

            @Override
            public int compare(Vec3i o1, Vec3i o2)
            {
                return Integer.compare(o1.getY(), o2.getY());
            }});
        
        DISTANCE_SORTED_CIRCULAR_OFFSETS = offsetList.toArray(new Vec3i[offsetList.size()]);
    }
    
    /**
     * Returns values in a sequence of horizontal offsets from X=0, Z=0.<br>
     * Y value is the euclidian distance from the origin.<br>
     * Values are sorted by distance from 0,0,0. Value at index 0 is the origin.<br>
     * Distance is up to 64 blocks from origin. Values outside that range return null.<br>
     */
    public static Vec3i getDistanceSortedCircularOffset(int index)
    {
        if(index < 0 || index >= DISTANCE_SORTED_CIRCULAR_OFFSETS.length) return null;
        return DISTANCE_SORTED_CIRCULAR_OFFSETS[index];
    }
    
    public static int min(int... input)
    {
        int result = Integer.MAX_VALUE;
        for(int i : input)
        {
            if(i < result) result = i;
        }
        return result;
    }

    public static int max(int... input)
    {
        int result = Integer.MIN_VALUE;
        for(int i : input)
        {
            if(i > result) result = i;
        }
        return result;
    }

    public static int max(int a, int b, int c)
    {
        if(a > b)
        {
            return a > c ? a : c;
        }
        else
        {
            return b > c ? b : c;
        }
    }

    /** 
     * Returns the maximum absolute magnitude of the three orthogonalAxis for the given vector.
     * Mainly useful when you know that only one of the orthogonalAxis has a non-zero value
     * and you want to retrieve it while avoiding the need for expendisive square root.
     */
    public static int maxAxisLength(Vec3i vec)
    {
        return Math.max(Math.max(Math.abs(vec.getX()), Math.abs(vec.getY())), Math.abs(vec.getZ()));
    }

    public static double linearInterpolate(double value1, double value2, double location)
    {
        return( value1 * (1 - location) + value2 * location);
    }

    /**
     * Computes long hash from long value. 
     * Use as many bits as needed/practical for specific application.
     * see http://brpreiss.com/books/opus4/html/page214.html 
     */
    public static long longHash(long l) 
    {
        // constant is golden ratio
        long h = l * 0x9E3779B97F4A7C15L;
        h ^= h >>> 32;
        return h ^ (h >>> 16);
    }

    /**
     * Creates an AABB with the bounds and rotation provided.
     */
    public static AxisAlignedBB makeRotatedAABB(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, Matrix4f rotation){
        Vector3f minPos = new Vector3f(minX, minY, minZ);
        Vector3f maxPos = new Vector3f(maxX, maxY, maxZ);
        net.minecraftforge.client.ForgeHooksClient.transform(minPos, rotation);
        net.minecraftforge.client.ForgeHooksClient.transform(maxPos, rotation);
        return new AxisAlignedBB(minPos.x, minPos.y, minPos.z, 
                maxPos.x, maxPos.y, maxPos.z);
    }

    /** 
     * Returns a list of packed block position x & z OFFSETS within the given radius.
     * Origin will be the first position.
     */
    public static TLongList fill2dCircleInPlaneXZ(int radius) 
    {
        TLongArrayList result = new TLongArrayList( (int) (2 * radius * 3.2));

        // uses midpoint circle algorithm
        if (radius > 0)
        {

            int x = radius;
            int z = 0;
            int err = 0;

            result.add(PackedBlockPos.pack(0, 0, 0));

            while (x >= z) 
            {

                if(z > 0)
                {
                    result.add(PackedBlockPos.pack(z, 0, z));
                    result.add(PackedBlockPos.pack(-z, 0, z));
                    result.add(PackedBlockPos.pack(z, 0, -z));
                    result.add(PackedBlockPos.pack(-z, 0, -z));
                }

                for(int i = x; i > z; i--)
                {
                    result.add(PackedBlockPos.pack(i, 0, z));
                    result.add(PackedBlockPos.pack(z, 0, i));
                    result.add(PackedBlockPos.pack(-i, 0, z));
                    result.add(PackedBlockPos.pack(-z, 0, i));
                    result.add(PackedBlockPos.pack(i, 0, -z));
                    result.add(PackedBlockPos.pack(z, 0, -i));
                    result.add(PackedBlockPos.pack(-i, 0, -z));
                    result.add(PackedBlockPos.pack(-z, 0, -i));
                }

                if(err <= 0)
                {
                    z += 1;
                    err += 2 * z + 1;
                }
                if(err > 0)
                {
                    x -= 1;
                    err -= 2*x + 1;
                }
            }
        }

        return result;
    }
    
    /** returns a list of packed block position x & z OFFSETS with the given radius */
    public static TLongList outline2dCircleInPlaneXZ(int radius) 
    {
        TLongArrayList result = new TLongArrayList( (int) (2 * radius * 3.2));

        // uses midpoint circle algorithm
        if (radius > 0)
        {

            int x = radius;
            int z = 0;
            int err = 0;

            while (x >= z) 
            {

                result.add(PackedBlockPos.pack(x, 0, z)); // Octant 1
                result.add(PackedBlockPos.pack(z, 0, x)); // Octant 2
                result.add(PackedBlockPos.pack(-x, 0, z)); // Octant 3
                result.add(PackedBlockPos.pack(-z, 0, x)); // Octant 4
                result.add(PackedBlockPos.pack(-x, 0, -z)); // Octant 5
                result.add(PackedBlockPos.pack(-z, 0, -x)); // Octant 6
                result.add(PackedBlockPos.pack(x, 0, -z)); // Octant 7
                result.add(PackedBlockPos.pack(z, 0, -x)); // Octant 8

                if(err <= 0)
                {
                    z += 1;
                    err += 2 * z + 1;
                }
                if(err > 0)
                {
                    x -= 1;
                    err -= 2*x + 1;
                }
            }
        }

        return result;
    }


    /** 
     * Returns a list of points on the line between the two given points, inclusive.
     * Points start at first position given.
     * Hat tip to https://github.com/fragkakis/bresenham.
     */
    public static TLongList line2dInPlaneXZ(long packedPos1, long packedPos2) 
    {
        int x1 = PackedBlockPos.getX(packedPos1);
        int z1 = PackedBlockPos.getZ(packedPos1);

        int x2 = PackedBlockPos.getX(packedPos2);
        int z2 = PackedBlockPos.getZ(packedPos2);

        int dx = Math.abs(x2 - x1);
        int dz = Math.abs(z2 - z1);

        TLongArrayList result = new TLongArrayList((int) (Math.max(dx, dz) * 3 / 2));

        int sx = x1 < x2 ? 1 : -1; 
        int sysz = z1 < z2 ? 1 : -1; 

        int err = dx-dz;
        int e2;
        int currentX = x1;
        int currentZ = z1;

        while(true) 
        {
            result.add(PackedBlockPos.pack(currentX, 0, currentZ));

            if(currentX == x2 && currentZ == z2) 
            {
                break;
            }

            e2 = 2*err;
            if(e2 > -1 * dz) 
            {
                err = err - dz;
                currentX = currentX + sx;
            }

            if(e2 < dx) 
            {
                err = err + dx;
                currentZ = currentZ + sysz;
            }
        }

        return result;
    }

    /** 
     * Returns average world height around the given BlockPos.
     */
    public static int getAvgHeight(World world, BlockPos pos, int radius, int sampleCount)
    {
        int total = 0;
        int range = radius * 2 + 1;

        for(int i = 0; i < sampleCount; i++)
        {
            total += world.getHeight(pos.east(ThreadLocalRandom.current().nextInt(range) - radius).north(ThreadLocalRandom.current().nextInt(range) - radius)).getY();
        }

        return total / sampleCount;
    }

    /**
     * Returns volume of given AABB
     */
    public static double volumeAABB(AxisAlignedBB box)
    {
        return (box.maxX - box.minX) * (box.maxY - box.minY) * (box.maxZ - box.minZ);
    }

    /**
     * Retuns sum of volume of all AABB in the list.
     */
    public static double volumeAABB(List<AxisAlignedBB> list)
    {
        double retVal = 0;
        for(AxisAlignedBB box : list)
        {
            retVal += volumeAABB(box);
        }
        return retVal;
    }
    
    /**
     * Returns true if the given ray intersects with the given AABB.
     */
    public static boolean doesRayIntersectAABB(Vec3d origin, Vec3d direction, AxisAlignedBB box)
    {

        double tmin = Double.NEGATIVE_INFINITY;
        double tmax = Double.POSITIVE_INFINITY;

        double t1, t2;

        if(direction.x != 0)
        {
            t1 = (box.minX - origin.x)/direction.x;
            t2 = (box.maxX - origin.x)/direction.x;
            tmin = Math.max(tmin, Math.min(t1, t2));
            tmax = Math.min(tmax, Math.max(t1, t2));
        }
        else if (origin.x <= box.minX || origin.x >= box.maxX) 
        {
            return false;
        }

        if(direction.y != 0)
        {
            t1 = (box.minY - origin.y)/direction.y;
            t2 = (box.maxY - origin.y)/direction.y;
            tmin = Math.max(tmin, Math.min(t1, t2));
            tmax = Math.min(tmax, Math.max(t1, t2));
        }
        else if (origin.y <= box.minY || origin.y >= box.maxY) 
        {
            return false;
        }

        if(direction.z != 0)
        {
            t1 = (box.minZ - origin.z)/direction.z;
            t2 = (box.maxZ - origin.z)/direction.z;
            tmin = Math.max(tmin, Math.min(t1, t2));
            tmax = Math.min(tmax, Math.max(t1, t2));
        }
        else if (origin.z <= box.minZ || origin.z >= box.maxZ) 
        {
            return false;
        }

        return tmax > tmin && tmax > 0.0;   
    }



    public static int squared(int x) 
    {
        return x * x;
    }

    /** 
     * Bit length needed to contain the given value.
     * Intended for unsigned values.
     */
    public static int bitLength(long maxValue)
    {
        return Long.SIZE - Long.numberOfLeadingZeros(maxValue - 1);
    }

    /** 
     * Bit length needed to contain the given value.
     * Intended for unsigned values.
     */
    public static int bitLength(int maxValue)
    {
        if(maxValue == 0) return 0;
        return Integer.SIZE - Integer.numberOfLeadingZeros(maxValue - 1);
    }

    /**
     * Returns bit mask for a value of given bit length 
     */
    public static long longBitMask(int bitLength)
    {
        bitLength = MathHelper.clamp(bitLength, 0, Long.SIZE);

        // note: can't use mask = (1L << (bitLength+1)) - 1 here due to overflow & signed values
        long mask = 0L;
        for(int i = 0; i < bitLength; i++)
        {
            mask |= (1L << i);
        }
        return mask;
    }

    /**
     * Returns bit mask for a value of given bit length 
     */
    public static int intBitMask(int bitLength)
    {
        bitLength = MathHelper.clamp(bitLength, 0, Integer.SIZE);

        // note: can't use mask = (1L << (bitLength+1)) - 1 here due to overflow & signed values
        int mask = 0;
        for(int i = 0; i < bitLength; i++)
        {
            mask |= (1L << i);
        }
        return mask;
    }

    /**
     * Returns first given default value if given ordinal is out of range.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<?>> T safeEnumFromOrdinal(int ord, T defaultValue)
    {
        if(ord < 0 || ord >= defaultValue.getClass().getEnumConstants().length)
        {
            return defaultValue;
        }
        return (T) defaultValue.getClass().getEnumConstants()[ord];
    }

    /**
     * Equivalent to offsetEnumValue(value, 1)
     * Probably slightly faster.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<?>> T nextEnumValue(T value)
    {
        int ord = value.ordinal() + 1;
        if(ord >= value.getClass().getEnumConstants().length)
        {
            ord = 0;
        }
        return (T) value.getClass().getEnumConstants()[ord];
    }

    /**
     * Returns enum value that is offset values distance
     * from given value.  Wraps.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<?>> T offsetEnumValue(T value, int offset)
    {
        int ord = (value.ordinal() + offset) % value.getClass().getEnumConstants().length;
        if(ord < 0) ord += value.getClass().getEnumConstants().length;
        return (T) value.getClass().getEnumConstants()[ord];
    }

    private static final Vec3d VOXEL_TEST_RAY = new Vec3d(5525, 13123, 7435);
    public static boolean isPointEnclosed(Vec3d point, List<RawQuad> quads)
    {
        int intersectionCount = 0;
        for(RawQuad quad : quads)
        {                  
            if(quad.intersectsWithRay(point, VOXEL_TEST_RAY)) intersectionCount++;
        }          
        return (intersectionCount & 0x1) == 1;
    }

    public static double clamp(double min, double max, double val)
    {
        return val < min ? min : val > max ? max : val;
    }

    public static byte[] intToByteArray(int value)
    {
        return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
    }

    //   /**
    //    * Kept for possible future use.
    //    * Optimized sort for three element array
    //    */
    //   private void sort3Cells(LavaCell[] cells)
    //   {
    //       LavaCell temp;
    //       
    //       if (cells[0].currentLevel < cells[1].currentLevel)
    //       {
    //           if (cells[1].currentLevel > cells[2].currentLevel)
    //           {
    //               if (cells[0].currentLevel < cells[2].currentLevel)
    //               {
    //                   temp = cells[1];
    //                   cells[1] = cells[2];
    //                   cells[2] = temp;
    //               }
    //               else
    //               {
    //                   temp = cells[0];
    //                   cells[0] = cells[2];
    //                   cells[2] = cells[1];
    //                   cells[1] = temp;
    //               }
    //           }
    //       }
    //       else
    //       {
    //           if (cells[1].currentLevel < cells[2].currentLevel)
    //           {
    //               if (cells[0].currentLevel < cells[2].currentLevel)
    //               {
    //                   temp = cells[0];
    //                   cells[0] = cells[1];
    //                   cells[1] = temp;
    //               }
    //               else
    //               {
    //                   temp = cells[0];
    //                   cells[0] = cells[1];
    //                   cells[1] = cells[2];
    //                   cells[2] = temp;
    //               }
    //           }
    //           else
    //           {
    //               temp = cells[0];
    //               cells[0] = cells[2];
    //               cells[2] = temp;
    //           }
    //       }
    //   }

    //   /**
    //    * Kept for future use
    //    * Optimized sort for four element array
    //    */
    //   private void sort4Cells(LavaCell[] cells)
    //   {
    //       LavaCell low1, high1, low2, high2, middle1, middle2, lowest, highest;
    //       
    //       if (cells[0].currentLevel < cells[1].currentLevel)
    //       {
    //           low1 = cells[0];
    //           high1 = cells[1];
    //       }
    //       else 
    //       {
    //           low1 = cells[1];
    //           high1 = cells[0];
    //       }
    //       if (cells[2].currentLevel < cells[3].currentLevel)
    //       {
    //           low2 = cells[2];
    //           high2 = cells[3];
    //       }
    //       else
    //       {
    //           low2 = cells[3];
    //           high2 = cells[2];
    //       }
    //       if (low1.currentLevel < low2.currentLevel)
    //       {
    //           lowest = low1;
    //           middle1 = low2;
    //       }
    //       else
    //       {
    //           lowest = low2;
    //           middle1 = low1;
    //       }
    //       
    //       if (high1.currentLevel > high2.currentLevel)
    //       {
    //           highest = high1;
    //           middle2 = high2;
    //       }
    //       else
    //       {
    //           highest = high2;
    //           middle2 = high1;
    //       }
    //
    //       if (middle1.currentLevel < middle2.currentLevel)
    //       {
    //           cells[0] = lowest;
    //           cells[1] = middle1;
    //           cells[2] = middle2;
    //           cells[3] = highest;
    //       }
    //       else
    //       {
    //           cells[0] = lowest;
    //           cells[1] = middle2;
    //           cells[2] = middle1;
    //           cells[3] = highest;
    //       }
    //   }
}
