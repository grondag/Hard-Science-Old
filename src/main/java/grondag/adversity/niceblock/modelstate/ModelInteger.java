package grondag.adversity.niceblock.modelstate;

public class ModelInteger implements IModelStateValue<ModelInteger, Integer>
{
    private final int value;
    private final ModelStateComponent<ModelInteger, Integer> type;
    
    public ModelInteger(ModelStateComponent<ModelInteger, Integer> type, int value)
    {
        this.type = type;
        this.value = value;
    }
    @Override
    public ModelStateComponent<ModelInteger, Integer> getComponentType()
    {
        return type;
    }

    @Override
    public Integer getValue()
    {
        return value;
    }

    @Override
    public long getBits()
    {
        return value;
    }
}
