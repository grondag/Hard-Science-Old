package grondag.adversity.niceblock.modelstate;

public interface IModelStateComponent<T>
{
    abstract public ModelStateComponentType getComponentType();
    abstract public long getBits();
}
