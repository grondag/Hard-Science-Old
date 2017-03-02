package grondag.adversity.feature.volcano.lava;

import java.util.concurrent.ThreadLocalRandom;

import grondag.adversity.feature.volcano.lava.cell.LavaCell2;

public class LavaConnection2
{
    protected static int nextConnectionID = 0;
    
    /** by convention, first cell will have the lower-valued id */
    public final LavaCell2 firstCell;
    
    /** by convention, second cell will have the higher-valued id */
    public final LavaCell2 secondCell;
    
    public final int id = nextConnectionID++;
    
    public final long key;
    
    public final int rand = ThreadLocalRandom.current().nextInt();
    
    protected int flowThisTick = 0;
    protected int lastFlowTick = 0;
    
    protected long sortKey;
    
    protected boolean isDirty = false;
    
    private boolean isDeleted = false;
 
    public LavaConnection2(LavaCell2 firstCell, LavaCell2 secondCell)
    {
        this.key = getConnectionKey(firstCell, secondCell);
        this.firstCell = firstCell;
        this.secondCell = secondCell;
        firstCell.addConnection(this);
        secondCell.addConnection(this);
        this.updateSortKey();
    }
    
    public static long getConnectionKey(LavaCell2 firstCell, LavaCell2 secondCell)
    {
        if(firstCell.id < secondCell.id)
        {
            return ((long)secondCell.id << 32) | firstCell.id;            
        }
        else
        {
            return ((long)firstCell.id << 32) | secondCell.id;     
        }
    }
    
    public LavaCell2 getOther(LavaCell2 cellIAlreadyHave)
    {
        return cellIAlreadyHave == this.firstCell ? this.secondCell : this.firstCell;
    }
    
    private int getFlowRate(LavaSimulatorNew sim)
    {
        //TODO: stub
        return 0;
    }
  
    /**
     *  Resets lastFlowTick and forces run at least once a tick.
     */
    public void doFirstStep(LavaSimulatorNew sim)
    {
            this.isDirty = false;
            this.flowThisTick = 0;
            this.lastFlowTick = sim.getTickIndex();
            this.doStepWork(sim);
    }
    
    public void doStep(LavaSimulatorNew sim)
    {
        if(this.isDirty)
        {
            this.isDirty = false;
            this.doStepWork(sim);
        }
    }
    
    /**
     * Guts of doStep.
     */
    private void doStepWork(LavaSimulatorNew sim)
    {
        int flow = this.getFlowRate(sim);
        
        if(flow != 0) this.flowAcross(sim, flow);
    }
    
    public void setDirty()
    {
        this.isDirty = true;
    }
    
    /** marks connection deleted. Does not release cells */
    public void setDeleted()
    {
        this.isDeleted = true;
    }
    
    public boolean isDeleted()
    {
        return this.isDeleted;
    }
    
    public void flowAcross(LavaSimulatorNew sim, int flow)
    {
        this.flowThisTick += flow;
        this.firstCell.changeLevel(sim.getTickIndex(), -flow);
        this.secondCell.changeLevel(sim.getTickIndex(), flow);
    }
    
    /**
     * Call when removing this connection so that cell references can be removed if appropriate.
     */
    public void releaseCells()
    {
        this.firstCell.removeConnection(this);
        this.secondCell.removeConnection(this);
    }
    
    /** True if cells are still connected */
    public boolean isValid()
    {
        return !this.isDeleted && this.firstCell.canConnectWith(this.secondCell);
    }
    
    /** 
     * Absolute difference in base elevation, or if base is same, in retained level.
     * Measured in fluid levels
     * For horizontal connections:
     *      Zero if there is no difference or if either block is a barrier.
     *      Higher drop means higher priority for flowing. 
     * For all other connections:
     *      Drop is always zero and not intended to be used.
     */
    public int getSortDrop()
    {
        //TODO
        return 0;
    }
    
    /**
     * Drop can change on validation, but is also used for sorting.
     * To maintain validity of sort index for retrieval, need to preserve drop
     * value until sort can be properly updated.
     * @return
     */
    public long getSortKey()
    {
        return this.sortKey;
    }
    
    @Override
    public int hashCode()
    {
        return this.id;
    }
    
    /** return true of sort key was changed */
    public boolean updateSortKey()
    {
        
        // drop - higher drops come first       16 bits
//        key |= ((long)((0xFFFF - this.getSortDrop()) & 0xFFFF) << 38);
        long key = ((long)((0xFF - this.getSortDrop()) & 0xFF) << 48);
        
        // random                               
        key |= (ThreadLocalRandom.current().nextInt(0xFFFF) << 32);
//        key |= ((Useful.longHash(this.packedConnectionPos) & 0x3F) << 32);
        
        // uniqueness  32 bits
        key |= this.id;
        
        if(key != this.sortKey)
        {
            this.sortKey = key;
            return true;
        }
        else
        {
            return false;
        }
    }
    
}