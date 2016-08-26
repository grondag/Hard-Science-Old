package grondag.adversity.niceblock.modelstate;

import grondag.adversity.library.joinstate.SimpleJoin;

public class ModelSimpleJoinComponent extends ModelStateComponent<ModelSimpleJoinComponent.ModelSimpleJoin, SimpleJoin>
{
    public ModelSimpleJoinComponent(int ordinal, boolean useWorldState)
    {
        super(ordinal, useWorldState, SimpleJoin.STATE_COUNT);
    }

    @Override
    public ModelSimpleJoinComponent.ModelSimpleJoin createValueFromBits(long bits)
    {
        return new ModelSimpleJoinComponent.ModelSimpleJoin(new SimpleJoin((int) bits));
    }

    @Override
    public Class<ModelSimpleJoinComponent.ModelSimpleJoin> getStateType()
    {
        return ModelSimpleJoinComponent.ModelSimpleJoin.class;
    }

    @Override
    public Class<SimpleJoin> getValueType()
    {
        return SimpleJoin.class;
    }
    
    public class ModelSimpleJoin extends ModelStateValue<ModelSimpleJoinComponent.ModelSimpleJoin, SimpleJoin>
    {
        private ModelSimpleJoin(SimpleJoin valueIn)
        {
            super(valueIn);
        }

        @Override
        public ModelStateComponent<ModelSimpleJoinComponent.ModelSimpleJoin, SimpleJoin> getComponent()
        {
            return ModelSimpleJoinComponent.this;
        }
    
        @Override
        public long getBits()
        {
            return this.value.getIndex();
        }
    }
}