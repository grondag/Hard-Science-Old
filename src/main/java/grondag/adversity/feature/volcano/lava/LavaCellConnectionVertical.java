package grondag.adversity.feature.volcano.lava;

public class LavaCellConnectionVertical extends LavaCellConnection
{
    protected LavaCellConnectionVertical(LavaCell firstCell, LavaCell secondCell, CellConnectionPos pos)
    {
        super(firstCell, secondCell, pos);
    }


    /**
     * Get flow between first and second cell on this connection.
     * Zero means no flow.
     * Positive numbers means 1st cell has higher pressure (upward flow in vertical connections.)
     * Positive numbers result in flow from 1st to 2nd.
     * Negative numbers mean 2nd cell has higher pressure (downward flow in vertical connections).
     * Negative numbers result in flow from 2nd to 1st.
     */
    @Override
    protected int getFlowRate(LavaSimulator sim)
    {
        int fluid1 = firstCell.getFluidAmount();
        int fluid2 = secondCell.getFluidAmount();
               
        // add floor of an empty first cell that will become melted if we flow down into it
        int level1 = fluid1 == 0 ? firstCell.getFloor() : fluid1;
        
        int totalAmount = level1 + fluid2;
        
        // no need to constrain vertical flows if everything can flow into bottom block
        if(totalAmount <= LavaCell.FLUID_UNITS_PER_BLOCK)
        {
            if(fluid2 == 0) return 0;
            
            // cumulative downward flow this tick would be negative, so adding instead of subtracting
            int max = MAX_DOWNWARD_FLOW_PER_TICK + this.flowThisTick;
            
            if(fluid2 < max)
            {
                return -fluid2;
            }
            else if(max > 0)
            {
                return -max;
            }
            else
            {
                return 0;
            }
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
            
            //want downward flow to result in negative value
            int flow = newUpperLevel - fluid2;
            
            if(flow < 0)
            {
                // downward flow
                
                // cumulative downward flow this tick should be negative unless changing direction, which we want to avoid in same tick
                int max = -MAX_DOWNWARD_FLOW_PER_TICK - this.flowThisTick;
                
                if(flow < max)
                {
                    return max < 0 ? max : 0;
                   
                }
                else
                {
                    return flow;
                }
            }
            else
            {
                //upward or zero flow
                int max = MAX_UPWARD_FLOW_PER_TICK - this.flowThisTick;
                
                if(flow > max)
                {
                    // damp small upward oscillations
                    return max < MINIMUM_INTERNAL_FLOW_UNITS ? 0 : max;
                }
                else
                {
                    // damp small upward oscillations
                    return flow < MINIMUM_INTERNAL_FLOW_UNITS ? 0 : flow;
                }
            }
        }
        else
        {
            // if flow is enough to fill both cells, simply want to equalize pressure, such that
            // (1 + p)u = d  AND u + d = starting_total
            // where    p = pressure per level
            //          d, u = ending down and upper levels
            
            int newUpperLevel = (totalAmount - PRESSURE_PER_LEVEL) / 2;
            
            //want downward flow to result in negative value
            int flow = newUpperLevel - fluid2;
            
            if(flow < 0)
            {
                // downward flow
                
                // cumulative downward flow this tick should be negative unless changing direction, which we want to avoid in same tick
                int max = -MAX_DOWNWARD_FLOW_PER_TICK - this.flowThisTick;
                
                if(flow < max)
                {
                    return max < 0 ? max : 0;
                   
                }
                else
                {
                    return flow;
                }
            }
            else
            {
                //upward or zero flow
                int max = MAX_UPWARD_FLOW_PER_TICK - this.flowThisTick;
                
                if(flow > max)
                {
                    // damp small upward oscillations
                    return max < MINIMUM_INTERNAL_FLOW_UNITS ? 0 : max;
                }
                else
                {
                    // damp small upward oscillations
                    return flow < MINIMUM_INTERNAL_FLOW_UNITS ? 0 : flow;
                }
            }
        }
    }
    
  
    //TODO: is this even needed?
//    @Override
//    public int getDrop()
//    {
//        return LavaCell.FLUID_UNITS_PER_BLOCK;
//    }
//    
}
