package grondag.adversity.feature.volcano.lava;

import grondag.adversity.Adversity;

public class LavaCellConnectionHorizontal extends LavaCellConnection
{
    protected LavaCellConnectionHorizontal(AbstractLavaSimulator sim, LavaCell firstCell, LavaCell secondCell, long packedConnectionPos)
    {
        super(sim, firstCell, secondCell, packedConnectionPos);
    }
    
    private enum FlowType
    {
        EQUALIZING,
        SPREAD_1_TO_2,
        SPREAD_2_TO_1,
        DROP_1_TO_2,
        DROP_2_TO_1,
        NONE
    }
    
    private FlowType flowTypeThisTick;
    
    private int flowMaxThisTick;
    
    private void setupTick(LavaSimulator sim)
    {
        // check against the most restrictive case and abort if there can be no flow
//        if(absoluteMaxFlow < MINIMUM_INTERNAL_FLOW_UNITS) return 0;

        if(this.firstCell.getFluidAmount() == 0 && this.secondCell.getFluidAmount() == 0)
        {
            this.flowTypeThisTick = FlowType.NONE;
            this.flowMaxThisTick = 0;
            return;
        }
        
        BottomType type1 = this.firstCell.getBottomType(sim);
        BottomType type2 = this.secondCell.getBottomType(sim);

        switch(type1)
        {
            case SUPPORTING:
                switch(type2)
                {
                    case SUPPORTING:
                        // both cells are supported from below, can equalize pressure
                        this.flowTypeThisTick = FlowType.EQUALIZING;
                        break;
                        
                    case DROP:
                    {
                        // 1st cell is supported from below, 2nd is a drop.
                        // 1st can donate excess to to 2nd if has enough
                        this.flowTypeThisTick = FlowType.DROP_1_TO_2;
                        break;
                    }
                        
                    case PARTIAL:
                    {
                        this.flowTypeThisTick = FlowType.SPREAD_1_TO_2;
                        break;
                    }
                    
                    default: // not normally possible
                        this.flowTypeThisTick = FlowType.NONE;
                        break;
                }
                break;
                
            case DROP:
                switch(type2)
                {
                    case SUPPORTING:
                    {
                        // 2nd cell is supported from below, 1st is a drop.
                        // 2nd can donate excess to to 1st if has enough
                        this.flowTypeThisTick = FlowType.DROP_2_TO_1;
                        break;
                    }
                    
                    case DROP:      //neither cell is supported - no flow
                    case PARTIAL:   //neither cell is supported - no flow
                    default:        // not normally possible
                        this.flowTypeThisTick = FlowType.NONE;
                        break;
                }
                break;
                
            case PARTIAL:
                {
                    switch(type2)
                    {
                        case SUPPORTING:
                        {
                            this.flowTypeThisTick = FlowType.SPREAD_2_TO_1;
                            break;
                        }
                        
                        case DROP:      //neither cell is supported - no flow
                        case PARTIAL:   //neither cell is supported - no flow
                        default:        // not normally possible
                            this.flowTypeThisTick = FlowType.NONE;
                            break;
                    }
                }
                break;
            
            default:
                // not normally possible
                this.flowTypeThisTick = FlowType.NONE;
                break;
        }
        
        switch(this.flowTypeThisTick)
        {
        case DROP_1_TO_2:
            this.flowMaxThisTick = LavaCellConnection.MAX_UPWARD_FLOW_PER_TICK;
            break;
            
        case DROP_2_TO_1:
            this.flowMaxThisTick = LavaCellConnection.MAX_DOWNWARD_FLOW_PER_TICK;
            break;
            
        case EQUALIZING:
        case SPREAD_1_TO_2:
        case SPREAD_2_TO_1:
            this.flowMaxThisTick = LavaCellConnection.MAX_HORIZONTAL_FLOW_PER_TICK 
                + (LavaCellConnection.MAX_HORIZONTAL_FLOW_PER_TICK * this.getSortDrop() / AbstractLavaSimulator.LEVELS_PER_BLOCK);
            break;
            
        case NONE:
        default:
            this.flowMaxThisTick = 0;
            break;
        
        }
    }
    
