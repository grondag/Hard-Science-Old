package grondag.adversity.feature.volcano.lava.simulator;

import java.util.concurrent.atomic.AtomicInteger;

import grondag.adversity.Adversity;
import grondag.adversity.library.ISimpleListItem;
import grondag.adversity.niceblock.modelstate.FlowHeightState;

public abstract class AbstractLavaCell
{
    private static AtomicInteger nextCellID = new AtomicInteger(0);
    
    public final int id = nextCellID.getAndIncrement();
    
    /** 
     * True if this cell is new or has expanded.
     * Used to determine if updateConnectionsIfNeeded should do anything.
     */
    private boolean isConnectionUpdateNeeded = true;
    
    public void invalidateConnections() { if(!this.isConnectionUpdateNeeded) this.isConnectionUpdateNeeded = true; }
    public boolean isConnectionUpdateNeeded() { return this.isConnectionUpdateNeeded; }
    public void clearConnectionUpdate() { if(this.isConnectionUpdateNeeded) this.isConnectionUpdateNeeded = false; }
    
    /** see {@link #getFloor()} */
    private int floor;
    
    /** 
     * Floor as fluid units instead of levels - derived from floor each time floor is set.
     * Here to avoid multiplying floor each connection pass.
     */
    protected int floorUnits;
    
    /** see {@link #bottomY()} */
    private short bottomY;
    
    /** see {@link #getCeiling()} */
    private int ceiling;
    
    /** see {@link #topY()} */
    private short topY;
    
    /** see {@link #isBottomFlow()} */
    protected boolean isBottomFlow;
    
    /**
     * Surface of the cell as measured in fluid units.  If equal to floorUnits, cell has no fluid.
     * Stored in this way instead of as the depth of fluid because most references / calculations 
     * need the surface instead of the depth.
     */
    protected AtomicInteger fluidSurfaceUnits = new AtomicInteger(0);

      /** calculates the block y from a top bound (inclusive) given as a fluid level */
    protected static int getYFromCeiling(int ceilingIn)
    {
        // examples of input -> output
        // 24 -> 1  top level of block at Y = 1
        // 25 -> 2  first level of block at Y = 2
        return (ceilingIn - 1) / LavaSimulator.LEVELS_PER_BLOCK;
    }

    protected abstract void clearHasFluidChanged();
    protected abstract void invalidateRawRetention();
    
    public int getFluidUnits()
    {
        return this.fluidSurfaceUnits.get() - this.floorUnits;
    }

    public boolean isEmpty()
    {
        return this.fluidSurfaceUnits.get() <= this.floorUnits;
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
        return Math.min(this.getCeiling(), this.fluidSurfaceUnits.get() / LavaSimulator.FLUID_UNITS_PER_LEVEL); 
    }

    /**
     * Returns the world Y-level of the uppermost block containing lava.
     * Returns {@link #bottomY()} if the cell does not contain fluid.
     */
    public int fluidSurfaceY()
    {
        if(this.fluidSurfaceUnits.get() == this.floorUnits) return this.bottomY();
        
        return getYFromCeiling(this.fluidSurfaceLevel());
    }

    /**
     * Returns the flow height (1 - 12) of lava in the lava block at fluidSurfaceY 
     * Returns 0 if this cell does not contain lava.
     */
    public int fluidSurfaceFlowHeight()
    {
        if(this.fluidSurfaceUnits.get() == this.floorUnits) return 0;
        
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
        return this.fluidSurfaceUnits.get(); 
    }

    /** See {@link #getFloor()} */
    public void setFloor(int newFloor, boolean isFlowFloor)
    {
        if(newFloor != this.floor || isFlowFloor != this.isBottomFlow)
        {
            // check for new connections whenever cell expands
            if(newFloor < this.floor) this.invalidateConnections();
            
            this.floor = newFloor;
            this.floorUnits = newFloor * LavaSimulator.FLUID_UNITS_PER_LEVEL;
            this.isBottomFlow = isFlowFloor;
            this.bottomY = (short) getYFromFloor(this.floor);
            
            if(this.fluidSurfaceUnits.get() <= this.floorUnits)
            {
                this.emptyCell();
                this.clearHasFluidChanged();
            }
            
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

    public int getFloorUnits()
    {
        return this.floorUnits;
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
    public void setCeiling(int newCeiling)
    {
        // check for new connections whenever cell expands
        if(newCeiling > this.ceiling) this.invalidateConnections();;
        
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

    public int getCeilingUnits()
    {
        return this.ceiling * LavaSimulator.FLUID_UNITS_PER_LEVEL;
    }

    /** Y of last (top) block that could contain lava */
    public int topY()
    {
        return this.topY;
    }

    /** calculates the block y from a bottom bound (exclusive) given as a fluid level */
    private static int getYFromFloor(int floorIn)
    {
        return floorIn / LavaSimulator.LEVELS_PER_BLOCK;
    }

    /** returns floor (exclusive) of the block position at level y */
    protected static int blockFloorFromY(int y)
    {
        return y * LavaSimulator.LEVELS_PER_BLOCK;
    }

    /** returns ceiling (inclusive) of the block position at level y */
    protected static int blockCeilingFromY(int y)
    {
        return (y + 1) * LavaSimulator.LEVELS_PER_BLOCK;
    }

    public AbstractLavaCell()
    {
        super();
    }

    protected void emptyCell()
    {
        this.fluidSurfaceUnits.set(this.floorUnits);
    }

    public boolean changeLevel(final int amount, final int expectedPreviousLevel)
    {
    //        if(amount > 0 && this.fluidUnits == 0)
    //        {
    //            //TODO: check for melting causing floor to merge with a non-barrier block below
    //            // should cause cell to merge will cell below if happens.
    //            // otherwise floor remains intact
    //        }
    
            int newLevel = expectedPreviousLevel + amount;
            if(newLevel < this.floorUnits)
            {
                Adversity.log.info(String.format("Fluid surface units below floor units.  Surface=%1$d Floor=%2$d cellID=%3$d", this.fluidSurfaceUnits.get(), this.floorUnits, this.id));
                newLevel = this.floorUnits;
            }
            
            return this.fluidSurfaceUnits.compareAndSet(expectedPreviousLevel, newLevel);
        }

}