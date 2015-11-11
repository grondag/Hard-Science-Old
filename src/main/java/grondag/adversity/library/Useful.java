package grondag.adversity.library;

import net.minecraft.util.BlockPos;

public class Useful {
	
	
	// sorts members of the vector so that x is highest and z is lowest
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

}
