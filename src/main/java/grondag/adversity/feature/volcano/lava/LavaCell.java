package grondag.adversity.feature.volcano.lava;


import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import net.minecraft.util.math.BlockPos;

public class LavaCell
{
    public final int id;
    
    /** in-world x coordinate of cell */
    public final int x;
    
    /** in-world x coordinate of cell */
    public final int z;
    
    private final CellTracker tracker;
    
    /** 
     * Absolute in-world top of cell, from 0 to 255 * FLUID_UNITS_PER_CELL.
     * Inclusive - DOES represent space within cell;
     */
    private short ceiling;
    
    /** 
     * Absolute in-world bottom of cell, from 0 to 255 * FLUID_UNITS_PER_CELL
     * Exclusive - does NOT represent space within cell.
     */
    private short floor;
    
    /** 
     * True if block defining floor of this cell is a Flow block.  
     * False if normal block.
     * Drop nodes on flow blocks can have thinner flows and still cover underlying terrain.
     */
    private boolean hasFlowBlockFloor = false;
    
    /** 
     * Absolute in-world top of lava contained in cell.
     * 
     * Will range from floor to ceiling.
     * Bottom of lava will always be same as floor
     * Ranges from 0 to 255 * FLUID_UNITS_PER_CELL
     */
    private short level;
    
    /** 
     * Used to track block updates.  Set to current level
     * when block updates are obtained.
     */
    private short lastVisibleLevel = 0;  
    
    public final HashSet<CellConnection> connections = new HashSet<CellConnection>();
    
    
    /** update this on each search pass - if less than current search index has not been visited yet */
    protected int lastSearchIndex = 0;

    /** generating counter for lastSearchIndex values */
    protected static int nextSearchIndex = 1;
    
    /** update this on each simulation time step - if less than current step index then per-step values must be refreshed*/
    protected int lastStepIndex = 0;
    
    
    //TODO: temporary
    protected float interpolatedTerrainHeight;
    
//    private final TreeSet<LavaCell> inputs = new TreeSet<LavaCell>();
//    private final TreeSet<LavaCell> outputs = new TreeSet<LavaCell>();
//    
//    private boolean isBlocked = true;
//    private boolean isCutOff = true;
//    
//    private boolean areConnectionsDirty = false;
    
    /**
     * Cell can flow down this much (in fluid units) in one second.
     */
    public static final int FLUID_UNITS_PER_BLOCK = 12;
    public static final short MAX_VERTICAL_DROP_PER_SECOND = 5 * FLUID_UNITS_PER_BLOCK;
    public static final short MAX_LEVEL = 255 * FLUID_UNITS_PER_BLOCK;
    
    //TODO: make configurable
    public final static int MINIMUM_OPENING = FLUID_UNITS_PER_BLOCK / 2;

//    public enum DefaultFlowType
//    {
//        DROP,   // can flow downward into one or more adjacent cells
//        SPREAD, // at least one adjacent cell at same level, rest are same or higher
//        POOL    // all adjacent cells (if any) are at higher level
//    }
//    
    /**
    * Send isFloorFlowBlock true if this space is the open, upper part of a flow block, or if the block below is a full-height flow block.
    * Private constructor expects fluid units for floor, ceiling.
    */
    private LavaCell(int x, int z, short floor, short ceiling, CellTracker tracker, boolean isFloorFlowBlock)
    {
        this.x = x;
        this.z = z;
        this.tracker = tracker;
        this.floor = floor;
        this.level = floor;
        this.ceiling = ceiling;
        this.id = tracker.getNextCellID();
//        this.hasFlowBlockFloor = isFloorFlowBlock;
    }
    
    @Override
    public int hashCode()
    {
        return this.id;
    }

    /**
    * Send isFloorFlowBlock true if this space is the open, upper part of a flow block, or if the block below is a full-height flow block.
    * Public constructor expects float for floor, ceiling.
    */
    public LavaCell(int x, int z, float floor, float ceiling, CellTracker tracker, boolean isFloorFlowBlock)
    {
        this(x, z, (short) (floor * FLUID_UNITS_PER_BLOCK), (short) (ceiling * FLUID_UNITS_PER_BLOCK), tracker, isFloorFlowBlock);
    }
    
