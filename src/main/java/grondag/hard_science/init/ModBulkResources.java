package grondag.hard_science.init;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.crafting.processing.DigesterAnalysis;
import grondag.hard_science.crafting.processing.DigesterRecipe;
import grondag.hard_science.matter.Compounds;
import grondag.hard_science.matter.IComposition;
import grondag.hard_science.matter.MatterColors;
import grondag.hard_science.matter.MatterPhase;
import grondag.hard_science.matter.Molecules;
import grondag.hard_science.simulator.resource.BulkResource;
import grondag.hard_science.superblock.color.BlockColorMapProvider;
import grondag.hard_science.superblock.color.ColorMap.EnumColorMap;
import net.minecraftforge.fluids.FluidRegistry;

public class ModBulkResources
{
    
    private static Map<String, BulkResource> all = new HashMap<String, BulkResource>();
    
    private static BulkResource register(
            String systemName,
            int color,
            IComposition molecule,
            double tempCelsius,
            double pressureAtm,
            MatterPhase phase,
            double density)
    {
        BulkResource result = new BulkResource(systemName, color, molecule, tempCelsius, pressureAtm, phase, density);
        all.put(systemName, result);
        result.registerFluid();
        return result;
    }
    
    private static BulkResource register(
            String systemName,
            int color,
            IComposition molecule,
            double tempCelsius,
            double pressureAtm)
    {
        BulkResource result = new BulkResource(systemName, color, molecule, tempCelsius, pressureAtm);
        all.put(systemName, result);
        result.registerFluid();
        return result;
    }
    
    public final static BulkResource WATER = register(
            FluidRegistry.WATER.getName(), 
            FluidRegistry.WATER.getColor(),
            Molecules.H2O_FLUID, 
            20, 
            1, 
            MatterPhase.LIQUID, 
            0.99823);
    
    
    public final static BulkResource FRESH_AIR = register("fresh_air", MatterColors.FRESH_AIR, Compounds.FRESH_AIR, 20, 1);
    public final static BulkResource RETURN_AIR = register("return_air", MatterColors.RETURN_AIR, Compounds.RETURN_AIR, 20, 1);
    public final static BulkResource RAW_MINERAL_DUST = register("raw_mineral_dust", MatterColors.RAW_MINERAL_DUST, Compounds.RAW_MINERAL_DUST, 20, 1, MatterPhase.SOLID, 2.0);    
    public final static BulkResource MINERAL_FILLER = register("mineral_filler", MatterColors.DEPLETED_MINERAL_DUST, Compounds.MINERAL_FILLER, 20, 1, MatterPhase.SOLID, 2.0);

    public final static BulkResource MICRONIZED_STONE = register("micronized_stone", MatterColors.RAW_MINERAL_DUST, Compounds.RAW_MINERAL_DUST, 20, 1, MatterPhase.SOLID, 2.0);    
    public final static BulkResource MICRONIZED_BASALT = register("micronized_basalt", BlockColorMapProvider.COLOR_BASALT.getColor(EnumColorMap.BASE), Compounds.RAW_MINERAL_DUST, 20, 1, MatterPhase.SOLID, 2.0);    

