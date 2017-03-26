package grondag.adversity.feature.volcano.lava.simulator;

import java.util.concurrent.ThreadLocalRandom;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.lava.simulator.LavaConnections.SortBucket;
import grondag.adversity.library.ISimpleListItem;
import grondag.adversity.library.Useful;

public class LavaConnection implements ISimpleListItem
{
    
//    public final static int MAX_FLOW_PER_TICK = LavaSimulator.FLUID_UNITS_PER_BLOCK / 10;
    
    protected static int nextConnectionID = 0;
    
    /** by convention, first cell will have the lower-valued id */
    public final LavaCell firstCell;
    
    /** by convention, second cell will have the higher-valued id */
    public final LavaCell secondCell;
    
    public final int id = nextConnectionID++;
        
    public final long key;
    
    public final int rand = ThreadLocalRandom.current().nextInt();
        
    protected int lastFlowTick = 0;
    
    private volatile SortBucket sortBucket;
    
    private volatile SortBucket lastSortBucket;
    
    protected boolean isDirty = false;
    
    private boolean isDeleted = false;
 
    private int flowRemainingThisTick;
    private int maxFlowPerStep;
    

    public LavaConnection(LavaCell firstCell, LavaCell secondCell)
    {
        this.key = getConnectionKey(firstCell, secondCell);
        this.firstCell = firstCell;
        this.secondCell = secondCell;
        firstCell.addConnection(this);
        secondCell.addConnection(this);
    }
    
    public static long getConnectionKey(LavaCell firstCell, LavaCell secondCell)
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
    
    public LavaCell getOther(LavaCell cellIAlreadyHave)
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
  
    private void setupTick(LavaSimulator sim)
    {
        if(this.firstCell.getFluidUnits() == 0 && this.secondCell.getFluidUnits() == 0)
        {
            this.flowRemainingThisTick = 0;
            this.maxFlowPerStep = 0;
        }
        else
        {
            int fluidTop = Math.max(this.firstCell.fluidSurfaceLevel(), this.secondCell.fluidSurfaceLevel());
            int spaceTop = Math.min(this.firstCell.getCeiling(), this.secondCell.getCeiling());
            
            this.flowRemainingThisTick = 
             (Math.min(fluidTop, spaceTop) - Math.max(this.firstCell.getFloor(), this.secondCell.getFloor()))
                * LavaSimulator.FLUID_UNITS_PER_LEVEL / 4;
            this.maxFlowPerStep = this.flowRemainingThisTick / 4;
            if(this.maxFlowPerStep == 0) this.maxFlowPerStep = 1;
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
     *  
     * @return Number of fluid units that should flow from high to low cell.
     */
    private int getEqualizingFlow(LavaCell cellHigh, int surfaceHigh, int surfaceLow)
    {    
        if(cellHigh.getFluidUnits() == 0) return 0;
        
        int availableFluidUnits = surfaceHigh - cellHigh.getSmoothedRetainedUnits();
        if(availableFluidUnits <= 0) return 0;
        
        if(availableFluidUnits > this.maxFlowPerStep) availableFluidUnits = maxFlowPerStep;
        if(availableFluidUnits > this.flowRemainingThisTick) availableFluidUnits = flowRemainingThisTick;
        
        int flow = (surfaceHigh - surfaceLow) / 2;

        return flow < availableFluidUnits ? flow : availableFluidUnits;
   
    }
    
    /**
     *  Resets lastFlowTick and forces run at least once a tick.
     */
    public void doFirstStep(LavaSimulator sim)
    {
        this.isDirty = false;
        this.lastFlowTick = sim.getTickIndex();
        this.setupTick(sim);
        this.doStepWork(sim);
    }
    
    public void doStep(LavaSimulator sim)
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
    private void doStepWork(LavaSimulator sim)
    {
//        if(this.firstCell.id == 1229 || this.secondCell.id == 1229)
//            Adversity.log.info("boop");
        
        boolean isIncomplete = true;
        do
        {
            if(this.firstCell.tryLock())
            {
                if(this.secondCell.tryLock())
                {
                    int flow = this.getFlowRate();
                    
                    int saveForDebug1 = firstCell.getFluidUnits();
                    int saveForDebug2 = secondCell.getFluidUnits();
                    
                    if(this.firstCell.getFluidUnits() -flow < 0) 
                        Adversity.log.info("derp1 " + saveForDebug1);
                    
                    if(this.secondCell.getFluidUnits() + flow < 0) 
                        Adversity.log.info("derp2 " + saveForDebug2);
                    
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
    
    public void flowAcross(LavaSimulator sim, int flow)
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

    /**
     * Should be null if non-active
     */
    public SortBucket getSortBucket()
    {
        return this.sortBucket;
    }
    
    public void setSortBucket(LavaConnections connections, SortBucket newBucket)
    {
        if(newBucket != this.sortBucket)
        {
            connections.invalidateSortBuckets();
            this.lastSortBucket = this.sortBucket;
            this.sortBucket = newBucket;
        }
    }
    
    public SortBucket getLastSortBucket()
    {
        return this.lastSortBucket;
    }
    
    public void clearLastSortBucket()
    {
        this.lastSortBucket = this.sortBucket;
    }
    
    @Override
    public int hashCode()
    {
        return this.id;
    }     
}