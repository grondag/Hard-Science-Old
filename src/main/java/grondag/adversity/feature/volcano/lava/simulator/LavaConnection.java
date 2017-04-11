package grondag.adversity.feature.volcano.lava.simulator;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import grondag.adversity.Adversity;
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
    
    private SortBucket sortBucket;
    
    private SortBucket lastSortBucket;
    
    private boolean isFlowEnabled = false;
    
    /** if true, flow is from 1 to 2, if false, is from 2 to 1 */
    private boolean isDirectionOneToTwo = true;
    
    private int flowReversalTick = 0;
    
    private boolean flowedLastStep = false;
    
    private boolean isDeleted = false;
 
    private int flowRemainingThisTick;
    private int maxFlowPerStep;
    
    private int floorUnitsFrom;
    private int floorUnitsTo;
    private int volumeUnitsFrom;
    private int volumeUnitsTo;
    private boolean isToLowerThanFrom;
    private int dualPressureThreshold;
    private int singlePressureThreshold;
    
    
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
        if(this.isDeleted) 
        {
            if(this.isFlowEnabled) this.isFlowEnabled = false;
            return;
        }
        
        int floorUnits1 = this.firstCell.floorUnits();
        int volumeUnits1 = this.firstCell.ceilingUnits() - floorUnits1;
        
        int floorUnits2 = this.secondCell.floorUnits();
        int volumeUnits2 = this.secondCell.ceilingUnits() - floorUnits2;
        
        int fluidUnits1 = this.firstCell.fluidUnits();
        int fluidUnits2 = this.secondCell.fluidUnits();
        
        int surface1 = AbstractLavaCell.pressureSurface(floorUnits1, volumeUnits1, fluidUnits1);
        int surface2 = AbstractLavaCell.pressureSurface(floorUnits2, volumeUnits2, fluidUnits2);
        
        if(surface1 == surface2)
        {
            //should not flow
            if(this.isFlowEnabled) this.isFlowEnabled = false;
        } 
        
        else if(surface1 > surface2)
        {
            if(this.shouldFlowThisTick(this.firstCell, this.secondCell, surface1, surface2, this.isDirectionOneToTwo))
            {
                this.isDirectionOneToTwo = true;
                this.isFlowEnabled = true;
                this.floorUnitsFrom = floorUnits1;
                this.floorUnitsTo = floorUnits2;
                this.volumeUnitsFrom = volumeUnits1;
                this.volumeUnitsTo = volumeUnits2;
                this.setPressureThresholds();
            }
            else
            {
                this.isFlowEnabled = false;
            }
        }
        else
        {
            if(this.shouldFlowThisTick(this.secondCell, this.firstCell, surface2, surface1, !this.isDirectionOneToTwo))
            {
                this.isDirectionOneToTwo = false;
                this.isFlowEnabled = true;
                this.floorUnitsFrom = floorUnits2;
                this.floorUnitsTo = floorUnits1;
                this.volumeUnitsFrom = volumeUnits2;
                this.volumeUnitsTo = volumeUnits1;
                this.setPressureThresholds();
            }
            else
            {
                this.isFlowEnabled = false;
            }
        }
    }
    
    private void setPressureThresholds()
    {
        int ceilFrom = this.floorUnitsFrom + this.volumeUnitsFrom;
        int ceilTo = this.floorUnitsTo + this.volumeUnitsTo;
        
        if(ceilFrom > ceilTo)
        {
            this.isToLowerThanFrom = true;
            this.dualPressureThreshold = AbstractLavaCell.dualPressureThreshold(this.floorUnitsFrom, this.volumeUnitsFrom, this.floorUnitsTo, this.volumeUnitsTo);
            this.singlePressureThreshold = AbstractLavaCell.singlePressureThreshold(this.floorUnitsFrom, this.floorUnitsTo, ceilTo);
        }
        else
        {
            this.isToLowerThanFrom = false;
            this.dualPressureThreshold = AbstractLavaCell.dualPressureThreshold(this.floorUnitsTo, this.volumeUnitsTo, this.floorUnitsFrom, this.volumeUnitsFrom);
            this.singlePressureThreshold = AbstractLavaCell.singlePressureThreshold(this.floorUnitsFrom, this.floorUnitsTo, ceilFrom);
        }
        
    }
    
    /**
     * Returns true if connectio should be allowed to flow from high to low
     */
    private boolean shouldFlowThisTick(LavaCell cellHigh, LavaCell cellLow, int surfaceHigh, int surfaceLow, boolean sameDirection)
    {
        int min = sameDirection ? 2 : LavaSimulator.FLUID_UNITS_PER_LEVEL;
        int diff = surfaceHigh - surfaceLow;
        if(diff < min || cellHigh.isEmpty())
        {
            //not enough lava to flow
            return false;
        }
     
        int flowWindow = Math.min(surfaceHigh, cellLow.ceilingUnits()) - Math.max(cellHigh.floorUnits(), cellLow.floorUnits());
        
        if(flowWindow < LavaSimulator.FLUID_UNITS_PER_LEVEL)
        {
            //cross-section too small
            return false;
        }
           
     
        this.flowRemainingThisTick =  flowWindow / 4;
        this.maxFlowPerStep = flowWindow / 16;
        return true;
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
    private int getConstrainedFlow(int availableFluidUnits, int flow)
    {    
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
        if(this.flowRemainingThisTick < 1 || this.isDeleted)
        {
            if(this.flowedLastStep) this.flowedLastStep = false;
            return;
        }

        //        long outerStart = System.nanoTime();
        //            tryCount.incrementAndGet();

        // very high-frequency loop here - so repeating some code to reduce comparisons for performance
        if(this.isDirectionOneToTwo)
        {
            this.doStepFromTo(this.firstCell, this.secondCell);
        }
        else
        {
            this.doStepFromTo(this.secondCell, this.firstCell);
        }

        //        successCount.incrementAndGet();
        //        outerTime.addAndGet(System.nanoTime() - outerStart);
    }
    
    private void doStepFromTo(LavaCell cellFrom, LavaCell cellTo )
    {
        for(int i = 0; i < 10; i++)
        {
            int fluidFrom = cellFrom.fluidUnits();
            
            int availableFluidUnits = fluidFrom - cellFrom.getSmoothedRetainedUnits();
            
            if(availableFluidUnits < LavaSimulator.MIN_FLOW_UNITS)
            {
                if(this.flowedLastStep) this.flowedLastStep = false;
                return;
            }
            else
            {
                if(this.tryFlow(cellFrom, cellTo, fluidFrom, availableFluidUnits)) return;
            }
        }        
    }
    
    /** 
     * Returns amount that should flow from "from" cell to "to" cell to equalize pressure.
     * 
     * Definitions  
     *      c   Pressure Factor (constant)
     *      t   total lava (invariant)
     *      Pa Pb   pressurized fluid surface
     *      Sa  Sb  normal fluid surface (bounded by cell ceiling)
     *      Fa Fb   column floor (fixed)
     *      Ua Ub   normal fluid units (bounded by cell volume)
     *      Xa Xb   extra (pressure) fluid units
     *      
     *      
     *      Sa = Fa + Ua, Sb = Fb + Ub  compute normal surfaces
     *      Pa = Sa + cXa, Pb = Sb + cXb    compute pressure surffaces
     *      Pa = Fa + Ua + cXa, Pb = Fb + Ub + cXb  expand pressure surface forumla
     *      
     *  1   t = Ua + Ub + Xa + Xb   conservation of fluid
     *  2   Pa = Pb equalize pressurized fluid surface
     *  3   Fa + Ua + cXa = Fb + Ub + cXb   expanded equalization formula
     *          
     *      Single Column Under Pressure    
     *  4   t = Ua + Ub + Xb    If b has pressure (Ub is fixed and Xa=0)
     *  5   Fa + Ua = Fb + Ub + cXb     
     *  6   Ua = Fb + Ub + cXb - Fa rearrange #5
     *  7   t = Fb + Ub + cXb - Fa + Ub + Xb    substitue #6 into #4
     *  8   t = Fb - Fa + 2Ub + (c+1)Xb simplify
     *  9   Xb = (t + Fa - Fb - 2Ub)/(c+1)  solve for Xb, then use #6 to obtain Ua
     */
    private int singlePressureFlow(int fluidTo, int fluidFrom, int fluidTotal)
    {
        // Single pressure flow is not symmetrical.
        // Formula assumes that the lower cell is full at equilibrium.
        // "Lower" means lowest ceiling.
        
        int newFluidFrom;
        
        if(this.isToLowerThanFrom)
        {
            // flowing from upper cell into lower, creating pressure in lower cell
            // "to" cell corresponds to subscript "b" in formula.
            final int pressureUnitsLow = (fluidTotal + this.floorUnitsFrom - this.floorUnitsTo - 2 * this.volumeUnitsTo) / AbstractLavaCell.PRESSURE_FACTOR_PLUS;
          
            newFluidFrom = fluidTotal - this.volumeUnitsTo - pressureUnitsLow;
        }
        else
        {
            // "from" cell corresponds to subscript "b" in formula.
            // flowing from lower cell into upper, relieving pressure in lower cell
            
            // adding pressure factor to numerator so that we round up the result without invoking floating point math
            // Rounding up so that we don't allow the new pressure surface of "from" cell to be lower than the "to" cell.
            final int pressureUnitsLow = (fluidTotal + this.floorUnitsTo - this.floorUnitsFrom - 2 * this.volumeUnitsFrom + AbstractLavaCell.PRESSURE_FACTOR) / AbstractLavaCell.PRESSURE_FACTOR_PLUS;
            
            newFluidFrom = this.volumeUnitsFrom + pressureUnitsLow;
            
        }
        
        //TODO: clean up
        int flow = fluidFrom - newFluidFrom;

        if(flow < 0)
            Adversity.log.debug("derp!");
        return flow;
    }
    
    /** 
     * Returns amount that should flow from "from" cell to "to" cell to equalize pressure.
     * 
     * Definitions  
     *      c   Pressure Factor (constant)
     *      t   total lava (invariant)
     *      Pa Pb   pressurized fluid surface
     *      Sa  Sb  normal fluid surface (bounded by cell ceiling)
     *      Fa Fb   column floor (fixed)
     *      Ua Ub   normal fluid units (bounded by cell volume)
     *      Xa Xb   extra (pressure) fluid units
     *      
     *      
     *      1    t = Ua + Ub + Xa + Xb   conservation of fluid, Ua and Ub are fixed
     *      2   Pa = Pb equalize pressurized fluid surface
     *      3   Fa + Ua + cXa = Fb + Ub + cXb   expanded equalization formula
     *      
     *              
     *      6   Xb = t - Ua - Ub - Xa   rearrange #1
     *      7   cXa = Fb + Ub - Fa - Ua +cXb    rearrange #3
     *      8   cXa = Fb + Ub - Fa - Ua +c(t - Ua - Ub - Xa)    substitute #6 into #3
     *      9   cXa = Fb + Ub - Fa - Ua +ct - cUa - cUb - cXa   
     *      10  2cXa = Fb - Fa + Ub - cUb - Ua - cUa +ct    
     *      11  2cXa = Fb - Fa + (1-c)Ub - (c+1)Ua + ct 
     *      12  Xa = (Fb - Fa + (1-c)Ub - (c+1)Ua + ct) / 2c    solve for Xa, then use #6 to obtain Xb
     */
    private int dualPressureFlow(int fluidTo, int fluidFrom, int fluidTotal)
    {        
        // Does not matter which cell has higher ceiling when both are under pressure. 
        // Assigning "from" cell to subscript a in formula.
        
        int fromPressureUnits = (this.floorUnitsTo - this.floorUnitsFrom 
                + (1 - AbstractLavaCell.PRESSURE_FACTOR) * this.volumeUnitsTo
                - AbstractLavaCell.PRESSURE_FACTOR_PLUS * this.volumeUnitsFrom
                + AbstractLavaCell.PRESSURE_FACTOR * fluidTotal

                // Adding PRESSURE_FACTOR to numerator term rounds up without floating point math
                // This ensure "from cell" does not flow so much that its's effective surface is below the "to cell."
                // If this happened it could lead to oscillation that would prevent cell cooling and waste CPU.
                + AbstractLavaCell.PRESSURE_FACTOR) / AbstractLavaCell.PRESSURE_FACTOR_X2;
        
        return fluidFrom - this.volumeUnitsFrom - fromPressureUnits;
    }
    
    /** 
     * Returns amount that should flow from "from" cell to "to" cell to equalize level in absence of pressure in either cell.
     * 
     * Definitions  
     *      t   total lava (invariant)
     *      Fa Fb   column floor (fixed)
     *      Ua Ub   normal fluid units (bounded by cell volume)
     *      
     *      1    t = Ua + Ub            conservation of fluid
     *      2   Fa + Ua = Fb + Ub       equalization condition
     *      
     *              
     *      3   Ub = t - Ua             rearrange #1
     *      4   Ua = Fb - Ub - Fa       rearrange #2
     *      
     *      5   Ua = Fb + t - Ua - Fa   substitute #3 into #4
     *      
     *      5   2Ua = Fb - Fa + t
     *      6   Ua = (Fb - Fa + t) / 2
     */
    private int freeFlow(int fluidTo, int fluidFrom, int fluidTotal)
    {        
        // Assigning "from" cell to subscript a in formula.
        // Adding 1 to round up without floating point math
        // This ensure "from cell" does not flow to level 1 below to cell.
        return fluidFrom - (this.floorUnitsTo - this.floorUnitsFrom + fluidTotal + 1) / 2;
    }
    
    /** 
     * Return true if complete, false if should retry.
     * True does NOT mean there was a flow, only that retry isn't needed.
     */
    private boolean tryFlow(LavaCell cellFrom, LavaCell cellTo, int fluidFrom, int availableFluidUnits)
    {
        int fluidTo = cellTo.fluidUnits();
        int surfaceTo = AbstractLavaCell.pressureSurface(this.floorUnitsTo, this.volumeUnitsTo, fluidTo);
        int surfaceFrom = AbstractLavaCell.pressureSurface(this.floorUnitsFrom, this.volumeUnitsFrom, fluidFrom);
        if(surfaceFrom > surfaceTo)
        {
            int fluidTotal = fluidTo + fluidFrom;
            int flow;
            
            if(fluidTotal > this.dualPressureThreshold)
            {
                flow = this.getConstrainedFlow(availableFluidUnits, this.dualPressureFlow(fluidTo, fluidFrom, fluidTotal));
            }
            else if(fluidTotal > this.singlePressureThreshold)
            {
                flow = this.getConstrainedFlow(availableFluidUnits, this.singlePressureFlow(fluidTo, fluidFrom, fluidTotal));
            }
            else
            {
                // TODO: can't use diff of surfaces here because may include pressure units in total
                // have to instead subtract volume below lowest common floor and then split remainder
                flow = this.getConstrainedFlow(availableFluidUnits, this.freeFlow(fluidTo, fluidFrom, fluidTotal));
            }
            
            //TODO: remove
            int newSurfaceLow = AbstractLavaCell.pressureSurface(this.floorUnitsTo, this.volumeUnitsTo, fluidTo + flow);
            int newSurfaceHigh = AbstractLavaCell.pressureSurface(this.floorUnitsFrom, this.volumeUnitsFrom, fluidFrom - flow);
            if(newSurfaceLow > newSurfaceHigh)
            {
                Adversity.log.info("Probable oscillation detected");
            }

            if(flow < LavaSimulator.MIN_FLOW_UNITS)
            {
                if(this.flowedLastStep) this.flowedLastStep = false;
                return true;
            }
            else 
            {
                if(cellFrom.changeFluidUnitsIfMatches(-flow, fluidFrom))
                {
                    if(cellTo.changeFluidUnitsIfMatches(flow, fluidTo))
                    {
//                        Adversity.log.info("flow " + flow + " from " + cellFrom.x() + ", " + cellFrom.z() + " to " + cellTo.x() + ", " + cellTo.z());
                        
                        if(!this.flowedLastStep) this.flowedLastStep = true;
                        this.flowRemainingThisTick -= flow;
                        if(ENABLE_FLOW_TRACKING) totalFlow.addAndGet(flow);
                        return true;
                    }
                    else
                    {
                        //undo first change if second isn't successful
                        cellFrom.changeFluidUnits(flow);
                        return false;
                    }
                }
                else
                {
                    return false;
                }
            }
        }
        else
        {
            if(this.flowedLastStep) this.flowedLastStep = false;
            return true;
        }
    }

    
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
        return this.firstCell.fluidUnits() > 0 || this.secondCell.fluidUnits() > 0;
    }
    
    /** 
     * Absolute difference in base elevation, or if base is same, in retained level.
     * Measured in fluid levels
     * Zero if there is no difference or if either block is a barrier.
     * Higher drop means higher priority for flowing. 
     */
    public int getSortDrop()
    {
        return Math.abs(this.firstCell.floorLevel() - this.secondCell.floorLevel());
    }

    public boolean isDirectionOneToTwo()
    {
        return this.isDirectionOneToTwo;
    }
    
    public boolean isFlowEnabled()
    {
        return this.isFlowEnabled && !this.isDeleted;
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