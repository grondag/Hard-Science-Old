package grondag.adversity.feature.volcano.lava.cell;

import java.util.concurrent.atomic.AtomicInteger;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.lava.AbstractLavaSimulator;
import grondag.adversity.feature.volcano.lava.LavaConnection2;
import grondag.adversity.feature.volcano.lava.LavaConnections;
import grondag.adversity.feature.volcano.lava.LavaSimulatorNew;
import grondag.adversity.library.PackedBlockPos;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

public class LavaCell2
{
    private static AtomicInteger nextCellID = new AtomicInteger(0);
    
    public final int id = nextCellID.getAndIncrement();
    
    /** 
     * Object held in common by all cells at our x, z coordinate.
     * Used to locate the first cell in the list as a synchronization object
     * for all operations potentially affecting more than one cell at x, z.
     * The first cell in this x, z column will create this instance. 
     * All subsequent additions to the column must obtain the same instance.
     */
    volatile CellLocator locator;
    
    /** 
     * Used to implement a doubly-linked list of all cells within an x,z coordinate.
     * Maintained by collection.
     */
    volatile LavaCell2 above;
    
    /** 
     * Used to implement a doubly-linked list of all cells within an x,z coordinate.
     * Maintained by collection.
     */
    volatile LavaCell2 below;
    
    /** set true when cell should no longer be processed and can be removed from storage */
    private volatile boolean isDeleted;
    
    private Int2ObjectOpenHashMap<LavaConnection2> connections = new Int2ObjectOpenHashMap<LavaConnection2>();
    
    /** see {@link #getFloor()} */
    private int floor;
    
    /** see {@link #bottomY()} */
    private byte bottomY;
    
    /** see {@link #getCeiling()} */
    private int ceiling;
    
    /** see {@link #topY()} */
    private byte topY;
    
    /** see {@link #isBottomFlow()} */
    private boolean isBottomFlow;
    
    
    private boolean isCoolingDisabled = false;
    
    /** 
     * True if this cell is new or has expanded.
     * Used to determine if updateConnectionsIfNeeded should do anything.
     */
    private boolean isConnectionUpdateNeeded = true;
    
    /**
     * Last level reported to world.
     */
    private int lastVisibleLevel; 
    
    /** 
     * False if it is possible currentVisibleLevel won't match lastVisible 
     * Saves cost of calculating currentVisible if isn't necessary.
     */
    private boolean isBlockUpdateCurrent = true;
    
    /**
     * Fluid units currently stored in this cell.
     */
    private int fluidUnits;
    
    
    public static final byte SUSPENDED_NONE = -1;
    /**
     * Used to signal that upper block levels may contain suspended lava blocks that
     * need to be set to air. Used when a cell with lava merges with a cell below.
     * This happens when a thin flow-block floor melts, or if player breaks a block.
     * Contains max y level in this cell that may contain a lava block.
     * Is -1 if no suspended blocks are known to exist.
     */
    private byte suspendedLevel = SUSPENDED_NONE;
    
    /** 
     * Fluid level will not drop below this - to emulate surface tension/viscosity.
     * Established when cell is first created.  Does not change until cell solidifies or bottom drops out.
     * Initialized to -1 to indicate has not yet been set or if needs to be recalculated.
     * The actual level used in world is a smoothed using a box filter.  
     * The raw value is stored because it should not change as neighbors change.
     */
    private int rawRetainedLevel = -1;
    
    /** 
     * Exponential average of current level - used for computing visible level.
     * Holds 6 bits of integer precision.  Needs >> 6 to get usable value.
     * Maintained by provideBlockUpdate.
     */
    public int avgFluidAmountWithPrecision = 0;
    
    /**
     * Maximum pressure level reported by any connected neighbor during the last connection processing pass.
     * Set to nextMaxNeighborPressure whenever propagate pressure sees a new stepIndex.
     */
    private int lastMaxNeighborPressure;
    
    /**
     * Maximum pressure level reported by any connected neighbor during the current connection processing pass.
     * Set to 0 whenever propagate pressure sees a new stepIndex.
     */
    private int nextMaxNeighborPressure;
    
    /**
     * The stepIndex that was last seen.
     * Step index will increment multiple times per tick.
     * When a new stepIndex is seen, is trigger to reset pressure propagation.
     */
    private int lastStepIndex;
    
    /**
     * The simulation tick that was last seen.
     * Flow index will increment multiple times per tick.
     * When a new flowIndex is seen, is trigger to reset pressure propagation.
     */
    private int lastTickIndex;
    
    //TODO
    // merge / split / validate
    // pressure propagation
    // calculate retained level
    // calculate smoothed retained level
    // make connections
    
