package grondag.hard_science.simulator.persistence;

public interface IDirtKeeper extends IDirtListener
{
    public abstract boolean isSaveDirty();

    public abstract void setSaveDirty(boolean isDirty);

    @Override
    public default void setDirty() { this.setSaveDirty(true); }
}