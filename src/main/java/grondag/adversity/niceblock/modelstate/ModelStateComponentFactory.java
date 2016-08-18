package grondag.adversity.niceblock.modelstate;

import grondag.adversity.library.IBlockTest;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModelStateComponentFactory<T extends IModelStateComponent<?>>
{
    protected final IModelStateComponent<?>[] VALUES;
    protected final AbstractModelStateComponentAdapter<T> adapter;
    
    protected ModelStateComponentFactory(AbstractModelStateComponentAdapter<T> adapter)
    {
        this.adapter = adapter;
        VALUES = new IModelStateComponent<?>[(int) adapter.getValueCount()];
        for(int i = 0; i < adapter.getValueCount(); i++)
        {
            VALUES[i] = (IModelStateComponent<?>) adapter.createValueFromBits(i);
        }
    }

    @SuppressWarnings("unchecked")
    public T getStateFromWorld(NiceBlock block, IBlockTest test, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return (T) VALUES[(int) adapter.getBitsFromWorld(block, test, state, world, pos)];
    }

    @SuppressWarnings("unchecked")
    public T getStateFromBits(long bits)
    {
        return (T) VALUES[(int) Math.max(0, Math.min(adapter.getValueCount(), bits))];
    }
        
}