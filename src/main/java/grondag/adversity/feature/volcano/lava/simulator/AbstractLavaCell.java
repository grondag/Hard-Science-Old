package grondag.adversity.feature.volcano.lava.simulator;

import java.util.concurrent.atomic.AtomicInteger;

import grondag.adversity.Output;
import grondag.adversity.niceblock.modelstate.FlowHeightState;

public abstract class AbstractLavaCell
{
    private static AtomicInteger nextCellID = new AtomicInteger(0);
    
    public final int id = nextCellID.getAndIncrement();
    
    /** 
     * Excess fluid in a cell above it's normal volume is multiplied by this
     * factor to compute an effective fluid surface for determining flow .
     */
    public static final int PRESSURE_FACTOR = 50;
    public static final int PRESSURE_FACTOR_PLUS = PRESSURE_FACTOR + 1;
    public static final int PRESSURE_FACTOR_MINUS = PRESSURE_FACTOR - 1;
    public static final int PRESSURE_FACTOR_X2 = PRESSURE_FACTOR * 2;
    
    /** 
     * True if this cell is new or has expanded.
     * Used to determine if updateConnectionsIfNeeded should do anything.
     */
    private boolean isConnectionUpdateNeeded = true;
    
    public void invalidateConnections() { if(!this.isConnectionUpdateNeeded) this.isConnectionUpdateNeeded = true; }
    public boolean isConnectionUpdateNeeded() { return this.isConnectionUpdateNeeded; }
    public void clearConnectionUpdate() { if(this.isConnectionUpdateNeeded) this.isConnectionUpdateNeeded = false; }
    
    /******************************************************
    * FLOOR STUFF
    ******************************************************/
    
    /** authoritative value - all others are derived from this */
    private int floorLevel;
    
    /** see {@link #isBottomFlow()} */
    private boolean isBottomFlow;
    
    /** 
     * Floor as fluid units instead of levels - derived from floor each time floor is set.
     * Here to avoid multiplying floor each connection pass.
     */
    private int floorUnits;
    
    
    /** Floor of cell as Y level instead of levels - derived from floor each time floor is set. 
     *  Is the lowest level of cell that can contain lava.
     */
    private short floorY;