    /**
     * Creates new cell and adds to processing array. 
     * Does NOT create linkages with existing cells in column.
     * @param cells
     * @param existingEntryCell
     * @param floor
     * @param ceiling
     * @param lavaLevel
     * @param isFlowFloor
     */
    public LavaCell2(LavaCells cells, LavaCell2 existingEntryCell, int floor, int ceiling, int lavaLevel, boolean isFlowFloor)
    {
        this.locator = existingEntryCell.locator;
        this.setFloor(floor, isFlowFloor);
        this.setCeiling(ceiling);
        this.fluidUnits = Math.max(0, lavaLevel - floor);
        cells.addCellToProcessingList(this);
    }
    
    /**
     * Creates new cell and adds to processing array. 
     * Does NOT create linkages with existing cells in column.
     * @param cells
     * @param x
     * @param z
     * @param floor
     * @param ceiling
     * @param lavaLevel
     * @param isFlowFloor
     */
    public LavaCell2(LavaCells cells, int x, int z, int floor, int ceiling, int lavaLevel, boolean isFlowFloor)
    {
        this.locator = new CellLocator(x, z, this);
        this.setFloor(floor, isFlowFloor);
        this.setCeiling(ceiling);
        this.fluidUnits = Math.max(0, lavaLevel - floor);
        cells.addCellToProcessingList(this);
    }
    
    public boolean isDeleted()
    {
        return this.isDeleted;
    }
    
    /** Removes all lava and prevents further processing.
     *  Also maintains above/below list references in remaining cells
     *  and removes references to/from this cell.
     */
    public void setDeleted()
    {
        this.fluidUnits = 0;
        if(this.below == null)
        {
            if(this.above != null) this.above.below = null;
        }
        else
        {
            this.below.linkAbove(this.above);
        }

        this.above = null;
        this.below = null;
        this.clearBlockUpdate();
        this.isDeleted = true;
    }
    
    /** 
     * Returns cell at given y block position if it exists.
     * Thread-safe.
     */
    LavaCell2 getCellIfExists(int y)
    {
        synchronized(this.locator)
        {
            if(y > this.topY())
            {
                LavaCell2 nextUp = this.above;
                while(nextUp != null)
                {
                    if(y > nextUp.topY())
                    {
                        nextUp = nextUp.above;
                    }
                    else if(y > nextUp.bottomY())
                    {
                        return nextUp;
                    }
                    else
                    {
                        return null;
                    }
                }
                return null;
            }
            else if(y > this.bottomY())
            {
                return this;
            }
            else
            {
                LavaCell2 nextDown = this.below;
                while(nextDown != null)
                {
                    if(y < nextDown.bottomY())
                    {
                        nextDown = nextDown.below;
                    }
                    else if(y <= nextDown.topY())
                    {
                        return nextDown;
                    }
                    else
                    {
                        return null;
                    }
                }
                return null;
            }
        }
    }

    static final String LAVA_CELL_NBT_TAG = "lavacells";
    static final int LAVA_CELL_NBT_WIDTH = 6;

    /** 
     * Enough to store 12000 * 256, which would be fluid in an entire world column.  
     * Fills 22 bits, and leaves enough room to pack in another byte.
     */
    private static final int FLUID_UNITS_MASK = 0x3FFFFF;
    private static final int FLUID_UNITS_BITS = 22;
    /** 
     * Writes data to array starting at location i.
     */
    void writeNBT(int[] saveData, int i)
    {
         
        saveData[i++] = this.locator.x;
        saveData[i++] = this.locator.z;
        saveData[i++] = (this.fluidUnits & FLUID_UNITS_MASK) | (this.suspendedLevel << FLUID_UNITS_BITS);
        saveData[i++] = this.rawRetainedLevel;
        
        // to save space, pack bounds into single int and save flow floor as sign bit
        int combinedBounds = this.getCeiling() << 12 | this.getFloor();
        if(this.isBottomFlow) combinedBounds = -combinedBounds;
        saveData[i++] = combinedBounds;
        
        // save never cools as sign bit on last tick index
        saveData[i++] = this.isCoolingDisabled ? -this.lastTickIndex : this.lastTickIndex;
    }
    
//    /** top level that would contain fluid if this column could expand vertically unconstrained */
//    public int fluidPressureSurfaceLevel()
//    {
//        int a = PRESSURE_UNITS_PER_LEVEL;
//        int b = 2 * FLUID_UNITS_PER_LEVEL - PRESSURE_UNITS_PER_LEVEL;
//        int c = -2 * this.fluidUnits;
//        
//        return this.floor + (int) ((-b + Math.sqrt(b * b - 4 * a * c)) / (2 * a));
//    }
    
