package grondag.adversity.feature.volcano.lava;

import grondag.adversity.niceblock.modelstate.FlowHeightState;

public class LavaColumn
{
    public static final int LEVELS_PER_BLOCK = FlowHeightState.BLOCK_LEVELS_INT;
    private static final int FLUID_UNITS_PER_LEVEL = 1024;
//    private static final int PRESSURE_UNITS_PER_LEVEL = 4;
    
    private final int x;
    private final int z;
    
    
    /** 
     * First (lowest) block level that can contain fluid, EXCLUSIVE.
     * Values range from 0 to (256 * LEVELS_PER_BLOCK) - 1 
     * Levels in Y=0 for example, are 0 thru 11.
     */
    private int floor;
    
    /** 
     * Last (highest) block level that can contain fluid, INCLUSIVE.
     * Values range from 1 to (256 * LEVELS_PER_BLOCK)
     * Levels in Y=0, for example are 1 thru 12.
     */
    private int maxLevel;
    
    /** 
     * True if the solid block under this columns is a flow block.
     * Note that it must be true if bottomFlowHeight > 0;
     */
    private boolean bottomIsFlow;
    
    private int fluidUnits;
    
    /**
     * Maximum pressure level reported by any connected neighbor during the last connection processing pass.
     * Set to nextMaxNeighborPressure whenever propagate pressure sees a new flowIndex.
     */
    private int lastMaxNeighborPressure;
    
    /**
     * Maximum pressure level reported by any connected neighbor during the current connection processing pass.
     * Set to 0 whenever propagate pressure sees a new flowIndex.
     */
    private int nextMaxNeighborPressure;
    
    /**
     * The flowIndex that was last seen.
     * Flow index will increment multiple times per tick.
     * When a new flowIndex is seen, is trigger to reset pressure propagation.
     */
    private int lastFlowIndex;
    
    /**
     * The simulation tick that was last seen.
     * Flow index will increment multiple times per tick.
     * When a new flowIndex is seen, is trigger to reset pressure propagation.
     */
    private int lastTickIndex;
    
    //TODO
    // merge / split / validate
    
    private LavaColumn(AbstractLavaSimulator sim, int x, int y, int z)
    {
        this.x = x;
        this.z = z;
        
        //todo populate range
    }

    /** Y of first (lowest) block that could contain lava */
    public int bottomY()
    {
        return this.floor / LEVELS_PER_BLOCK;
    }
    
    /** Y of last (top) block that could contain lava */
    public int topY()
    {
        return (this.maxLevel - 1) / LEVELS_PER_BLOCK;
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
     * Top level that contains fluid in the world. For columns under pressure
     * may be less than fluid amount would normally indicate. 
     */
    public int fluidSurfaceLevel()
    {
        return Math.min(this.maxLevel, this.floor + this.fluidUnits / FLUID_UNITS_PER_LEVEL); 
    }
    
    /** 
     * Absolute surface level as measured in fluid units. Solid blocks/levels below count 
     * as if they contained fluid.  Also include any excess fluid for cells under pressure.
     */
    public int fluidSurfaceUnits()
    {
        return this.floor * FLUID_UNITS_PER_LEVEL + this.fluidUnits; 
    }
}
