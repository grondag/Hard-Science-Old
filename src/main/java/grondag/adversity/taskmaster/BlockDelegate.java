package grondag.adversity.taskmaster;

import grondag.adversity.taskmaster.base.Delegate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class BlockDelegate extends Delegate{
	
	private BlockPos pos;
	
	public BlockDelegate(BlockPos pos)
	{
		this.pos = pos;
	}
	
	public BlockPos getPos() { return pos; }
}
