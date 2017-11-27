package grondag.hard_science.simulator.persistence;

/** use this to avoid checking null on dirt listener */
public class NullDirtListener implements IDirtListener
{
    public static final NullDirtListener INSTANCE = new NullDirtListener();

    public void setDirty() {}
    
}