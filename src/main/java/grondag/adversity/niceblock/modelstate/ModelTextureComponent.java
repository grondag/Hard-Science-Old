package grondag.adversity.niceblock.modelstate;

import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.IBlockTest;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModelTextureComponent extends ModelStateComponent<ModelTextureComponent.ModelTextureIndex, Integer>
{
    private final IAlternator alternator;

    public ModelTextureComponent(int ordinal, int alternateCount)
    {
        super(ordinal, alternateCount > 1, alternateCount);
        alternator = Alternator.getAlternator(alternateCount);
    }

    @Override
    public long getBitsFromWorld(NiceBlock block, IBlockTest test, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return alternator.getAlternate(pos);
    }

    @Override
    public ModelTextureComponent.ModelTextureIndex createValueFromBits(long bits)
    {
        return new ModelTextureComponent.ModelTextureIndex((int) bits);
    }

    @Override
    public Class<ModelTextureComponent.ModelTextureIndex> getStateType()
    {
        return ModelTextureComponent.ModelTextureIndex.class;
    }

    @Override
    public Class<Integer> getValueType()
    {
        return Integer.class;
    }
    
    public class ModelTextureIndex extends ModelStateValue<ModelTextureComponent.ModelTextureIndex, Integer>
    {
        private ModelTextureIndex(Integer value)
        {
            super(value);
        }

        @Override
        public long getBits()
        {
            return this.value;
        }

        @Override
        public ModelStateComponent<ModelTextureComponent.ModelTextureIndex, Integer> getComponent()
        {
            return ModelTextureComponent.this;
        }
    }
}
