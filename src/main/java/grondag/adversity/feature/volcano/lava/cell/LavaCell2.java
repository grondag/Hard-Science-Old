package grondag.adversity.feature.volcano.lava.cell;

import java.util.concurrent.atomic.AtomicInteger;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.lava.AbstractLavaSimulator;
import grondag.adversity.feature.volcano.lava.LavaConnection2;
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
    
    /** used by collection for locating in storage array */
    volatile int index = -1;
    
    private Int2ObjectOpenHashMap<LavaConnection2> connections = new Int2ObjectOpenHashMap<LavaConnection2>();
    
    /** see {@link #getFloor()} */
    private int floor;
    
    /** see {@link #floorY()} */
    private byte floorY;
    
    /** see {@link #getCeiling()} */
    private int ceiling;
    
    /** see {@link #topY()} */
    private byte topY;
    
    /** see {@link #isBottomFlow()} */
    private boolean isBottomFlow;
    private boolean neverCools;
    
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
    
    /** 
     * Returns cell at given y block position if it exists.
     * Thread-safe.
     */
    LavaCell2 getCellIfExists(LavaSimulatorNew sim, int y)
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
                    else if(y > nextUp.floorY())
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
            else if(y > this.floorY())
            {
                return this;
            }
            else
            {
                LavaCell2 nextDown = this.below;
                while(nextDown != null)
                {
                    if(y < nextDown.floorY())
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
    
//    /** 
//     * Returns cell at given y block position, creating it 
//     * or expanding an existing cell if cell does not already exist.
//     * Returns null if space cannot contain lava.
//     * 
//     * NOT INTENDED FOR CONCURRENT ACCESS
//     * Synchronized to worldBuffer to prevent multiple threads hitting MC world.
//     */
//    LavaCell2 getCell(LavaSimulatorNew sim, LavaCells cells, int y)
//    {
//        synchronized(sim.worldBuffer)
//        {
//            if(y > this.topY())
//            {
//                LavaCell2 nextUp = this.above;
//                LavaCell2 lastUp = this;
//                while(nextUp != null)
//                {
//                    if(y > nextUp.topY())
//                    {
//                        // keep going up
//                        lastUp = nextUp;
//                        nextUp = nextUp.above;
//                    }
//                    else if(y > nextUp.getFloorY())
//                    {
//                        // found a match
//                        return nextUp;
//                    }
//                    else
//                    {
//                        // next up is too high, so must be between last and next
//                        return addOrConfirmSpaceAtY(sim, y, lastUp, nextUp );
//                    }
//                }
//                // reached the top cell and still not found
//                return addOrConfirmSpaceAtY(sim, y, lastUp, null);
//            }
//            else if(y > this.getFloorY())
//            {
//                return this;
//            }
//            else
//            {
//                LavaCell2 nextDown = this.below;
//                LavaCell2 lastDown = this;
//                while(nextDown != null)
//                {
//                    if(y < nextDown.getFloorY())
//                    {
//                        // keep going down
//                        lastDown = nextDown;
//                        nextDown = nextDown.below;
//                    }
//                    else if(y <= nextDown.topY())
//                    {
//                        // found a match
//                        return nextDown;
//                    }
//                    else
//                    {
//                        // next down is too low, so must between last and next
//                        return addOrConfirmSpaceAtY(sim, y, nextDown, lastDown );
//                    }
//                }
//                // reached the bottom cell and still not found
//                return addOrConfirmSpaceAtY(sim, y, null, lastDown);
//            }
//        }
//    }
    
    /**
     * Confirms the space at Y is part of a cell.  Validates and returns if so. 
     * If it is not a lava space and a cell exists, shrinks and/or splits the cell that was there.
     * If so, adds space to the cell above or below if they are non-null and adjacent.
     * If new space is adjacent to both above and below, merges them and returns the merged cell.
     * In case of merge, new cell inherits links of cells above and below, as appropriate.
     * If no non-null, adjacent cell is found, creates a new cell, linking it to the others if they are non-null.
     * Returns null if not.


     */
    private static LavaCell2 getCellFromWorldAtY(LavaSimulatorNew sim, int y, LavaCell2 cellBelow, LavaCell2 cellAbove)
    {
        return null;
    }
    
    public LavaCell2(LavaSimulatorNew sim, BlockPos pos)
    {
        this(pos.getX(), pos.getZ());
    }
 
    public LavaCell2(LavaSimulatorNew sim, long packedBlockPos)
    {
        this(PackedBlockPos.getX(packedBlockPos), PackedBlockPos.getZ(packedBlockPos));
    }
    
    private LavaCell2(CellLocator locator, int floor, int ceiling, int lavaLevel, boolean isFlowFloor)
    {
        this.locator = locator;
        this.setFloor(floor, isFlowFloor);
        this.setCeiling(ceiling);
        this.fluidUnits = Math.max(0, lavaLevel - floor);
    }
    
    public LavaCell2(int x, int z)
    {
        this.locator = new CellLocator(x, z, this);
    }
    
    /** 
     * Reads data from array starting at location i.
     */
    LavaCell2(int[] saveData, int i)
    {
        // see writeNBT to understand how data are persisted
        this.locator = new CellLocator(saveData[i++], saveData[i++], this);
        this.fluidUnits = saveData[i++];
        this.rawRetainedLevel = saveData[i++];
        int combinedBounds = saveData[i++];
        
        if(combinedBounds < 0)
        {
            combinedBounds = -combinedBounds;
            this.isBottomFlow = true;
        }
        else
        {
            this.isBottomFlow = false;
        }
        
        this.setFloor(combinedBounds & 0xFFF);
        this.setCeiling(combinedBounds >> 12);

        this.lastTickIndex = saveData[i++];
        if(this.lastTickIndex < 0)
        {
            this.lastTickIndex = -this.lastTickIndex;
            this.neverCools = true;
        }
        else
        {
            this.neverCools = false;
        }
    }
    
    static final String LAVA_CELL_NBT_TAG = "lavacells";
    static final int LAVA_CELL_NBT_WIDTH = 6;

    /** 
     * Writes data to array starting at location i.
     */
    void writeNBT(int[] saveData, int i)
    {
         
        saveData[i++] = this.locator.x;
        saveData[i++] = this.locator.z;
        saveData[i++] = this.fluidUnits;
        saveData[i++] = this.rawRetainedLevel;
        
        // to save space, pack bounds into single int and save flow floor as sign bit
        int combinedBounds = this.getCeiling() << 12 | this.getFloor();
        if(this.isBottomFlow) combinedBounds = -combinedBounds;
        saveData[i++] = combinedBounds;
        
        // save never cools as sign bit on last tick index
        saveData[i++] = this.neverCools ? -this.lastTickIndex : this.lastTickIndex;
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
    
    /** 
     * Returns distance from this cell to the given region. 
     * If this cell intersects with the given region, returns -1 
     */
    private int distanceOrIntersection(int floor, int ceiling)
    {
        
        /**
         * Examples:
         * cellFloor    cellCeiling     floor       ceiling     result      f-cc            cf - c
         * ==================================================================================================
         * 12           24              0           6           6           0-24 = -24      12-6 = 6
         * 12           24              0           12          0           0-24 = -24      12-12 = 0
         * 12           24              0           18          -1          0-24 = -24      12-18 = -6
         * 12           24              0           32          -1         0-24 = -24      12-32 = -24
         * 12           24              13          16          -1         13-24 = -11     12-16 = -4
         * 12           24              13          32          -1         13-24 = -11     12-32 = -20
         * 12           24              25          48          1           24-24 = 1       12-48 = -36
         */
        
        
        int topDist = floor - this.getCeiling();
        // region is above the cell
        if(topDist >= 0) return topDist;
        
        int bottomDist = this.getFloor() - ceiling;
        // region is below this cell
        if(bottomDist >= 0) return bottomDist;
        
        //if both values negative indicates intersection
       return -1;
    }
    
    /** 
     * Adds new cells in this column or expands or merges existing cells as needed.
     * Ceiling is inclusive, floor is not.
     * Returns cell that should be used as starting reference for this column.
     */
    public LavaCell2 addOrConfirmSpace(int floor, int ceiling, int lavaLevel, boolean isFlowFloor)
    {
        synchronized(this.locator)
        {
            // find the cell below this space, if there is one
            LavaCell2 below = this.findNearestCellBelowLeve(floor);
            
            if(below == null)
            {
                // No existing cell below this space, so result depends 
                // on where space stands related to the first (lowest) existing cell.
                LavaCell2 first = this.locator.firstCell;
                
                if(first.floor > ceiling)
                {
                    // space is below the first cell, and thus the new first (lowest) cell
                    LavaCell2 newCell = new LavaCell2(this.locator, floor, ceiling, lavaLevel, isFlowFloor);
                    newCell.above = this.locator.firstCell;
                    newCell.above.below = newCell;
                    this.locator.firstCell = newCell;
                    return this;
                }
                else if(first.floor == ceiling)
                {
                    // space is below and adjacent to the first cell and thus should expand it
                    first.setFloor(floor, isFlowFloor);
                    first.fluidUnits += Math.max(0, lavaLevel - floor);
                    return this;
                    
                }
                else
                {
                    // space intersects with the first cell
                    // so expand first cell to include entire region
                    
                    // if first cell now intersects with cells above it, merge them in also
                    return first.mergeUpInRegsion()
                } 
            }
        
        
            // if intersects or is adjacent to two cells, expand and merge those cells
            // lower cell wins
            // confirm lava level in merged cell
            
            // if intersects or is adjacent to a single cell, expand that cell
            // confirm lava level in expanded cell
            
            // if no intersection, insert a new cell in the chain
            
            
            if(this.isVerticallyAdjacentTo(floor, ceiling) || this.intersectsWith(floor, ceiling))
            {
                this.setCeiling(Math.max(this.getCeiling(), ceiling));
                this.setFloor(Math.min(this.getFloor(), floor));
                return true;
            }
            else
            {
                return false;
            }
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
    private void  setFloor(int newFloor, boolean isFlowFloor)
    {
        if(newFloor != this.floor || isFlowFloor != this.isBottomFlow)
        {
            //force retention recalc
            this.rawRetainedLevel = -1;
            this.floor = newFloor;
            this.isBottomFlow = isFlowFloor;
            this.floorY = (byte) getYFromFloor(this.floor);
        }
    }
    
    /** 
     * First (lowest) fluid level that can contain fluid, EXCLUSIVE.
     * Values range from 0 to (256 * LEVELS_PER_BLOCK) - 1 
     * Levels in Y=0 for example, are 0 thru 11.
     * ALWAYS USE setFloor() instead of floor to maintain bottomY.
     */
    int getFloor()
    {
        return this.floor;
    }
    
    /** Y of first (lowest) block that could contain lava */
    public int floorY()
    {
        return this.floorY; 
    }
   
    /** Flow height of solid portion of block at {@link #floorY()}
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
    private void  setCeiling(int newCeiling)
    {
        this.ceiling = newCeiling;
        this.topY = (byte) getYFromCeiling(this.ceiling);
    }
    
    /** 
     * Last (highest) block level that can contain fluid, INCLUSIVE.
     * Values range from 1 to (256 * LEVELS_PER_BLOCK)
     * Levels in Y=0, for example are 1 thru 12.
     * ALWAYS USE setCeiling() to maintain topY.
     */
    int getCeiling()
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
    
    /** true if can be removed without losing anything */
    public boolean isUseless()
    {
        return this.fluidUnits == 0 && this.connections.isEmpty();
    }

    public boolean canConnectWith(LavaCell2 other)
    {
        return this.getFloor() < other.getCeiling()
                && this.getCeiling() > other.getFloor();
    }
    
    /**
     * For use when updating from world and no need to re-update world.
     */
    public void clearBlockUpdate(LavaSimulatorNew sim)
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
    
    /**
     * Assumes block updates will be applied to world/worldBuffer before any more world interaction occurs.
     * Consistent with this expectations, it sets lastVisibleLevel = currentVisibleLevel.
     * Returns the number of updates provided.
     */
    public void provideBlockUpdateIfNeeded(LavaSimulatorNew sim)
    {

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
