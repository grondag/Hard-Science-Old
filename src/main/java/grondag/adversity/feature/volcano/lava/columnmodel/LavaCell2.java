package grondag.adversity.feature.volcano.lava.columnmodel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.ComparisonChain;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.lava.AbstractLavaSimulator;
import grondag.adversity.feature.volcano.lava.columnmodel.LavaConnections.SortBucket;
import grondag.adversity.library.ISimpleListItem;
import grondag.adversity.library.PackedBlockPos;
import grondag.adversity.library.Useful;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LavaCell2 implements ISimpleListItem
{
    private static AtomicInteger nextCellID = new AtomicInteger(0);
    
    public final int id = nextCellID.getAndIncrement();
    
    /**
     * True if locked for update via {@link #tryLock()}
     * and {@link #unlock()}.
     */
    private final AtomicBoolean isLocked = new AtomicBoolean(false);
    
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
    
    /** holds all connections with other cells */
    private Int2ObjectOpenHashMap<LavaConnection2> connections = new Int2ObjectOpenHashMap<LavaConnection2>();
    
    /** see {@link #getFloor()} */
    private int floor;
    
    /** see {@link #bottomY()} */
    private short bottomY;
    
    /** see {@link #getCeiling()} */
    private int ceiling;
    
    /** see {@link #topY()} */
    private short topY;
    
    /** see {@link #isBottomFlow()} */
    private boolean isBottomFlow;
    
    private boolean isCoolingDisabled = false;
    
    /** 
     * True if this cell is new or has expanded.
     * Used to determine if updateConnectionsIfNeeded should do anything.
     */
    private boolean isConnectionUpdateNeeded = true;
    
    /** value for {@link #lastVisibleLevel} indicating level has never been reported via {@link #provideBlockUpdateIfNeeded(LavaSimulatorNew)} */
    private static final int NEVER_REPORTED = -1;

    /**
     * Last level reported to world via {@link #provideBlockUpdateIfNeeded(LavaSimulatorNew)}.
     * Will be {@value #NEVER_REPORTED} if that method has not yet been called.
     */
    private int lastVisibleLevel = NEVER_REPORTED; 
    
    /** 
     * False if it is possible currentVisibleLevel won't match lastVisible 
     * Saves cost of calculating currentVisible if isn't necessary.
     */
    private boolean isBlockUpdateCurrent = true;
    
    /** true if this cell should remain loaded */
    private volatile boolean isActive = false;
    
    
    /**
     * Fluid units currently stored in this cell.
     */
    private volatile int fluidUnits;
    
    
    public static final short REFRESH_NONE = -1;
    /**
     * Use to signal that block levels may contain suspended lava or empty cells that should contain lava.
     * These blocks should be refreshed to world on the next block update.
     * Set to REFRESH_NONE if no blocks needing refresh are known to exist.
     */
    private short refreshTopY = REFRESH_NONE;
    
    /** 
     * See {@link #refreshTopY}
     */
    private short refreshBottomY = REFRESH_NONE;
    
    private static final int RETENTION_NEEDS_UPDATE = -1;
    
    /** 
     * Fluid level will not drop below this - to emulate surface tension/viscosity.
     * Represented as block levels. (Not as fluid units!)
     * Initialized to -1 to indicate has not yet been set or if needs to be recalculated.
     * Computed during first update after cell is first created.  Does not change until cell solidifies or bottom drops out.
     * The raw value is persisted because it should not change as neighbors change.
     */
    private int rawRetainedLevel = RETENTION_NEEDS_UPDATE;
    
    /** 
     * As with {@link #rawRetainedLevel} but smoothed with neighbors using a box filter.  
     * Is not persisted because can be recomputed from neighbors.
     * Is computed lazily as needed.  Invalidated whenever raw retention in this cell or a neighboring cell changes.
     */
    private int smoothedRetainedLevel = RETENTION_NEEDS_UPDATE;
    
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
//    private int lastMaxNeighborPressure;
    
    /**
     * Maximum pressure level reported by any connected neighbor during the current connection processing pass.
     * Set to 0 whenever propagate pressure sees a new stepIndex.
     */
//    private int nextMaxNeighborPressure;
    
    /**
     * The stepIndex that was last seen.
     * Step index will increment multiple times per tick.
     * When a new stepIndex is seen, is trigger to reset pressure propagation.
     */
//    private int lastStepIndex;
    
    /**
     * The simulation tick that was last seen.
     * Flow index will increment multiple times per tick.
     * When a new flowIndex is seen, is trigger to reset pressure propagation.
     */
    private int lastTickIndex;
    
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
    public LavaCell2(LavaCell2 existingEntryCell, int floor, int ceiling, int lavaLevel, boolean isFlowFloor)
    {
        this.locator = existingEntryCell.locator;
        this.setFloor(floor, isFlowFloor);
        this.setCeiling(ceiling);
        this.fluidUnits = Math.max(0, lavaLevel - floor);
        this.locator.cellChunk.cells.add(this);
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
        this.locator = new CellLocator(x, z, this, cells.getOrCreateCellChunk(x, z));
        this.setFloor(floor, isFlowFloor);
        this.setCeiling(ceiling);
        this.fluidUnits = Math.max(0, lavaLevel - floor) * AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL;
        cells.add(this);
        this.updateActiveStatus();
    }
    
    /** 
     * Attempts to lock this cell for update.
     * Returns true if lock was successful.
     * If AND ONLY IF successful, caller MUST call {@link #unLock()}
     * @return true if cell was successfully locked. 
     */
    public boolean tryLock()
    {
        return this.isLocked.compareAndSet(false, true);
    }
    
    /**
     * Unlocks this cell.  
     * MUST be called by a thread IF AND ONLY IF earlier call 
     * to {@link #tryLock()} was successful.
     * Does not track which thread owned the lock, so could be abused
     * to break a lock held by another thread. Don't do that. :-)
     */
    public void unlock()
    {
        this.isLocked.set(false);
    }
    
    /**
     * True if cells in this column have been marked for validation with world state.
     */
    public boolean isValidationNeeded()
    {
        return this.locator.isValidationNeeded();
    }
    
    /** 
     * Marks cells in this column for validation with world state.
     */
    public void setValidationNeeded(boolean isNeeded)
    {
        this.locator.setValidationNeeded(isNeeded);
    }    
    
    @Override
    public boolean isDeleted()
    {
        if(Adversity.DEBUG_MODE && !this.isDeleted && this.locator.cellChunk.isUnloaded())
        {
            Adversity.log.warn("Orphaned lava cell - cell not deleted but chunk is unloaded.");
        }
        return this.isDeleted;
    }
    
    /** Removes all lava and prevents further processing.
     *  Invalidates all connections but does not actually remove them.
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
        
        for (LavaConnection2 connection : this.connections.values())
        {
            connection.setDeleted();
        }
        
        this.updateActiveStatus();
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
            else if(y >= this.bottomY())
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
     * Enough to store 12 * 256 + 1.  
     * +1 because need to allow for value of -1.
     * Fills 20 bits, and leaves enough room to pack in another byte or so.
     */
    private static final int FLUID_LEVELS_MASK = 0xFFFFF;
    private static final int FLUID_LEVELS_BITS = 20;
    
    /** 
     * Enough to store 256 + 1.  
     * +1 because need to allow for value of -1.
     */
    private static final int BLOCK_LEVELS_MASK = 0x1FF;
//    private static final int BLOCK_LEVELS_BITS = 9;
    
    /** max value for ceiling */
    private static final int MAX_LEVEL = 256 * AbstractLavaSimulator.LEVELS_PER_BLOCK;
    
    /** 
     * Writes data to array starting at location i.
     */
    void writeNBT(int[] saveData, int i)
    {
         
        saveData[i++] = this.locator.x;
        saveData[i++] = this.locator.z;
        saveData[i++] = (this.fluidUnits & FLUID_UNITS_MASK) | (((this.refreshTopY + 1) & BLOCK_LEVELS_MASK) << FLUID_UNITS_BITS);
        saveData[i++] = ((this.rawRetainedLevel + 1) & FLUID_LEVELS_MASK) | (((this.refreshBottomY + 1) & BLOCK_LEVELS_MASK) << FLUID_LEVELS_BITS);
        
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
     */
    protected void readNBTArray(int[] saveData, int i)
    {
        // see writeNBT to understand how data are persisted
        // assumes first two positions (x and z) have already been read.
        
        int word = saveData[i++];
        this.fluidUnits = word & FLUID_UNITS_MASK;
        this.refreshTopY = (short) ((word >> FLUID_UNITS_BITS) - 1);
        
        word = saveData[i++];
        this.rawRetainedLevel = (word & FLUID_LEVELS_MASK) - 1;
        this.refreshBottomY = (short) ((word >> FLUID_LEVELS_BITS) - 1);
        
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
     * If cell has no fluid, is the level of the cell floor, as if it were fluid.
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
    
//    /** 
//     * Distance from this cell to the given space, in block levels.
//     * Space floor is exclusive, space ceiling in inclusive. Inputs are in block levels.
//     * Returns 0 if the space is adjacent or intersecting.
//     */
//    private int distanceToSpace(int spaceFloor, int spaceCeiling)
//    {
//        if(this.getFloor() > spaceCeiling)
//            return this.getFloor() - spaceCeiling;
//        
//        else if(this.getCeiling() < spaceFloor)
//            
//            return spaceFloor - this.getCeiling();
//        else
//            // intersects or adjacent
//            return 0; 
//            
//    }
    
    /** 
     * Finds the uppermost cell within this column that is below to the given level.
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
            
            this.floor = newFloor;
            this.isBottomFlow = isFlowFloor;
            this.bottomY = (short) getYFromFloor(this.floor);
            
            //force retention recalc
            this.invalidateRawRetention();
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
    
    /**
     * Locates neighboring lava cell that shares a floor surface with this cell.
     * Cells must connect to share a floor surface.
     * Diagonal neighbors must connect via one of the directly adjacent neighbors.
     * Most cells only connect with one other cell at a given offset and thus share the same floor.
     * But if there are two or more connecting, one must be higher than the other
     * and the lower cell is considered to be the neighboring floor for purposes of retention smoothing.  
     * 
     * @param xOffset must be in range -1 to +1
     * @param zPositive must be in range -1 to +1
     * @return LavaCell that was found, null if none was found, self if xOffset == 0 and zOffset == 0
     */
    private LavaCell2 getFloorNeighbor(int xOffset, int zOffset)
    {
        //handling is different for directly adjacent vs. diagonally adjacent
        if(xOffset == 0)
        {
            if(zOffset == 0)
            {
                return this;
            }
            else
            {
                return getLowestNeighborDirectlyAdjacent(this.locator.cellChunk.cells, xOffset, zOffset);
            }
        }
        else if(zOffset == 0)
        {
            return getLowestNeighborDirectlyAdjacent(this.locator.cellChunk.cells, xOffset, zOffset);
        }
        else
        {
            // diagonally adjacent
            LavaCells cells = this.locator.cellChunk.cells;

            LavaCell2 nX = getLowestNeighborDirectlyAdjacent(cells, xOffset, 0);
            if(nX != null)
            {
                nX = nX.getLowestNeighborDirectlyAdjacent(cells, xOffset, zOffset);
            }
            
            LavaCell2 nZ = getLowestNeighborDirectlyAdjacent(cells, 0, zOffset);
            if(nZ != null)
            {
                nZ = nZ.getLowestNeighborDirectlyAdjacent(cells, xOffset, zOffset);
            }
            
            if(nX == null) 
            {
                return nZ;
            }
            else if(nZ == null)
            {
                return nX;
            }
            else
            {
                return(nX.getFloor() < nZ.getFloor() ? nX : nZ);
            }
        }
    }
    
    private LavaCell2 getLowestNeighborDirectlyAdjacent(LavaCells cells, int xOffset, int zOffset)
    {
        LavaCell2 candidate = cells.getEntryCell(this.x() + xOffset, this.z() + zOffset);
        if(candidate == null) return null;
        
        candidate = candidate.findCellNearestY(this.bottomY());
        
        if(!candidate.isConnectedTo(this))
        {
            if(candidate.below != null && candidate.below.isConnectedTo(this))
            {
                candidate = candidate.below;
            }
            else if(candidate.above != null && candidate.above.isConnectedTo(this))
            {
                candidate = candidate.above;
            }
            else
            {
                return null;
            }
        }
        
        while(candidate != null && candidate.below != null && candidate.below.isConnectedTo(this))
        {
            candidate = candidate.below;
        }
        
        return candidate;
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
        this.topY = (short) getYFromCeiling(this.ceiling);
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
    
    /** cells should not meet - use this to assert */
    public boolean isVerticallyAdjacentTo(LavaCell2 other)
    {
        return  this.locator.x == other.locator.x 
                && this.locator.z == other.locator.z
                && this.isVerticallyAdjacentTo(other.getFloor(), other.getCeiling());
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
    public void addLavaAtLevel(int tickIndex, int level, int fluidUnits)
    {
        this.changeLevel(tickIndex, fluidUnits);
        
        //TODO: implement something more interesting, including persistence.
    }
    
    
    /** Use this to report when lava blocks are detected in the world within this cell.
     * If the reported lava exists above the cell's surface, they need to be set to air on next block update. 
     * See {@link #suspendedY}. 
     * Does not actually add any lava to this cell.
     */
    public void notifySuspendedLava(int y)
    {
        this.setRefreshRange(y, y);
    }
    
    private void doFallingParticles(int y, World world)
    {
        {
            double motionX = 0;
            double motionY = 0;
            double motionZ = 0;
            world.spawnParticle(
                  EnumParticleTypes.DRIP_LAVA, 
                  this.x() + 0.5, 
                  y + 0.5, 
                  this.z(), 
                  motionX, 
                  motionY, 
                  motionZ);
        }
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
     * Generally does not add or remove lava from cells - moves lava down if cells expand
     * down or if an upper cell with lava merges into a lower cell.
     * 
     * @param y  Location of space as world level
     * @param isFlowFloor  True if floorHeight = 0 and block below is flow block with height=12.  Should also be true of floorHeight > 0.
     * @param floorHeight  If is a partial solid flow block, height of floor within this y block
     * @return Cell to which the space belongs
     */
    public LavaCell2 addOrConfirmSpace(int y, int floorHeight, boolean isFlowFloor)
    {
        /**
         * Here are the possible scenarios:
         * 1) space is already included in this cell and floor is consistent or y is not at the bottom
         * 2) space is already included in this cell, but y is at the bottom and floor is different type
         * 3) space is adjacent to the top of this cell and need to expand up.
         * 4) space is adjacent to the bottom of this cell and need to expand down.
         * 5) One of scenarios 1-4 is true for a different cell.
         * 6) Scenarios 1-4 are not true for any cell, and a new cell needs to be added.
         *
         * In scenarios 2-5, if a newly expanded cell is vertically adjacent to another cell, the cells must be merged.
         * 
         * Note that partial spaces within this cell but above the floor will be handled as if they are full 
         * spaces. So, a less-than-full-height solid flow block in the middle of a cell would be handled
         * as if it were empty space.  This is because these blocks will melt if lava flows into their space.
         * If we did not handle it this way, it would be necessary to split the cell when these blocks
         * exist within a cell, only to have the cells merge back together as soon as lava flows into that block.
         * This same logic applies if the floor is already a partial floor and a space is added below.
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
            else
            {
                return this;
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
        
        // If space is not related to this cell, try again with the cell that is closest
        // We don't check this first because validation routine will try to position us
        // to the closest cell most of the time before calling, and thus will usually not be necessary.
        LavaCell2 closest = this.findCellNearestY(y);
        if(closest != this) return closest.addOrConfirmSpace(y, floorHeight, isFlowFloor);
        
        
        // if we get here, this is the closest cell and Y is not adjacent
        // therefore the space represents a new cell.
        
        LavaCell2 newCell = new LavaCell2(this, y * FlowHeightState.BLOCK_LEVELS_INT + floorHeight, (y + 1) * FlowHeightState.BLOCK_LEVELS_INT, 0, isFlowFloor);
        
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
        if(lowerCell == null || upperCell == null) return false;
        
        if(lowerCell.topY() >= upperCell.bottomY()) return true;
        
        if(lowerCell.topY() + 1 == upperCell.bottomY()
                && (upperCell.floorFlowHeight() == 0 || upperCell.getFluidUnits() > 0)) return true;
        
        return false;
    }
    
    /** 
     * Merges upper cell into lower cell. 
     * All lava in upper cell is added to lower cell.
     * Returns the lower cell. 
     * Does no checking - call {@link #canMergeCells(LavaCell2, LavaCell2)} before calling this.
     */
    private static LavaCell2 mergeCells(LavaCell2 lowerCell, LavaCell2 upperCell)
    {
        
        //change cell dimensions and fixup references
        lowerCell.setCeiling(upperCell.getCeiling());
        lowerCell.linkAbove(upperCell.above);
        
        if(upperCell.getFluidUnits() > 0)
        {
            // ensure lava blocks in world by upper cell are cleared by block next update
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
                    
                    lowerCell.addLavaAtLevel(lowerCell.locator.cellChunk.cells.sim.getTickIndex(), y * AbstractLavaSimulator.LEVELS_PER_BLOCK, y == topY ? remaining : AbstractLavaSimulator.FLUID_UNITS_PER_BLOCK);
                    remaining -=  AbstractLavaSimulator.FLUID_UNITS_PER_BLOCK;
                }
            }
        }
 
        // delete upper cell 
        upperCell.setDeleted();
        
        return lowerCell;
    }
    
    /** 
     * Splits the given cell into two cells and returns the upper cell. 
     * Cell is split by creating a barrier at Y.  
     * If flowHeight = 12, this is a full barrier, partial barrier otherwise.
     * If it is a full boundary, given cell must be at least 3 blocks high, 
     * for partial, given cell must be at least 2 blocks high.
     * 
     * Lava in volume of barrier is destroyed, rest of lava is distributed appropriately.
     * 
     * If the barrier is partial and lava would exist above it, the call is ignored
     * and the original cell returned, because the barrier would immediately melt and turn back into lava.
     * 
     * @param cells Needed to maintain cell array when new cell is created.
     * @param y  Location of space as world level
     * @param floorHeight  Height of barrier within the y block.  Should be 1-12.  12 indicates a full barrier.
     * @param isFlowFloor  True full barrier is a flow block with height=12.  Should also be true if floorHeight < 12.
     * @return Returns the upper cell that results from the split or given cell if split is not possible.
     */
    private LavaCell2 splitCell(int y, int flowHeight, boolean isFlowFloor)
    {
        // validity check: barrier has to be above my floor
        if(y == this.bottomY()) return this;
        
        boolean isFullBarrier = flowHeight == AbstractLavaSimulator.LEVELS_PER_BLOCK;
        
        // validity check: barrier has to be below my ceiling OR be a partial barrier
        if(isFullBarrier && y == this.topY()) return this;

        int newCeilingForThisCell = y * AbstractLavaSimulator.LEVELS_PER_BLOCK;
        int floorForNewCell = newCeilingForThisCell + flowHeight;
        // validity check: partial barriers within lava are ignored because they melt immediately
        if(!isFullBarrier && this.fluidSurfaceLevel() > floorForNewCell) return this;
        
        LavaCell2 newCell = new LavaCell2(this, floorForNewCell, this.getCeiling(), this.fluidSurfaceLevel(), isFlowFloor);
        if(this.fluidSurfaceLevel() > newCeilingForThisCell)
        {
            this.changeLevel(0, -(this.fluidSurfaceLevel() - newCeilingForThisCell) * AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL);
        }
        this.setCeiling(newCeilingForThisCell);
        newCell.linkAbove(this.above);
        this.linkAbove(newCell);
        return newCell;
    }
    
    /**
     * Confirms solid space within this cell stack. 
     * Shrinks, splits or removes cells if necessary.
     * 
     * Used to validate vs. world and to handle block events.
     * Does remove lava from cells if barrier is placed where lava should have been.
     * 
     * Unlike addOrConfirmSpace does not accept a flow height.  Solid blocks that 
     * are partially full should be confirmed with addOrConfirmSpace.
     * 
     * @param cells Needed to maintain cell array if cells must be split.
     * @param y  Location of space as world level
     * @param isFlowBlock  True if this barrier is a full-height flow block.
     * @return Cell nearest to the barrier location, or cell above it if two are equidistant. Null if no cells remain.
     */
    public LavaCell2 addOrConfirmBarrier(int y, boolean isFlowBlock)
    {
        /**
         * Here are the possible scenarios:
         * 1) this cell is closest to the barrier and y does not intersect with this cell- no action needed
         * 2) this cell is not the closest to the barrier - need to find that cell and retry
         * 3) barrier location is at the bottom of this cell - cell floor must be moved up
         * 4) barrier location is at the top of this cell - cell ceiling must be moved down
         * 5) barrier is in between floor and ceiling - cell must be split
         * 
         * Logic here borrows heavily from findCellNearestY.
         */
        
        int myDist = this.distanceToY(y);
        
        // intersects
        if(myDist == 0)
        {
            // remove lava if needed
            int surfaceY = this.fluidSurfaceY();
            if(y == surfaceY)
            {
                int flowHeight = this.fluidSurfaceFlowHeight();
                if(flowHeight > 0)
                {
                    this.changeLevel(this.locator.cellChunk.cells.sim.getTickIndex(), -flowHeight * AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL);
                }
            }
            else if( y < surfaceY)
            {
                this.changeLevel(this.locator.cellChunk.cells.sim.getTickIndex(), -AbstractLavaSimulator.FLUID_UNITS_PER_BLOCK);
            }
                
            if(y == this.topY())
            {
                if(y == this.bottomY())
                {
                    // removing last space in cell - cell must be deleted
                    LavaCell2 result = this.aboveCell() == null ? this.belowCell() : this.aboveCell();
                    this.setDeleted();
                    return result;
                }
                else
                {
                    // lower ceiling by one
                    this.setCeiling(y * AbstractLavaSimulator.LEVELS_PER_BLOCK);
                }
            }
            else if(y == this.bottomY())
            {
                // raise floor by one
                this.setFloor((y + 1) * AbstractLavaSimulator.LEVELS_PER_BLOCK, isFlowBlock);
            }
            else
            {
                // split & return upper cell
                return this.splitCell(y, AbstractLavaSimulator.LEVELS_PER_BLOCK, isFlowBlock);
            }
        }
        
        if(this.aboveCell() != null)
        {
            int aboveDist = this.aboveCell().distanceToY(y);
            if(aboveDist < myDist) return this.aboveCell().addOrConfirmBarrier(y, isFlowBlock);
        }
        
        if(this.belowCell() != null)
        {
            int belowDist = this.belowCell().distanceToY(y);
            if(belowDist < myDist) return this.belowCell().addOrConfirmBarrier(y, isFlowBlock);
        }
        
        // no adjacent cell is closer than this one - barrier must already be between cells
        return this;
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
     */
    private void updateConnectionsWithColumn(LavaCell2 entryCell, LavaConnections connections)
    {
        if(entryCell == null) return;
        
        LavaCell2 candidate = entryCell.firstCell();
        
        /** 
         * Tracks if connection was found earlier so can stop once out of range for new.
         */
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
    
    /** prevent massive garbage collection each tick */
    private static ThreadLocal<ArrayList<LavaConnection2>> sorter = new ThreadLocal<ArrayList<LavaConnection2>>() 
    {
        @Override
        protected ArrayList<LavaConnection2> initialValue() 
        {
           return new ArrayList<LavaConnection2>();
        }
     };
  
    
    /**
     * Assigns a sort bucket to each outbound connection.
     */
    public void prioritizeOutboundConnections()
    {
        ArrayList<LavaConnection2> sort = sorter.get();
        sort.clear();
        
        for(LavaConnection2 connection : this.connections.values())
        {
            if(connection.isActive())
            {
                LavaCell2 other = connection.getOther(this);
                if(other.getFloor() < this.getFloor())
                {
                    // this cell is responsible for sorting active connections that have a lower floor than it
                    sort.add(connection);
                }
                else if(other.getFloor() == this.getFloor())
                {
                    // if floors are the same, the cell that is "first" handles sorting
                    if(connection.firstCell == this) sort.add(connection);
                }
            }
        
        }
        
        if(sort.size() > 0)
        {
            sort.sort(new Comparator<LavaConnection2>()
            {
                @Override
                public int compare(LavaConnection2 o1, LavaConnection2 o2)
                {
                    return ComparisonChain.start()
                            // larger drops first
                            .compare(o2.getSortDrop(), o1.getSortDrop())
                            // random breaks ties
                            .compare(o1.rand, o2.rand)
                            .result();
                }
            });
            
            for(int i = 0; i < sort.size(); i++)
            {
                // Don't think it is even possible for a cell to have more than four neighbors with a lower or same floor, but in case I'm wrong...
                // For cells with more than four outbound connections, all connections beyond the first four get dropped in the last bucket.
                sort.get(i).setSortBucket(i < 4 ? SortBucket.values()[i] : SortBucket.D);
            }
        }
    }
    
    /**
     * Called once per tick for each cell before simulation steps are run.
     * Use for housekeeping tasks.
     */
    public void update(LavaSimulatorNew sim, LavaCells cells, LavaConnections connections)
    {
        this.updateConnectionsIfNeeded(cells, connections);
        
        this.updateRawRetentionIfNeeded();
        
        // TODO: Pressure propagation
    }
    
    /** maintains indication of whether or not this cell must remain loaded */
    public void updateActiveStatus()
    {
        boolean shouldBeActive = !this.isDeleted && (this.fluidUnits > 0 || !this.isBlockUpdateCurrent);
        
        if(this.isActive)
        {
            if(!shouldBeActive) 
            {
                this.locator.cellChunk.decrementActiveCount();
                this.isActive = false;
            }
        }
        else // cell is not active
        {
            if(shouldBeActive) 
            {
                this.locator.cellChunk.incrementActiveCount();
                this.isActive = true;
            }
        }
    }

    public boolean canConnectWith(LavaCell2 other)
    {
        return this.getFloor() < other.getCeiling()
                && this.getCeiling() > other.getFloor()
                && !this.isDeleted() && !other.isDeleted();
    }
    
    /**
     * True if this cell has not had a flow in a configured number of ticks
     * and it has fewer than four connections to cells that are also lava. (Is on an edge.)
     */
    public boolean canCool(int simTickIndex)
    {
        //TODO: make ticks to cool configurable
        if(this.isCoolingDisabled || this.isDeleted || this.getFluidUnits() == 0 || simTickIndex - this.lastTickIndex < 20000) return false;
        
        if(this.connections.size() < 4) return true;
        
        int hotCount = 0;
        for(LavaConnection2 c : this.connections.values())
        {
            if(c.getOther(this).getFluidUnits() > 0) hotCount++;
            if(hotCount >= 4) return false;
        }
        
        return true;
    }
    
    /** 
     * Removes all lava from this cell and raises the floor as if the lava has cooled.
     * Does not perform any block updates to change lava to basalt in world. 
     * Caller is expected to do that after calling canCool and before calling this method.
     */
    public void coolAndShrink()
    {
        if(this.isDeleted) return;
        
        int newFloor = this.fluidSurfaceLevel();
        if(newFloor >= this.getCeiling())
        {
            this.setDeleted();
        }
        else
        {
            this.changeLevel(0, -this.getFluidUnits());
            this.clearBlockUpdate();
            this.setFloor(newFloor, true);
        }
    }
    
    /**
     * For use when updating from world and no need to re-update world.
     */
    public void clearBlockUpdate()
    {
        this.avgFluidAmountWithPrecision = this.fluidUnits << 6;
        this.lastVisibleLevel = this.getCurrentVisibleLevel();
        this.isBlockUpdateCurrent = true;
        this.updateActiveStatus();
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
        if(this.lastVisibleLevel == NEVER_REPORTED)
        {
            return Math.min(this.getCeiling(), this.getFloor() + this.fluidUnits / AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL); 
        }
        else
        {
            return this.lastVisibleLevel;
        }
    }
        
    private void invalidateRawRetention()
    {
        this.rawRetainedLevel = RETENTION_NEEDS_UPDATE;
        invalidateSmoothedRetention();
    }
    
    /** see {@link #rawRetainedLevel} */
    public int getRawRetainedLevel()
    {
        if(this.rawRetainedLevel == RETENTION_NEEDS_UPDATE)
        {
            if(Adversity.DEBUG_MODE) Adversity.log.warn("Raw retention update requested ad-hoc - raw retention update should occur during cell update."
                    + "This may result in concurrent access to minecraft world objects.");
            
            this.updateRawRetention();
        }
        return this.rawRetainedLevel;
    }
    
    /** see {@link #rawRetainedLevel} */
    public void updateRawRetentionIfNeeded()
    {
        if(this.rawRetainedLevel == RETENTION_NEEDS_UPDATE)
        {
            this.updateRawRetention();
        }
    }
    
    /** see {@link #rawRetainedLevel} */
    private void updateRawRetention()
    {
        int depth = this.isBottomFlow() 
                ? this.getFlowFloorRawRetentionDepth()
                : (int)(locator.cellChunk.cells.sim.terrainHelper
                        .computeIdealBaseFlowHeight(PackedBlockPos.pack(this.x(), this.bottomY(), this.z()))
                        * AbstractLavaSimulator.LEVELS_PER_BLOCK);
                
        this.rawRetainedLevel = this.getFloor() + depth;
    }
    
    /**
     * Returns retained depth of lava on the given flow block in block levels.
     */
    private int getFlowFloorRawRetentionDepth()
    {
        if(Adversity.DEBUG_MODE && !this.isBottomFlow()) 
            Adversity.log.warn("Flow floor retention depth computed for non-flow-floor cell.");
        
        int myFloor = this.getFloor();
        
        int floorMin = Math.max(0, (this.bottomY() - 2) * AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS);
        int floorMax = Math.min(MAX_LEVEL, (this.bottomY() + 3) * AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS);
        
        int x = this.x();
        int z = this.z();
        
        int negX = Useful.clamp(getFloorForNeighbor(x - 1, z, myFloor ), floorMin, floorMax);
        int posX = Useful.clamp(getFloorForNeighbor(x + 1, z, myFloor ), floorMin, floorMax);
        int negZ = Useful.clamp(getFloorForNeighbor(x, z - 1, myFloor ), floorMin, floorMax);
        int posZ = Useful.clamp(getFloorForNeighbor(x, z + 1, myFloor ), floorMin, floorMax);
        
        int negXnegZ = Useful.clamp(getFloorForNeighbor(x - 1, z - 1, myFloor ), floorMin, floorMax);
        int negXposZ = Useful.clamp(getFloorForNeighbor(x - 1, z + 1, myFloor ), floorMin, floorMax);
        int posXnegZ = Useful.clamp(getFloorForNeighbor(x + 1, z - 1, myFloor ), floorMin, floorMax);
        int posXposZ = Useful.clamp(getFloorForNeighbor(x + 1, z + 1, myFloor ), floorMin, floorMax);

        // Normalize the resulting delta values to the approximate range -1 to 1
        float deltaX = (posXnegZ + posX + posXposZ - negXnegZ - negX - negXposZ) / 6F / AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS;
        float deltaZ = (negXposZ + posZ + posXposZ - negXnegZ - negZ - negXnegZ) / 6F / AbstractLavaSimulator.LEVELS_PER_TWO_BLOCKS;
        double slope = Useful.clamp(Math.sqrt(deltaX * deltaX + deltaZ * deltaZ), 0.0, 1.0);
      
        int depth = (int) (AbstractLavaSimulator.LEVELS_PER_BLOCK * (1.0 - slope));
        
        // Abandoned experiment...
        // this function gives a value of 1 for slope = 0 then drops steeply 
        // as slope increases and then levels off to 1/4 height as slope approaches 1.
        // Function is only well-behaved for our purpose within the range 0 to 1.
        // More concisely, function is (1-sqrt(x)) ^ 2, applied to the top 3/4 of a full block height.
        // int depth = (int) (0.25 + 0.75 * Math.pow(1 - Math.sqrt(slope), 2));
        
        //clamp to at least 1/4 of a block and no more than 1.25 block
        depth = Useful.clamp(depth, AbstractLavaSimulator.LEVELS_PER_QUARTER_BLOCK, AbstractLavaSimulator.LEVELS_PER_BLOCK_AND_A_QUARTER);
      
        return depth;
    }
    
    /** 
     * For use by getFlowFloorRawRetentionDepth.
     * Returns default value if neighbor is null.
     */
    private int getFloorForNeighbor(int xOffset, int zOffset, int defaultValue)
    {
        LavaCell2 neighbor = this.getFloorNeighbor(xOffset, zOffset);
        return neighbor == null ? defaultValue : neighbor.getFloor();
    }
    
    /** see {@link #smoothedRetainedLevel} */
    public int getSmoothedRetainedLevel()
    {
        if(this.smoothedRetainedLevel == RETENTION_NEEDS_UPDATE)
        {
            this.updatedSmoothedRetention();
        }
        return this.smoothedRetainedLevel;
    }

    /** see {@link #smoothedRetainedLevel} */
    public void invalidateSmoothedRetention()
    {
        this.smoothedRetainedLevel = RETENTION_NEEDS_UPDATE;
    }

    /** see {@link #smoothedRetainedLevel} */
    public void updatedSmoothedRetentionIfNeeded()
    {
        if(this.smoothedRetainedLevel == RETENTION_NEEDS_UPDATE)
        {
            this.updatedSmoothedRetention();
        }
    }

    /** see {@link #smoothedRetainedLevel} */
    private void updatedSmoothedRetention()
    {
        //TODO: retention smoothing, this is a stub
        this.smoothedRetainedLevel = this.rawRetainedLevel;
    }
    
    public void setRefreshRange(int yLow, int yHigh)
    {
        if(this.refreshBottomY == REFRESH_NONE || yLow < this.refreshBottomY)
        {
            this.refreshBottomY = (short) yLow;
        }
     
        if(this.refreshTopY == REFRESH_NONE || yHigh > this.refreshTopY)
        {
            this.refreshTopY = (short) yHigh;
        }
    }
    
    public void clearRefreshRange()
    {
        this.refreshBottomY = REFRESH_NONE;
        this.refreshTopY = REFRESH_NONE;
    }
    
    public boolean hasRefreshRange()
    {
        return this.refreshBottomY != REFRESH_NONE && this.refreshTopY != REFRESH_NONE;
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
     * Also refreshes world for any blocks reported as suspended or destroyed and calls {@link #clearRefreshRange()}
     */
    public void provideBlockUpdateIfNeeded(LavaSimulatorNew sim)
    {
        if(this.isDeleted) return;
        
        if(!isBlockUpdateCurrent) 
        {
            // if we are empty always reflect that immediately - otherwise have ghosting in world as lava drains from drop cells
            if(this.fluidUnits == 0)
            {
                this.avgFluidAmountWithPrecision = 0;
                this.isBlockUpdateCurrent = true;
            }
            else
            {
                final int avgAmount = this.avgFluidAmountWithPrecision >> 6;
        
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
    
            // needed because block update status is a factor for active status
            if(this.isBlockUpdateCurrent) this.updateActiveStatus();
        }
        
        final int lastVisible = this.getLastVisibleLevel();
        final int currentVisible = this.getCurrentVisibleLevel();
        // Need to constrain to bottomY() because getYFromCeiling will return block below our floor if
        // we are empty and floor is at a block boundary.
        final int currentSurfaceY = Math.max(this.bottomY(), getYFromCeiling(currentVisible));
        
        int bottomY = 256;
        int topY = -1;
        boolean shouldGenerate = false;
        
        if(lastVisible != currentVisible)
        {
            shouldGenerate = true;
            int lastSurfaceY = Math.max(this.bottomY(), getYFromCeiling(lastVisible));
            bottomY = Math.min(lastSurfaceY, currentSurfaceY);
            topY = Math.max(lastSurfaceY, currentSurfaceY);
            this.lastVisibleLevel = currentVisible;
        }
        
        if(this.hasRefreshRange())
        {
            shouldGenerate = true;
            bottomY = Math.min(bottomY, this.refreshBottomY);
            topY = Math.max(topY, this.refreshTopY);
            this.clearRefreshRange();
        }
        
        if(shouldGenerate)
        {
            final boolean hasLava = this.fluidUnits > 0;
            
            for(int y = bottomY; y <= topY; y++)
            {
                if(hasLava && y == currentSurfaceY)
                {
                    IBlockState priorState = sim.worldBuffer.getBlockState(this.locator.x, y, this.locator.z);
                    
                    sim.worldBuffer.setBlockState(this.locator.x, y, this.locator.z, 
                            IFlowBlock.stateWithDiscreteFlowHeight(NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState(), currentVisible - currentSurfaceY * FlowHeightState.BLOCK_LEVELS_INT),
                            priorState);
                }
                else if(hasLava && y < currentSurfaceY)
                {
                    IBlockState priorState = sim.worldBuffer.getBlockState(this.locator.x, y, this.locator.z);
                    sim.worldBuffer.setBlockState(this.locator.x, y, this.locator.z, 
                            IFlowBlock.stateWithDiscreteFlowHeight(NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState(), FlowHeightState.BLOCK_LEVELS_INT),
                            priorState);
                }
                else
                {
                    IBlockState priorState = sim.worldBuffer.getBlockState(this.locator.x, y, this.locator.z);
                    sim.worldBuffer.setBlockState(this.locator.x, y, this.locator.z, Blocks.AIR.getDefaultState(), priorState);
                }
            }
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
                this.invalidateRawRetention();
            }
            else if(this.fluidUnits == 0)
            {
                //force recalc of retained level when a cell becomes empty
                this.invalidateRawRetention();
            }
            
            this.updateActiveStatus();
        }
    }
    
    /**
     * Called when lava is placed via a world event.
     * If lava does not already exist at level, adds lava to cell in appropriate way.
     * If lava does already exist, does nothing.
     * Also does nothing if location is partially occupied - indicates cell should be revalidated vs. world.
     * 
     * @param y  World y level of lava block.
     * @param flowHeight Height (1-12) of placed lava block.
     * @return true if successful, false if not. If false, chunk should be revalidated.
     */
    public boolean notifyPlacedLava(int tickIndex, int y, int flowHeight)
    {
        
        /**
         * Paths
         * -----
         * Lava already exists at same or higher level - no action
         * Lava already exists but at a lower level - increase level
         * Lava does not exist and is a fully open space close to surface - increase level, adjust visible height
         * Lava does not exist and is a fully open space above surface - create falling particles
         * 
         * Space is outside this cell - super strange - re-validate this chunk
         * Lava does not exist and space is a partial barrier at floor - super strange - re-validate this chunk
         */
        
        //handle strangeness
        if(y > this.topY() || y < this.bottomY()) return false;
        
        // handle more strangeness - partially solid floors should not normally be replaced by block events
        // if this happens, best to revalidate this chunk
        if(y == this.bottomY() && this.floorFlowHeight() != 0) return false;
        
        if(y == this.fluidSurfaceY())
        {
            int levelDiff = flowHeight - this.fluidSurfaceFlowHeight();
            if(levelDiff > 0)
            {
                this.changeLevel(tickIndex, levelDiff * AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL);
            }
        }
        else if(y > this.fluidSurfaceY())
        {
            this.notifySuspendedLava(y);
            this.addLavaAtLevel(tickIndex, y * AbstractLavaSimulator.LEVELS_PER_BLOCK, flowHeight * AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL);
        }
        
        return true;
   
    }
    
    /**
     * Called when lava is destroyed via a world event.
     * If lava does not already exist at level, does nothing.
     * If lava does already exist, removes an appropriate amount of lava from this cell.
     * 
     * @param y  World y level of lava block destroyed.
     */
    public void notifyDestroyedLava(int y)
    {
        //handle strangeness
        if(y > this.topY() || y < this.bottomY()) return;
        
        if(y == this.fluidSurfaceY())
        {
            this.changeLevel(this.locator.cellChunk.cells.sim.getTickIndex(), -this.fluidSurfaceFlowHeight() * AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL);
        }
        else if(y < this.fluidSurfaceY())
        {
            this.changeLevel(this.locator.cellChunk.cells.sim.getTickIndex(), -AbstractLavaSimulator.FLUID_UNITS_PER_BLOCK);
        }
    }
        
    // CELL-COLUMN COORDINATION / SYNCHONIZATION CLASS
    
    static private class CellLocator
    {
        LavaCell2 firstCell;
        public final int x;
        public final int z;
        
        public final long locationKey;
        
        /** True if cells in this column should be validated with world state */
        private boolean isValidationNeeded = false;
        
        /**
         * Reference to cell chunk where this cell column lives.
         */
        public final CellChunk cellChunk;
        
        private CellLocator(int x, int z, LavaCell2 firstCell, CellChunk cellChunk)
        {
            this.x = x;
            this.z = z;
            this.cellChunk = cellChunk;
            this.locationKey = LavaCell2.computeKey(x, z);
            this.firstCell = firstCell;
        }

        public void setValidationNeeded(boolean isNeeded)
        {
            // when first marked for validation, increment validation request count with cell chunk
            if(isNeeded & !this.isValidationNeeded) this.cellChunk.incrementValidationCount();
            
            this.isValidationNeeded = isNeeded;
        }

        public boolean isValidationNeeded()
        {
            return this.isValidationNeeded;
        }
    }
}