    /** 
     * Reads data from array starting at location i.
     * Does NOT add cell to processing array because intended only for NBT read, which does this directly.
     */
    LavaCell2(int[] saveData, int i)
    {
        // see writeNBT to understand how data are persisted
        this.locator = new CellLocator(saveData[i++], saveData[i++], this);
        
        int fluidData = saveData[i++];
        this.fluidUnits = fluidData & FLUID_UNITS_MASK;
        this.suspendedLevel = (byte) (fluidData >> FLUID_UNITS_BITS);
        
        this.rawRetainedLevel = saveData[i++];
        int combinedBounds = saveData[i++];

        boolean isBottomFlow = combinedBounds < 0;
        if(isBottomFlow) combinedBounds = -combinedBounds;
        
        this.setFloor(combinedBounds & 0xFFF, isBottomFlow);
        this.setCeiling(combinedBounds >> 12);

        this.lastTickIndex = saveData[i++];
        if(this.lastTickIndex < 0)
        {
            this.lastTickIndex = -this.lastTickIndex;
            this.isCoolingDisabled = true;
        }
        else
        {
            this.isCoolingDisabled = false;
        }
    }
    public int getFluidUnits()
    {
        return this.fluidUnits;
    }
    
    public int getVolume()
    {
        return this.getCeiling() - this.getFloor();
    }
    
    /** 
     * Top level that contains fluid in the world. For columns under pressure
     * may be less than fluid amount would normally indicate. 
     * Will return cell floor if there is no fluid in the cell.
     */
    public int fluidSurfaceLevel()
    {
        return Math.min(this.getCeiling(), this.getFloor() + this.fluidUnits / AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL); 
    }
    
    /**
     * Returns the world Y-level of the uppermost block containing lava.
     * Returns -1 if the cell does not contain fluid.
     */
    public int fluidSurfaceY()
    {
        if(this.fluidUnits == 0) return -1;
        
        return getYFromCeiling(this.fluidSurfaceLevel());
    }

    /**
     * Returns the flow height (1 - 12) of lava in the lava block at fluidSurfaceY 
     * Returns 0 if this cell does not contain lava.
     */
    public int fluidSurfaceFlowHeight()
    {
        if(this.fluidUnits == 0) return 0;
        
        // examples of fluidSurfaceLevel -> output
        // 23 -> 11
        // 24 -> 12
        // 25 -> 1
        return this.fluidSurfaceLevel() - this.fluidSurfaceY() * FlowHeightState.BLOCK_LEVELS_INT;
    }
    
    /** 
     * Absolute surface level as measured in fluid units. Solid blocks/levels below count 
     * as if they contained fluid.  Also include any excess fluid for cells under pressure.
     */
    public int fluidSurfaceUnits()
    {
        return this.getFloor() * AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL + this.fluidUnits; 
    }
    
    /** 
    * Finds the cell that intersects with or is closest to given Y level.
    * When two cells are equidistant, preference is given to the cell above y
    * because that is useful for inferring properties when y is a flow floor block. 
    */
    public LavaCell2 findCellNearestY(int y)
    {
        int myDist = this.distanceToY(y);
        
        // intersects
        if(myDist == 0) return this;
        
        if(this.aboveCell() != null)
        {
            int aboveDist = this.aboveCell().distanceToY(y);
            if(aboveDist <= myDist) return this.aboveCell().findCellNearestY(y);
        }
        
        if(this.belowCell() != null)
        {
            int belowDist = this.belowCell().distanceToY(y);
            if(belowDist < myDist) return this.belowCell().findCellNearestY(y);
        }
        
        // no adjacent cell is closer than this one
        return this;

    }
    
    /** 
     * Distance from this cell to the given y level.
     * Returns 0 if this cell intersects.
     */
    private int distanceToY(int y)
    {
        if(this.bottomY() > y)
            return this.bottomY() - y;
        
        else if(this.topY() < y)
            
            return y - this.topY();
        else
            // intersects
            return 0; 
            
    }
    
    /** 
     * Distance from this cell to the given space, in block levels.
     * Space floor is exclusive, space ceiling in inclusive. Inputs are in block levels.
     * Returns 0 if the space is adjacent or intersecting.
     */
    private int distanceToSpace(int spaceFloor, int spaceCeiling)
    {
        if(this.getFloor() > spaceCeiling)
            return this.getFloor() - spaceCeiling;
        
        else if(this.getCeiling() < spaceFloor)
            
            return spaceFloor - this.getCeiling();
        else
            // intersects or adjacent
            return 0; 
            
    }
    
