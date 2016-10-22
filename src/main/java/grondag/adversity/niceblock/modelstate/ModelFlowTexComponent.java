package grondag.adversity.niceblock.modelstate;

import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.model.BigTexModelFactory;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
* Selects one of 256 models in a repeating 16x16x16 volume
*/
public class ModelFlowTexComponent extends ModelStateComponent<ModelFlowTexComponent.ModelFlowTex, Integer>
{
    
    public ModelFlowTexComponent(int ordinal)
    {
        super(ordinal, WorldRefreshType.CACHED, 64);
    }

    @Override
    public ModelFlowTexComponent.ModelFlowTex createValueFromBits(long bits)
    {
        return new ModelFlowTexComponent.ModelFlowTex((int) bits);
    }

    @Override
    public Class<ModelFlowTexComponent.ModelFlowTex> getStateType()
    {
        return ModelFlowTexComponent.ModelFlowTex.class;
    }

    @Override
    public Class<Integer> getValueType()
    {
        return Integer.class;
    }

    @Override
    public long getBitsFromWorld(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
         return ((pos.getX() & 7) << 3) | (pos.getZ() & 7);
    }

    public class ModelFlowTex extends ModelStateValue<ModelFlowTexComponent.ModelFlowTex, Integer>
    {
        private ModelFlowTex(Integer value)
        {
            super(value);
        }

        @Override
        public long getBits()
        {
            return this.value;
        }

        @Override
        public ModelStateComponent<ModelFlowTex, Integer> getComponent()
        {
            return ModelFlowTexComponent.this;
        }
    }
}