    @Override
    public void doFirstStep(LavaSimulator sim)
    {
        this.setupTick(sim);
        super.doFirstStep(sim);
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
        
        if(this.flowMaxThisTick == 0) return 0;

        int absoluteMaxFlow = this.flowMaxThisTick - Math.abs(this.flowThisTick);
        
        if(absoluteMaxFlow < 1) return 0;
        
        switch(this.flowTypeThisTick)
        {
        
        case EQUALIZING:
            return this.getEqualizedSupportedFlow(sim, absoluteMaxFlow);
            
        case SPREAD_1_TO_2:
            return getPartiallySupportedFlow(sim, absoluteMaxFlow, this.firstCell, this.secondCell);
            
        case SPREAD_2_TO_1:
            return -getPartiallySupportedFlow(sim, absoluteMaxFlow, this.secondCell, this.firstCell);
            
        case DROP_1_TO_2:
        {
            // compute amount able to donate
            int flow = firstCell.getFluidAmount() - firstCell.getRawRetainedLevel(sim);
            
            if(flow < 1) return 0;
            
            // limit to per-tick max - guaranteed to be at least LavaCell.FLUID_UNITS_PER_LEVEL or we wouldn't be here
            if(flow > absoluteMaxFlow) flow = absoluteMaxFlow;
            
            //flowing 1st to 2nd, so positive sign
            return flow;
        }
            
        case DROP_2_TO_1:
        {
            // compute amount able to donate
            int flow = secondCell.getFluidAmount() - secondCell.getRawRetainedLevel(sim);
            
            if(flow < 1) return 0;
            
            // limit to per-tick max - guaranteed to be at least LavaCell.FLUID_UNITS_PER_LEVEL or we wouldn't be here
            if(flow > absoluteMaxFlow) flow = absoluteMaxFlow;
            
            //flowing 2nd to 1st, so negative sign
            return -flow;
        }
        
        case NONE:
        default:
            return 0;
        }
    }

    /** 
     * Computes amount of flow that should occur from supported to partially supported cell. 
     * Will return zero or higher.
     */
    private int getPartiallySupportedFlow(LavaSimulator sim, int absoluteMaxFlow, LavaCell supportedCell, LavaCell partiallySupportedCell)
    {
        // 1st cell is supported from below, 2nd has partial support.
        // 1st can donate excess to to 2nd so long as doesn't make higher than self
        // or go below its own retention level.
        
        int fluid1 = supportedCell.getFluidAmount();
        
        // no point if nothing to donate
        if(fluid1 == 0) return 0;
        
        // Prevent flowing below retention level
        int available = fluid1 - supportedCell.getRawRetainedLevel(sim);
        if(available < 1) return 0;
        
        int level2 = partiallySupportedCell.getFluidAmount();
        if(level2 == 0) 
        {
            // if no fluid, could still have a floor that becomes melted, acts as fluid for our purpose
            level2 = partiallySupportedCell.getInteriorFloor() * AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL;
        }
        
        // Compute difference.
        // Nominal flow is half the difference - even out the levels
        // If level2 >= fluid1 will be negative or zero and can abort.
        int flow = (fluid1 - level2) / 2;
        
        if(flow < 1) return 0;
        
        if(available < flow)
        {
            // Constrained by retention
            return available > absoluteMaxFlow ? absoluteMaxFlow : available;
        }
        else
        {
            // Flow is not constrained by retention
            return flow > absoluteMaxFlow ? absoluteMaxFlow : flow;
        }
    }
    
    /**
     * Computes flow to equalize levels within constraints when both cells are fully supported from below. 
     * Cells may or may not have fluid - this is determined within routine and handled appropriately.
     * Does not check that they are supported - should only be called when this is known to be the case.
     */
    private int getEqualizedSupportedFlow(LavaSimulator sim, int absoluteMaxFlow)
    {
        int fluid1 = this.firstCell.getFluidAmount();
        int fluid2 = this.secondCell.getFluidAmount();
      
        if(fluid1 > 0)
        {
            if(fluid2 > 0)
            {
                // fluid in both cells - equalize
                return this.getEqualizedSupportedFlowDouble(sim, absoluteMaxFlow, fluid1, fluid2);
            }
            else
            {
                // fluid in first cell only
                // sign should remain positive (if non-zero) because going from 1 to 2
                return this.getEqualizedSupportedFlowSingle(sim, absoluteMaxFlow, this.firstCell, fluid1, this.secondCell);
            }
        }
        else
        {
            if(fluid2 > 0)
            {
                // fluid in second cell only
                // sign should be negative (if non-zero) because going from 2 to 1
                return -this.getEqualizedSupportedFlowSingle(sim, absoluteMaxFlow, this.secondCell, fluid2, this.firstCell);
            }
            else
            {
                // No fluid in either cell, nothing to do
                // Should be rare - cells should not remain connected if no fluid in either one.
                return 0;
            }
        }
    }

