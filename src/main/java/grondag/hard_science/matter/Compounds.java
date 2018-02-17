package grondag.hard_science.matter;

public class Compounds
{
//    public static final Compound FRESH_AIR = Compound.builder()
//            .add(Molecules.N2, 0.7802320)
//            .add(Molecules.O2, 0.2100000)
//            .add(Molecules.Ar, 0.0093662)
//            .add(Molecules.CO2_GAS, 0.0004000)
//            .add(Molecules.METHANE_GAS, 0.0000018).build();
//
//    public static final Compound SAND = Compound.builder()
//            .add(Molecules.SILICA, 0.8)
//            .add(Molecules.CALCIUM_CARBONATE, 0.2).build();
//
//    public static final Compound MINERAL_DUST = Compound.builder()
//            .add(Molecules.SILICA, 0.6)
//            .add(Molecules.CALCIUM_CARBONATE, 0.2)
//            .add(Molecules.MAGNETITE, 0.2).build();
    
    public static final Compound FRESH_AIR = Compound.builder()
            .add(Molecules.N2_GAS, 0.7802320)
            .add(Molecules.O2_GAS, 0.2100000)
            .add(Molecules.AR_GAS, 0.0093662)
            .add(Molecules.CO2_GAS, 0.0004000)
            .add(Molecules.METHANE_GAS, 0.0000018).build();

            public static final Compound RETURN_AIR = Compound.builder()
            .add(Molecules.N2_GAS, 1.0000000).build();

            public static final Compound MINERAL_FILLER = Compound.builder()
                    .add(Molecules.SILICA, 0.8)
                    .add(Molecules.CALCIUM_CARBONATE, 0.2).build();

            public static final Compound RAW_MINERAL_DUST = Compound.builder()
            .add(Molecules.SILICA, 0.6)
            .add(Molecules.CALCIUM_CARBONATE, 0.2)
            .add(Molecules.MAGNETITE, 0.2).build();
}
