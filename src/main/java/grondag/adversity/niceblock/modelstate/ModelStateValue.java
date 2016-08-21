package grondag.adversity.niceblock.modelstate;

public abstract class ModelStateValue<T extends ModelStateValue<T,V>, V>
{
    protected final V value;
    
    ModelStateValue(V valueIn)
    {
        this.value = valueIn;
    }
    
    abstract public ModelStateComponent<T, V> getComponent();
    public V getValue() {return value; }
    abstract public long getBits();
}