    /** 
     * Finds the uppermost cell that is below to the given level.
     * Cells that are below and adjacent (cell ceiling = level) count as below.
     * If the lowest existing cell is above or intersecting with the level, returns null.
     */
    public LavaCell2 findNearestCellBelowLeve(int level)
    {
        LavaCell2 candidate = this;

        if(candidate.getCeiling() > level)
        {
            //candidate is above or intersecting the level, try going down
            while(candidate.below != null)
            {
                candidate = candidate.below;
                if(candidate.getCeiling() <= level)
                {
                    return candidate;
                }
            }
            return null;
        }
        
        else if(candidate.getCeiling() == level)
        {
            // candidate is below and adjacent to level, and must therefore be the closest
            return candidate;
        }
        
        else
        {
            // candidate is below the level, is another cell closer?
            while(candidate.above != null && candidate.above.getCeiling() <= level)
            {
                candidate = candidate.above;
            }
            return candidate;
        }
        
    }
    
    /** Returns the lowest cell containing lava or the upper most cell if no cells contain lava */
    public LavaCell2 selectStartingCell()
    {
        LavaCell2 candidate = this.locator.firstCell;
        while(candidate.fluidUnits == 0 && candidate.above != null)
        {
            candidate = candidate.above;
        }
        return candidate;
    }
     
    // LOCATION INFO
    
    /** See {@link #getFloor()} */
    public void  setFloor(int newFloor, boolean isFlowFloor)
    {
        if(newFloor != this.floor || isFlowFloor != this.isBottomFlow)
        {
            // check for new connections whenever cell expands
            if(newFloor < this.floor) this.isConnectionUpdateNeeded = true;
            
            //force retention recalc
            this.rawRetainedLevel = -1;
            this.floor = newFloor;
            this.isBottomFlow = isFlowFloor;
            this.bottomY = (byte) getYFromFloor(this.floor);
        }
    }
    
    /** 
     * First (lowest) fluid level that can contain fluid, EXCLUSIVE.
     * Values range from 0 to (256 * LEVELS_PER_BLOCK) - 1 
     * Levels in Y=0 for example, are 0 thru 11.
     * ALWAYS USE setFloor() instead of floor to maintain bottomY.
     */
    public int getFloor()
    {
        return this.floor;
    }
    
    /** Y of first (lowest) block that could contain lava */
    public int bottomY()
    {
        return this.bottomY; 
    }
   
    /** Flow height of solid portion of block at {@link #bottomY()}
     *  Will be 0 if floor is not a flow block.
     *  Will also be 0 if floor is a full-height flow block at Y-1.
     *  Will be in range 1-11 if floor is within the block at Y.
     *  Note that 12 is not a valid result because that would mean 
     *  block at Y is not the floor because it could not contain lava.
     */
    public int floorFlowHeight()
    {
        //Examples of this.floor -> output
        // 12 -> 0
        // 13 -> 1
        // 23 -> 11
        // 24 -> 0
        return this.floor % FlowHeightState.BLOCK_LEVELS_INT;
    }
    
    /** 
     * True if the solid block under this columns is a flow block.
     * Note that it must be true if bottomFlowHeight > 0;
     */
    public boolean isBottomFlow()
    {
        return this.isBottomFlow;
    }
  
    
    /** See {@link #getCeiling()} */
    public void  setCeiling(int newCeiling)
    {
        // check for new connections whenever cell expands
        if(newCeiling > this.ceiling) this.isConnectionUpdateNeeded = true;
        
        this.ceiling = newCeiling;
        this.topY = (byte) getYFromCeiling(this.ceiling);
    }
    
    /** 
     * Last (highest) block level that can contain fluid, INCLUSIVE.
     * Values range from 1 to (256 * LEVELS_PER_BLOCK)
     * Levels in Y=0, for example are 1 thru 12.
     * ALWAYS USE setCeiling() to maintain topY.
     */
    public int getCeiling()
    {
        return this.ceiling;
    }
    
    /** Y of last (top) block that could contain lava */
    public int topY()
    {
        return this.topY;
    }
    
    /** calculates the block y from a top bound (inclusive) given as a fluid level */
    private static int getYFromCeiling(int ceilingIn)
    {
        // examples of input -> output
        // 24 -> 1  top level of block at Y = 1
        // 25 -> 2  first level of block at Y = 2
        return (ceilingIn - 1) / AbstractLavaSimulator.LEVELS_PER_BLOCK;
    }
    
    /** calculates the block y from a bottom bound (exclusive) given as a fluid level */
    private static int getYFromFloor(int floorIn)
    {
        return floorIn / AbstractLavaSimulator.LEVELS_PER_BLOCK;
    }
   
    /** 
     * Ceiling is inclusive, floor is not. 
     */
    public boolean isVerticallyAdjacentTo(int floorIn, int ceilingIn)
    {
        return this.getFloor() == ceilingIn || this.getCeiling() == floorIn;
    }
    
