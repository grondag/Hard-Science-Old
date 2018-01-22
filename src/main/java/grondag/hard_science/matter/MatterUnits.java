package grondag.hard_science.matter;

import grondag.hard_science.machines.energy.MachinePower;

public class MatterUnits
{
    public static final long nL_LITER = VolumeUnits.LITER.nL;
    public static final long nL_ONE_BLOCK = CubeSize.BLOCK.nanoLiters;
    public static final long nL_TWO_BLOCKS = nL_ONE_BLOCK * 2;
    public static final long nL_HALF_STACK_OF_BLOCKS_nL = nL_ONE_BLOCK * 32;
    public static final long nL_FULL_STACK_OF_BLOCKS_nL = nL_ONE_BLOCK * 64;
    public static final long nL_HALF_BLOCK = nL_ONE_BLOCK / 2;
    public static final long nL_QUARTER_BLOCK_ = nL_ONE_BLOCK / 4;
    public static final long nL_EIGHTH_BLOCK = nL_ONE_BLOCK / 8;
    public static final long nL_ONE_HUNDRED_BLOCKS_nL = nL_ONE_BLOCK * 100;

    public static final long nL_HS_CUBE_ZERO = CubeSize.BLOCK.nanoLiters;
    public static final long nL_HS_CUBE_ONE = CubeSize.ONE.nanoLiters;
    public static final long nL_HS_CUBE_TWO = CubeSize.TWO.nanoLiters;
    public static final long nL_HS_CUBE_THREE = CubeSize.THREE.nanoLiters;
    public static final long nL_HS_CUBE_FOUR = CubeSize.FOUR.nanoLiters;
    public static final long nL_HS_CUBE_FIVE = CubeSize.FIVE.nanoLiters;
    public static final long nL_HS_CUBE_SIX = CubeSize.SIX.nanoLiters;
    
    private static final float DENSITY_WOOD = 0.75f;
    private static final float DENSITY_CURED_RESIN = 1.50f;

    /**
     * Volume of filler material that is assumed to be air.
     * For construction, volume of filler consumed is equal to constructed volume and
     * volume of resin consumed will be constructed volume multiplied by this number.
     */
    public static final float FILLER_VOID_RATIO = 0.5f;
    
    /**
     * How much mixed resin needed when foaming it to make a wood-like material.
     * Multiply by the constructed volume.  Divide by half again to get required volume of A & B.
     */
    public static final float RESIN_WOOD_FRACTION_BY_VOLUME = DENSITY_CURED_RESIN / DENSITY_WOOD;
    
    /**
     * Liters of nano-lights needed to make an emmissive block. <br><br>
     * 
     * Nano lights include enough battery capacity to glow at full brightness (sunlight)
     * for 24 hours.  After that they will require re-charging via wireless power.<br><br>
     * 
     * Size of the block can't matter here because light output is the same regardless of volume.<br><br>
     * 
     * If they are simply glowing (but producing no light) then they output 16 lumens (sum of all directions)
     * and a full charge will last over 3000 days. <br><br>
     * 
     * Brightness can be turned up or down with a tool I haven't made yet. ;-P <br><br>
     * 
     */
    public static final long nL_NANO_LIGHTS_PER_BLOCK = VolumeUnits.LITER.nL * 2;
    
    /**
     * Multiply by the volume of nano lights in your block to determine the volume of HDPE needed
     * for a 10-year fuel supply.
     */
    public static final long nl_NANO_LIGHT_HDPE_FUEL_MULITPLIER = 1;
    
    /**
     * Many paints are around 10M^2 per liter, and they are about 1/2 solids.
     */
    public static final int M2_PIGMENT_COVERAGE_SQUARE_METERS_PER_LITER = 20;
    
    /**
     * For use in {@link #polyEthyleneJoulesFromNanoLiters(long, int)}
     */
    private static final long PE_NORMALIZATION_FACTOR = nL_LITER * 100;
    
    /**
     * Avoids floating point math.<br>
     * Will return zero for small quantities.<br>
     * 
     */
    public static long polyEthyleneJoulesFromNanoLiters(long nanoLitersPE, int percentEfficiency)
    {
        return nanoLitersPE * MachinePower.JOULES_PER_POLYETHYLENE_LITER * percentEfficiency / PE_NORMALIZATION_FACTOR;
    }
    
}
