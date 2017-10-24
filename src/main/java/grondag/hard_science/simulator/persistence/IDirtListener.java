package grondag.hard_science.simulator.persistence;

/**
 * Tracks if the instance needs to be persisted.  Used by persistence nodes and some sub nodes.
 */
public interface IDirtListener 
{
    public void setDirty();

    public interface IDirtKeeper extends IDirtListener
    {
        public abstract boolean isSaveDirty();
    
        public abstract void setSaveDirty(boolean isDirty);
    
        @Override
        public default void setDirty() { this.setSaveDirty(true); }
    }

    public interface IDirtListenerProvider
    {
        public IDirtListener getDirtListener();
    }
    
    /** use this to avoid checking null on dirt listener */
    public static class NullDirtListener implements IDirtListener
    {
        public static final NullDirtListener INSTANCE = new NullDirtListener();

        public void setDirty() {}
        
    }
    
    public interface IDirtNotifier extends IDirtListener
    {
        public abstract void setDirtKeeper(IDirtKeeper listener);
    }
}