    public boolean intersectsWith(int floorIn, int ceilingIn)
    {
        return //to overlap, top of cell must be above my floor
                ceilingIn > this.getFloor()
                //to overlap, bottom of cell must be below my ceiling
                && floorIn < this.getCeiling();
    }
    
    public boolean intersectsWith(BlockPos pos)
    {
        return this.locator.x == pos.getX() 
                && this.locator.z == pos.getZ()
                && intersectsWith(blockFloorFromY(pos.getY()), blockCeilingFromY(pos.getY()));
    }
    
    /** cells should not overlap - use this to assert */
    public boolean intersectsWith(LavaCell2 other)
    {
        return this.locator.x == other.locator.x 
                && this.locator.z == other.locator.z
                && this.intersectsWith(other.getFloor(), other.getCeiling());
    }
    
    /** returns floor (exclusive) of the block position at level y */
    private static int blockFloorFromY(int y)
    {
        return y * AbstractLavaSimulator.LEVELS_PER_BLOCK;
    }
    
    /** returns ceiling (inclusive) of the block position at level y */
    private static int blockCeilingFromY(int y)
    {
        return (y + 1) * AbstractLavaSimulator.LEVELS_PER_BLOCK;
    }
    
    
 /**
     * Use to input lava into this cell (potentially) above the fluid surface.
     * Will add lava to surface after an appropriate falling time and 
     * render particles as appropriate.
     * Level is fluid level (12 per block) not world y level.
     */
    public void addLavaAtLevel(int level, int fluidUnits)
    {
        this.changeLevel(0, fluidUnits);
        
        //TODO: implement something more interesting, including persistence.
    }
    
    
    /** Use this to report when suspended lava blocks exist above lava surface
     * that need to be set to air on next block update. See {@link #suspendedLevel}. 
     * Does not actually add any lava to this cell.
     */
    public void notifySuspendedLava(int y)
    {
        if(y > this.suspendedLevel) this.suspendedLevel = (byte) y;
    }
    
    /**
     * Confirms non-solid space exists in this cell stack. 
     * The space defined is entirely within a single y level.
     * Creates a new cell or expands existing cells if necessary.
     * If new space causes two cells to be connected, merges upper cell into lower.
     * Can also cause cells to be split if a partial space is set within an existing cell.
     * 
     * Used to validate vs. world and to handle block events.
     * Should call this before placing lava in this space to ensure cell exists.
     * down or if an upper cell with lava merges into a lower cell.
     * 
     * @param cells Needed to maintain cell array if cells must be created or merged.
     * @param y  Location of space as world level
     * @param isFlowFloor  True if floorHeight = 0 and block below is flow block with height=12.  Should also be true of floorHeight > 0.
     * @param floorHeight  If is a partial solid flow block, height of floor within this y block
     * @return Cell to which the space belongs
     */
    public LavaCell2 addOrConfirmSpace(LavaCells cells, int y, int floorHeight, boolean isFlowFloor)
    {
        /**
         * Here are the possible scenarios:
         * 1) space is already included in this cell and floor is consistent or y is not at the bottom
         * 2) space is already included in this cell, but y is at the bottom and floor is different type
         * 3) space is adjacent to the top of this cell and need to expand up.
         * 4) space is adjacent to the bottom of this cell and need to expand down.
         * 5) One of scenarios 1-4 is true for a different cell.
         * 6) Scenarios 1-4 are not true for any cell, and a new cell needs to be added.

         * In scenarios 2-5, if a newly expanded cell is vertically adjacent to another cell, the cells must be merged.
         */
        
        int myTop = this.topY();
        int myBottom = this.bottomY();
        
        // space is already in this cell
        if(y > myBottom && y <= myTop) return this;
        
        // space is my bottom space, confirm floor
        if(y == myBottom)
        {
            if(this.floorFlowHeight() != floorHeight || this.isBottomFlow() != isFlowFloor)
            {
                this.setFloor(y * FlowHeightState.BLOCK_LEVELS_INT + floorHeight, isFlowFloor);
                return this.checkForMergeDown();
            }
        }
        
        // space is one below, expand down
        else if(y == myBottom - 1)
        {
            this.setFloor(y * FlowHeightState.BLOCK_LEVELS_INT + floorHeight, isFlowFloor);
            return this.checkForMergeDown();
        }
        
        // space is one above, expand up
        else if(y == myTop + 1)
        {
            this.setCeiling((y + 1) * FlowHeightState.BLOCK_LEVELS_INT);
            return this.checkForMergeUp();
        }
        
        // If this is not the closest cell, try again with the cell that is closest
        // We don't check this first because validation routine will try to position us
        // to the closest cell most of the time before calling, and thus will usually not be necessary.
        LavaCell2 closest = this.findCellNearestY(y);
        
        
        // if we get here, this is the closest cell and Y is not adjacent
        // therefore the space represents a new cell.
        
        LavaCell2 newCell = new LavaCell2(cells, this, y * FlowHeightState.BLOCK_LEVELS_INT + floorHeight, (y + 1) * FlowHeightState.BLOCK_LEVELS_INT, 0, isFlowFloor);
        
        if(y > myTop)
        {
            // if space is above, insert new cell above this one
            LavaCell2 existingAbove = this.above;
            this.linkAbove(newCell);
            if(existingAbove != null)
            {
                newCell.linkAbove(existingAbove);
                
            }
        }
        else
        {
            // space (and new cell) must be below
            LavaCell2 existingBelow = this.below;
            newCell.linkAbove(this);
            if(existingBelow != null)
            {
                existingBelow.linkAbove(newCell);
            }
        }
        
        return newCell;
        
    }
    
