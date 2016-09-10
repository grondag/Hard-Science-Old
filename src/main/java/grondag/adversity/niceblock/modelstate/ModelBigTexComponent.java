package grondag.adversity.niceblock.modelstate;

import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
* Selects one of 4096 models in a repeating 16x16x16 volume
*/
public class ModelBigTexComponent extends ModelStateComponent<ModelBigTexComponent.ModelBigTex, Integer>
{
    public final boolean useMetaVariants;
    
    public ModelBigTexComponent(int ordinal, boolean useMetaVariants)
    {
        super(ordinal, WorldRefreshType.CACHED, useMetaVariants ? 4096 * 16 : 4096);
        this.useMetaVariants = useMetaVariants;
    }

    @Override
    public ModelBigTexComponent.ModelBigTex createValueFromBits(long bits)
    {
        return new ModelBigTexComponent.ModelBigTex((int) bits);
    }

    @Override
    public Class<ModelBigTexComponent.ModelBigTex> getStateType()
    {
        return ModelBigTexComponent.ModelBigTex.class;
    }

    @Override
    public Class<Integer> getValueType()
    {
        return Integer.class;
    }

    @Override
    public long getBitsFromWorld(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        if(this.useMetaVariants)
        {
             return state.getValue(NiceBlock.META) << 12 | ((pos.getX() & 15) << 8) | ((pos.getY() & 15) << 4) | (pos.getZ() & 15);
        }
        else
        {
            return ((pos.getX() & 15) << 8) | ((pos.getY() & 15) << 4) | (pos.getZ() & 15);
        }
    }

    public class ModelBigTex extends ModelStateValue<ModelBigTexComponent.ModelBigTex, Integer>
    {
        private ModelBigTex(Integer value)
        {
            super(value);
        }

        @Override
        public long getBits()
        {
            return this.value;
        }

        @Override
        public ModelStateComponent<ModelBigTex, Integer> getComponent()
        {
            return ModelBigTexComponent.this;
        }
    }
}
