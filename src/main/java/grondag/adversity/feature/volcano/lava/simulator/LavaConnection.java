package grondag.adversity.feature.volcano.lava.simulator;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import grondag.adversity.feature.volcano.lava.simulator.LavaConnections.FlowDirection;
import grondag.adversity.feature.volcano.lava.simulator.LavaConnections.SortBucket;
import grondag.adversity.library.ISimpleListItem;
import grondag.adversity.simulator.Simulator;

@SuppressWarnings("unused")
public class LavaConnection implements ISimpleListItem
{
    
    protected static int nextConnectionID = 0;
    
    /** by convention, first cell will have the lower-valued id */
    public final LavaCell firstCell;
    
    /** by convention, second cell will have the higher-valued id */
    public final LavaCell secondCell;
    
    public final int id = nextConnectionID++;
        
    public final long key;
    
    public final int rand = ThreadLocalRandom.current().nextInt();
        
//    protected int lastFlowTick = 0;
    
    private SortBucket sortBucket;
    
    private SortBucket lastSortBucket;
    
    private FlowDirection flowDirection = FlowDirection.NONE;
    
//    protected boolean isDirty = false;
    
    private boolean flowedLastStep = false;
    
    private boolean isDeleted = false;
 
    private int flowRemainingThisTick;
    private int maxFlowPerStep;
    
//    public static ConcurrentPerformanceCounter perfFocus = new ConcurrentPerformanceCounter();
    
    public static final boolean ENABLE_FLOW_TRACKING = true;
    public static AtomicInteger totalFlow = new AtomicInteger(0);

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
    
    /**
     * Determine if connection can flow, and if so, in which direction.
     * If flowed last tick, then direction cannot reverse - must first go to none and then to opposite direction.
     */
    public void setupTick()
    {
        int surface1 = this.firstCell.fluidSurfaceUnits();
        int surface2 = this.secondCell.fluidSurfaceUnits();
        
        //TODO: make cells not flow if they haven't flowed for several ticks?
        // and allow cells to continue flowing without much diff if they flowed last tick?
        // consider retention level?
        
        if(surface1 == surface2)
        {
            //should not flow
            if(this.flowDirection != FlowDirection.NONE)
            {
                this.flowDirection = FlowDirection.NONE;
            }
        } 
        else if(surface1 > surface2)
        {
            this.setupTickInner(this.firstCell, this.secondCell, surface1, surface2, FlowDirection.ONE_TO_TWO, FlowDirection.TWO_TO_ONE);
        }
        else
        {
            this.setupTickInner(this.secondCell, this.firstCell, surface2, surface1, FlowDirection.TWO_TO_ONE, FlowDirection.ONE_TO_TWO);
        }
    }
    
