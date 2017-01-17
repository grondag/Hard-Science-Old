package grondag.adversity.feature.volcano.lava;

import grondag.adversity.Adversity;
import grondag.adversity.library.Useful;
import grondag.adversity.niceblock.base.IFlowBlock;

public class LavaCellConnection
{
    private static int nextConnectionID = 0;
    
    /** by convention, first cell will have the lower-valued coordinate (x or z) */
    public final LavaCell firstCell;
    
    /** by convention, second cell will have the higher-valued coordinate (x or z) */
    public final LavaCell secondCell;
    
    public final int id;
    
    public final int rand = Useful.SALT_SHAKER.nextInt();
    
    private int flowThisTick = 0;
    private int lastFlowTick = 0;
    
    private int sortDrop;
 
    private final static int PRESSURE_PER_LEVEL = LavaCell.FLUID_UNITS_PER_BLOCK / 20;
    
    /** smallest flow into a block that already contains fluid */
    private final static int MINIMUM_INTERNAL_FLOW_UNITS = PRESSURE_PER_LEVEL / 10;
    
    /** smallest flow into a block that has no fluid already - applies to horizontal flow only */
    private final static int MINIMUM_EXTERNAL_FLOW_UNITS = PRESSURE_PER_LEVEL;
    
    private final static int UNITS_PER_ONE_BLOCK_WITH_PRESSURE = LavaCell.FLUID_UNITS_PER_BLOCK + PRESSURE_PER_LEVEL;
    private final static int UNITS_PER_TWO_BLOCKS = LavaCell.FLUID_UNITS_PER_BLOCK * 2 + PRESSURE_PER_LEVEL;
    public final static float INVERSE_PRESSURE_FACTOR = (float)LavaCell.FLUID_UNITS_PER_BLOCK/UNITS_PER_ONE_BLOCK_WITH_PRESSURE;
    
    //TODO: make configurable?
    /** Maximum flow through any block connection in a single tick. 
     * Not changing this to vary with pressure because most lava flows are 
     * along the surface and higher-velocity flows (down a slope) will also
     * have a smaller cross-section due to retained height calculations.
     */
    private final static int MAX_HORIZONTAL_FLOW_PER_TICK = LavaCell.FLUID_UNITS_PER_BLOCK / 10;
    private final static int MAX_UPWARD_FLOW_PER_TICK = MAX_HORIZONTAL_FLOW_PER_TICK / 2;
    private final static int MAX_DOWNWARD_FLOW_PER_TICK = MAX_HORIZONTAL_FLOW_PER_TICK * 2;
    
    public final boolean isVertical;
    
    public LavaCellConnection(LavaCell firstCell, LavaCell secondCell)
    {

        
        // TODO: remove
//        if((firstCell.pos.getX() == 70 && firstCell.pos.getY() == 79 && firstCell.pos.getZ() == 110) ||(secondCell.pos.getX() == 70 && secondCell.pos.getY() == 79 && secondCell.pos.getZ() == 110))
//            Adversity.log.info("boop");
        
        this.id = nextConnectionID++;
        
//        Adversity.log.info("connection create");
//        firstCell.retain("connection");
//        secondCell.retain("connection");
        
        this.isVertical = firstCell.pos.getY() != secondCell.pos.getY();
        
        if(this.isVertical)
        {
            if(firstCell.pos.getY() < secondCell.pos.getY())
            {
                this.firstCell = firstCell;
                this.secondCell = secondCell;
            }
            else
            {
                this.secondCell = firstCell;
                this.firstCell = secondCell;
            }
        }
        else if(firstCell.pos.getX() == secondCell.pos.getX())
        {
            if(firstCell.pos.getZ() < secondCell.pos.getZ())
            {
                this.firstCell = firstCell;
                this.secondCell = secondCell;
            }
            else
            {
                this.secondCell = firstCell;
                this.firstCell = secondCell;
            }
        }
        else 
        {
            if(firstCell.pos.getX() < secondCell.pos.getX())
            {
                this.firstCell = firstCell;
                this.secondCell = secondCell;
            }
            else
            {
                this.secondCell = firstCell;
                this.firstCell = secondCell;
            }
        }
        this.updateSortDrop();
    }
    
    public LavaCell getOther(LavaCell cellIAlreadyHave)
    {
        if(cellIAlreadyHave == this.firstCell)
        {
            return this.secondCell;
        }
        else
        {
            return this.firstCell;
        }
    }
    
