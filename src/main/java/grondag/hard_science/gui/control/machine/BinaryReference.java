package grondag.hard_science.gui.control.machine;

public class BinaryReference<T> 
{
    public final T trueReference;
    public final T falseReference;

    public BinaryReference(T trueTextureID, T falseTextureID)
    {
        this.trueReference = trueTextureID;
        this.falseReference = falseTextureID;
    }

    public T apply(boolean selector)
    {
        return selector ? trueReference : falseReference;
    }
}