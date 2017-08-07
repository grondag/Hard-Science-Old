package grondag.hard_science.simulator.persistence;

/**
 * Tracks if the instance needs to be persisted.  Used by persistence nodes and some sub nodes.
 */
public interface IDirtKeeper
{
    public abstract boolean isSaveDirty();
    
    public abstract void setSaveDirty(boolean isDirty);
    
    public default void setSaveDirty() { this.setSaveDirty(true); }

}
