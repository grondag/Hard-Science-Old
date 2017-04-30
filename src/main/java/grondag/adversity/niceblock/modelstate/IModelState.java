package grondag.adversity.niceblock.modelstate;

import grondag.adversity.niceblock.base.ModelFactory;

public interface IModelState
{
    public ModelShape getShape();
    public ModelFactory getModel(int i);
    
    public <T extends ModelStateValue<T, V>, V> V getValue(ModelStateComponent<T, V> type);
    public <T extends ModelStateValue<T, V>, V> T getWrappedValue(ModelStateComponent<T, V> type);
   
}
