package grondag.adversity.feature.volcano.lava;

public class CellConnection
{
    private static int nextConnectionID = 0;
    
    /** by convention, first cell will have the lower-valued coordinate (x or z) */
    public final LavaCell firstCell;
    
    /** by convention, second cell will have the higher-valued coordinate (x or z) */
    public final LavaCell secondCell;
    
    public final int id;
    
    /**
     * Absolute value of flow.
     * Reset to zero at each step.
     */
    protected short flowThisStep = 0;
    
    
    private enum FlowDirection
    {
        UNDETERMINED,
        FIRST_TO_SECOND,
        SECOND_TO_FIRST;
    }
    protected FlowDirection flowDirection = FlowDirection.UNDETERMINED;
    
    boolean canFlowFirstToSecond = false;
    boolean canFlowSecondToFirst = false;

    /**
     * Absolute magnitude of maximum flow across this connection in a single step.
     * Measured in fluid units.
     * Refreshed each step.
     */
    private short maxFlowFirstToSecond = 0;
    private short maxFlowSecondToFirst = 0;    
    
//    /** update this on each search pass - if less than current search index has not been visited yet */
//    protected int lastSearchIndex = 0;

    /** update this on each simulation time step - if less than current step index then per-step values must be refreshed*/
    protected int lastStepIndex = 0;
            
    public CellConnection(LavaCell firstCell, LavaCell secondCell)
    {
        this.id = nextConnectionID++;
        if(firstCell.x == secondCell.x)
        {
            if(firstCell.z < secondCell.z)
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
            if(firstCell.x < secondCell.x)
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
        firstCell.connections.add(this);
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
     * Call this at start of all operations that depend on per-step state.
     */
    private void refreshOrConfirmStepSetup(int stepIndex)
    {
        if(this.lastStepIndex == stepIndex) return;
        
        this.lastStepIndex = stepIndex;
        this.flowThisStep = 0;
        this.calcMaxFlowAndDirection();
    }
    
    private void calcMaxFlowAndDirection()
    {

        int firstLevel = firstCell.getLevel();
        int secondLevel = secondCell.getLevel();
        int maxFloor = Math.max(firstCell.getFloor(), secondCell.getFloor());
        int minCeiling  = Math.min(firstCell.getCeiling(), secondCell.getCeiling());
        

        //can only flow up if under pressure
        this.canFlowFirstToSecond = firstLevel >= secondLevel || firstLevel == firstCell.getCeiling();
        this.maxFlowFirstToSecond = canFlowFirstToSecond ? calcMaxFlowInUnitsPerStep(Math.min(firstLevel, minCeiling) - maxFloor) : 0;
            
        this.canFlowSecondToFirst = secondLevel >= firstLevel || secondLevel == secondCell.getCeiling();
        this.maxFlowSecondToFirst = canFlowSecondToFirst ? calcMaxFlowInUnitsPerStep(Math.min(secondLevel, minCeiling) - maxFloor) : 0;
            
        if(canFlowFirstToSecond && !canFlowSecondToFirst)
        {
            this.flowDirection = FlowDirection.FIRST_TO_SECOND;
        }
        else if(canFlowSecondToFirst && !canFlowFirstToSecond)
        {
            this.flowDirection = FlowDirection.SECOND_TO_FIRST;
        }
        else
        {
            this.flowDirection = FlowDirection.UNDETERMINED;
        }
    }
    
    /**
     * Loosely based on "Cellular Automata for the Flow Simulations on the Earth Surface, Optimization Computation Process"
     * Juraj Cirbus1,∗ and Michal Podhoranyi, 2013
     */
    private short calcMaxFlowInUnitsPerStep(int flowWindowHeight)
    {
        return (short) (Math.sqrt((flowWindowHeight * flowWindowHeight * flowWindowHeight)) / LavaManager2.STEPS_PER_SECOND);
    }
    
    public boolean canFlowFrom(LavaCell fromCell, int stepIndex, int searchIndex)
    {
        this.refreshOrConfirmStepSetup(stepIndex);
        
        if(fromCell == this.firstCell)
        {
            if(this.secondCell.lastSearchIndex == searchIndex) return false;
            secondCell.lastSearchIndex = searchIndex;
            return this.canFlowFirstToSecond && this.flowDirection != FlowDirection.SECOND_TO_FIRST;
        }
        else
        {
            if(this.firstCell.lastSearchIndex == searchIndex) return false;
            firstCell.lastSearchIndex = searchIndex;
            return this.canFlowSecondToFirst && this.flowDirection != FlowDirection.FIRST_TO_SECOND;
        }
        
    }
    
}