    public void flow()
    {
        //mimic effect of adhesion to ground
        if(this.getDepth() <= this.getMinimumDepth()) return;
        
        HashSet<LavaCell> visited = new HashSet<LavaCell>();
//        LinkedList<LavaCell> best = this.getPathToBestDestination(this, nextSearchIndex++);
        
//        if(!best.isEmpty()  && best.getLast().level < this.level)
//        {
//            this.addLava(-1);
//            best.getLast().addLava(1);
//        }
//        
    }
    
//    private LinkedList<LavaCell> getPathToBestDestination(LavaCell origin, int searchIndex)
//    {
//        
//   
//        LavaCell result = null;
//
//        //default to self if have useful capacity
//        if(this.level < this.ceiling && this.level < origin.level) 
//        {
//            result = this;
//        }
//        
//        //can't transmit if can't flow  TODO: remove? - check may be redundant of check made in flow - or remove there
//        if(this.getDepth() >= this.getMinimumDepth()) 
//        {
//            
//            for(CellConnection connection : this.connections)
//            {
//                LavaCell otherCell = connection.getOther(this);
//                if(!visited.contains(otherCell))
//                {
//                    LavaCell candidate = otherCell.getBestDestination(origin, visited);
//                    if(candidate != null && (result == null || candidate.level < result.level))
//                    {
//                        result = candidate;
//                    }
//                }
//            }
//        }
//        
//        return result;
//    }
    
    private int getMinimumDepth()
    {
        return MINIMUM_OPENING;
    }
    
    /**
     * Must call this after space initialization and after all incremental changes on this cell or neighboring cells.
     */
    public void updateConnections()
    {
        for(CellConnection connection: this.connections)
        {
            connection.getOther(this).connections.remove(connection);
        }
       this.connections.clear();
       
       for(LavaCell neighbor : tracker.getMinimallyAdjacentCells(this))
       {
           // constructor adds itself to collections of each cell
           CellConnection connection = new CellConnection(this, neighbor);
       }
    }
    
//    public boolean isCellAnInput(LavaCell cell)
//    {
//        return this.inputs.contains(cell);
//    }
//    
//    public boolean isCellAnOutput(LavaCell cell)
//    {
//        return this.outputs.contains(cell);
//    }
//    
    
    // LAVA INFO
    
    /** 
     * Depth of lava in this cell, measured in fluid units. 
     */
    private short getDepth()
    {
        return (short) (this.level - this.floor);
    }
    
//    public short getLowestAdjacentLevel(CellTracker tracker)
//    {
//        short lowest = MAX_LEVEL;
//        for(LavaCell cell : tracker.getMinimallyAdjacentCells(this))
//        {
//            if(cell.floor < lowest) lowest = cell.floor;
//        }
//        return lowest;
//    }
    
//    public List<LavaCell> getLowestAdjacentNeighbors(CellTracker tracker)
//    {
//        LinkedList<LavaCell> result = new LinkedList<LavaCell>();
//        for(LavaCell cell : tracker.getMinimallyAdjacentCells(this))
//        {
//            if(result.isEmpty())
//            {
//                result.add(cell);
//            }
//            else if(result.peekFirst().floor == cell.floor)
//            {
//                result.add(cell);
//            }
//            else if(result.peekFirst().floor > cell.floor)
//            {
//                result.clear();
//                result.add(cell);
//            }
//        }
//        return Collections.unmodifiableList(result);
//    }
    
    /** 
     * Returns a number between 0 and 1 representing level of lava at the given Y level.
     * If Y level is not within this cell, returns 0.
     */
    private static float getLevelWithinBlock(int levelIn, int y)
    {   
        return Math.min(1, Math.max(0, (float) (levelIn - y * FLUID_UNITS_PER_BLOCK) / FLUID_UNITS_PER_BLOCK));
        
        //examples
        //level = 0, Y = 0 -> 0
        //level = 0, y = 200 -> 0
        //level = 121, y = 0 -> 1
        //level = 121, y = 10 -> 1/12
    }
    
