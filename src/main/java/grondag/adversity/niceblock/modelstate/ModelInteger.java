package grondag.adversity.niceblock.modelstate;

public class ModelInteger extends ModelStateValue<ModelInteger, Integer>
{
    private final ModelStateComponent<ModelInteger, Integer> component;
    
    public ModelInteger(ModelStateComponent<ModelInteger, Integer> type, int value)
    {
        super(value);
        this.component = type;
    }
    @Override
    public ModelStateComponent<ModelInteger, Integer> getComponent()
    {
        return component;
    }

    @Override
    public long getBits()
    {
        return this.value;
    }
}
