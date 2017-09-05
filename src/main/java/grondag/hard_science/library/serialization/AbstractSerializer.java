package grondag.hard_science.library.serialization;

public abstract class AbstractSerializer<T> implements ISerializer<T>
{
  
    public final boolean isServerSideOnly;

    public AbstractSerializer(boolean isServerSideOnly)
    {
        this.isServerSideOnly = isServerSideOnly;
    }
   
}