    /** 
     * If cell above is non-null and vertically adjacent, merges it into this cell and returns this cell.
     * Lava in cell above transfers to this cell.
     * Otherwise returns this cell.
     */
    private LavaCell2 checkForMergeUp()
    {
        return canMergeCells(this, this.above) ? mergeCells(this, this.above) : this;
    }
    
    /** 
     * If cell below is non-null and vertically adjacent, merges this cell into it and returns lower cell.
     * Lava in this cell transfers to cell below.
     * Otherwise returns this cell.
     */
    private LavaCell2 checkForMergeDown()
    {
        return canMergeCells(this.below, this) ? mergeCells(this.below, this) : this;
    }
    
    /**
     * Returns true if both cells are non-null and can be merged together.
     * Cells can be merged if no barrier between them 
     * and floor of top cell is at bottom of block or has melted.
     * If upper cell has any lava in it, we assume any flow floor has melted.
     */
    private static boolean canMergeCells(LavaCell2 lowerCell, LavaCell2 upperCell)
    {
        return lowerCell != null 
                && upperCell != null
                && lowerCell.topY() + 1 == upperCell.bottomY()
                && (upperCell.floorFlowHeight() == 0 || upperCell.getFluidUnits() > 0);
    }
    
    /** 
     * Merges upper cell into lower cell. 
     * All lava in upper cell is added to lower cell.
     * Returns the lower cell. 
     * Does no checking - call {@link #canMergeCells(LavaCell2, LavaCell2)} before calling this.
     */
    private static LavaCell2 mergeCells(LavaCell2 lowerCell, LavaCell2 upperCell)
    {
        if(upperCell.getFluidUnits() > 0)
        {
            // ensure lava blocks placed in world by upper cell are cleared by block next update
            lowerCell.notifySuspendedLava(upperCell.fluidSurfaceY());
            
            
            // add lava from upper cell if it has any
            if(upperCell.getFloor() - lowerCell.fluidSurfaceLevel() < AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS)
            {
                lowerCell.changeLevel(0, upperCell.getFluidUnits());
            }
            else
            {
                // Add at height only if fall distance is significant
                int topY = upperCell.fluidSurfaceY();
                int remaining = upperCell.getFluidUnits();
                for(int y = upperCell.bottomY(); y <= topY; y++)
                {
                    //handle strangeness that should never occur
                    if(remaining <= 0)
                    {
                        if(Adversity.DEBUG_MODE) Adversity.log.debug("Strange: Upper cell being merged at hieght ran out of lava before it reached fluid surface.");
                        
                        break;
                    }
                    
                    lowerCell.addLavaAtLevel(y * AbstractLavaSimulator.LEVELS_PER_BLOCK, y == topY ? remaining : AbstractLavaSimulator.FLUID_UNITS_PER_BLOCK);
                    remaining -=  AbstractLavaSimulator.FLUID_UNITS_PER_BLOCK;
                }
                
            }
         
        }
        upperCell.setDeleted();
        
        
        return lowerCell;
    }
    
    /**
     * Adds cell at the appropriate place in the linked list of cells.
     * Used in NBT load.  Should only be used when know that cell does not overlap existing cells.
     */
    public void addCellToColumn(LavaCell2 newCell)
    {
        newCell.locator = this.locator;
        
        synchronized(this.locator)
        {
            if(newCell.getFloor() < this.locator.firstCell.getFloor())
            {
                newCell.above = this.locator.firstCell;
                this.locator.firstCell.below = newCell;
                locator.firstCell = newCell;
            }
            else
            {
                LavaCell2 lowerCell = this.locator.firstCell;
                LavaCell2 upperCell = lowerCell.above;
                
                while(upperCell != null)
                {
                    if(newCell.getFloor() < upperCell.getFloor())
                    {
                        newCell.below = lowerCell;
                        lowerCell.above = newCell;
                        
                        newCell.above = upperCell;
                        upperCell.below = newCell;
                        
                        return;
                    }
                    lowerCell = upperCell;
                    upperCell = lowerCell.above;
                }
                
                // if we get to here, new cell is the uppermost
                newCell.below = lowerCell;
                lowerCell.above = newCell;
            }
        }

        
    }
    