    /**
     * Get unconstrained flow between first and second cell on this connection,
     * adjusted for vertical level. Zero means no flow.
     * Positive numbers means 1st cell has higher pressure (upward flow in vertical connections.)
     * Positive numbers result in flow from 1st to 2nd.
     * Negative numbers mean 2nd cell has higher pressure (downward flow in vertical connections).
     * Negative numbers result in flow from 2nd to 1st.
     */
    private int getVerticalFlow(LavaSimulator sim)
    {
        
        // Nothing to do if top cell is empty unless have upwards pressure
        if(secondCell.getFluidAmount() == 0 && firstCell.getFluidAmount() <= LavaCell.FLUID_UNITS_PER_BLOCK) return 0;
        
        int firstCellAdjustedLevel = firstCell.getFluidAmount();
        // add floor of an empty first cell that will become melted if we flow down into it
        if(firstCellAdjustedLevel == 0 && firstCell.getFloor() > 0) 
        {
            firstCellAdjustedLevel += firstCell.getFloor();
        }
        
        int totalAmount = firstCellAdjustedLevel + secondCell.getFluidAmount();
        
        // no need to constrain vertical flows if everything can flow into bottom block
        // REMOVED: But don't flow down if lower cell is an empty drop cell.
        // REMOVED: This implies upper cell is also a drop cell - wait for particles.
        if(totalAmount <= LavaCell.FLUID_UNITS_PER_BLOCK) // + MINIMUM_CELL_CONTENT)
        {
//            return (firstCell.getFluidAmount() == 0 && firstCell.isDrop(sim)) ? 0 : -secondCell.getFluidAmount();
            return -secondCell.getFluidAmount();
        }
        else if(totalAmount < UNITS_PER_TWO_BLOCKS)
        {
            // we want end state to be such that
            // 1 + pu = d  AND u + d = starting_total
            // where    p = pressure per level
            //          d, u = ending down and upper levels
            // 1 + pu = d
            // d = t - u
            
            // 1 + pu = t - u
            // pu + u = t - 1
            // u(p + 1) = t - 1;
            // u = (t-1)/(p + 1)
            
            int newUpperLevel = (totalAmount - LavaCell.FLUID_UNITS_PER_BLOCK) * LavaCell.FLUID_UNITS_PER_BLOCK / UNITS_PER_ONE_BLOCK_WITH_PRESSURE;
            
//            if(newUpperLevel < MINIMUM_CELL_CONTENT) newUpperLevel = 0;
            
            //want downward flow to result in negative value
            return newUpperLevel - secondCell.getFluidAmount();
        }
        else
        {
            // if flow is enough to fill both cells, simply want to equalize pressure, such that
            // (1 + p)u = d  AND u + d = starting_total
            // where    p = pressure per level
            //          d, u = ending down and upper levels
            
            int newUpperLevel = (totalAmount - PRESSURE_PER_LEVEL) / 2;
            
//            if(newUpperLevel < MINIMUM_CELL_CONTENT) newUpperLevel = 0;
            
            //want downward flow to result in negative value
            return newUpperLevel - secondCell.getFluidAmount();
        }
    }
    
