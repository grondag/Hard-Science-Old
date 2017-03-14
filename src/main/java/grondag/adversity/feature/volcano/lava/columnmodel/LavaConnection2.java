package grondag.adversity.feature.volcano.lava.columnmodel;

import java.util.concurrent.ThreadLocalRandom;

import grondag.adversity.feature.volcano.lava.columnmodel.LavaConnections.SortBucket;
import grondag.adversity.library.ISimpleListItem;

public class LavaConnection2 implements ISimpleListItem
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
    
    private SortBucket sortBucket;
    
    protected boolean isDirty = false;
    
    private boolean isDeleted = false;
 
    public LavaConnection2(LavaCell2 firstCell, LavaCell2 secondCell)
    {
        this.key = getConnectionKey(firstCell, secondCell);
        this.firstCell = firstCell;
        this.secondCell = secondCell;
        firstCell.addConnection(this);
        secondCell.addConnection(this);
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
        boolean isIncomplete = true;
        do
        {
            if(this.firstCell.tryLock())
            {
                if(this.secondCell.tryLock())
                {
                    int flow = this.getFlowRate(sim);
                    
                    if(flow != 0) this.flowAcross(sim, flow);
                    
                    isIncomplete = false;
                    
                    this.secondCell.unlock();
                }
                this.firstCell.unlock();
            }
        } while(isIncomplete);
    }
    
    /**
     * Marks connection for inclusion in next round of processing.
     */
    public void setDirty()
    {
        this.isDirty = true;
    }
    
    /** marks connection deleted. Does not release cells */
    public void setDeleted()
    {
        this.isDeleted = true;
    }
    
    @Override
    public boolean isDeleted()
    {
        // cells that can no longer connect should be deleted
        return this.isDeleted || !this.firstCell.canConnectWith(this.secondCell);
    }
    
    @Override
    public void onDeletion()
    {
        this.firstCell.removeConnection(this);
        this.secondCell.removeConnection(this);
    }
    
    public void flowAcross(LavaSimulatorNew sim, int flow)
    {
        this.flowThisTick += flow;
        this.firstCell.changeLevel(sim.getTickIndex(), -flow);
        this.secondCell.changeLevel(sim.getTickIndex(), flow);
    }
    
    /** 
     * Absolute difference in base elevation, or if base is same, in retained level.
     * Measured in fluid levels
     * Zero if there is no difference or if either block is a barrier.
     * Higher drop means higher priority for flowing. 
     */
    public int getSortDrop()
    {
        return Math.abs(this.firstCell.getFloor() - this.secondCell.getFloor());
    }

    public SortBucket getSortBucket()
    {
        return this.sortBucket;
    }
    
    public void setSortBucket(SortBucket newBucket)
    {
        this.sortBucket = newBucket;
    }
    
    @Override
    public int hashCode()
    {
        return this.id;
    }     
}