package grondag.adversity.niceblock.modelstate;

import grondag.adversity.library.IBlockTest;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
* Selects one of 4096 models in a repeating 16x16x16 volume
*/
public class ModelBigTexComponent extends ModelStateComponent<ModelInteger, Integer>
{
    public ModelBigTexComponent(int ordinal)
    {
        super(ordinal, true);
    }

    @Override
    public long getValueCount()
    {
        return 0xFFF;
    }

    @Override
    public ModelInteger createValueFromBits(long bits)
    {
        return new ModelInteger(this, (int) bits);
    }

    @Override
    public Class<ModelInteger> getStateType()
    {
        return ModelInteger.class;
    }

    @Override
    public Class<Integer> getValueType()
    {
        return Integer.class;
    }

    @Override
    public long getBitsFromWorld(NiceBlock block, IBlockTest test, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return ((pos.getX() & 15) << 8) | ((pos.getY() & 15) << 4) | (pos.getZ() & 15);
    }

}
