package grondag.adversity.feature.volcano.lava;

import grondag.adversity.Adversity;

public class LavaCellConnectionHorizontal extends LavaCellConnection
{
    protected LavaCellConnectionHorizontal(LavaCell firstCell, LavaCell secondCell, long packedConnectionPos)
    {
        super(firstCell, secondCell, packedConnectionPos);
    }
    
    /**
     * Get flow between first and second cell on this connection,
     * Zero means no flow.
     * Positive numbers means 1st cell has higher pressure (flow from lower to higher coordinate)
     * Positive numbers result in flow from 1st to 2nd.
     * Negative numbers mean 2nd cell has higher pressure (flow from higher to lower coordinate).
     * Negative numbers result in flow from 2nd to 1st.
     */
    @Override
    protected int getFlowRate(LavaSimulator sim)
    {
        // Assume flow will not change direction within the same tick
        // and abort if the difference is less than the minimum internal flow.
        int absoluteMaxFlow =
                this.flowThisTick == 0
                ? absoluteMaxFlow = MAX_HORIZONTAL_FLOW_PER_TICK
                : MAX_HORIZONTAL_FLOW_PER_TICK - Math.abs(this.flowThisTick);
    
        // check against the most restrictive case and abort if there can be no flow
        if(absoluteMaxFlow < MINIMUM_INTERNAL_FLOW_UNITS) return 0;

        BottomType type1 = this.firstCell.getBottomType(sim);
        BottomType type2 = this.secondCell.getBottomType(sim);

        switch(type1)
        {
            case SUPPORTING:
                switch(type2)
                {
                    case SUPPORTING:
                        // both cells are supported from below, can equalize pressure
                        return getEqualizedSupportedFlow(absoluteMaxFlow);
                        
                    case DROP:
                    {
                        // 1st cell is supported from below, 2nd is a drop.
                        // 1st can donate excess to to 2nd if has enough
                        
                        // if connection is maxed out, abort - no point in continuing
                        if(absoluteMaxFlow < LavaCell.FLUID_UNITS_PER_LEVEL) return 0;
                        
                        // compute amount able to donate
                        int flow = firstCell.getFluidAmount() - firstCell.getRetainedLevel();
                        
                        // confirm is enough for a particle
                        if(flow < LavaCell.FLUID_UNITS_PER_LEVEL) return 0;
                        
                        // limit to per-tick max - guaranteed to be at least LavaCell.FLUID_UNITS_PER_LEVEL or we wouldn't be here
                        if(flow > absoluteMaxFlow) flow = absoluteMaxFlow;
                        
                        //flowing 1st to 2nd, so positive sign
                        return flow;
                    }
                        
                    case PARTIAL:
                    {
                        return getPartiallySupportedFlow(absoluteMaxFlow, this.firstCell, this.secondCell);
                    }
                    
                    default: // not normally possible
                        return 0;
                }
                
            case DROP:
                switch(type2)
                {
                    case SUPPORTING:
                    {
                        // 2nd cell is supported from below, 1st is a drop.
                        // 2nd can donate excess to to 1st if has enough
                        
                        // if connection is maxed out, abort - no point in continuing
                        if(absoluteMaxFlow < LavaCell.FLUID_UNITS_PER_LEVEL) return 0;
                        
                        // compute amount able to donate
                        int flow = secondCell.getFluidAmount() - secondCell.getRetainedLevel();
                        
                        // confirm is enough for a particle
                        if(flow < LavaCell.FLUID_UNITS_PER_LEVEL) return 0;
                        
                        // limit to per-tick max - guaranteed to be at least LavaCell.FLUID_UNITS_PER_LEVEL or we wouldn't be here
                        if(flow > absoluteMaxFlow) flow = absoluteMaxFlow;
                        
                        //flowing 2nd to 1st, so negative sign
                        return -flow;
                    }
                    
                    case DROP:      //neither cell is supported - no flow
                    case PARTIAL:   //neither cell is supported - no flow
                    default:        // not normally possible
                        return 0;
                }
                
            case PARTIAL:
            {
                switch(type2)
                {
                    case SUPPORTING:
                    {
                        // need to flip sign because flow is from 2 to 1.
                        return -getPartiallySupportedFlow(absoluteMaxFlow, this.secondCell, this.firstCell);
                    }
                    
                    case DROP:      //neither cell is supported - no flow
                    case PARTIAL:   //neither cell is supported - no flow
                    default:        // not normally possible
                        return 0;
                }
            }
            
            default:
                // not normally possible
                return 0;
        
        }
     }