    private int getEqualizedSupportedFlowSingle(LavaSimulator sim, int absoluteMaxFlow, LavaCell fluidCell, int fluidLevel, LavaCell emptyCell)
    {
        // add a margin to solid flows because fluid gets rounded down on cooling and don't
        // want lava flowing back into the space that just cooled
        int emptyLevel = (emptyCell.getInteriorFloor() + 1 ) * AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL - 1;
        // abort if empty cell has a floor that would prevent 
        if(emptyLevel >= fluidLevel) return 0;

  
        int flow = getDifferentialFlow(fluidLevel, emptyLevel, fluidCell.getRawRetainedLevel(sim), emptyCell.getRawRetainedLevel(sim));

        // Check remaining constraints.
      
        // has to be big enough
        if(flow < 1) return 0;

        // can't exceed per-tick max
        if(flow > absoluteMaxFlow) return absoluteMaxFlow;         
        
        return flow;
    }
    
    private int getEqualizedSupportedFlowDouble(LavaSimulator sim, int absoluteMaxFlow, int fluid1, int fluid2)
    {
        // flow between two cells that both have fluid
        
        // No flow if no difference
        if(fluid1 == fluid2) return 0;
    
        // 1st cell has higher level - flow should be positive or zero
        if(fluid1 > fluid2)
        {

            int flow = getDifferentialFlow(fluid1, fluid2, this.firstCell.getRawRetainedLevel(sim), this.secondCell.getRawRetainedLevel(sim));

            // Check remaining constraints.
            
            // has to be something
            if(flow < 1) return 0;
            
            // can't exceed per-tick max
            if(flow > absoluteMaxFlow) return absoluteMaxFlow;         
            
            // Result will already be positive, no need to flip sign
            return flow;
            
        }
        else // 2nd cell has higher level - flow should be negative or zero
        {
            
            // difference from above is parameter order
            int flow = getDifferentialFlow(fluid2, fluid1, this.secondCell.getRawRetainedLevel(sim), this.firstCell.getRawRetainedLevel(sim));

            // Check remaining constraints.
            
            // has to be something
            if(flow < 1) return 0;

            // can't exceed per-tick max
            if(flow > absoluteMaxFlow) return  -absoluteMaxFlow;         
            
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
//            else if(retentionLow != 0 && fluidHigh > retentionHigh / 2 && levelLow < fluidHigh / 2)
//            {
//                // If here, both cells have a non-zero retention level.
//                // If donating cell has more than half its retention level already, 
//                // then will donate enough to so that newLevel2 = newLevel1 - retentionHigh.  
//                // However, won't donate enough to make newLevel2 > newLevel1 / 3 nor higher than retention2, 
//                // otherwise 1 would never reach it's own retention due to continued leakage into 2.
//                // Will also not donate enough to drop below it's own retention level once reached.
//                // This prevents steep edges of flows on horizontal surfaces.
//                
//                /**
//                 * newLevelHigh / 2 = newLevelLow
//                 * newLevel2 + newLevel2 = totalLevel -> newLevel2 = totalLevel - newLevel1
//                 * Substituting:
//                 * newLevelHigh / 2 = totalLevel - newLevelHigh
//                 * newLevelHigh = 2 * totalLevel - 2 * newLevelHigh
//                 * newLevelHigh = 2 * totalLevel / 3;
//                 */
//                
//                int totalLevels = fluidHigh + levelLow;
//                int newLevelHigh =  totalLevels * 2 / 3;
//                int newLevelLow = totalLevels - newLevelHigh;
//                
//                // Constrain to retention levels
//                if(newLevelHigh < retentionHigh / 2 || newLevelLow > retentionLow / 2)
//                {
//                    return 0;
//                }
//                else
//                {
//                    return fluidHigh - newLevelHigh;
//                }
//            }
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
        
        int diff = Math.abs(firstCell.getDistanceToFlowFloor() - secondCell.getDistanceToFlowFloor());
        
//        if(diff == 0)
//        {
//            return Math.min(LavaCell.LEVELS_PER_BLOCK, Math.abs(firstCell.getRetainedLevel() - secondCell.getRetainedLevel()) / LavaCell.FLUID_UNITS_PER_LEVEL);
//        }
//        else
//        {
            return Math.min(AbstractLavaSimulator.LEVELS_PER_BLOCK, diff);
//        }
    }
}
