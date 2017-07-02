package grondag.hard_science.library.world;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Quat4f;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class WorldHelper
{

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

    public static EnumFacing bottomOf(EnumFacing faceIn, EnumFacing topFace)
       {
           return topFace.getOpposite();
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

    /**
     * Convenience method to keep code more readable
     */
    public static boolean isBlockReplaceable(IBlockAccess worldIn, BlockPos pos)
    {
        return worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos);
    }
}
