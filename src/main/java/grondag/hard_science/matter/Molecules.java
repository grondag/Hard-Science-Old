package grondag.hard_science.matter;

public class Molecules
{
    public static final Molecule H2 = new Molecule("H2", 0);
    public static final Molecule N2 = new Molecule("N2", 0);
    public static final Molecule O2 = new Molecule("O2", 0);
    public static final Molecule GRAPHITE = new Molecule("C", 0);
    public static final Molecule DIAMOND = new Molecule("C", 1.9);
    public static final Molecule CARBON_VAPOR = new Molecule("C", 716.67);

    public static final Molecule CO2 = new Molecule("C O2", -393.509);
    public static final Molecule H2O_FLUID = new Molecule("H2 O", -285.8);
    public static final Molecule H2O_VAPOR = new Molecule("H2 O", -241.818);
    public static final Molecule AMMONIA_GAS = new Molecule("N H3", -45.90);
    public static final Molecule Ar = new Molecule("Ar", 0);
    public static final Molecule Ne = new Molecule("Ne", 0);
    public static final Molecule He = new Molecule("He", 0);
    public static final Molecule METHANE_GAS = new Molecule("C H4", -74.9);
    public static final Molecule Kr = new Molecule("Kr", 0);
    public static final Molecule Xe = new Molecule("Xe", 0);
    public static final Molecule ETHENE_GAS = new Molecule("C2 H4", 52.47);
    public static final Molecule ETHANOL_LIQUID = new Molecule("C2 H5 O H", -277.0);
    
    public static final Molecule SILICA = new Molecule("Si O2", -910.86);
    public static final Molecule CALCIUM_CARBONATE = new Molecule("Ca C O3", -1207);
    public static final Molecule MAGNETITE = new Molecule("Fe3 O4", -1118.4);
}