    /** 
     * Computes amount of flow that should occur from supported to partially supported cell. 
     * Will return zero or higher.
     */
    private int getPartiallySupportedFlow(int absoluteMaxFlow, LavaCell supportedCell, LavaCell partiallySupportedCell)
    {
        // 1st cell is supported from below, 2nd has partial support.
        // 1st can donate excess to to 2nd so long as doesn't make higher than self
        // or go below its own retention level.
        
        int fluid1 = supportedCell.getFluidAmount();
        
        // no point if nothing to donate
        if(fluid1 == 0) return 0;
        
        // Assume 2nd cell already has fluid, making it easier to flow - mimics surface tension/self-adhesion
        // Assumption is corrected below if determined that 2nd cell has no fluid.
        int minimumFlow = LavaCellConnection.MINIMUM_INTERNAL_FLOW_UNITS;
        
        int level2 = partiallySupportedCell.getFluidAmount();
        if(level2 == 0) 
        {
            // If the minimum will be ess than our per-tick maximum, we won't be able to flow, so abort.
            // Check at the very top for internal_min won't catch because external_min > internal_min
            if(absoluteMaxFlow < MINIMUM_EXTERNAL_FLOW_UNITS) return 0;
            
            // 2nd cell has no fluid, so harder to flow - mimics surface tension/self-adhesion
            minimumFlow = LavaCellConnection.MINIMUM_EXTERNAL_FLOW_UNITS;
            
            // if no fluid, could still have a floor that becomes melted, acts as fluid for our purpose
            level2 = partiallySupportedCell.getInteriorFloor() * LavaCell.FLUID_UNITS_PER_LEVEL;
        }
        
        // Compute difference.
        // Nominal flow is half the difference - even out the levels
        // If level2 > fluid1 will be negative and thus less than minimum level,
        // resulting in zero return value below.
        int flow = (fluid1 - level2) / 2;
    
        // Prevent flowing below retention level
        int available = fluid1 - supportedCell.getRetainedLevel();
        
        if(available < flow)
        {
            // Constrained by retention
            if(available < minimumFlow) return 0;
            return available > absoluteMaxFlow ? absoluteMaxFlow : available;
        }
        else
        {
            // Flow is not constrained by retention
            if(flow < minimumFlow) return 0;
            return flow > absoluteMaxFlow ? absoluteMaxFlow : flow;
        }
    }
    
    /**
     * Computes flow to equalize levels within constraints when both cells are fully supported from below. 
     * Cells may or may not have fluid - this is determined within routine and handled appropriately.
     * Does not check that they are supported - should only be called when this is known to be the case.
     */
    private int getEqualizedSupportedFlow(int absoluteMaxFlow)
    {
        int fluid1 = this.firstCell.getFluidAmount();
        int fluid2 = this.secondCell.getFluidAmount();
      
        if(fluid1 > 0)
        {
            if(fluid2 > 0)
            {
                // fluid in both cells - equalize
                return this.getEqualizedSupportedFlowDouble(absoluteMaxFlow, fluid1, fluid2);
            }
            else
            {
                // fluid in first cell only
                // sign should remain positive (if non-zero) because going from 1 to 2
                return this.getEqualizedSupportedFlowSingle(absoluteMaxFlow, this.firstCell, fluid1, this.secondCell);
            }
        }
        else
        {
            if(fluid2 > 0)
            {
                // fluid in second cell only
                // sign should be negative (if non-zero) because going from 2 to 1
                return -this.getEqualizedSupportedFlowSingle(absoluteMaxFlow, this.secondCell, fluid2, this.firstCell);
            }
            else
            {
                // No fluid in either cell, nothing to do
                // Should be rare - cells should not remain connected if no fluid in either one.
                return 0;
            }
        }
    }

    private int getEqualizedSupportedFlowSingle(int absoluteMaxFlow, LavaCell fluidCell, int fluidLevel, LavaCell emptyCell)
    {
        // Flowing into cell without fluid - requires more flow to mimc adhesion/surface tension
        // Confirm first that we can meet this threshold.
        if(absoluteMaxFlow < MINIMUM_EXTERNAL_FLOW_UNITS) return 0;
        
        // abort if empty cell has a floor that would prevent 
        int emptyLevel = emptyCell.getInteriorFloor() * LavaCell.FLUID_UNITS_PER_LEVEL;
        if(emptyLevel >= fluidLevel) return 0;

  
        int flow = getDifferentialFlow(fluidLevel, emptyLevel, fluidCell.getRetainedLevel(), emptyCell.getRetainedLevel());

        // Check remaining constraints.
        // Note this block is identical to same block below
        // Duplicated in-line for performance due to frequency of calling
        {
            // has to be big enough
            if(flow < MINIMUM_EXTERNAL_FLOW_UNITS) return 0;

            // can't exceed per-tick max
            if(flow > absoluteMaxFlow) flow = absoluteMaxFlow;         
        }
        
        return flow;
    }
    
