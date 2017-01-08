package grondag.adversity.feature.volcano.lava;

import grondag.adversity.Adversity;
import grondag.adversity.library.Useful;

public class LavaCellConnection
{
    private static int nextConnectionID = 0;
    
    /** by convention, first cell will have the lower-valued coordinate (x or z) */
    public final LavaCell firstCell;
    
    /** by convention, second cell will have the higher-valued coordinate (x or z) */
    public final LavaCell secondCell;
    
    public final int id;
    
    private final int rand = Useful.SALT_SHAKER.nextInt(64);
    
    private float flowThisTick = 0;
    private int lastFlowTick = 0;
    
    private float currentFlowRate = 0;
    
 
    private final static float PRESSURE_PER_LEVEL = 0.05F;
    public final static float INVERSE_PRESSURE_FACTOR = 1F/(PRESSURE_PER_LEVEL + 1);
    
    //TODO: make configurable?
    /** Maximum flow through any block connection in a single tick. 
     * Not changing this to vary with pressure because most lava flows are 
     * along the surface and higher-velocity flows (down a slope) will also
     * have a smaller cross-section due to retained height calculations.
     */
    private final static float MAX_FLOW_PER_TICK = 0.1F;
    
    private final boolean isVertical;
    
    public LavaCellConnection(LavaCell firstCell, LavaCell secondCell)
    {

        
        // TODO: remove
//        if((firstCell.pos.getX() == 70 && firstCell.pos.getY() == 79 && firstCell.pos.getZ() == 110) ||(secondCell.pos.getX() == 70 && secondCell.pos.getY() == 79 && secondCell.pos.getZ() == 110))
//            Adversity.log.info("boop");
        
        this.id = nextConnectionID++;
        
//        Adversity.log.info("connection create");
        firstCell.retain("connection");
        secondCell.retain("connection");
        
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
    private float getVerticalFlow(LavaSimulator sim)
    {
        // Nothing to do if top cell is empty unless have upwards pressure
        if(secondCell.getFluidAmount() == 0 && firstCell.getFluidAmount() <= 1) return 0;
        
        float firstCellAdjustedLevel = firstCell.getFluidAmount();
        // add floor of an empty first cell that will become melted if we flow down into it
        if(firstCellAdjustedLevel == 0 && firstCell.getFloor() > 0) 
        {
            firstCellAdjustedLevel += firstCell.getFloor();
        }
        
        float totalAmount = firstCellAdjustedLevel + secondCell.getFluidAmount();
        
        // no need to constrain vertical flows if everything can flow into bottom block
        // But don't flow down if lower cell is an empty drop cell.
        // This implies upper cell is also a drop cell - wait for particles.
        if(totalAmount <= 1) // + MINIMUM_CELL_CONTENT)
        {
            return (firstCell.getFluidAmount() == 0 && firstCell.isDrop(sim)) ? 0 : -secondCell.getFluidAmount();
        }
        else if(totalAmount < 2 + PRESSURE_PER_LEVEL)
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
            
            float newUpperLevel = (totalAmount - 1) * INVERSE_PRESSURE_FACTOR;
            
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
            
            float newUpperLevel = (totalAmount - PRESSURE_PER_LEVEL) * 0.5F;
            
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
    private float getHorizontalFlow(LavaSimulator sim)
    {
        float level1 = Math.max(this.firstCell.getFluidAmount(), this.firstCell.getFloor());
        float level2 = Math.max(this.secondCell.getFluidAmount(), this.secondCell.getFloor());

        // For horizontal connection, flow is always towards cell with no bottom
        // and if neither has a bottom, there is no flow.
        if(!this.firstCell.isSupported(sim)) level1 = 0;
        if(!this.secondCell.isSupported(sim)) level2 = 0;
        
        float difference = level1 - level2;
        
        if(difference == 0) return 0;
        
        
        /**
         * If both cells have a non-zero retention level 
         * and donatating cell has more than half its retention level already,
         * then will donate enough to bring target closer to its half-retention level,
         * so that DonorLevel/DonorRetention = 2 * TargetLevel/TargetRetention.  
         * This prevents steep edges of flows.
         */
        float retention1 = this.firstCell.getRetainedLevel();
        float retention2 = this.secondCell.getRetainedLevel();
        boolean dropFlag = false;
        
        // Positive numbers means 1st cell has higher pressure.
        if(difference > 0)
        {
            //if 1st cell is solid, nothing to do
            if(this.firstCell.getFluidAmount() == 0) return 0;
            
            dropFlag = this.secondCell.isDrop(sim);
            
            //see note on retention level above
            if(!dropFlag && retention1 > 0 && retention2 > 0 && level1 > Math.max(0.5F * retention1, 0.5F)  && level2 < 0.5F * retention2 )
            {
                float ratio = retention1 / retention2;
                float total = level1 + level2;
                float newLevel1 = 2 * total * ratio / (1 + 2 * ratio);
                newLevel1 = Math.max(newLevel1, Math.max(0.5F * retention2, 0.5F));
                
                return level1 - newLevel1;
            }
            else
            {
                //otherwise just donate anything above my retention level
                float bound = level1 - this.firstCell.getRetainedLevel();
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
            if(!dropFlag && retention1 > 0 && retention2 > 0 && level2 > Math.max(0.5F * retention2, 0.5F)  && level1 < 0.5F * retention1 )
            {
                float ratio = retention2 / retention1;
                float total = level1 + level2;
                float newLevel2 = 2 * total * ratio / (1 + 2 * ratio);
                newLevel2 = Math.max(newLevel2, Math.max(0.5F * retention2, 0.5F));
                
                return -(level2 - newLevel2);
            }
            else
            {
                float bound = level2 - this.secondCell.getRetainedLevel();
                if(bound <= 0) return 0;
                // flow is negative in this case, so need to flip application of bound
                difference = Math.max(difference, -bound);
            }
        }
        
        // Donate full amount if going to a drop cell/particle.
        // Otherwise split the difference to average out the pressure
        return dropFlag? difference : difference * 0.5F;
        

    }
    
    public void updateFlowRate(LavaSimulator sim)
    {
        float flow;
        // barriers don't need processing - TODO: may not be needed because checked in connection processing loop
        if(this.firstCell.isBarrier() || this.secondCell.isBarrier())
        {
            flow = 0;
        }
        else if(this.isVertical)
        {
            flow = this.getVerticalFlow(sim);    
          //Damp tiny oscillations, but always allow downward flow
            if(flow > 0 && flow < 0.001) flow = 0;
        }
        else
        {
            flow = this.getHorizontalFlow(sim);
            
            //Damp tiny oscillations
            //TODO: make threshold configurable
            if(Math.abs(flow) < 0.001) flow = 0;
        }
        this.currentFlowRate = flow;

    }
    
    public void doStep(LavaSimulator sim)
    {
        // TODO: Particle output
        
        // TODO: remove
//        if((this.firstCell.pos.getX() == 70 && firstCell.pos.getY() == 79 && firstCell.pos.getZ() == 110) ||(this.secondCell.pos.getX() == 70 && secondCell.pos.getY() == 79 && secondCell.pos.getZ() == 110))
//            Adversity.log.info("boop");
        
        //TODO: make this local again if going to update each time
        this.updateFlowRate(sim);
        
        if (this.currentFlowRate == 0) return;
        
        float flow = this.currentFlowRate;
        
        //TODO: make bound configurable
         
        // Positive numbers means 1st cell has higher pressure.
        if(flow > 0)
        {
            flow = Math.min(flow, MAX_FLOW_PER_TICK - this.getFlowThisTick(sim));
        }
        else
        {
            // flow is negative in this case, so need to flip handling of bound
            flow = Math.max(flow, -MAX_FLOW_PER_TICK - this.getFlowThisTick(sim));
        }
        
        this.flowAcross(sim, flow);
    }
    
    public void flowAcross(LavaSimulator sim, float flow)
    {
        // shouldn't be needed but was getting zeros here - maybe floating-point weirdness?
        if(flow ==0) return;
        
        if(sim.getTickIndex() != this.lastFlowTick)
        {
            lastFlowTick = sim.getTickIndex();
            this.flowThisTick = flow;
        }
        else
        {
            this.flowThisTick += flow;
        }
        
        this.firstCell.changeLevel(sim, -flow, false);
        this.secondCell.changeLevel(sim, flow, false);

        //TODO: remove
//        if(!this.isVertical && this.firstCell.getCurrentLevel() >= this.firstCell.getRetainedLevel() && this.firstCell.getCurrentLevel() + this.firstCell.getDelta() < this.firstCell.getRetainedLevel())
//        {
//            Adversity.log.info("DERP!!");
//        }
//        if(!this.isVertical && this.secondCell.getCurrentLevel() >= this.secondCell.getRetainedLevel() && this.secondCell.getCurrentLevel() + this.secondCell.getDelta() < this.secondCell.getRetainedLevel())
//        {
//            Adversity.log.info("DERP!!");
//        }
        
        this.firstCell.applyUpdates(sim);
        this.secondCell.applyUpdates(sim);

        sim.setSaveDirty(true);
    }
    
    public float getFlowThisTick(LavaSimulator sim)
    {
        return sim.getTickIndex() == this.lastFlowTick ? this.flowThisTick : 0;
    }

    /**
     * Call when removing this connection so that cell references can be removed if appropriate.
     */
    public void releaseCells()
    {
        
        // TODO: remove
        if((this.firstCell.pos.getX() == 70 && firstCell.pos.getY() == 79 && firstCell.pos.getZ() == 110) ||(this.secondCell.pos.getX() == 70 && secondCell.pos.getY() == 79 && secondCell.pos.getZ() == 110))
            Adversity.log.info("boop");
        
        
//        Adversity.log.info("connection release");
        this.firstCell.release("connection");
        this.secondCell.release("connection");
    }
    
    /** 
     * Absolute difference in base elevation, or if base is same, in retained level.
     * Zero if there is no difference.
     * Horizontal cells above the ground have a drop of 0.
     * Vertical cells have a drop of 1.
     * Higher drop means higher priority for flowing. 
     */
    public float getDrop()
    {
        return this.isVertical ? 1 : Math.abs(firstCell.getRetainedLevel() - secondCell.getRetainedLevel());
    }
    
    public int getSortKey()
    {
        int axisBit = this.isVertical ? 0 : 1;
        int y = 255 - this.firstCell.pos.getY();
        int slope = 63 - (int) (63F * this.getDrop());
        return (axisBit << 20) | (y << 12) | (slope << 6) | this.rand;
    }
    
    public float getCurrentFlowRate()
    {
        return this.currentFlowRate;
    }
}