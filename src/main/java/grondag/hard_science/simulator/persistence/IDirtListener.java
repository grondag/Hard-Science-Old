package grondag.hard_science.simulator.persistence;

/**
 * Tracks if the instance needs to be persisted.  Used by persistence nodes and some sub nodes.
 */
public interface IDirtListener 
{
    public void setDirty();
}
