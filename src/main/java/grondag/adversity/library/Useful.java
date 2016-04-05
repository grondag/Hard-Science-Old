package grondag.adversity.library;

import java.util.Random;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;

import org.lwjgl.util.vector.Vector3f;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;


/**
 * Random utilities that have not yet found a more appropriate home.
 */
public class Useful {
	
    /** for misc. non-deterministic random number generation */
    public static final Random SALT_SHAKER = new Random();
	
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
   
   public static void fill2dCircleInPlaneXZ(World worldObj, int x0, int y, int z0, int radius, IBlockState state) {

       // uses midpoint circle algorithm

       if (radius <= 0)
           return;

       worldObj.setBlockState(new BlockPos(x0, y, z0), state);

       for (int r = 1; r <= radius; ++r) {

           int x = r;
           int z = 0;
           int decisionOver2 = 1 - r;

           while (z <= x) {
               
               worldObj.setBlockState(new BlockPos(x + x0, y, z + z0), state); // Octant 1
               worldObj.setBlockState(new BlockPos(z + x0, y, x + z0), state); // Octant 2
               worldObj.setBlockState(new BlockPos(-x + x0, y, z + z0), state); // Octant 3
               worldObj.setBlockState(new BlockPos(-z + x0, y, x + z0), state); // Octant 4
               worldObj.setBlockState(new BlockPos(-x + x0, y, -z + z0), state); // Octant 5
               worldObj.setBlockState(new BlockPos(-z + x0, y, -x + z0), state); // Octant 6
               worldObj.setBlockState(new BlockPos(x + x0, y, -z + z0), state); // Octant 7
               worldObj.setBlockState(new BlockPos(z + x0, y, -x + z0), state); // Octant 8
               z++;

               if (decisionOver2 <= 0) {
                   decisionOver2 += 2 * z + 1; // Change in decision criterion for y -> y+1
               } else {
                   worldObj.setBlockState(new BlockPos(x + x0, y, z + z0), state); // Octant 1
                   worldObj.setBlockState(new BlockPos(z + x0, y, x + z0), state); // Octant 2
                   worldObj.setBlockState(new BlockPos(-x + x0, y, z + z0), state); // Octant 3
                   worldObj.setBlockState(new BlockPos(-z + x0, y, x + z0), state); // Octant 4
                   worldObj.setBlockState(new BlockPos(-x + x0, y, -z + z0), state); // Octant 5
                   worldObj.setBlockState(new BlockPos(-z + x0, y, -x + z0), state); // Octant 6
                   worldObj.setBlockState(new BlockPos(x + x0, y, -z + z0), state); // Octant 7
                   worldObj.setBlockState(new BlockPos(z + x0, y, -x + z0), state); // Octant 8
                   x--;
                   decisionOver2 += 2 * (z - x) + 1; // Change for y -> y+1, x -> x-1
               }
           }
       }
   }
}
