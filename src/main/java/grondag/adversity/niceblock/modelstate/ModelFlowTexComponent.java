package grondag.adversity.niceblock.modelstate;

import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.model.BigTexModelFactory;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
* Selects one of 512 models in a repeating 8x8x8 volume
*/
public class ModelFlowTexComponent extends ModelStateComponent<ModelFlowTexComponent.ModelFlowTex, ModelFlowTexComponent.FlowTexValue>
{
    
    public ModelFlowTexComponent(int ordinal)
    {
        super(ordinal, WorldRefreshType.CACHED, 8 * 8 * 8);
    }

    @Override
    public ModelFlowTexComponent.ModelFlowTex createValueFromBits(long bits)
    {
        return new ModelFlowTexComponent.ModelFlowTex(new FlowTexValue((int) bits));
    }

    @Override
    public Class<ModelFlowTexComponent.ModelFlowTex> getStateType()
    {
        return ModelFlowTexComponent.ModelFlowTex.class;
    }

    @Override
    public Class<FlowTexValue> getValueType()
    {
        return FlowTexValue.class;
    }

    @Override
    public long getBitsFromWorld(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
         return ((pos.getX() & 7) << 6) | ((pos.getY() & 7) << 3) | (pos.getZ() & 7);
    }

    public class ModelFlowTex extends ModelStateValue<ModelFlowTexComponent.ModelFlowTex, FlowTexValue>
    {
        private ModelFlowTex(FlowTexValue value)
        {
            super(value);
        }

        @Override
        public long getBits()
        {
            return this.value.bits;
        }

        @Override
        public ModelStateComponent<ModelFlowTex, FlowTexValue> getComponent()
        {
            return ModelFlowTexComponent.this;
        }
    }

    public class FlowTexValue
    {
        private final long bits;
        
        private FlowTexValue(long bits)
        {
            this.bits = bits;
        }
        
        public int getX()
        {
             return (int) ((bits >> 6) & 7); 
        }
        
        public int getY()
        {
             return (int) ((bits >> 3) & 7); 
        }
        
        public int getZ()
        {
             return (int) (bits & 7); 
        }
    }
}