    /**
     * Returns the Y coordinate based on fluid units.
     * A completely full block counts as the full block, not the block above.
     * For convenience, input level 0 will return Y  = 0;
     */
    private static int getY(int levelIn)
    {
        return Math.max(0, (levelIn - 1) / FLUID_UNITS_PER_BLOCK);

        //examples
        // levelIn = 0 -> 0
        // levelIn = 1 -> 0
        // levelIn = 12 -> 0
        // levelIn = 13 -> 1
    }
    
    public List<LavaBlockUpdate> getBlockUpdates()
    {
        LinkedList<LavaBlockUpdate> result = new LinkedList<LavaBlockUpdate>();
        
        if(this.level != this.lastVisibleLevel)
        {
            int startLevel = Math.min(this.level, lastVisibleLevel);
            int endLevel =  Math.max(this.level, lastVisibleLevel);
            int startY = Math.max(this.getMinY(), getY(startLevel));
            int endY = Math.min(this.getMaxY(), getY(endLevel));
            for(int y = startY; y <= endY; y++ )
            {
                float oldLevel = getLevelWithinBlock(this.lastVisibleLevel, y);
                float newLevel = getLevelWithinBlock(this.level, y);
                if(oldLevel != newLevel)
                {
                    BlockPos pos = new BlockPos(this.x, y, this.z);
                    result.add(new LavaBlockUpdate(pos, newLevel));
                }
            }
            this.lastVisibleLevel = this.level;
        }
        
        return Collections.unmodifiableList(result);
    }
    
    /** 
     * Returns true if space is already fully or partially included in this cell, or if space is vertically adjacent.
     * If the space is vertically adjacent, this cell is expanded to include the space. 
     * If the input space is not included or adjacent, it is not added, and returns false.
     * 
     * Send isFloorFlowBlock true if this space is the open, upper part of a flow block, or if the block below is a full-height flow block.
     * inputs are floating point values that represent partial block height.
     */
    public boolean addOrConfirmSpace(float floorFloat, float ceilingFloat, boolean isFloorFlowBlock)
    {
        int floorUnits = Math.round(floorFloat * FLUID_UNITS_PER_BLOCK);
        int ceilingUnits = Math.round(ceilingFloat * FLUID_UNITS_PER_BLOCK);
        
        if(this.isVerticallyAdjacentToDiscretely(floorUnits, ceilingUnits) || this.intersectsWithDiscretely(floorUnits, ceilingUnits))
        {
            this.ceiling = (short) Math.max(this.ceiling, ceilingUnits);
            this.floor = (short) Math.min(this.floor, floorUnits);
            
            //if this space defines our floor, then update if we are resting on a flow boundary
            if(this.floor == floorUnits) this.hasFlowBlockFloor = isFloorFlowBlock;
            
            return true;
        }
        else
        {
            return false;
        }
    }
    
    // LOCATION INFO
    /**
     * Returns world Y coordinate for uppermost block that could contain lava
     */
    public int getMaxY()
    {

        //handle strange case that should never happen
        if(ceiling <= floor) return getMinY();
        
        return getY(this.ceiling);
        
        //examples
        // ceiling 0 -> 0
        // ceiling 1 -> 0
        // ceiling 12 -> 0
        // ceiling 120 -> 9
    }
    
    /**
     * Returns world Y coordinate for lowest block that could contain lava.
     * Is equivalent to getY(floor + 1) because floor is exclusive. 
     */
    public int getMinY()
    {
        return this.floor / FLUID_UNITS_PER_BLOCK;
        
        //examples
        // floor 0 -> 0
        // floor 1 -> 0
        // floor 12 -> 1
        // floor 120 -> 10
    }
    
    /**
     * Use this version when you have fluid units.
     */
    private boolean isVerticallyAdjacentToDiscretely(int floorIn, int ceilingIn)
    {
        return this.floor == ceilingIn || this.ceiling == floorIn;
    }
    
