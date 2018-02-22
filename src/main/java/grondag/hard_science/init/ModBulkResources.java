package grondag.hard_science.init;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import grondag.hard_science.matter.Compounds;
import grondag.hard_science.matter.IComposition;
import grondag.hard_science.matter.MatterColors;
import grondag.hard_science.matter.MatterPhase;
import grondag.hard_science.matter.Molecules;
import grondag.hard_science.simulator.resource.BulkResource;
import grondag.hard_science.superblock.color.BlockColorMapProvider;
import grondag.hard_science.superblock.color.ColorMap.EnumColorMap;
import net.minecraftforge.fluids.FluidRegistry;

//@Mod.EventBusSubscriber
//@ObjectHolder(HardScience.MODID)
public class ModBulkResources
{
    
//    public static final BulkResource empty = null;
//    
//    @SubscribeEvent
//    public static void registerResources(RegistryEvent.Register<BulkResource> event) 
//    {
//        event.getRegistry().register(new BulkResource(ModRegistries.EMPTY_KEY));
//    }
    
    private static Map<String, BulkResource> all = new HashMap<String, BulkResource>();
    
    private static Set<BulkResource> managed = new HashSet<BulkResource>();

    /**
     * Unmanaged resources aren't tracked at domain level and
     * don't have stocking levels.  They never exist outside machine buffers.
     */
    private static BulkResource registerUnmanaged(
            String systemName,
            int color,
            String label,
            IComposition molecule,
            double tempCelsius,
            double pressureAtm,
            MatterPhase phase,
            double density)
    {
        BulkResource result = new BulkResource(systemName, color, label, molecule, tempCelsius, pressureAtm, phase, density);
        all.put(systemName, result);
        return result;
    }
    
    private static BulkResource register(
            String systemName,
            int color,
            String label,
            IComposition molecule,
            double tempCelsius,
            double pressureAtm,
            MatterPhase phase,
            double density)
    {
        BulkResource result = registerUnmanaged(systemName, color, label, molecule, tempCelsius, pressureAtm, phase, density);
        managed.add(result);
        return result;
    }
    
    /**
     * Unmanaged resources aren't tracked at domain level and
     * don't have stocking levels.  They never exist outside machine buffers.
     */
    private static BulkResource registerUnmanaged(
            String systemName,
            int color,
            String label,
            IComposition molecule,
            double tempCelsius,
            double pressureAtm)
    {
        BulkResource result = new BulkResource(systemName, color, label, molecule, tempCelsius, pressureAtm);
        all.put(systemName, result);
        return result;
    }
    
    private static BulkResource register(
            String systemName,
            int color,
            String label,
            IComposition molecule,
            double tempCelsius,
            double pressureAtm)
    {
        BulkResource result = registerUnmanaged(systemName, color, label, molecule, tempCelsius, pressureAtm);
        managed.add(result);
        return result;
    }
    
    public final static BulkResource WATER = register(
            FluidRegistry.WATER.getName(), 
            FluidRegistry.WATER.getColor(),
            "H2O",
            Molecules.H2O_FLUID, 
            20, 
            1, 
            MatterPhase.LIQUID, 
            0.99823);
    
    
    public final static BulkResource FRESH_AIR = register("fresh_air", MatterColors.FRESH_AIR, "air", Compounds.FRESH_AIR, 20, 1);
    public final static BulkResource RETURN_AIR = register("return_air", MatterColors.RETURN_AIR, "air-", Compounds.RETURN_AIR, 20, 1);
    public final static BulkResource RAW_MINERAL_DUST = register("raw_mineral_dust", MatterColors.RAW_MINERAL_DUST, "md", Compounds.RAW_MINERAL_DUST, 20, 1, MatterPhase.SOLID, 2.0);    
    public final static BulkResource MINERAL_FILLER = register("mineral_filler", MatterColors.DEPLETED_MINERAL_DUST, "mf", Compounds.MINERAL_FILLER, 20, 1, MatterPhase.SOLID, 2.0);

    public final static BulkResource BUFFERED_STONE = registerUnmanaged("buffered_stone", MatterColors.RAW_MINERAL_DUST, "cs", Compounds.RAW_MINERAL_DUST, 20, 1, MatterPhase.SOLID, 2.0);    
    public final static BulkResource BUFFERED_BASALT = registerUnmanaged("buffered_basalt", BlockColorMapProvider.COLOR_BASALT.getColor(EnumColorMap.BASE), "cb", Compounds.RAW_MINERAL_DUST, 20, 1, MatterPhase.SOLID, 2.0);    

