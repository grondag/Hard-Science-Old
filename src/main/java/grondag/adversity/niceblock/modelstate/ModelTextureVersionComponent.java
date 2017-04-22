package grondag.adversity.niceblock.modelstate;

import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModelTextureVersionComponent extends ModelStateComponent<ModelTextureVersionComponent.ModelTextureIndex, Integer>
{
    private final IAlternator alternator;

    public ModelTextureVersionComponent(int ordinal, int alternateCount)
    {
        super(ordinal, alternateCount > 1 ? WorldRefreshType.CACHED : WorldRefreshType.NEVER, alternateCount);
        alternator = Alternator.getAlternator(alternateCount, 957293844);
    }

    @Override
    public long getBitsFromWorld(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return alternator.getAlternate(pos);
    }

    @Override
    public ModelTextureVersionComponent.ModelTextureIndex createValueFromBits(long bits)
    {
        return new ModelTextureVersionComponent.ModelTextureIndex((int) bits);
    }

    @Override
    public Class<ModelTextureVersionComponent.ModelTextureIndex> getStateType()
    {
        return ModelTextureVersionComponent.ModelTextureIndex.class;
    }

    @Override
    public Class<Integer> getValueType()
    {
        return Integer.class;
    }
    
    public class ModelTextureIndex extends ModelStateValue<ModelTextureVersionComponent.ModelTextureIndex, Integer>
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
        public ModelStateComponent<ModelTextureVersionComponent.ModelTextureIndex, Integer> getComponent()
        {
            return ModelTextureVersionComponent.this;
        }
    }
}