    public boolean isVerticallyAdjacentTo(float floorIn, float ceilingIn)
    {
        int floorUnits = Math.round(floorIn * FLUID_UNITS_PER_BLOCK);
        int ceilingUnits = Math.round(ceilingIn * FLUID_UNITS_PER_BLOCK);
        
        return this.isVerticallyAdjacentToDiscretely(floorUnits, ceilingUnits);
    }
    
    /**
     * Use this version when you have fluid units.
     */
    private boolean intersectsWithDiscretely(int floorIn, int ceilingIn)
    {
        return //to overlap, top of cell must be above my floor
                ceilingIn > this.floor
                //to overlap, bottom of cell must be below my ceiling
                && floorIn < this.ceiling;
    }
    
    public boolean intersectsWith(float floorIn, float ceilingIn)
    {
        int floorUnits = Math.round(floorIn * FLUID_UNITS_PER_BLOCK);
        int ceilingUnits = Math.round(ceilingIn * FLUID_UNITS_PER_BLOCK);
        
        return this.intersectsWithDiscretely(floorUnits, ceilingUnits);
    }
    
    /**
     * This version requires that the height of intersection be at least MIN_OPENING tall.
     * Expects fluid units.
     */
    public boolean intersectsMinimallyWithDiscretely(int floorIn, int ceilingIn)
    {
        return intersectsWithDiscretely(floorIn, ceilingIn)
                && Math.min(ceilingIn, this.ceiling) - Math.max(floorIn, this.floor) >= MINIMUM_OPENING;
    }
    
    /** cells should not overlap - use this to assert */
    public boolean intersectsWith(LavaCell other)
    {
        return this.x == other.x 
                && this.z == other.z
                && this.intersectsWithDiscretely(other.floor, other.ceiling);
    }
    
    //FLOW TRACKING
    
//    /** Called by setLevel to update lava levels in cells contained by this node */
//    protected void updateCellLevel(float newLevel)
//    {
//     //TODO   
//    }
//    
//    /** can this flow accept lava at its current vertical level? */
//    public boolean canAcceptAtCurrentLevel()
//    {
//        //TODO
//        return false;
//    }
//    
//    
//    public void addInput(LavaCell inputNode)
//    {
//        inputs.add(inputNode);
//        inputNode.outputs.add(this);
//        this.checkForCutOff();
//        inputNode.checkForBlocked();
//    }
//    
//    public void removeInput(LavaCell inputNode)
//    {
//        inputs.remove(inputNode);
//        inputNode.outputs.remove(this);
//        this.checkForCutOff();
//        inputNode.checkForBlocked();
//    }
//    
//    public void addOutput(LavaCell outputNode)
//    {
//        outputs.add(outputNode);
//        outputNode.inputs.add(this);
//        this.checkForBlocked();
//        outputNode.checkForCutOff();
//    }
//    
//    public void removeOutput(LavaCell outputNode)
//    {
//        outputs.remove(outputNode);
//        outputNode.inputs.remove(this);
//        this.checkForBlocked();
//        outputNode.checkForCutOff();
//    }
//    
//    public void removeLinks()
//    {
//        for(LavaCell output: outputs)
//        {
//            output.inputs.remove(this);
//            output.checkForCutOff();
//        }
//        outputs.clear();
//        this.isBlocked = true;
//        
//        for(LavaCell input: inputs)
//        {
//            input.outputs.remove(this);
//            input.checkForBlocked();
//        }
//        inputs.clear();
//        this.isCutOff = true;        
//    }
//    
//    public void transferLinksTo(LavaCell other)
//    {
//        other.outputs.addAll(outputs);
//        for(LavaCell output: outputs)
//        {
//            output.inputs.add(other);
//            output.checkForCutOff();
//        }
//        outputs.clear();
//        this.isBlocked = true;
//        
//        other.inputs.addAll(inputs);
//        for(LavaCell input: inputs)
//        {
//            input.outputs.add(other);
//            input.checkForBlocked();
//        }
//        inputs.clear();
//        this.isCutOff = true;
//        
//    }
    
//    public Set<LavaCell> getOutputs()
//    {
//        return Collections.unmodifiableSet(outputs);
//    }
//    
//    public Set<LavaCell> getInputs()
//    {
//        return Collections.unmodifiableSet(inputs);
//    }
    