    /**
     * Get unconstrained flow between first and second cell on this connection,
     * for horizontally connected cells. Zero means no flow.
     * Positive numbers means 1st cell has higher pressure (flow from lower to higher coordinate)
     * Positive numbers result in flow from 1st to 2nd.
     * Negative numbers mean 2nd cell has higher pressure (flow from higher to lower coordinate).
     * Negative numbers result in flow from 2nd to 1st.
     */
    private int getHorizontalFlow(LavaSimulator sim)
    {
        // For horizontal connection, flow is always towards cell with no bottom
        // and if neither has a bottom, there is no flow.
        int level1 = this.firstCell.isSupported(sim) ? this.firstCell.getFluidAmount() > 0 ? this.firstCell.getFluidAmount() : this.firstCell.getFloor() : 0;
        int level2 = this.secondCell.isSupported(sim) ? this.secondCell.getFluidAmount() > 0 ? this.secondCell.getFluidAmount() : this.secondCell.getFloor() : 0;
        
        int difference = level1 - level2;
        
        if(difference == 0) return 0;
        
//        if(this.firstCell.hashCode() == 4414 || this.secondCell.hashCode() == 4414)
//        {
//            Adversity.log.info("boop");
//        }
        
        /**
         * If both cells have a non-zero retention level 
         * and donatating cell has more than half its retention level already,
         * then will donate enough to bring target closer to its half-retention level,
         * so that DonorLevel/DonorRetention = 2 * TargetLevel/TargetRetention.  
         * This prevents steep edges of flows.
         */
        int retention1 = this.firstCell.getRetainedLevel();
        int retention2 = this.secondCell.getRetainedLevel();
        boolean dropFlag = false;
        
        // Positive numbers means 1st cell has higher pressure.
        if(difference > 0)
        {
            //if 1st cell is solid, nothing to do
            if(this.firstCell.getFluidAmount() == 0) return 0;
            
            dropFlag = this.secondCell.isDrop(sim);
            
            //see note on retention level above
            if(!dropFlag && retention1 > 0 && retention2 > 0 && level1 > Math.max(retention1 / 2, LavaCell.FLUID_UNTIS_PER_HALF_BLOCK)  && level2 < retention2 / 2)
            {
                float ratioDoubled = 2 * retention1 / retention2;
                float total = level1 + level2;
                int newLevel1 =  (int) (total * ratioDoubled / (1 + ratioDoubled));
                newLevel1 = Math.max(newLevel1, Math.max(retention2 / 2, LavaCell.FLUID_UNTIS_PER_HALF_BLOCK));
                
                // If the second cell is already higher than the ideal level for this formula 
                // still don't allow flow from lower cell to higher.  
                // Once case this happens is when the lower has a floor or fluid from an earlier flow.
                return Math.max(0, level1 - newLevel1);
            }
            else
            {
                //otherwise just donate anything above my retention level, up to my fluid amount
                int bound = level1 - this.firstCell.getRetainedLevel();
                if(bound <= 0) return 0;
                difference = Math.min(difference, bound);
            }
        }
        // Negative numbers means 2nd cell has higher pressure.
        else
        {
            //if 2nd cell is solid, nothing to do
            if(this.secondCell.getFluidAmount() == 0) return 0;
            
            dropFlag = this.firstCell.isDrop(sim);
            
            //see note on retention level above
            if(!dropFlag && retention1 > 0 && retention2 > 0 && level2 > Math.max(retention2 / 2, LavaCell.FLUID_UNTIS_PER_HALF_BLOCK)  && level1 < retention1 / 2)
            {
                float ratioDoubled = 2 * retention2 / retention1;
                float total = level1 + level2;
                int newLevel2 = (int) (total * ratioDoubled / (1 + ratioDoubled));
                newLevel2 = Math.max(newLevel2, Math.max(retention2 / 2, LavaCell.FLUID_UNTIS_PER_HALF_BLOCK));
                
                // If the first cell is already higher than the ideal level for this formula 
                // still don't allow flow from lower cell to higher.  
                // One case this happens is when the first cell has a floor or fluid from an earlier flow.
                return Math.min(0, -(level2 - newLevel2));
            }
            else
            {
                int bound = level2 - this.secondCell.getRetainedLevel();
                if(bound <= 0) return 0;
                // flow is negative in this case, so need to flip application of bound
                difference = Math.max(difference, -bound);
            }
        }
        
        // If going to a drop cell/particle donate full amount 
        // unless would result in very small particle. (Do nothing in that case.)
        // If not drop split the difference to average out the pressure
        return dropFlag? Math.abs(difference) >= LavaCell.FLUID_UNITS_PER_LEVEL ? difference : 0 : difference / 2;
        

    }
    
    public static long getFlowRateTime;
    public static int getFlowRateCount;
    
    public static long getHorizontalFlowRateTime;
    public static int getHorizontalFlowRateCount;
    
    public static long getVerticalFlowRateTime;
    public static int getVerticalFlowRateCount;
    
