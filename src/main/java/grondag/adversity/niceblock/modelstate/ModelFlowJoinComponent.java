package grondag.adversity.niceblock.modelstate;

import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModelFlowJoinComponent extends ModelStateComponent<ModelFlowJoinComponent.ModelFlowJoin, FlowHeightState>
{    
    public ModelFlowJoinComponent(int ordinal, WorldRefreshType refreshType)
    {
        super(ordinal, refreshType, FlowHeightState.STATE_BIT_MASK);
    }

    @Override
    public ModelFlowJoin createValueFromBits(long bits)
    {
        return new ModelFlowJoin(new FlowHeightState(bits));
    }

    @Override
    public Class<ModelFlowJoin> getStateType()
    {
        return ModelFlowJoinComponent.ModelFlowJoin.class;
    }

    @Override
    public Class<FlowHeightState> getValueType()
    {
        return FlowHeightState.class;
    }


    @Override
    public long getBitsFromWorld(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return FlowHeightState.getBitsFromWorldStatically(block, state, world, pos);
    }

    public static FlowHeightState getFlowState(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return new FlowHeightState(FlowHeightState.getBitsFromWorldStatically(block, state, world, pos));
    }

    public class ModelFlowJoin extends ModelStateValue<ModelFlowJoinComponent.ModelFlowJoin, FlowHeightState>
    {
        ModelFlowJoin(FlowHeightState valueIn)
        {
            super(valueIn);
        }

        @Override
        public ModelStateComponent<ModelFlowJoinComponent.ModelFlowJoin, FlowHeightState> getComponent()
        {
            return ModelFlowJoinComponent.this;
        }

        @Override
        public long getBits()
        {
            return this.value.getStateKey();
        }
    }
}
