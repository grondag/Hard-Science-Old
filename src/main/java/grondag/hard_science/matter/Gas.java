package grondag.hard_science.matter;

public class Gas
{
    public static final double PASCALS_PER_ATMOSPHERE = 101325;
    
    /**
     * Assumes pascals for pressure values
     */
    public static final double IDEAL_GAS_CONSTANT = 8.3144598;

    public static double atmToPascals(double atmospheres)
    {
        return atmospheres * PASCALS_PER_ATMOSPHERE;
    }
    
    public static double pascalsToAtm(double pascals)
    {
        return pascals / PASCALS_PER_ATMOSPHERE;
    }
    
    /**
     * Cubic meters occupied by an ideal gas at the given temperature and pressure.
     */
    public static double idealGasMolarVolumeKP(double tempK, double pressurePascals)
    {
        return IDEAL_GAS_CONSTANT * tempK / pressurePascals;
    }
    
    public static double idealGasMolarVolumeCA(double tempC, double pressureAtm)
    {
        return idealGasMolarVolumeKP(Temperature.celsiusToKelvin(tempC), Gas.atmToPascals(pressureAtm));
    }
    /**
     * Density of the given molecule as an ideal gas at the given
     * temperature and pressure. In g/cm3
     */
    public static double idealGasDensityKP(IComposition molecule, double tempKelvin, double pressurePascals)
    {
        return pressurePascals * molecule.weight() / (IDEAL_GAS_CONSTANT * tempKelvin) / 1000000;
    }
    
    /**
     * Density of the given molecule as an ideal gas at the given
     * temperature and pressure. In g/cm3
     */
    public static double idealGasDensityCA(IComposition molecule, double tempCelsius, double pressureAtm)
    {
        return idealGasDensityKP(
                molecule, 
                Temperature.celsiusToKelvin(tempCelsius),
                Gas.atmToPascals(pressureAtm));
    }
}
