package grondag.adversity.feature.volcano.lava;


import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import grondag.adversity.niceblock.base.IFlowBlock;
import net.minecraft.util.math.BlockPos;

public class LavaCell
{
    public final int id;
    
    /** in-world x coordinate of cell */
    public final int x;
    
    /** in-world x coordinate of cell */
    public final int z;
    
    /** absolute in-world top of cell, from 0 to 255 */
    private float ceiling;
    
    /** absolute in-world bottom of cell, from 0 to 255 */
    private float floor;
    
    /** 
     * True if block defining floor of this cell is a Flow block.  
     * False if normal block.
     * Drop nodes on flow blocks can have thinner flows and still cover underlying terrain.
     */
    private boolean hasFlowBlockFloor = false;
    
    /** 
     * Absolute in-world top of lava contained in cell.
     * Will range from floor to ceiling.
     * Bottom of lava will always be same as floor */
    private float level;
    
    /** 
     * Used to track block updates.  Set to current level
     * when block updates are obtained.
     */
    private float lastVisibleLevel = 0;  
    
    private FlowNode flowNode = null;
    
    //TODO: make configurable
    public final static float MINIMUM_OPENING = 0.5F;

    public enum DefaultFlowType
    {
        DROP,   // can flow downward into one or more adjacent cells
        SPREAD, // at least one adjacent cell at same level, rest are same or higher
        POOL    // all adjacent cells (if any) are at higher level
    }
    
    /**
    * Send isFloorFlowBlock true if this space is the open, upper part of a flow block, or if the block below is a full-height flow block.
    */
    protected LavaCell(int x, int z, float floor, float ceiling, CellTracker tracker, boolean isFloorFlowBlock)
    {
        this.x = x;
        this.z = z;
        this.floor = floor;
        this.ceiling = ceiling;
        this.id = tracker.getNextCellID();
        this.hasFlowBlockFloor = isFloorFlowBlock;
    }
          
    public void setFlowNode(FlowNode node)
    {
        this.flowNode = node;
    }
    
    public FlowNode getFlowNode()
    {
        return this.flowNode;
    }
    
    // LAVA INFO
    
    /** 
     * Depth of lava in this cell. 
     */
    public float getDepth()
    {
        return this.getLevel() - this.floor;
    }
    
    public DefaultFlowType getDefaultCellType(CellTracker tracker)
    {   
        float lowest = this.getLowestAdjacentLevel(tracker);
        if(lowest < this.floor)
        {
            return DefaultFlowType.DROP;
        }
        else if(lowest == this.floor)
        {
            return DefaultFlowType.SPREAD;
        }
        else
        {
            return DefaultFlowType.POOL;
        }
    }
    
    public float getLowestAdjacentLevel(CellTracker tracker)
    {
        float lowest = 255;
        for(LavaCell cell : tracker.getAdjacentCells(x, z, this.floor, Math.min(this.floor + 0.5F, this.ceiling), MINIMUM_OPENING))
        {
            lowest = Math.min(lowest, cell.floor);
        }
        return lowest;
    }
    
    public List<LavaCell> getLowestAdjacentNeighbors(CellTracker tracker)
    {
        LinkedList<LavaCell> result = new LinkedList<LavaCell>();
        for(LavaCell cell : tracker.getAdjacentCells(x, z, this.floor, Math.min(this.floor + 0.5F, this.ceiling), MINIMUM_OPENING))
        {
            if(result.isEmpty())
            {
                result.add(cell);
            }
            else if(result.peekFirst().floor == cell.floor)
            {
                result.add(cell);
            }
            else if(result.peekFirst().floor > cell.floor)
            {
                result.clear();
                result.add(cell);
            }
        }
        return Collections.unmodifiableList(result);
    }
    
    /** 
     * Returns a number between 0 and 1 representing level of lava in BlockPos.
     * Returns -1 if given block pos does not intersect with this cell.
     */
    public float getLevelForBlockPos(BlockPos pos)
    {
        if(!intersectsWith(pos)) return -1;
        
        return Math.max(0, this.getLevel() - pos.getY());
    }
    
    public List<LavaBlockUpdate> getBlockUpdates()
    {
        LinkedList<LavaBlockUpdate> result = new LinkedList<LavaBlockUpdate>();
        
        // For now, lava level can only go up, never down
        if(this.level > this.lastVisibleLevel)
        {
            int start = (int) Math.floor(Math.max(this.floor, lastVisibleLevel));
            int end = (int) Math.floor(this.level);
            for(int y = start; y <= end; y++ )
            {
                BlockPos pos = new BlockPos(this.x, y, this.z);
                float blockLevel = Math.min(1, level - y);
                if(blockLevel > 0)
                {
                    result.add(new LavaBlockUpdate(pos, blockLevel));
                }
            }
            this.lastVisibleLevel = this.level;
        }
        
        return Collections.unmodifiableList(result);
    }
    
    /** 
     * Returns true if space is already fully or partially included in this cell, or if space is vertically adjacent.
     * If the space is vertically adjacent, this cell is expanded to include the space. 
     * Send isFloorFlowBlock true if this space is the open, upper part of a flow block, or if the block below is a full-height flow block.
     */
    public boolean addOrConfirmSpace(float floor, float ceiling, boolean isFloorFlowBlock)
    {
        if(this.isVerticallyAdjacentTo(floor, ceiling) || this.intersectsWith(floor, ceiling))
        {
            this.ceiling = Math.max(this.ceiling, ceiling);
            this.floor = Math.min(this.floor, floor);
            
            //if this space defines our floor, then update if we are resting on a flow boundary
            if(this.floor == floor) this.hasFlowBlockFloor = isFloorFlowBlock;
            
            return true;
        }
        else
        {
            return false;
        }
    }
    
    // LOCATION INFO
    
    public BlockPos getTopBlockPos()
    {

        //handle strange case that should never happen
        if(ceiling <= floor) return getBottomBlockPos();
        
        //This is tricker than bottom, because ceiling at a block boundary indicates the block below.
        //So can't just round down.
        int y = (int) Math.floor(ceiling);
        if(y == ceiling) y -= 1;

        return new BlockPos(this.x, y, this.z);
    }

    public BlockPos getBottomBlockPos()
    {
        return new BlockPos(this.x, (int) Math.floor(floor), this.z);
    }
    
    public boolean isVerticallyAdjacentTo(float floorIn, float ceilingIn)
    {
        return this.floor == ceiling || this.ceiling == floorIn;
    }
    
    public boolean intersectsWith(float floorIn, float ceilingIn)
    {
        return //to overlap, top of cell must be above my floor
                ceilingIn > this.floor
                //to overlap, bottom of cell must be below my ceiling
                && floorIn < this.ceiling;
    }
    
    public boolean intersectsWith(BlockPos pos)
    {
        return this.x == pos.getX() 
                && this.z == pos.getZ()
                && intersectsWith(pos.getY(), pos.getY() + 1);
    }
    
    /** cells should not overlap - use this to assert */
    public boolean intersectsWith(LavaCell other)
    {
        return this.x == other.x 
                && this.z == other.z
                && this.intersectsWith(other.floor, other.ceiling);
    }
    
    // GETTERS & SETTERS
    
    public float getCeiling()
    {
        return ceiling;
    }

    public float getFloor()
    {
        return floor;
    }

    public float getLevel()
    {
        return Math.min(ceiling, Math.max(floor, level));
    }
    
    public void setLevel(float level)
    {
        this.level = level;
    }
    
    public boolean hasFlowBlockFloor()
    {
        return this.hasFlowBlockFloor;
    }
}