    /** See {@link #floorLevel()} */
    public void setFloorLevel(int newFloorLevel, boolean isFlowFloor)
    {
        if(newFloorLevel != this.floorLevel || isFlowFloor != this.isBottomFlow)
        {
            this.invalidateConnections();
            
            this.floorLevel = newFloorLevel;
            this.floorUnits = newFloorLevel * LavaSimulator.FLUID_UNITS_PER_LEVEL;
            this.isBottomFlow = isFlowFloor;
            this.floorY = (short) getYFromFloor(this.floorLevel);
            
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
    public int floorLevel() { return this.floorLevel; }

    /** floor as units instead of block levels */
    public int floorUnits() { return this.floorUnits; }

    /** Y of first (lowest) block that could contain lava */
    public int floorY() { return this.floorY; }

    /** Flow height of solid portion of block at {@link #floorY()}
     *  Will be 0 if floor is not a flow block.
     *  Will also be 0 if floor is a full-height flow block at Y-1.
     *  Will be in range 1-11 if floor is within the block at Y.
     *  Note that 12 is not a valid result because that would mean 
     *  block at Y is not the floor because it could not contain lava.
     */
    public int floorFlowHeight()
    {
        //Examples of this.floorLevel -> output
        // 12 -> 0
        // 13 -> 1
        // 23 -> 11
        // 24 -> 0
        return this.floorLevel % FlowHeightState.BLOCK_LEVELS_INT;
    }

    /** 
     * True if the solid block under this columns is a flow block.
     * Note that it must be true if bottomFlowHeight > 0;
     */
    public boolean isBottomFlow() { return this.isBottomFlow; }
    
    /** calculates the block y from a bottom bound (exclusive) given as a fluid level */
    private static int getYFromFloor(int floorIn)
    {
        return floorIn / LavaSimulator.LEVELS_PER_BLOCK;
    }

    /** returns floor level (exclusive) of the block position at level y */
    protected static int blockFloorFromY(int y)
    {
        return y * LavaSimulator.LEVELS_PER_BLOCK;
    }
    
    /******************************************************
    * CEILING STUFF
    ******************************************************/
    
    /** Value of record - all others derived from this one. */
    private int ceilingLevel;
    
    /** see {@link #ceilingUnits()} */
    private int ceilingUnits;
    
    /** see {@link #ceilingY()} */
    private short ceilingY;
    

    /** See {@link #ceilingLevel()} */
    public void setCeilingLevel(int newCeilingLevel)
    {
        if(newCeilingLevel != this.ceilingLevel) 
        {
            this.invalidateConnections();
            this.ceilingLevel = newCeilingLevel;
            this.ceilingUnits = newCeilingLevel * LavaSimulator.FLUID_UNITS_PER_LEVEL;
            this.ceilingY = (short) getYFromCeilingLevel(this.ceilingLevel);
        }
    }

    /** 
     * Last (highest) block level that can contain fluid, INCLUSIVE.
     * Values range from 1 to (256 * LEVELS_PER_BLOCK)
     * Levels in Y=0, for example are 1 thru 12.
     * ALWAYS USE setCeiling() to maintain topY.
     */
    public int ceilingLevel() { return this.ceilingLevel; }

    /* Ceiling as fluid units */
    public int ceilingUnits() { return this.ceilingUnits; }    

    /** Y of last (top) block that could contain lava */
    public int ceilingY() { return this.ceilingY; }

    /** calculates the block y from a top bound (inclusive) given as a fluid level */
    protected static int getYFromCeilingLevel(int ceilingLevelIn)
    {
        // examples of input -> output
        // 24 -> 1  top level of block at Y = 1
        // 25 -> 2  first level of block at Y = 2
        return (ceilingLevelIn - 1) / LavaSimulator.LEVELS_PER_BLOCK;
    }
    
    /** returns ceiling level (inclusive) of the block position at level y */
    protected static int blockCeilingFromY(int y)
    {
        return (y + 1) * LavaSimulator.LEVELS_PER_BLOCK;
    }
    
    /******************************************************
    * VOLUME STUFF
    ******************************************************/
    
    /** volume of space in this cell, in block levels (currently 12 per block) */
    public int volumeLevels()
    {
        return this.ceilingLevel() - this.floorLevel();
    }

    /** volume of space in this cell, in fluid units */
    public int volumeUnits()
    {
        return this.ceilingUnits() - this.floorUnits();
    }
    
    /******************************************************
    * FLUID STUFF
    ******************************************************/
    
    /**
     * Amount of fluid currently in the cell as measured in fluid units.  
     */
    private AtomicInteger fluidUnits = new AtomicInteger(0);
    
    public void changeFluidUnits(int deltaUnits)
    {
        if(this.fluidUnits.addAndGet(deltaUnits) < 0)
        {
            if(Output.DEBUG_MODE)
                Output.info(String.format("Negative fluid units detected.  NewAmount=%1$d Delta=%2$d cellID=%3$d", this.fluidUnits.get(), deltaUnits, this.id));
            this.fluidUnits.set(0);
        }
        
    }
    
    public void setFluidUnits(int newUnits)
    {
        if(newUnits < 0)
        {
            if(Output.DEBUG_MODE)
                Output.info(String.format("Negative fluid units detected.  NewAmount=%1$d cellID=%2$d", newUnits, this.id));
            newUnits = 0;
        }
        this.fluidUnits.set(newUnits);
    }
    
    public boolean changeFluidUnitsIfMatches(int deltaUnits, int expectedPriorUnits)
    {
        int newUnits = expectedPriorUnits + deltaUnits;
        
        if(newUnits < 0)
        {
            newUnits = 0;
            if(Output.DEBUG_MODE)
                    Output.info(String.format("Negative fluid units detected.  PriorAmount=%1$d Deltar=%2$d cellID=%3$d", expectedPriorUnits, deltaUnits, this.id));
        }
      
        return this.fluidUnits.compareAndSet(expectedPriorUnits, expectedPriorUnits + deltaUnits);
    }

//    protected abstract void clearHasSurfaceChanged();
    protected abstract void invalidateRawRetention();
    
    public int fluidUnits()
    {
        return this.fluidUnits.get();
    }
    
    /**
     * Returns at least 1 if cell has any fluid, even if less than one full level.
     */
    public int fluidLevels()
    {
        int units = this.fluidUnits.get();
        return units == 0 ? 0 : Math.max(1, units / LavaSimulator.FLUID_UNITS_PER_LEVEL);
    }

    public boolean isEmpty()
    {
        return this.fluidUnits.get() == 0;
    }

    protected void emptyCell()
    {
        this.fluidUnits.set(0);
    }

    /******************************************************
     * WORLD SURFACE STUFF
     ******************************************************/

    /** 
     * Absolute surface level as measured in fluid units. Solid blocks/levels below count 
     * as if they contained fluid.  Does not reflect excess fluid for cells under pressure.
     * If cell has no fluid, is the level of the cell floor, as if it were fluid.
     */
    public int worldSurfaceUnits()
    {
        return Math.min(this.ceilingUnits(), this.floorUnits() + this.fluidUnits()); 
    }
    
    /** 
     * Top level that contains fluid in the world. For columns under pressure
     * does not reflect extra fluid - is limited by the cell ceiling. 
     * Will return cell floor if there is no fluid in the cell.
     * If cell has any fluid at all, will be at least one level above floor.
     */
    public int worldSurfaceLevel()
    {
        //fluidLevels() handles case of showing level = 1 when fluid units are less than a full level
        return Math.min(this.ceilingLevel(), this.floorLevel() + this.fluidLevels()); 
    }

    /**
     * Returns the world Y-level of the uppermost block containing lava.
     * Returns {@link #floorY()} if the cell does not contain fluid.
     */
    public int worldSurfaceY()
    {
        if(this.isEmpty()) return this.floorY();
        
        return getYFromCeilingLevel(this.worldSurfaceLevel());
    }

    /**
     * Returns the flow height (1 - 12) of lava in the lava block at worldSurfaceY 
     * Returns 0 if this cell does not contain lava.
     */
    public int worldSurfaceFlowHeight()
    {
        if(this.isEmpty()) return 0;
        
        // examples of fluidSurfaceLevel -> output
        // 23 -> 11
        // 24 -> 12
        // 25 -> 1
        return this.worldSurfaceLevel() - this.worldSurfaceY() * FlowHeightState.BLOCK_LEVELS_INT;
    }
    
    
    /******************************************************
     * PRESSURE SURFACE STUFF
     ******************************************************/
    
    public static int pressureSurface(int floorUnitsIn, int volumeUnitsIn, int fluidUnitsIn)
    {
        return fluidUnitsIn > volumeUnitsIn
            ? floorUnitsIn + volumeUnitsIn + (fluidUnitsIn - volumeUnitsIn) * PRESSURE_FACTOR
            : floorUnitsIn + fluidUnitsIn;
    }
    
    /**
     * When total fluid in both cells is above this amount, 
     * both cells will be under pressure at equilibrium.
     * 
     * Definitions  
     * c   Pressure Factor (constant)
     * t   total lava (invariant)
     * Pa Pb   pressurized fluid surface
     * Sa  Sb  normal fluid surface (bounded by cell ceiling)
     * Fa Fb   column floor (fixed)
     * Ua Ub   normal fluid units (bounded by cell volume)
     * Xa Xb   extra (pressure) fluid units
     *
     * 1    t = Ua + Ub + Xa + Xb   conservation of fluid
     * 2   Pa = Pb equalize pressurized fluid surface
     * 3   Fa + Ua + cXa = Fb + Ub + cXb   expanded equalization formula
     *         
     * 10  Sa >= Sb    assume ceiling of cell A is higher than B
     * 11  Fb + Ub + cXb - Fa - Ua = 0 find max value for Xb such that Xa = 0
     * 12  t = Ua + Ub + Xb    Ua and Ub are fixed at max depth, t and Xb are unknown
     * 13  Xb = t - Ua - Ub    rearrange #12
     * 14  Fb + Ub + c(t - Ua - Ub) - Fa - Ua = 0  substitute #13 into #11
     *     ct - cUa - cUb = Fa + Ua - Fb - Ub  solve for t
     *     ct = Fa + Ua - Fb - Ub + cUa + cUb  
     *     ct = Fa + Ua + cUa - Fb - Ub + cUb  
     *     ct = Fa + (c+1)Ua - Fb + (c-1)Ub    
     *     t = (Fa + (c+1)Ua - Fb + (c-1)Ub) / c   when total in both cells is above this, both cells have pressure
     *     
     *     "High" cell should be cell with the highest ceiling
     */
    public static int dualPressureThreshold(int floorHigh, int volumeHigh, int floorLow, int volumeLow )
    {
        return (floorHigh + PRESSURE_FACTOR_PLUS * volumeHigh - floorLow + PRESSURE_FACTOR_MINUS * volumeLow) / PRESSURE_FACTOR;
    }
    
    /**
     * Floor order does not matter but ceiling must be min of both cells. 
     */
    public static int singlePressureThreshold(int floor1, int floor2, int ceilingMin)
    {
        return ceilingMin - floor1 + ceilingMin - floor2;
    }
}