package grondag.adversity.feature.volcano.lava;

import grondag.adversity.Adversity;

public class LavaCellConnection
{
    private static int nextConnectionID = 0;
    
    /** by convention, first cell will have the lower-valued coordinate (x or z) */
    public final LavaCell firstCell;
    
    /** by convention, second cell will have the higher-valued coordinate (x or z) */
    public final LavaCell secondCell;
    
    public final int id;
    
    private final static float PRESSURE_PER_LEVEL = 0.05F;
    private final static float INVERSE_PRESSURE_FACTOR = 1F/(PRESSURE_PER_LEVEL + 1);
    
    private final boolean isVertical;
    
    public LavaCellConnection(LavaCell firstCell, LavaCell secondCell)
    {

        this.id = nextConnectionID++;
        
//        Adversity.log.info("connection create");
        firstCell.retain();
        secondCell.retain();
        
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
        if(secondCell.getCurrentLevel() == 0 && firstCell.getCurrentLevel() <= 1) return 0;
        
        float totalAmount = firstCell.getCurrentLevel() + secondCell.getCurrentLevel();
        
        // no need to constrain vertical flows if everything can flow into bottom block
        if(totalAmount <= 1)
        {
            return -secondCell.getCurrentLevel();
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
            
            //want downward flow to result in negative value
            return newUpperLevel - secondCell.getCurrentLevel();
        }
        else
        {
            // if flow is enough to fill both cells, simply want to equalize pressure, such that
            // (1 + p)u = d  AND u + d = starting_total
            // where    p = pressure per level
            //          d, u = ending down and upper levels
            
            float newUpperLevel = (totalAmount - PRESSURE_PER_LEVEL) * 0.5F;
            
            //want downward flow to result in negative value
            return newUpperLevel - secondCell.getCurrentLevel();
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
        float pressure1 = this.firstCell.getCurrentLevel();
        float pressure2 = this.secondCell.getCurrentLevel();

        // For horizontal connection, flow is always towards cell with no bottom
        // and if neither has a bottom, there is no flow.
        if(!this.firstCell.isSupported(sim)) pressure1 = 0;
        if(!this.secondCell.isSupported(sim)) pressure2 = 0;
        
        float difference = pressure1 - pressure2;
        
        if(difference == 0) return 0;
        
        // Positive numbers means 1st cell has higher pressure.
        if(difference > 0)
        {
            float bound = pressure1 - this.firstCell.getRetainedLevel();
            if(bound <= 0) return 0;
            difference = Math.min(difference, bound);
        }
        else
        {
            float bound = pressure2 - this.secondCell.getRetainedLevel();
            if(bound <= 0) return 0;
            // flow is negative in this case, so need to flip application of bound
            difference = Math.max(difference, -bound);
        }
        
        // split the difference to average out the pressure
        return difference * 0.5F;

    }
    
    public void doStep(LavaSimulator sim, double seconds)
    {
        // barriers don't need processing - TODO: may not be needed because checked in connection processing loop
        if(this.firstCell.isBarrier() || this.secondCell.isBarrier()) return;
        
        // TODO: Particle output
        if(this.firstCell.hashCode() == 12430 || this.secondCell.hashCode() == 12430)
            Adversity.log.info("boop");
        
        // compute flow based on pressure differential
        float flow = this.isVertical ? this.getVerticalFlow(sim) : this.getHorizontalFlow(sim);
        
        if (flow == 0) return;
        
        // restrict flow amount by retained height and maximum flow rate
        
        // TODO: make configurable
        float bound = 0.05F;
        
        //TODO: clamp retained level at 1.0 for lower cell?
        
        // Positive numbers means 1st cell has higher pressure.
        if(flow > 0)
        {
            flow = Math.min(flow, bound);
        }
        else
        {
            // flow is negative in this case, so need to flip application of bound
            flow = Math.max(flow, -bound);
        }
        
        // shouldn't be needed but was getting zeros here - maybe floating-point weirdness?
        if(flow ==0) return;
        
        this.firstCell.changeLevel(sim, -flow, false);
        this.secondCell.changeLevel(sim, flow, false);
        this.firstCell.applyUpdates(sim);
        this.secondCell.applyUpdates(sim);
        sim.setSaveDirty(true);
    }
    
    /**
     * Call when removing this connection so that cell references can be removed if appropriate.
     */
    public void releaseCells()
    {
//        Adversity.log.info("connection release");
        this.firstCell.release();
        this.secondCell.release();
    }
}
