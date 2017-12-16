package grondag.hard_science.library.concurrency;

public interface CountedJobTask<V>
{
    public abstract void doJobTask(V operand);
}