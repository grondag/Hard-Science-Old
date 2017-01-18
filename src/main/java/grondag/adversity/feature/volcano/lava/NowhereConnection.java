package grondag.adversity.feature.volcano.lava;

/**
 * Version of LavaCellConnection that doesn't do anything.
 * Can be safely held in reference and methods called without an effect.
 * Allows us to avoid doing null checks for connections that don't actually exist.
 */
public class NowhereConnection extends LavaCellConnection
{
    
    public static NowhereConnection INSTANCE = new NowhereConnection();
    
    public NowhereConnection()
    {
        super();
    }

    @Override
    public LavaCell getOther(LavaCell cellIAlreadyHave)
    {
        return null;
    }

    @Override
    public void doStep(LavaSimulator sim, boolean force)
    {
        //NOOP
    }

    @Override
    public void setDirty()
    {
        //NOOP
    }

    @Override
    public void flowAcross(LavaSimulator sim, int flow)
    {
        //NOOP
    }

    @Override
    public void releaseCells()
    {
        //NOOP
    }

    @Override
    public int getDrop()
    {
        return 0;
    }

    @Override
    public int getSortDrop()
    {
        return 0;
    }

    @Override
    public void updateSortDrop()
    {
        //NOOP
    }

    @Override
    public boolean isBottomDrop()
    {
        // nowhere connection implies barrier, so cannot be a free drop
        return false;
    }

    @Override
    public boolean isBottomSupporting()
    {
        // nowhere connection implies barrier, so must be supporting
        // (or if non-vertical value doesn't matter)
        return true;
    }
    
    
    
    

}
