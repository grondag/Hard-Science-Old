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
        super(NullCell.INSTANCE, NullCell.INSTANCE);
    }

    @Override
    public LavaCell getOther(LavaCell cellIAlreadyHave)
    {
        return null;
    }

    @Override
    public void doStep(LavaSimulator sim)
    {
        //NOOP
    }

    @Override
    public void doFirstStep(LavaSimulator sim)
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
    public long getSortKey()
    {
        return 0;
    }

    @Override
    public void updateSortKey()
    {
        //NOOP
    }

    @Override
    protected int getFlowRate(LavaSimulator sim)
    {
        return 0;
    }

    private static class NullCell extends LavaCell
    {
        private static final NullCell INSTANCE = new NullCell();
        
        private NullCell()
        {
            super(null, 0, 0);            
        }
        
        @Override public boolean isBarrier()
        {
            return true;
        }
    }
}