    public final static BulkResource CRUSHED_STONE = register("crushed_stone", MatterColors.RAW_MINERAL_DUST, "cs", Compounds.RAW_MINERAL_DUST, 20, 1, MatterPhase.SOLID, 2.0);    
    public final static BulkResource CRUSHED_BASALT = register("crushed_basalt", BlockColorMapProvider.COLOR_BASALT.getColor(EnumColorMap.BASE), "cb", Compounds.RAW_MINERAL_DUST, 20, 1, MatterPhase.SOLID, 2.0);    

    public final static BulkResource H2_GAS = register("h2_gas", 0xFFEA3323, "H2", Molecules.H2_GAS, 20, 1);
    public final static BulkResource N2_GAS = register("n2_gas", 0xFF526FDC, "N2", Molecules.N2_GAS, 20, 1);
    public final static BulkResource O2_GAS = register("o2_gas", 0xFFBC4039, "O2", Molecules.O2_GAS, 20, 1);
    public final static BulkResource AR_GAS = register("ar_gas", 0xFFDEFEFE, "Ar", Molecules.AR_GAS, 20, 1);
    public final static BulkResource CO2_GAS = register("co2_gas", 0xFFD9B2B0, "CO2", Molecules.CO2_GAS, 20, 1);
    public final static BulkResource H2O_FLUID = register("h2o_fluid", MatterColors.WATER, "H2O", Molecules.H2O_FLUID, 20, 1, MatterPhase.LIQUID, 0.9982);
    public final static BulkResource METHANE_GAS = register("methane_gas", 0xFFFFFFFF, "CH4", Molecules.METHANE_GAS, 20, 1);
    public final static BulkResource AMMONIA_GAS = register("ammonia_gas", MatterColors.AMMONIA, "NH3", Molecules.AMMONIA_GAS, 20, 1);
    public final static BulkResource AMMONIA_LIQUID = register("ammonia_liquid", MatterColors.AMMONIA, "NH3", Molecules.AMMONIA_LIQUID, 20, 20, MatterPhase.LIQUID, 0.6470);
    public final static BulkResource SILICA = register("silica", MatterColors.SILICA, "SiO2", Molecules.SILICA, 20, 1, MatterPhase.SOLID, 2.648);
    public final static BulkResource LITHIUM = register("lithium", 0xFFC28EF8, "Li", Molecules.LITHIUM, 20, 1, MatterPhase.SOLID, 0.5340);
    public final static BulkResource GRAPHITE = register("graphite", 0xFF746C67, "C", Molecules.GRAPHITE, 20, 1, MatterPhase.SOLID, 2.2660);
    public final static BulkResource ALUMINUM = register("aluminum", 0xFFDAA4A1, "Al", Molecules.ALUMINUM, 20, 1, MatterPhase.SOLID, 2.7000);
    public final static BulkResource SILICON = register("silicon", 0xFFE9CAA5, "Si", Molecules.SILICON, 20, 1, MatterPhase.SOLID, 2.3290);
    public final static BulkResource SULFER = register("sulfer", 0xFFEAF857, "S", Molecules.SULFER, 20, 1, MatterPhase.SOLID, 2.0670);
    public final static BulkResource IRON = register("iron", 0xFF808080, "Fe", Molecules.IRON, 20, 1, MatterPhase.SOLID, 7.8740);
    public final static BulkResource COPPER = register("copper", 0xFFBE8343, "Cu", Molecules.COPPER, 20, 1, MatterPhase.SOLID, 8.9600);
    public final static BulkResource NICKEL = register("nickel", 0xFF75CD61, "Ni", Molecules.NICKEL, 20, 1, MatterPhase.SOLID, 8.9120);
    public final static BulkResource SILVER = register("silver", 0xFFC0C0C0, "Ag", Molecules.SILVER, 20, 1, MatterPhase.SOLID, 10.5010);
    public final static BulkResource TIN = register("tin", 0xFF6A7C7E, "Sn", Molecules.TIN, 20, 1, MatterPhase.SOLID, 7.2870);
    public final static BulkResource PLATINUM = register("platinum", 0xFFD0D0DF, "Pt", Molecules.PLATINUM, 20, 1, MatterPhase.SOLID, 21.4600);
    public final static BulkResource GOLD = register("gold", 0xFFF8D34F, "Au", Molecules.GOLD, 20, 1, MatterPhase.SOLID, 19.2820);
    public final static BulkResource LEAD = register("lead", 0xFF575960, "Pb", Molecules.LEAD, 20, 1, MatterPhase.SOLID, 11.3420);
    public final static BulkResource BORON = register("boron", 0xFFF4B8B7, "B", Molecules.BORON, 20, 1, MatterPhase.SOLID, 2.3400);
    public final static BulkResource SODIUM = register("sodium", 0xFFA17FD1, "Na", Molecules.SODIUM, 20, 1, MatterPhase.SOLID, 0.9710);
    public final static BulkResource MAGNESIUM = register("magnesium", 0xFFACD847, "Mg", Molecules.MAGNESIUM, 20, 1, MatterPhase.SOLID, 1.7380);
    public final static BulkResource PHOSPHORUS = register("phosphorus", 0xFFEF8733, "P", Molecules.PHOSPHORUS, 20, 1, MatterPhase.SOLID, 1.8200);
    public final static BulkResource POTASSIUM = register("potassium", 0xFF836CAF, "K", Molecules.POTASSIUM, 20, 1, MatterPhase.SOLID, 0.8620);
    public final static BulkResource CALCIUM = register("calcium", 0xFF8ABC3F, "Ca", Molecules.CALCIUM, 20, 1, MatterPhase.SOLID, 1.5400);
    public final static BulkResource TITANIUM = register("titanium", 0xFFF6F6F6, "Ti", Molecules.TITANIUM, 20, 1, MatterPhase.SOLID, 4.5400);
    public final static BulkResource CHROMIUM = register("chromium", 0xFF8D98C3, "Cr", Molecules.CHROMIUM, 20, 1, MatterPhase.SOLID, 7.1500);
    public final static BulkResource MANGANESE = register("manganese", 0xFFA5335F, "Mn", Molecules.MANGANESE, 20, 1, MatterPhase.SOLID, 7.4400);
    public final static BulkResource COBALT = register("cobalt", 0xFF346FFF, "Co", Molecules.COBALT, 20, 1, MatterPhase.SOLID, 8.8600);
    public final static BulkResource ZINC = register("zinc", 0xFF7E80AC, "Zn", Molecules.ZINC, 20, 1, MatterPhase.SOLID, 7.1340);
    public final static BulkResource MOLYBDENUM = register("molybdenum", 0xFF6FBEC2, "Mo", Molecules.MOLYBDENUM, 20, 1, MatterPhase.SOLID, 10.2200);
    public final static BulkResource NEODYMIUM = register("neodymium", 0xFFCFAB5C, "Nd", Molecules.NEODYMIUM, 20, 1, MatterPhase.SOLID, 7.0070);
    public final static BulkResource TUNGSTEN_POWDER = register("tungsten_powder", 0xFFA3609F, "W", Molecules.TUNGSTEN_POWDER, 20, 1, MatterPhase.SOLID, 19.2500);
    public final static BulkResource DYE_CYAN = register("dye_cyan", 0xFF99FFFF, "Cmy", Molecules.DYE_CYAN, 20, 1, MatterPhase.LIQUID, 1.0000);
    public final static BulkResource DYE_MAGENTA = register("dye_magenta", 0xFFFF99FF, "cMy", Molecules.DYE_MAGENTA, 20, 1, MatterPhase.LIQUID, 1.0000);
    public final static BulkResource DYE_YELLOW = register("dye_yellow", 0xFFFFFF99, "cmY", Molecules.DYE_YELLOW, 20, 1, MatterPhase.LIQUID, 1.0000);
    public final static BulkResource FLEX_RESIN = register("flex_resin", 0xFFE8F5C6, "xR", Molecules.FLEX_RESIN, 20, 1, MatterPhase.LIQUID, 1.0000);
    public final static BulkResource FLEX_FIBER = register("flex_fiber", 0xFFD0DDB3, "xF", Molecules.FLEX_FIBER, 20, 1, MatterPhase.LIQUID, 1.0000);
    public final static BulkResource DURA_RESIN = register("dura_resin", 0xFFAA9292, "dR", Molecules.DURA_RESIN, 20, 1, MatterPhase.SOLID, 2.0000);
    public final static BulkResource PEROVSKITE = register("perovskite", 0xFFFFFFFF, "ps", Molecules.PEROVSKITE, 20, 1, MatterPhase.SOLID, 6.0000);
    public final static BulkResource H2O_VAPOR = register("h2o_vapor", MatterColors.WATER, "H20", Molecules.H2O_VAPOR, 20, 1);
    public final static BulkResource ETHENE_GAS = register("ethene_gas", MatterColors.ETHENE    , "C2H4", Molecules.ETHENE_GAS, 20, 1);
    public final static BulkResource ETHANOL_LIQUID = register("ethanol_liquid", 0xFFFFFFFF, "C2H5OH", Molecules.ETHANOL_LIQUID, 20, 1, MatterPhase.LIQUID, 0.7890);
    public final static BulkResource SUPER_FUEL = register("super_fuel", 0xFFFFFFFF, "C7H8", Molecules.SUPER_FUEL, 20, 1, MatterPhase.LIQUID, 0.9820);
    public final static BulkResource HDPE = register("hdpe", MatterColors.HDPE, "CH2", Molecules.HDPE, 20, 1, MatterPhase.SOLID, 0.9700);
    public final static BulkResource CALCIUM_CARBONATE = register("calcium_carbonate", 0xFFFFFFFF, "CaCO3", Molecules.CALCIUM_CARBONATE, 20, 1, MatterPhase.SOLID, 2.7110);
    public final static BulkResource MAGNETITE = register("magnetite", 0xFFFFFFFF, "Fe3O4", Molecules.MAGNETITE, 20, 1, MatterPhase.SOLID, 5.0000);
    public final static BulkResource DIAMOND = register("diamond", 0xFFFFFFFF, "C", Molecules.DIAMOND, 20, 1, MatterPhase.SOLID, 3.5000);
    public final static BulkResource CARBON_VAPOR = register("carbon_vapor", 0xFFFFFFFF, "C", Molecules.CARBON_VAPOR, 20, 1);
    public final static BulkResource FLEX_ALLOY = register("flex_alloy", 0xFFFFFFFF, "xA", Molecules.FLEX_ALLOY, 20, 1, MatterPhase.SOLID, 2.7000);
    public final static BulkResource SILICON_NITRIDE = register("silicon_nitride", 0xFFFFFFFF, "Si3N4", Molecules.SILICON_NITRIDE, 20, 1, MatterPhase.SOLID, 3.4400);
    public final static BulkResource MONOCALCIUM_PHOSPHATE = register("monocalcium_phosphate", 0xFFFFFFFF, "CaH4P2O8", Molecules.MONOCALCIUM_PHOSPHATE, 20, 1, MatterPhase.SOLID, 2.2200);
    public final static BulkResource CALCIUM_NITRATE = register("calcium_nitrate", 0xFFFFFFFF, "CaN2O6", Molecules.CALCIUM_NITRATE, 20, 1, MatterPhase.SOLID, 2.5040);
    public final static BulkResource SODIUM_NITRATE = register("sodium_nitrate", 0xFFFFFFFF, "NaNO3", Molecules.SODIUM_NITRATE, 20, 1, MatterPhase.SOLID, 2.2570);
    public final static BulkResource POTASSIUM_NITRATE = register("potassium_nitrate", 0xFFFFFFFF, "KNO3", Molecules.POTASSIUM_NITRATE, 20, 1, MatterPhase.SOLID, 2.1090);
    public final static BulkResource LITHIUM_NITRATE = register("lithium_nitrate", 0xFFFFFFFF, "LiNO3", Molecules.LITHIUM_NITRATE, 20, 1, MatterPhase.SOLID, 2.3800);
    public final static BulkResource MAGNESIUM_NITRATE = register("magnesium_nitrate", 0xFFFFFFFF, "H12MgN2 O12", Molecules.MAGNESIUM_NITRATE, 20, 1, MatterPhase.SOLID, 2.3000);
    public final static BulkResource SODIUM_CHLORIDE = register("sodium_chloride", 0xFFFFFFFF, "NaCl", Molecules.SODIUM_CHLORIDE, 20, 1, MatterPhase.SOLID, 2.1650);
    public final static BulkResource CALCIUM_FLUORIDE = register("calcium_fluoride", 0xFFFFFFFF, "CaF2", Molecules.CALCIUM_FLUORIDE, 20, 1, MatterPhase.SOLID, 3.1800);
    
    public static Map<String, BulkResource> all()
    {
        if(all instanceof HashMap) all = ImmutableMap.copyOf(all);
        return all;
    }
    
    public static Set<BulkResource> managed()
    {
        if(managed instanceof HashSet) managed = ImmutableSet.copyOf(managed);
        return managed;
    }
    
    public static BulkResource get(String name)
    {
        return all.get(name);
    }
}