    private void setupTickInner(LavaCell cellHigh, LavaCell cellLow, int surfaceHigh, int surfaceLow, FlowDirection highToLow, FlowDirection lowToHigh)
    {
        int diff = surfaceHigh - surfaceLow;
        if(diff < LavaSimulator.MIN_FLOW_UNITS_X2 || cellHigh.isEmpty())
        {
            //should not flow
            if(this.flowDirection != FlowDirection.NONE)
            {
                this.flowDirection = FlowDirection.NONE;
            }
        }
        else
        {
            int flowWindow = (Math.min(surfaceHigh, cellLow.getCeilingUnits()) - Math.max(cellHigh.getFloorUnits(), cellLow.getFloorUnits()));
            
            if(flowWindow < LavaSimulator.FLUID_UNITS_PER_LEVEL || this.flowDirection == lowToHigh)
            {
                //should not flow
                if(this.flowDirection != FlowDirection.NONE)
                {
                    this.flowDirection = FlowDirection.NONE;
                }
            }
            else 
            {
                if(this.flowDirection != highToLow)
                {
                    this.flowDirection = highToLow;
                }
                this.flowRemainingThisTick =  flowWindow / 4;
                this.maxFlowPerStep = flowWindow / 16;
            }
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
    private int getEqualizingFlow(int availableFluidUnits, int surfaceHigh, int surfaceLow)
    {    
        int flow = (surfaceHigh - surfaceLow) / 2;
        
        if(this.maxFlowPerStep < this.flowRemainingThisTick)
        {
            // maxFlow is the constraint
            if(flow < availableFluidUnits)
            {
                return flow <= this.maxFlowPerStep ? flow : this.maxFlowPerStep;
            }
            else
            {
                return availableFluidUnits <= this.maxFlowPerStep ? availableFluidUnits : this.maxFlowPerStep;
            }
        }
        else
        {
            // flow remaining is the constraint
            if(flow < availableFluidUnits)
            {
                return flow <= this.flowRemainingThisTick ? flow : this.flowRemainingThisTick;
            }
            else
            {
                return availableFluidUnits <= this.flowRemainingThisTick ? availableFluidUnits : this.flowRemainingThisTick;
            }
        }
    }
    
    /**
     *  Resets lastFlowTick and forces run at least once a tick.
     */
    public void doFirstStep()
    {
//        this.lastFlowTick = newTickIndex;
        this.doStepWork();
        if(this.flowedLastStep)
        {
            int tick = Simulator.instance.getTick();
            this.firstCell.updateTickIndex(tick);
            this.secondCell.updateTickIndex(tick);
        }
    }
    
    public void doStep()
    {
        if(this.flowedLastStep)
        {
            this.doStepWork();
        }
    }
    
//    public static AtomicInteger tryCount = new AtomicInteger(0);
//    public static AtomicInteger successCount = new AtomicInteger(0);
    
//    public static AtomicLong innerTime = new AtomicLong(0);
//    public static AtomicLong outerTime = new AtomicLong(0);
    
//    public static ConcurrentPerformanceCounter perfFlowRate = new ConcurrentPerformanceCounter();
//    public static ConcurrentPerformanceCounter perfFlowAcross = new ConcurrentPerformanceCounter();
    
    /**
     * Guts of doStep.
     */
    private void doStepWork()
    {
    if(this.flowRemainingThisTick < LavaSimulator.MIN_FLOW_UNITS)
    {
        if(this.flowedLastStep) this.flowedLastStep = false;
        return;
    }
        
//        long outerStart = System.nanoTime();
        
        do
        {
//            tryCount.incrementAndGet();
            
            // very high-frequency loop here - so repeating some code to reduce comparisons for performance
            if(this.flowDirection == FlowDirection.ONE_TO_TWO)
            {
                int surface1 = this.firstCell.fluidSurfaceUnits();

                int availableFluidUnits = surface1 - this.firstCell.getSmoothedRetainedUnits();
                if(availableFluidUnits < LavaSimulator.MIN_FLOW_UNITS)
                {
                    if(this.flowedLastStep) this.flowedLastStep = false;
                    break;
                }
                else
                {
                    int surface2 = this.secondCell.fluidSurfaceUnits();

                    if(surface1 > surface2)
                    {
                        int flow = this.getEqualizingFlow(availableFluidUnits, surface1, surface2);
                        if(flow < LavaSimulator.MIN_FLOW_UNITS)
                        {
                            if(this.flowedLastStep) this.flowedLastStep = false;
                            break;
                        }
                        else 
                        {
//                            long innerStart = System.nanoTime();
                            
                            if(this.firstCell.changeLevel(-flow, surface1) && this.secondCell.changeLevel(flow, surface2))
                            {
                                if(!this.flowedLastStep) this.flowedLastStep = true;
                                this.flowRemainingThisTick -= flow;
                                if(ENABLE_FLOW_TRACKING) totalFlow.addAndGet(flow);
                                
//                                innerTime.addAndGet(System.nanoTime() - innerStart);
                                break;
                            }
                            
//                            innerTime.addAndGet(System.nanoTime() - innerStart);
                        }
                    }
                    else
                    {
                        if(this.flowedLastStep) this.flowedLastStep = false;
                        break;
                    }
                }
            }
            else // this.flowDirection == FlowDirection.TWO_TO_ONE
            {
                int surface2 = this.secondCell.fluidSurfaceUnits();

                int availableFluidUnits = surface2 - this.secondCell.getSmoothedRetainedUnits();
                if(availableFluidUnits < LavaSimulator.MIN_FLOW_UNITS)
                {
                    if(this.flowedLastStep) this.flowedLastStep = false;
                    break;
                }
                else
                {
                    int surface1 = this.firstCell.fluidSurfaceUnits();

                    if(surface2 > surface1)
                    {
                        int flow = this.getEqualizingFlow(availableFluidUnits, surface2, surface1);
                        if(flow < LavaSimulator.MIN_FLOW_UNITS)
                        {
                            if(this.flowedLastStep) this.flowedLastStep = false;
                            break;
                        }
                        else 
                        {
//                            long innerStart = System.nanoTime();
                            
                            // flip sign vs above because going from 2 to 1
                            if(this.firstCell.changeLevel(flow, surface1) && this.secondCell.changeLevel(-flow, surface2))
                            {
                                if(!this.flowedLastStep) this.flowedLastStep = true;
                                this.flowRemainingThisTick -= flow;
                                if(ENABLE_FLOW_TRACKING) totalFlow.addAndGet(flow);
                                
//                                innerTime.addAndGet(System.nanoTime() - innerStart);
                                break;
                            }
                            
//                            innerTime.addAndGet(System.nanoTime() - innerStart);
                        }
                    }
                    else
                    {
                        if(this.flowedLastStep) this.flowedLastStep = false;
                        break;
                    }
                }
            }


            return;
            
        } while(true);
        
//        successCount.incrementAndGet();
//        outerTime.addAndGet(System.nanoTime() - outerStart);
    }
    
//    /**
//     * Marks connection for inclusion in next round of processing.
//     */
//    public void setDirty()
//    {
//        this.isDirty = true;
//    }
    
    /** marks connection deleted. Does not release cells */
    public void setDeleted()
    {
        this.isDeleted = true;
    }
    
    @Override
    public boolean isDeleted()
    {
        return this.isDeleted;
    }
    
    public boolean isValid()
    {
        return !this.isDeleted && this.firstCell.canConnectWith(this.secondCell);
    }
    
    /** true if either cell has fluid */
    public boolean isActive()
    {
        return this.firstCell.getFluidUnits() > 0 || this.secondCell.getFluidUnits() > 0;
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

    public FlowDirection flowDirection()
    {
        return this.flowDirection;
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
    
    //TODO: use this to reduce sorting overhead or eliminate it and next method
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