package grondag.adversity.niceblock.modelstate;

import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.IBlockTest;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModelAlternateComponent extends ModelStateComponent<ModelInteger, Integer>
{
    private final IAlternator alternator;

    public ModelAlternateComponent(int ordinal, int alternateCount)
    {
        super(ordinal, alternateCount > 1);
        alternator = Alternator.getAlternator(alternateCount);
    }

    @Override
    public long getValueCount()
    {
        return alternator.getAlternateCount();
    }

    @Override
    public long getBitsFromWorld(NiceBlock block, IBlockTest test, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return alternator.getAlternate(pos);
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
}