    private int getFlowRate(LavaSimulator sim)
    {
//        getFlowRateCount++;
//        long startTime = System.nanoTime();
        
        // barriers don't need processing - TODO: may not be needed because checked in connection processing loop
        if(this.firstCell.isBarrier() || this.secondCell.isBarrier())
        {
            return 0;
        }
        
        int flow = 0;
        if(this.isVertical)
        {
            // Don't melt a cooled flowblock above us.
            // Won't be detected as a barrier so have to check floor.
            if(secondCell.getFloor() == 0)
            {
                
//                getVerticalFlowRateCount++;
//                long subStart = System.nanoTime();
                flow = this.getVerticalFlow(sim);    
//                getVerticalFlowRateTime += (System.nanoTime() - subStart);
                
                //Damp tiny oscillations, but always allow downward flow
                if(flow > 0 && flow < MINIMUM_INTERNAL_FLOW_UNITS) flow = 0;
            }
            
//            if(flow < -100)
//                Adversity.log.info("boop");
        }
        else
        {
//            getHorizontalFlowRateCount++;
//            long subStart = System.nanoTime();
            flow = this.getHorizontalFlow(sim);
//            getHorizontalFlowRateTime += (System.nanoTime() - subStart);
            
            //Damp tiny oscillations
            //Threshold is higher for flowing into empty blocks.
            //This prevents remelting neighboring cooling blocks that
            //just changed level slight as a result of cooling (happens due to rounding).
            
            if(flow != 0)
            {
                if(flow > 0)
                {
                    //flow from 1st to 2nd
                    if(flow < (this.secondCell.getFluidAmount() == 0 ? MINIMUM_EXTERNAL_FLOW_UNITS : MINIMUM_INTERNAL_FLOW_UNITS)) flow = 0;
                }
                else
                {
                    //negative flow
                    //from 2nd to 1st
                    if(flow > (this.firstCell.getFluidAmount() == 0 ? -MINIMUM_EXTERNAL_FLOW_UNITS : -MINIMUM_INTERNAL_FLOW_UNITS)) flow = 0;
                }
            }
            
        }
        
//        getFlowRateTime += (System.nanoTime() - startTime);
        
        return flow;

    }
    
    public void doStep(LavaSimulator sim)
    {

        
        if(this.lastFlowTick != sim.getTickIndex())
        {
//            if(this.firstCell.hashCode() == 10006 || this.secondCell.hashCode() == 10006)
//                Adversity.log.info("boop");
            
            this.flowThisTick = 0;
            this.lastFlowTick = sim.getTickIndex();
        }
            
        int flow = this.getFlowRate(sim);
        
        if(flow == 0) return;
       
         //TODO: make bound configurable
        // Positive numbers means 1st cell has higher pressure.
        if(flow > 0)
        {
            flow = Math.max(0, Math.min(flow, 
                    (this.isVertical ? MAX_UPWARD_FLOW_PER_TICK : MAX_HORIZONTAL_FLOW_PER_TICK) - this.flowThisTick));
        }
        else
        {
            // flow is negative in this case, so need to flip handling of bound
            flow = Math.min(0, Math.max(flow, 
                    (this.isVertical ? -MAX_DOWNWARD_FLOW_PER_TICK : -MAX_HORIZONTAL_FLOW_PER_TICK) - this.flowThisTick));

        }
        
        if(flow == 0) return;
        
        this.flowAcross(sim, flow);
    }
    
    public void flowAcross(LavaSimulator sim, int flow)
    {
        // shouldn't be needed but was getting zeros here - maybe floating-point weirdness?
//        if(flow ==0) return;
 
        this.flowThisTick += flow;
        this.firstCell.changeLevel(sim, -flow);
        this.secondCell.changeLevel(sim, flow);
    }
    
//    /**
//     * Call when removing this connection so that cell references can be removed if appropriate.
//     */
//    public void releaseCells()
//    {
////        Adversity.log.info("connection release");
//        this.firstCell.release("connection");
//        this.secondCell.release("connection");
//    }
    
    /** 
     * Absolute difference in base elevation, or if base is same, in retained level.
     * Zero if there is no difference.
     * Horizontal cells above the ground have a drop of 0.
     * Vertical cells have a drop of 1.
     * Higher drop means higher priority for flowing. 
     */
    public int getDrop()
    {
        return this.isVertical ? LavaCell.FLUID_UNITS_PER_BLOCK : Math.abs(firstCell.getRetainedLevel() - secondCell.getRetainedLevel());
    }
    
    /**
     * Drop can change on validation, but is also used for sorting.
     * To maintain validity of sort index for retrieval, need to preserve drop
     * value until sort can be properly updated.
     * @return
     */
    public int getSortDrop()
    {
        return this.sortDrop;
    }
    
    /** seems to be needed for perfect consistency between HashMap and TreeSet */
    @Override
    public int hashCode()
    {
        return this.id;
    }
    
    public void updateSortDrop()
    {
        this.sortDrop = this.getDrop();
    }
    
}