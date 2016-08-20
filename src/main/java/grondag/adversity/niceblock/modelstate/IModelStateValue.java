package grondag.adversity.niceblock.modelstate;

public interface IModelStateValue<T extends IModelStateValue<T,V>, V>
{
    abstract public ModelStateComponent<T, V> getComponentType();
    abstract public V getValue();
    abstract public long getBits();
//    public default long getBits()
//    {
//        return getComponentType().getBits(getValue());
//    }
}
