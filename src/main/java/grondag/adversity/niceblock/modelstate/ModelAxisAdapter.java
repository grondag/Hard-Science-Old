package grondag.adversity.niceblock.modelstate;

import grondag.adversity.library.IBlockTest;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModelAxisAdapter extends AbstractModelStateComponentAdapter<ModelAxis>
{
    public static final ModelAxisAdapter INSTANCE = new ModelAxisAdapter();    

    @Override
    public long getBitsFromWorld(NiceBlock block, IBlockTest test, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return Math.max(0, Math.min(2, state.getValue(NiceBlock.META)));
    } 

    @Override
    public long getValueCount()
    {
        return ModelAxis.values().length;
    }

    @Override
    public ModelAxis createValueFromBits(long bits)
    {
        return ModelAxis.values()[(int) bits];
    }

    @Override
    public Class<ModelAxis> getType()
    {
        return ModelAxis.class;
    }

}