    private int getEqualizedSupportedFlowDouble(int absoluteMaxFlow, int fluid1, int fluid2)
    {
        // flow between two cells that both have fluid
        
        // No flow if no difference
        if(fluid1 == fluid2) return 0;
    
        // 1st cell has higher level - flow should be positive or zero
        if(fluid1 > fluid2)
        {

            int flow = getDifferentialFlow(fluid1, fluid2, this.firstCell.getRetainedLevel(), this.secondCell.getRetainedLevel());

            // Check remaining constraints.
            // Note this block is identical to same block below
            // Duplicated in-line for performance due to frequency of calling
            {
                // has to be big enough
                if(flow < MINIMUM_INTERNAL_FLOW_UNITS) return 0;
                
                // can't exceed per-tick max
                if(flow > absoluteMaxFlow) flow = absoluteMaxFlow;         
            }
    
            
            // Result will already be positive, no need to flip sign
            return flow;
            
        }
        else // 2nd cell has higher level - flow should be negative or zero
        {
            
            // difference from above is parameter order
            int flow = getDifferentialFlow(fluid2, fluid1, this.secondCell.getRetainedLevel(), this.firstCell.getRetainedLevel());

            // Check remaining constraints.
            // Note this block is identical to same block above
            // Duplicated in-line for performance due to frequency of calling
            {
                // has to be big enough
                if(flow < MINIMUM_INTERNAL_FLOW_UNITS) return 0;

                // can't exceed per-tick max
                if(flow > absoluteMaxFlow) flow = absoluteMaxFlow;         
            }
    
            
            // Need to flip sign because going from 2 to 1
            return -flow;
        }
    }
    
    /**
     * Core flow computation for equalizing horizontally adjacent cells.
     * The "high" cell should have fluid in it.  Low cell may be a floor or empty or have fluid, 
     * but should (per the name) have a lower level than the high cell.
     */
    private int getDifferentialFlow(int fluidHigh, int levelLow, int retentionHigh, int retentionLow)
    {        
        if(retentionHigh == 0)
        {
            // simplest case - just average out the levels
            return (fluidHigh - levelLow) / 2;
        }
        else 
        {
            // if here, implies cell 1 has a retention level
            if(fluidHigh > retentionHigh)
            {
                // also simple, donate as much as we can without going below our retention level
                int flow = (fluidHigh - levelLow) / 2;
                int max = fluidHigh - retentionHigh;
                return flow > max ? max : flow;
            }
            else if(retentionLow != 0 && fluidHigh > retentionHigh / 2 && levelLow < fluidHigh / 2)
            {
                // If here, both cells have a non-zero retention level. 
                // If donating cell has more than half its retention level already, 
                // then will donate enough to so that newLevel2 = newLevel1 - retentionHigh.  
                // However, won't donate enough to make newLevel2 > newLevel1 / 3 nor higher than retention2, 
                // otherwise 1 would never reach it's own retention due to continued leakage into 2.
                // This prevents steep edges of flows on horizontal surfaces.
                
                /**
                 * newLevelHigh / 2 = newLevelLow
                 * newLevel2 + newLevel2 = totalLevel -> newLevel2 = totalLevel - newLevel1
                 * Substituting:
                 * newLevelHigh / 2 = totalLevel - newLevelHigh
                 * newLevelHigh = 2 * totalLevel - 2 * newLevelHigh
                 * newLevelHigh = 2 * totalLevel / 3;
                 */
                
                int totalLevels = fluidHigh + levelLow;
                int newLevelHigh =  totalLevels * 2 / 3;
                int newLevelLow = totalLevels - newLevelHigh;
                
                // Constrain to retention levels
                if(newLevelHigh < retentionHigh / 2 || newLevelLow > retentionLow / 2)
                {
                    return 0;
                }
                else
                {
                    return fluidHigh - newLevelHigh;
                }
            }
            else
            {
                return 0;
            }
        }
    }
    

    @Override
    public int getSortDrop()
    {
        if(Adversity.DEBUG_MODE)
        {
            if(this.firstCell.isBarrier() || this.secondCell.isBarrier())
            {
                Adversity.log.info("Barrier cell in horizontal connection detected.  Should not normally occur.");
                return 0;
            }
        }
        
        int diff = LavaCell.FLUID_UNITS_PER_LEVEL * Math.abs(firstCell.getDistanceToFlowFloor() - secondCell.getDistanceToFlowFloor());
        
        if(diff == 0)
        {
            return Math.abs(firstCell.getRetainedLevel() - secondCell.getRetainedLevel());
        }
        else
        {
            return diff;
        }
    }
    
    @Override
    public int getOtherDistanceToFlowFloor(LavaCell cellIAlreadyHave)
    {
        if(cellIAlreadyHave == this.firstCell)
        {
            return this.secondCell == null ? 0 : this.secondCell.getDistanceToFlowFloor();
        }
        else
        {
            return this.firstCell == null ? 0 : this.firstCell.getDistanceToFlowFloor();
        }
    }
}
