package grondag.hard_science.library.world;

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
     * Convenience method to keep code more readable
     */
    public static boolean isBlockReplaceable(IBlockAccess worldIn, BlockPos pos)
    {
        return worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos);
    }
}