    public void addConnection(LavaConnection2 connection)
    {
        this.connections.put(connection.getOther(this).id, connection);
    }
    
    public void removeConnection(LavaConnection2 connection)
    {
        this.connections.remove(connection.getOther(this).id);
    }
    
    public boolean isConnectedTo(LavaCell2 otherCell)
    {
        return this.connections.containsKey(otherCell.id);
    }
    
    /** 
     * Forms new connections if necessary.
     * Does NOT remove invalid connections. Invalid connections are expected to be removed during connection processing.
     */
    public void updateConnectionsIfNeeded(LavaCells cells, LavaConnections connections)
    {
        if(this.isConnectionUpdateNeeded)
        {
            int x = this.x();
            int z = this.z();
            this.updateConnectionsWithColumn(cells.getEntryCell(x - 1, z), connections);
            this.updateConnectionsWithColumn(cells.getEntryCell(x + 1, z), connections);
            this.updateConnectionsWithColumn(cells.getEntryCell(x, z - 1), connections);
            this.updateConnectionsWithColumn(cells.getEntryCell(x, z + 1), connections);
            this.isConnectionUpdateNeeded = false;
        }
    }
    
    /** 
     * Forms new connections with cells in the column with the given entry cell.
     * For use by updateConnectionsIfNeeded;
     */
    private void updateConnectionsWithColumn(LavaCell2 entryCell, LavaConnections connections)
    {
        if(entryCell == null) return;
        
        LavaCell2 candidate = entryCell.firstCell();
        
        // lets us know if a connection was found earlier so can stop once out of range for new
        boolean wasConnectionFound = false;
        
        while(candidate != null)
        {
            if(this.canConnectWith(candidate))
            {
                connections.createConnectionIfNotPresent(this, candidate);
                wasConnectionFound = true;
            }
            else if(wasConnectionFound)
            {
                // if connected earlier must be getting cells that are too high up now - stop
                return;
            }
                
            candidate = candidate.aboveCell();
        }
    }
    
    /** true if can be removed without losing anything */
    public boolean isUseless()
    {
        return this.fluidUnits == 0 && this.connections.isEmpty();
    }

    public boolean canConnectWith(LavaCell2 other)
    {
        return this.getFloor() < other.getCeiling()
                && this.getCeiling() > other.getFloor()
                && !this.isDeleted() && !other.isDeleted();
    }
    
    /**
     * For use when updating from world and no need to re-update world.
     */
    public void clearBlockUpdate()
    {
        this.avgFluidAmountWithPrecision = this.fluidUnits << 6;
        this.lastVisibleLevel = this.getCurrentVisibleLevel();
        this.isBlockUpdateCurrent = true;
    }
    
    /**
     * Just like fluidSurfaceLevel except based on exponential average.
     */
    public int getCurrentVisibleLevel()
    {
        return Math.min(this.getCeiling(), this.getFloor() + (this.avgFluidAmountWithPrecision >> 6) / AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL);
    }

    /**
     * Value that should be in the world. 
     */
    public int getLastVisibleLevel()
    {
        return this.lastVisibleLevel;
    }
    
    public static long computeKey(int x, int z)
    {
        return PackedBlockPos.pack(x, 0, z);  
    }
    
    @Override
    public int hashCode()
    {
        return this.id;
    }
    
    public int x()
    {
        return this.locator.x;
    }
    
    public int z()
    {
        return this.locator.z;
    }
    
    public long locationKey()
    {
        return this.locator.locationKey;
    }
    
    public LavaCell2 firstCell()
    {
        return this.locator.firstCell;
    }
    
    public LavaCell2 aboveCell()
    {
        return this.above;
    }
    
    /** 
     * Links cell to the given cell known to be just above it.
     * Link is both ways if the given cell is non-null. Thus no need for linkBelow method.
     * @param cellAbove  May be null - in which case simply sets above link to null if it was not already.
     */
    public void linkAbove(LavaCell2 cellAbove)
    {
        this.above = cellAbove;
        if(cellAbove != null) cellAbove.below = this;
    }
    
    public LavaCell2 belowCell()
    {
        return this.below;
    }
    