    public final static BulkResource H2_GAS = register("h2_gas", 0xFFEA3323, Molecules.H2_GAS, 20, 1);
    public final static BulkResource N2_GAS = register("n2_gas", 0xFF526FDC, Molecules.N2_GAS, 20, 1);
    public final static BulkResource O2_GAS = register("o2_gas", 0xFFBC4039, Molecules.O2_GAS, 20, 1);
    public final static BulkResource AR_GAS = register("ar_gas", 0xFFDEFEFE, Molecules.AR_GAS, 20, 1);
    public final static BulkResource CO2_GAS = register("co2_gas", 0xFFD9B2B0, Molecules.CO2_GAS, 20, 1);
    public final static BulkResource H2O_FLUID = register("h2o_fluid", MatterColors.WATER, Molecules.H2O_FLUID, 20, 1, MatterPhase.LIQUID, 0.9982);
    public final static BulkResource METHANE_GAS = register("methane_gas", 0xFFFFFFFF, Molecules.METHANE_GAS, 20, 1);
    public final static BulkResource AMMONIA_GAS = register("ammonia_gas", MatterColors.AMMONIA, Molecules.AMMONIA_GAS, 20, 1);
    public final static BulkResource AMMONIA_LIQUID = register("ammonia_liquid", MatterColors.AMMONIA, Molecules.AMMONIA_LIQUID, 20, 20, MatterPhase.LIQUID, 0.6470);
    public final static BulkResource SILICA = register("silica", MatterColors.SILICA, Molecules.SILICA, 20, 1, MatterPhase.SOLID, 2.648);
    public final static BulkResource LITHIUM = register("lithium", 0xFFC28EF8, Molecules.LITHIUM, 20, 1, MatterPhase.SOLID, 0.5340);
    public final static BulkResource GRAPHITE = register("graphite", 0xFF746C67, Molecules.GRAPHITE, 20, 1, MatterPhase.SOLID, 2.2660);
    public final static BulkResource ALUMINUM = register("aluminum", 0xFFDAA4A1, Molecules.ALUMINUM, 20, 1, MatterPhase.SOLID, 2.7000);
    public final static BulkResource SILICON = register("silicon", 0xFFE9CAA5, Molecules.SILICON, 20, 1, MatterPhase.SOLID, 2.3290);
    public final static BulkResource SULFUR = register("sulfer", 0xFFEAF857, Molecules.SULFUR, 20, 1, MatterPhase.SOLID, 2.0670);
    public final static BulkResource IRON = register("iron", 0xFF808080, Molecules.IRON, 20, 1, MatterPhase.SOLID, 7.8740);
    public final static BulkResource COPPER = register("copper", 0xFFBE8343, Molecules.COPPER, 20, 1, MatterPhase.SOLID, 8.9600);
    public final static BulkResource NICKEL = register("nickel", 0xFF75CD61, Molecules.NICKEL, 20, 1, MatterPhase.SOLID, 8.9120);
    public final static BulkResource SILVER = register("silver", 0xFFC0C0C0, Molecules.SILVER, 20, 1, MatterPhase.SOLID, 10.5010);
    public final static BulkResource TIN = register("tin", 0xFF6A7C7E, Molecules.TIN, 20, 1, MatterPhase.SOLID, 7.2870);
    public final static BulkResource PLATINUM = register("platinum", 0xFFD0D0DF, Molecules.PLATINUM, 20, 1, MatterPhase.SOLID, 21.4600);
    public final static BulkResource GOLD = register("gold", 0xFFF8D34F, Molecules.GOLD, 20, 1, MatterPhase.SOLID, 19.2820);
    public final static BulkResource LEAD = register("lead", 0xFF575960, Molecules.LEAD, 20, 1, MatterPhase.SOLID, 11.3420);
    public final static BulkResource BORON = register("boron", 0xFFF4B8B7,  Molecules.BORON, 20, 1, MatterPhase.SOLID, 2.3400);
    public final static BulkResource SODIUM = register("sodium", 0xFFA17FD1, Molecules.SODIUM, 20, 1, MatterPhase.SOLID, 0.9710);
    public final static BulkResource MAGNESIUM = register("magnesium", 0xFFACD847, Molecules.MAGNESIUM, 20, 1, MatterPhase.SOLID, 1.7380);
    public final static BulkResource PHOSPHORUS = register("phosphorus", 0xFFEF8733, Molecules.PHOSPHORUS, 20, 1, MatterPhase.SOLID, 1.8200);
    public final static BulkResource POTASSIUM = register("potassium", 0xFF836CAF, Molecules.POTASSIUM, 20, 1, MatterPhase.SOLID, 0.8620);
    public final static BulkResource CALCIUM = register("calcium", 0xFF8ABC3F, Molecules.CALCIUM, 20, 1, MatterPhase.SOLID, 1.5400);
    public final static BulkResource TITANIUM = register("titanium", 0xFFF6F6F6,Molecules.TITANIUM, 20, 1, MatterPhase.SOLID, 4.5400);
    public final static BulkResource CHROMIUM = register("chromium", 0xFF8D98C3, Molecules.CHROMIUM, 20, 1, MatterPhase.SOLID, 7.1500);
    public final static BulkResource MANGANESE = register("manganese", 0xFFA5335F, Molecules.MANGANESE, 20, 1, MatterPhase.SOLID, 7.4400);
    public final static BulkResource COBALT = register("cobalt", 0xFF346FFF, Molecules.COBALT, 20, 1, MatterPhase.SOLID, 8.8600);
    public final static BulkResource ZINC = register("zinc", 0xFF7E80AC, Molecules.ZINC, 20, 1, MatterPhase.SOLID, 7.1340);
    public final static BulkResource MOLYBDENUM = register("molybdenum", 0xFF6FBEC2,Molecules.MOLYBDENUM, 20, 1, MatterPhase.SOLID, 10.2200);
    public final static BulkResource NEODYMIUM = register("neodymium", 0xFFCFAB5C, Molecules.NEODYMIUM, 20, 1, MatterPhase.SOLID, 7.0070);
    public final static BulkResource TUNGSTEN_POWDER = register("tungsten_powder", 0xFFA3609F, Molecules.TUNGSTEN_POWDER, 20, 1, MatterPhase.SOLID, 19.2500);
    public final static BulkResource DYE_CYAN = register("dye_cyan", 0xFF99FFFF, Molecules.DYE_CYAN, 20, 1, MatterPhase.LIQUID, 1.0000);
    public final static BulkResource DYE_MAGENTA = register("dye_magenta", 0xFFFF99FF, Molecules.DYE_MAGENTA, 20, 1, MatterPhase.LIQUID, 1.0000);
    public final static BulkResource DYE_YELLOW = register("dye_yellow", 0xFFFFFF99, Molecules.DYE_YELLOW, 20, 1, MatterPhase.LIQUID, 1.0000);
    public final static BulkResource FLEX_RESIN = register("flex_resin", 0xFFE8F5C6, Molecules.FLEX_RESIN, 20, 1, MatterPhase.LIQUID, 1.0000);
    public final static BulkResource FLEX_FIBER = register("flex_fiber", 0xFFD0DDB3, Molecules.FLEX_FIBER, 20, 1, MatterPhase.LIQUID, 1.0000);
    public final static BulkResource DURA_RESIN = register("dura_resin", 0xFFAA9292, Molecules.DURA_RESIN, 20, 1, MatterPhase.SOLID, 2.0000);
    public final static BulkResource PEROVSKITE = register("perovskite", 0xFFFFFFFF, Molecules.PEROVSKITE, 20, 1, MatterPhase.SOLID, 6.0000);
    public final static BulkResource H2O_VAPOR = register("h2o_vapor", MatterColors.WATER, Molecules.H2O_VAPOR, 20, 1);
    public final static BulkResource ETHENE_GAS = register("ethene_gas", MatterColors.ETHENE    , Molecules.ETHENE_GAS, 20, 1);
    public final static BulkResource ETHANOL_LIQUID = register("ethanol_liquid", 0xFFFFFFFF, Molecules.ETHANOL_LIQUID, 20, 1, MatterPhase.LIQUID, 0.7890);
    public final static BulkResource SUPER_FUEL = register("super_fuel", 0xFFFFFFFF, Molecules.SUPER_FUEL, 20, 1, MatterPhase.LIQUID, 0.9820);
    public final static BulkResource HDPE = register("hdpe", MatterColors.HDPE, Molecules.HDPE, 20, 1, MatterPhase.SOLID, 0.9700);
    public final static BulkResource CALCIUM_CARBONATE = register("calcium_carbonate", 0xFFFFFFFF, Molecules.CALCIUM_CARBONATE, 20, 1, MatterPhase.SOLID, 2.7110);
    public final static BulkResource MAGNETITE = register("magnetite", 0xFFFFFFFF, Molecules.MAGNETITE, 20, 1, MatterPhase.SOLID, 5.0000);
    public final static BulkResource DIAMOND = register("diamond", 0xFFFFFFFF, Molecules.DIAMOND, 20, 1, MatterPhase.SOLID, 3.5000);
    public final static BulkResource CARBON_VAPOR = register("carbon_vapor", 0xFFFFFFFF, Molecules.CARBON_VAPOR, 20, 1);
    public final static BulkResource FLEX_ALLOY = register("flex_alloy", 0xFFFFFFFF, Molecules.FLEX_ALLOY, 20, 1, MatterPhase.SOLID, 2.7000);
    public final static BulkResource SILICON_NITRIDE = register("silicon_nitride", 0xFFFFFFFF, Molecules.SILICON_NITRIDE, 20, 1, MatterPhase.SOLID, 3.4400);
    public final static BulkResource MONOCALCIUM_PHOSPHATE = register("monocalcium_phosphate", 0xFFFFFFFF, Molecules.MONOCALCIUM_PHOSPHATE, 20, 1, MatterPhase.SOLID, 2.2200);
    public final static BulkResource CALCIUM_NITRATE = register("calcium_nitrate", 0xFFFFFFFF, Molecules.CALCIUM_NITRATE, 20, 1, MatterPhase.SOLID, 2.5040);
    public final static BulkResource SODIUM_NITRATE = register("sodium_nitrate", 0xFFFFFFFF, Molecules.SODIUM_NITRATE, 20, 1, MatterPhase.SOLID, 2.2570);
    public final static BulkResource POTASSIUM_NITRATE = register("potassium_nitrate", 0xFFFFFFFF, Molecules.POTASSIUM_NITRATE, 20, 1, MatterPhase.SOLID, 2.1090);
    public final static BulkResource LITHIUM_NITRATE = register("lithium_nitrate", 0xFFFFFFFF, Molecules.LITHIUM_NITRATE, 20, 1, MatterPhase.SOLID, 2.3800);
    public final static BulkResource MAGNESIUM_NITRATE = register("magnesium_nitrate", 0xFFFFFFFF, Molecules.MAGNESIUM_NITRATE, 20, 1, MatterPhase.SOLID, 2.3000);
    public final static BulkResource SODIUM_CHLORIDE = register("sodium_chloride", 0xFFFFFFFF, Molecules.SODIUM_CHLORIDE, 20, 1, MatterPhase.SOLID, 2.1650);
    public final static BulkResource CALCIUM_FLUORIDE = register("calcium_fluoride", 0xFFFFFFFF, Molecules.CALCIUM_FLUORIDE, 20, 1, MatterPhase.SOLID, 3.1800);
    public static final BulkResource BORIC_ACID = register("boric_acid", 0xFFFFFFFF, Molecules.BORIC_ACID, 20, 1, MatterPhase.SOLID, 1.435);
    public static final BulkResource PHOSPHORIC_ACID = register("phosphoric_acid", 0xFFFFFFFF, Molecules.PHOSPHORIC_ACID, 20, 1, MatterPhase.SOLID, 2.030);
    public static final BulkResource ALUMINA = register("alumina", 0xFFFFFFFF, Molecules.ALUMINA, 20, 1, MatterPhase.SOLID, 3.987);

