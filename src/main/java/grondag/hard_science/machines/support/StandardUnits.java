package grondag.hard_science.machines.support;

public class StandardUnits
{
    public static final long nL_LITER = VolumeUnits.LITER.nL;
    public static final long nL_ONE_BLOCK = VolumeUnits.KILOLITER.nL;
    public static final long nL_HALF_BLOCK = nL_ONE_BLOCK / 2;
    public static final long nL_QUARTER_BLOCK_ = nL_ONE_BLOCK / 4;
    public static final long nL_EIGHTH_BLOCK = nL_ONE_BLOCK / 8;
    public static final long nL_FULL_STACK_OF_BLOCKS_nL = nL_ONE_BLOCK * 64;
    public static final long nL_ONE_HUNDRED_BLOCKS_nL = nL_ONE_BLOCK * 100;

    public static final long nL_HS_CUBE_ZERO = nL_ONE_BLOCK;
    public static final long nL_HS_CUBE_ONE = nL_HS_CUBE_ZERO / 8;
    public static final long nL_HS_CUBE_TWO = nL_HS_CUBE_ONE / 8;
    public static final long nL_HS_CUBE_THREE = nL_HS_CUBE_TWO / 8;
    public static final long nL_HS_CUBE_FOUR = nL_HS_CUBE_THREE / 8;
    public static final long nL_HS_CUBE_FIVE = nL_HS_CUBE_FOUR / 8;
    public static final long nL_HS_CUBE_SIX = nL_HS_CUBE_FIVE / 8;
    
    public static final long J_ENERGY_PER_POLYETHYLENE_LITER = 42600000;
    
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
        return nanoLitersPE * J_ENERGY_PER_POLYETHYLENE_LITER * percentEfficiency / PE_NORMALIZATION_FACTOR;
    }
    
}
