package grondag.adversity.library;


import java.util.concurrent.ThreadLocalRandom;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;

import org.lwjgl.util.vector.Vector3f;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;


/**
 * Random utilities that have not yet found a more appropriate home.
 */
public class Useful {
	
    /** clamps the input value to the given range, inclusive */
    public static int clamp(int input, int min, int max)
    {
        if(input < min)
            return min;
        else if(input > max)
            return max;
        else
            return input;
    }

    public static double clamp(double input, double min, double max)
    {
        if(input < min)
            return min;
        else if(input > max)
            return max;
        else
            return input;
    }
    
    public static float clamp(float input, float min, float max)
    {
        if(input < min)
            return min;
        else if(input > max)
            return max;
        else
            return input;
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
	 * Sorts members of the BlockPos vector so that x is largest and z is smallest.
	 * Useful when BlockPos represents a volume instead of a position.
	 */
	public static BlockPos sortedBlockPos(BlockPos pos){
		
		if(pos.getX() > pos.getY()){
			if(pos.getY() > pos.getZ()){
				//x > y > z
				return pos;
			} else if (pos.getX() > pos.getZ()){
				//x > z > y
				return new BlockPos(pos.getX(), pos.getZ(), pos.getY());
			} else {
				//z > x > y
				return new BlockPos(pos.getZ(), pos.getX(), pos.getY());
			}
		} else if(pos.getX() > pos.getZ()){
			// y > x > z
			return new BlockPos(pos.getY(), pos.getX(), pos.getY());
		} else if(pos.getY() > pos.getZ()){
			// y > z > x
			return new BlockPos(pos.getY(), pos.getZ(), pos.getX());
		} else {
			// z > y >x
			return new BlockPos(pos.getZ(), pos.getY(), pos.getX());
		}
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
	
	/** returns the face that is normally the "top" of the given face */
	public static EnumFacing defaultTopOf(EnumFacing faceIn)
	{
	    switch(faceIn)
	    {
        case UP:
            return EnumFacing.NORTH;
        case DOWN:
            return EnumFacing.SOUTH;
        default:
            return EnumFacing.UP;
	    }
	}

   public static EnumFacing rightOf(EnumFacing faceIn, EnumFacing topFace)
   {
       switch (faceIn)
       {
           case NORTH:
               switch (topFace)
               {
                   case UP:
                       return EnumFacing.WEST;
                   case EAST:
                       return EnumFacing.UP;
                   case DOWN:
                       return EnumFacing.EAST;
                   case WEST:
                   default:
                       return EnumFacing.DOWN;
               }
           case SOUTH:
               switch (topFace)
               {
                   case UP:
                       return EnumFacing.EAST;
                   case EAST:
                       return EnumFacing.DOWN;
                   case DOWN:
                       return EnumFacing.WEST;
                   case WEST:
                   default:
                       return EnumFacing.UP;
               }
           case EAST:
               switch (topFace)
               {
                   case UP:
                       return EnumFacing.NORTH;
                   case NORTH:
                       return EnumFacing.DOWN;
                   case DOWN:
                       return EnumFacing.SOUTH;
                   case SOUTH:
                   default:
                       return EnumFacing.UP;
               }
           case WEST:
               switch (topFace)
               {
                   case UP:
                       return EnumFacing.SOUTH;
                   case NORTH:
                       return EnumFacing.UP;
                   case DOWN:
                       return EnumFacing.NORTH;
                   case SOUTH:
                   default:
                       return EnumFacing.DOWN;
               }
           case UP:
               switch (topFace)
               {
                   case NORTH:
                       return EnumFacing.EAST;
                   case EAST:
                       return EnumFacing.SOUTH;
                   case SOUTH:
                       return EnumFacing.WEST;
                   case WEST:
                   default:
                       return EnumFacing.NORTH;
               }
           case DOWN:
           default:
               switch (topFace)
               {
                   case NORTH:
                       return EnumFacing.WEST;
                   case EAST:
                       return EnumFacing.NORTH;
                   case SOUTH:
                       return EnumFacing.EAST;
                   case WEST:
                   default:
                       return EnumFacing.SOUTH;
               }
       }
   }   
   
   public static EnumFacing bottomOf(EnumFacing faceIn, EnumFacing topFace)
   {
       return topFace.getOpposite();
   }
   
   public static EnumFacing leftOf(EnumFacing faceIn, EnumFacing topFace)
   {
       return rightOf(faceIn, topFace).getOpposite();
   }
   
   public static EnumFacing getAxisTop(EnumFacing.Axis axis)
   {
       switch(axis)
       {
       case Y: 
           return EnumFacing.UP;
       case X:
           return EnumFacing.EAST;
       default:
           return EnumFacing.NORTH;
       }
   }
   
   /**
    * Builds the appropriate quaternion to rotate around the given axis.
    */
   public final static Quat4f rotationForAxis(EnumFacing.Axis axis, double degrees)
   {
   	Quat4f retVal = new Quat4f();
   	switch (axis) {
   	case X:
   		retVal.set(new AxisAngle4d(1, 0, 0, Math.toRadians(degrees)));
   		break;
   	case Y:
   		retVal.set(new AxisAngle4d(0, 1, 0, Math.toRadians(degrees)));
   		break;
   	case Z:
   		retVal.set(new AxisAngle4d(0, 0, 1, Math.toRadians(degrees)));
   		break;
   	}
   	return retVal;
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
   public static double AABBVolume(AxisAlignedBB box)
   {
       return (box.maxX - box.minX) * (box.maxY - box.minY) * (box.maxZ - box.minZ);
   }
   
   /**
    * Returns true if the given ray intersects with the given AABB.
    */
   public static boolean doesRayIntersectAABB(Vec3d origin, Vec3d direction, AxisAlignedBB box)
   {

       double tmin = Double.NEGATIVE_INFINITY;
       double tmax = Double.POSITIVE_INFINITY;
      
       double t1, t2;
       
       if(direction.xCoord != 0)
       {
           t1 = (box.minX - origin.xCoord)/direction.xCoord;
           t2 = (box.maxX - origin.xCoord)/direction.xCoord;
           tmin = Math.max(tmin, Math.min(t1, t2));
           tmax = Math.min(tmax, Math.max(t1, t2));
       }
       else if (origin.xCoord <= box.minX || origin.xCoord >= box.maxX) 
       {
           return false;
       }
       
       if(direction.yCoord != 0)
       {
           t1 = (box.minY - origin.yCoord)/direction.yCoord;
           t2 = (box.maxY - origin.yCoord)/direction.yCoord;
           tmin = Math.max(tmin, Math.min(t1, t2));
           tmax = Math.min(tmax, Math.max(t1, t2));
       }
       else if (origin.yCoord <= box.minY || origin.yCoord >= box.maxY) 
       {
           return false;
       }
           
       if(direction.zCoord != 0)
       {
           t1 = (box.minZ - origin.zCoord)/direction.zCoord;
           t2 = (box.maxZ - origin.zCoord)/direction.zCoord;
           tmin = Math.max(tmin, Math.min(t1, t2));
           tmax = Math.min(tmax, Math.max(t1, t2));
       }
       else if (origin.zCoord <= box.minZ || origin.zCoord >= box.maxZ) 
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
       bitLength = clamp(bitLength, 0, Long.SIZE);

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
       bitLength = clamp(bitLength, 0, Integer.SIZE);

       // note: can't use mask = (1L << (bitLength+1)) - 1 here due to overflow & signed values
       int mask = 0;
       for(int i = 0; i < bitLength; i++)
       {
           mask |= (1L << i);
       }
       return mask;
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
