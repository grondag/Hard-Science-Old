package grondag.adversity.niceblock.modelstate;

public interface IModelState
{
    public <T extends ModelStateValue<T, V>, V> V getValue(ModelStateComponent<T, V> type);
    public <T extends ModelStateValue<T, V>, V> T getWrappedValue(ModelStateComponent<T, V> type);
   
}
