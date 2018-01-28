package grondag.hard_science.matter;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.minecraftforge.fluids.FluidRegistry;

public class Matters
{
    private static Map<String, Matter> all = new HashMap<String, Matter>();
    
    private static Matter register(
            String systemName,
            int color,
            IComposition molecule,
            double tempCelsius,
            double pressureAtm,
            MatterPhase phase,
            double density)
    {
        Matter result = new Matter(systemName, color, molecule, tempCelsius, pressureAtm, phase, density);
        all.put(systemName, result);
        return result;
    }
    
    private static Matter register(
            String systemName,
            int color,
            IComposition molecule,
            double tempCelsius,
            double pressureAtm)
    {
        Matter result = new Matter(systemName, color, molecule, tempCelsius, pressureAtm);
        all.put(systemName, result);
        return result;
    }
    
    public final static Matter WATER = register(
            FluidRegistry.WATER.getName(), 
            FluidRegistry.WATER.getColor(), 
            Molecules.H2O_FLUID, 
            20, 
            1, 
            MatterPhase.LIQUID, 
            0.99823);
    
    public final static Matter H2O = register(
            "purified_water", 
            MatterColors.WATER, 
            Molecules.H2O_FLUID, 
            20, 
            1, 
            MatterPhase.LIQUID, 
            0.99823);
    
    public final static Matter AMMONIA = register(
            "ammonia", 
            MatterColors.AMMONIA,
            Molecules.AMMONIA_GAS,
            20,
            1);
    
    public final static Matter FRESH_AIR = register(
            "fresh_air", 
            MatterColors.FRESH_AIR,
            Compounds.FRESH_AIR,
            20,
            1);
   
    public final static Matter RETURN_AIR = register(
            "return_air", 
            MatterColors.RETURN_AIR,
            Compounds.FRESH_AIR,
            20,
            1);
    
    public final static Matter OXYGEN_GAS = register(
            "oxygen_gas", 
            MatterColors.OXYGEN,
            Molecules.O2,
            20,
            1);
    
    public final static Matter HYDROGEN_GAS = register(
            "hydrogen_gas", 
            MatterColors.HYDROGEN,
            Molecules.H2,
            20,
            1);
    
    public final static Matter ETHENE_GAS = register(
            "ethene_gas", 
            MatterColors.ETHENE,
            Molecules.ETHENE_GAS,
            20,
            1);
    
    public final static Matter FLOWABLE_GRAPHITE = register(
            "flowable_graphite", 
            MatterColors.GRAPHITE, 
            Molecules.GRAPHITE, 
            20, 
            1, 
            MatterPhase.SOLID, 
            2.09);
    
    public final static Matter FLOWABLE_MINERAL_FILLER = register(
            "flowable_mineral_filler", 
            MatterColors.DEPLETED_MINERAL_DUST, 
            Compounds.SAND, 
            20, 
            1, 
            MatterPhase.SOLID, 
            1.5);

    public final static Matter RAW_MINERAL_DUST = register(
            "raw_mineral_dust", 
            MatterColors.RAW_MINERAL_DUST, 
            Compounds.MINERAL_DUST, 
            20, 
            1, 
            MatterPhase.SOLID, 
            1.8);
    
    public final static Matter DEPLETED_MINERAL_DUST = register(
            "depleted_mineral_dust", 
            MatterColors.DEPLETED_MINERAL_DUST, 
            Compounds.SAND, 
            20, 
            1, 
            MatterPhase.SOLID, 
            1.5);
    
    public static Map<String, Matter> all()
    {
        if(all instanceof HashMap) all = ImmutableMap.copyOf(all);
        return all;
    }
}