    /**
     * Assumes block updates will be applied to world/worldBuffer before any more world interaction occurs.
     * Consistent with this expectations, it sets lastVisibleLevel = currentVisibleLevel.
     * Returns the number of updates provided.
     */
    public void provideBlockUpdateIfNeeded(LavaSimulatorNew sim)
    {

        if(this.suspendedLevel != SUSPENDED_NONE)
        {
            //TODO: implement suspended lava clearing
            
        }
        
        if(isBlockUpdateCurrent) return;

        //        if(this.id == 1104 || this.id == 8187)
        //            Adversity.log.info("boop");

        // if we are empty always reflect that immediately - otherwise have ghosting in world as lava drains from drop cells
        if(this.fluidUnits == 0)
        {
            this.avgFluidAmountWithPrecision = 0;
            this.isBlockUpdateCurrent = true;
        }
        else
        {
            int avgAmount = this.avgFluidAmountWithPrecision >> 6;
    
            // don't average big changes
            if(Math.abs(avgAmount - this.fluidUnits) > AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL * 4)
            {
                this.avgFluidAmountWithPrecision = this.fluidUnits << 6;
                this.isBlockUpdateCurrent = true;
            }
            else
            {
                this.avgFluidAmountWithPrecision -= avgAmount; 
                this.avgFluidAmountWithPrecision += this.fluidUnits;
    
                if(this.avgFluidAmountWithPrecision  == this.fluidUnits << 6)
                {
                    this.isBlockUpdateCurrent = true;
                }
            }
        }

        int currentVisible = this.getCurrentVisibleLevel();
        if(this.lastVisibleLevel != currentVisible)
        {
            int lastTopY = getYFromCeiling(this.lastVisibleLevel);
            int newTopY = getYFromCeiling(currentVisible);

            IBlockState priorState = sim.worldBuffer.getBlockState(this.locator.x, newTopY, this.locator.z);
            
            sim.worldBuffer.setBlockState(this.locator.x, newTopY, this.locator.z, 
                    IFlowBlock.stateWithDiscreteFlowHeight(NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState(), currentVisible),
                    priorState);
            

            // if sunk, remove lava blocks from top
            if(lastTopY > newTopY)
            {
                for(int y = lastTopY; y > newTopY; y--)
                {
                    priorState = sim.worldBuffer.getBlockState(this.locator.x, y, this.locator.z);
                    sim.worldBuffer.setBlockState(this.locator.x, y, this.locator.z, Blocks.AIR.getDefaultState(), priorState);
                }
            }
            // implies have gone up one or more blocks
            // ensure full lava blocks below
            else if(lastTopY < newTopY)
            {
                for(int y = lastTopY; y < newTopY; y++)
                {
                    priorState = sim.worldBuffer.getBlockState(this.locator.x, y, this.locator.z);
                    sim.worldBuffer.setBlockState(this.locator.x, newTopY, this.locator.z, 
                            IFlowBlock.stateWithDiscreteFlowHeight(NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState(), FlowHeightState.BLOCK_LEVELS_INT),
                            priorState);
                }
            }

            this.lastVisibleLevel = currentVisible;
        }
    }
    
    /** pass tickIndex == 0 if calling outside of connection processing */
    public void changeLevel(int tickIndex, int amount)
    {
        if(amount != 0)
        {

            if(tickIndex != 0) this.lastTickIndex = tickIndex;

            if(amount > 0 && this.fluidUnits == 0)
            {
                //TODO: check for melting causing floor to merge with a non-barrier block below
                // should cause cell to merge will cell below if happens.
                // otherwise floor remains intact
            }
            
            this.fluidUnits += amount;

            isBlockUpdateCurrent = false;
            this.connections.values().forEach(c -> c.setDirty());


            if(this.fluidUnits < 0)
            {
                Adversity.log.info("Negative cell level detected: " + this.fluidUnits + " cellID=" + this.id);
                this.fluidUnits = 0;

                //force recalc of retained level when a cell becomes empty
                this.rawRetainedLevel = -1;
            }
            else if(this.fluidUnits == 0)
            {
                //force recalc of retained level when a cell becomes empty
                this.rawRetainedLevel = -1;
            }
        }
    }
        
    // CELL-COLUMN COORDINATION / SYNCHONIZATION CLASS
    
    static private class CellLocator
    {
        LavaCell2 firstCell;
        public final int x;
        public final int z;
        
        public final long locationKey;
        
        private CellLocator(int x, int z, LavaCell2 firstCell)
        {
            this.x = x;
            this.z = z;
            this.locationKey = LavaCell2.computeKey(x, z);
            this.firstCell = firstCell;
        }
    }
}
