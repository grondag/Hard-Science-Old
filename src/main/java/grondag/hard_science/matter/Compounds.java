package grondag.hard_science.matter;

public class Compounds
{
    public static final Compound FRESH_AIR = Compound.builder()
            .add(Molecules.N2, 0.7802336)
            .add(Molecules.O2, 0.2100000)
            .add(Molecules.Ar, 0.0093400)
            .add(Molecules.CO2, 0.0004000)
            .add(Molecules.Ne, 0.0000182)
            .add(Molecules.He, 0.0000052)
            .add(Molecules.METHANE_GAS, 0.0000018)
            .add(Molecules.Kr, 0.0000011)
            .add(Molecules.Xe, 0.0000001)
            .build();
    
    public static final Compound SAND = Compound.builder()
            .add(Molecules.SILICA, 0.80)
            .add(Molecules.CALCIUM_CARBONATE, 0.20)
            .build();
    
    public static final Compound MINERAL_DUST = Compound.builder()
            .add(Molecules.SILICA, 0.60)
            .add(Molecules.MAGNETITE, 0.20)
            .add(Molecules.CALCIUM_CARBONATE, 0.20)
            .build();
}
