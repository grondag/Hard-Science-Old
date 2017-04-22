package grondag.adversity.niceblock.modelstate;

import grondag.adversity.niceblock.base.ModelAppearance;
import grondag.adversity.niceblock.base.ModelAppearanceList;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModelAppearanceComponent extends ModelStateComponent<ModelAppearanceComponent.ModelApperanceWrapper, ModelAppearance>
{
    private final ModelAppearanceList apperanceList;
    
    public ModelAppearanceComponent(int ordinal, WorldRefreshType refreshType, ModelAppearanceList apperanceList)
    {
        super(ordinal, refreshType, apperanceList.size());
        this.apperanceList = apperanceList;
    }

    @Override
    public long getBitsFromWorld(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        //default value is first appearance, if user selects different will be stored in tile entity
        return 0L;
    }

    @Override
    public ModelApperanceWrapper createValueFromBits(long bits)
    {
        return new ModelApperanceWrapper(Math.min((int)getValueCount() - 1, Math.max(0, (int) bits)));
    }

    @Override
    public Class<ModelApperanceWrapper> getStateType()
    {
        return ModelApperanceWrapper.class;
    }

    @Override
    public Class<ModelAppearance> getValueType()
    {
        return ModelAppearance.class;
    }
    
    public class ModelApperanceWrapper extends ModelStateValue<ModelAppearanceComponent.ModelApperanceWrapper, ModelAppearance>
    {
        //values don't know their own index, so need to save it
        private final int index;
        
        private ModelApperanceWrapper(int index)
        {
            super(apperanceList.get(index));
            this.index = index;
        }
        
        @Override
        public ModelStateComponent<ModelAppearanceComponent.ModelApperanceWrapper, ModelAppearance> getComponent()
        {
            return ModelAppearanceComponent.this;
        }

        @Override
        public long getBits()
        {
            return index;
        }
    }

}
