package grondag.hard_science.simulator.persistence;

public interface IDirtNotifier extends IDirtListener
{
    public abstract void setDirtKeeper(IDirtKeeper listener);
}