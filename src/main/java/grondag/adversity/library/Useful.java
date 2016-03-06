package grondag.adversity.library;

import javax.vecmath.Matrix4f;

import org.lwjgl.util.vector.Vector3f;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Random utilities that have not yet found a more appropriate home.
 */
public class Useful {
	
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
   
}
