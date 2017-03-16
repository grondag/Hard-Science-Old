package grondag.adversity.feature.volcano.lava.columnmodel;

import java.util.concurrent.ThreadLocalRandom;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.lava.AbstractLavaSimulator;
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
        
    protected int lastFlowTick = 0;
    
    private SortBucket sortBucket;
    
    protected boolean isDirty = false;
    
    private boolean isDeleted = false;
 
    private int flowRemainingThisTick;
    

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
    
    private int getFlowRate()
    {
        if(this.flowRemainingThisTick <= 0) return 0;
        
        int surface1 = this.firstCell.fluidSurfaceUnits();
        int surface2 = this.secondCell.fluidSurfaceUnits();
        
        if(surface1 == surface2)
        {
            return 0;
        }
        else if(surface1 > surface2)
        {
            return this.getEqualizingFlow(this.firstCell, surface1, surface2);
        }
        else // surface1 < surface2
        {
            // flip sign because going from 2 to 1
            return -this.getEqualizingFlow(this.secondCell, surface2, surface1);
        }
   
    }
  
    private void setupTick(LavaSimulatorNew sim)
    {
  
        if(this.firstCell.getFluidUnits() == 0 && this.secondCell.getFluidUnits() == 0)
        {
            this.flowRemainingThisTick = 0;
        }
        else
        {
            this.flowRemainingThisTick = 
             (Math.min(this.firstCell.getCeiling(), this.secondCell.getCeiling()) - Math.max(this.firstCell.getFloor(), this.secondCell.getFloor()))
                * AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL / 20;
        }
    }
    
    /**
     * Core flow computation for equalizing surface level of adjacent cells.
     * The "high" cell should have fluid in it.  
     * "Low" cell may be empty or have fluid, 
     * but should (per the name) have a lower surface level than the high cell.
     * 
     * Constrains flow by the retention level of the high cell but does NOT constrain by the amount available fluid. 
     * (It doesn't know how many fluid units are in the cell, only the surface and retention level.)
     * Retention level serves to mimic adhesion of lava to horizontal surfaces.
     * 
     * @param surfaceHigh fluid surface of high cell - in fluid units relative to world floor
     * @param surfaceLow  floor or fluid surface of low cell - in fluid units relative to world floor
     * @param retentionHigh minimum fluid surface of high cell after any outflow - in fluid units relative to world floor. 
     *  
     * @return Number of fluid units that should flow from high to low cell.
     */
    private int getEqualizingFlow(LavaCell2 cellHigh, int surfaceHigh, int surfaceLow)
    {    
        if(cellHigh.getFluidUnits() == 0) return 0;
        
        int availableFluidUnits = surfaceHigh - cellHigh.getSmoothedRetainedLevel();
        
        if(availableFluidUnits <= 0) return 0;
        
        int flow = (surfaceHigh - surfaceLow) / 2;
        
        if(flow > availableFluidUnits) flow = availableFluidUnits;
        
        if(flow > this.flowRemainingThisTick) flow = flowRemainingThisTick;
        
        return flow;
        
    }
    /**
     *  Resets lastFlowTick and forces run at least once a tick.
     */
    public void doFirstStep(LavaSimulatorNew sim)
    {
            this.isDirty = false;
            this.lastFlowTick = sim.getTickIndex();
            this.setupTick(sim);
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
                    int flow = this.getFlowRate();
                    
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
    
    /** true if either cell has fluid */
    public boolean isActive()
    {
        return this.firstCell.getFluidUnits() > 0 || this.secondCell.getFluidUnits() > 0;
    }
    
    @Override
    public void onDeletion()
    {
        this.firstCell.removeConnection(this);
        this.secondCell.removeConnection(this);
    }
    
    public void flowAcross(LavaSimulatorNew sim, int flow)
    {
        this.flowRemainingThisTick -= Math.abs(flow);
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
        //TODO: remove
        if(this.sortBucket == null) 
            Adversity.log.info("derp");
        
        return this.sortBucket;
    }
    
    public void setSortBucket(SortBucket newBucket)
    {
        //TODO: remove 
        Adversity.log.info("sort bucket = " + newBucket + " for connection id = " + this.id);
        
        this.sortBucket = newBucket;
    }
    
    @Override
    public int hashCode()
    {
        return this.id;
    }     
}