    //BLOCKAGE TRACKING
    
//    public boolean isCutOff()
//    {
//        return this.isCutOff;
//    }
//    
//    public boolean isBlocked()
//    {
//        return this.isBlocked;
//    }
//    
//    protected void setCutOff(boolean isCutOff)
//    {
//        if(isCutOff != this.isCutOff)
//        {
//            this.isCutOff = isCutOff;
//            notifyAllOutputsToCheckForCutoff();
//        }
//    }
//    
//    protected void setBlocked(boolean isBlocked)
//    {
//        if(isBlocked != this.isBlocked)
//        {
//            this.isBlocked = isBlocked;
//            notifyAllInputsToCheckForBlocked();
//        }
//    }
    
//    protected void notifyAllInputsToCheckForBlocked()
//    {
//        if(inputs.isEmpty()) return;
//        
//        for(LavaCell input : inputs)
//        {
//            input.checkForBlocked();
//        }
//    }
//    
//    protected void notifyAllOutputsToCheckForCutoff()
//    {
//        if(outputs.isEmpty()) return;
//        
//        for(LavaCell output : outputs)
//        {
//            output.checkForCutOff();
//        }
//    }
//    
//    /** returns true if status changed and outputs were notified */
//    protected boolean checkForCutOff()
//    {
//        boolean shouldBeCutOff = true;
//        
//        if(!this.inputs.isEmpty())
//        {
//            for(LavaCell input : this.inputs)
//            {
//                if(!input.isCutOff() && input.level >= this.minimumLevelAccepted())
//                {
//                    shouldBeCutOff = false;
//                    break;
//                }
//            }
//        }
//        
//        if(shouldBeCutOff != this.isBlocked())
//        {
//            setCutOff(shouldBeCutOff);
//            return true;
//        }
//        else
//        {
//            return false;
//        }
//    }
    
//    /** returns true if status changed and inputs were notified */
//    protected boolean checkForBlocked()
//    {
//        boolean shouldBeBlocked = true;
//        
//        if(!this.outputs.isEmpty())
//        {
//            for(LavaCell output : this.outputs)
//            {
//                if(!output.isBlocked() && this.level >= output.minimumLevelAccepted())
//                {
//                    shouldBeBlocked = false;
//                    break;
//                }
//            }
//        }
//        
//        if(shouldBeBlocked != this.isBlocked())
//        {
//            setBlocked(shouldBeBlocked);
//            return true;
//        }
//        else
//        {
//            return false;
//        }
//    }

    
    /** 
     * Updates our vertical flow level and notifies inputs and outputs to update flow status.
     * Is an absolute level, not an amount of lava.  Should be >= floor and <= ceiling.
     * EXPECTS FLUID UNITS
     */
    private void setLevel(int newLevel)
    {
        if(newLevel != this.level)
        {
//            areConnectionsDirty = true;
            
            if(this.level == this.floor)
            {
                this.tracker.addPendingLavaCell(this);
            }
            else if(newLevel == this.floor)
            {
                this.tracker.removePendingLavaCell(this);
            }
            
            this.level = (short) newLevel;
        }
    }
    
    
    //TODO: temporary hacky testing method
    public void addLava(int fluidUnits)
    {
        int newLevel = this.level + fluidUnits;
        newLevel = Math.min(this.ceiling, newLevel);
        newLevel = Math.max(this.floor, newLevel);
        this.setLevel(newLevel);
    }
    
    /** 
     * Vertical level that must be offered for this node to accept more fluid.
     * If this node can expand outwards will generally be equal to flows existing level0
     * If this node can only expand up, will be equal to current level + 1
     */
//    private int minimumLevelAccepted()
//    {
//        if(this.canAcceptAtCurrentLevel())
//        {
//            return this.level;
//        }
//        else
//        {
//            return this.level + 1;
//        }
//    }
    
    // GETTERS & SETTERS
    public short getFloor()
    {
        return this.floor;
    }
    
    public short getCeiling()
    {
        return this.ceiling;
    }
    
    /** 
     * returns fluid units
     */
    public short getLevel()
    {
        return this.level;
    }
  
}