    // These should be needed as fluids - only exist within residues
    // If they are later needed, densities needs to be updated
//    public static final BulkResource CUPRIC_OXIDE 
//        = register("cupric_oxide", 0xFFFFFFFF, Molecules.CUPRIC_OXIDE, 20, 1, MatterPhase.SOLID, 3.1800);
//    public static final BulkResource LEAD_OXIDE 
//        = register("lead_oxide", 0xFFFFFFFF, Molecules.LEAD_OXIDE, 20, 1, MatterPhase.SOLID, 3.1800);
//    public static final BulkResource COBALT_OXIDE 
//        = register("cobalt_oxide", 0xFFFFFFFF, Molecules.COBALT_OXIDE, 20, 1, MatterPhase.SOLID, 3.1800);
//    public static final BulkResource NICKEL_OXIDE 
//        = register("nickel_oxide", 0xFFFFFFFF, Molecules.NICKEL_OXIDE, 20, 1, MatterPhase.SOLID, 3.1800);
//    public static final BulkResource ZINC_OXIDE 
//        = register("zinc_oxide", 0xFFFFFFFF, Molecules.ZINC_OXIDE, 20, 1, MatterPhase.SOLID, 3.1800);
//    public static final BulkResource MANGANESE_DIOXIDE 
//        = register("manganese_dioxide", 0xFFFFFFFF, Molecules.MANGANESE_DIOXIDE, 20, 1, MatterPhase.SOLID, 3.1800);
//    public static final BulkResource TIN_DIOXIDE 
//        = register("tin_dioxide", 0xFFFFFFFF, Molecules.TIN_DIOXIDE, 20, 1, MatterPhase.SOLID, 3.1800);
//    public static final BulkResource MOLYBDENUM_TRIOXIDE 
//        = register("molybdenum_trioxide", 0xFFFFFFFF, Molecules.MOLYBDENUM_TRIOXIDE, 20, 1, MatterPhase.SOLID, 3.1800);
//    public static final BulkResource HEMATITE 
//        = register("hematite", 0xFFFFFFFF, Molecules.HEMATITE, 20, 1, MatterPhase.SOLID, 3.1800);
//    public static final BulkResource TUNGSTEN_TRIOXIDE 
//        = register("tungsten_trioxide", 0xFFFFFFFF, Molecules.TUNGSTEN_TRIOXIDE, 20, 1, MatterPhase.SOLID, 3.1800);
//    public static final BulkResource TITANIUM_DIOXIDE 
//        = register("titanium_dioxide", 0xFFFFFFFF, Molecules.TITANIUM_DIOXIDE, 20, 1, MatterPhase.SOLID, 3.1800);
//    public static final BulkResource ZIRCONIUM_DIOXIDE 
//        = register("zirconium_dioxide", 0xFFFFFFFF, Molecules.ZIRCONIUM_DIOXIDE, 20, 1, MatterPhase.SOLID, 3.1800);
//    public static final BulkResource CHROMIUM_OXIDE 
//        = register("chromium_oxide", 0xFFFFFFFF, Molecules.CHROMIUM_OXIDE, 20, 1, MatterPhase.SOLID, 3.1800);
//    public static final BulkResource NEODYMIUM_OXIDE 
//        = register("neodymium_oxide", 0xFFFFFFFF, Molecules.NEODYMIUM_OXIDE, 20, 1, MatterPhase.SOLID, 3.1800);
//    public static final BulkResource SELENIUM 
//        = register("selenium", 0xFFFFFFFF, Molecules.SELENIUM, 20, 1, MatterPhase.SOLID, 3.1800);
    
    
    public final static String DIGEST_NAME_PREFIX = "digested_";
    
    static
    {
        // create bulk resources / fluids for all digester outputs
        for(String res : Configurator.PROCESSING.digesterInputs)
        {
            BulkResource br = all.get(res);
            if(br == null)
            {
                Log.warn("Digester recipe output not created. Unable to find bulk resource named " + res);
            }
            else
            {
                DigesterAnalysis da = DigesterAnalysis.get(br);
                register(
                        DIGEST_NAME_PREFIX + res, 
                        da.residualColor(), 
                        da.residueCompound, 
                        20, 
                        1, 
                        MatterPhase.SOLID, 
                        da.residualDensity());
                DigesterRecipe.add(br);
            }
        }
    }
    
    public static Map<String, BulkResource> all()
    {
        if(all instanceof HashMap) all = ImmutableMap.copyOf(all);
        return all;
    }
    
    public static BulkResource get(String name)
    {
        return all.get(name);
    }
}
