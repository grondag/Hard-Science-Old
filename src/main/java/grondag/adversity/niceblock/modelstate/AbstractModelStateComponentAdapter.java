package grondag.adversity.niceblock.modelstate;

import grondag.adversity.library.IBlockTest;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public abstract class AbstractModelStateComponentAdapter<T extends IModelStateComponent<?>> 
{
    abstract public Class<T> getType();
    abstract public long getValueCount();
    abstract public long getBitsFromWorld(NiceBlock block, IBlockTest test, IBlockState state, IBlockAccess world, BlockPos pos);
    abstract public T createValueFromBits(long bits);
}
