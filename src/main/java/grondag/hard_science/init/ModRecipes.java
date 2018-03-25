package grondag.hard_science.init;

import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.crafting.processing.MicronizerRecipe;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.domain.ProcessManager;
import grondag.hard_science.simulator.resource.BulkResource;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

public class ModRecipes
{
    public static void init(FMLInitializationEvent event) 
    {
        
        ///////////////////////////////////////////////////////////
        /// MICRONIZER
        ///////////////////////////////////////////////////////////

        for(String csv : Configurator.PROCESSING.micronizerRecipes)
        {
            try
            {
                String[] args = csv.split(",");
                
                if(args.length != 4)
                {
                    HardScience.INSTANCE.warn("Skipping invalid micronizer recipe: " + csv);
                    continue;
                }
                
                Ingredient ing = ProcessManager.readIngredient(args[0].trim());
                if(ing == null || ing == Ingredient.EMPTY)
                {
                    HardScience.INSTANCE.warn("Skipping invalid micronizer recipe: " + csv);
                    continue;
                }
                
                Fluid fluid = FluidRegistry.getFluid(ModBulkResources.micronizerOutputName(args[1].trim()));
                BulkResource br = BulkResource.fromFluid(fluid);
                if(br == null)
                {
                    HardScience.INSTANCE.warn("Skipping invalid micronizer recipe: " + csv);
                    continue;
                }
                
                double liters = Double.parseDouble(args[2].trim());
                double energyFactor = Double.parseDouble(args[3].trim());
                
                MicronizerRecipe.add(
                        ing,
                        BulkResource.fromFluid(fluid),
                        energyFactor,
                        new MicronizerRecipe.FixedConverter((long) (liters * VolumeUnits.LITER.nL)));
            }
            catch(Exception e)
            {
                HardScience.INSTANCE.error("Unable to read micronizer recipe: " + csv, e);
            }
        }
        
        MicronizerRecipe.add(
                ProcessManager.readIngredient("hard_science:basalt_cool_static_height"),
                ModBulkResources.MICRONIZED_BASALT,
                1.2,
                MicronizerRecipe.TerrainConverter.INSTANCE);
                
        MicronizerRecipe.add(
                ProcessManager.readIngredient("hard_science:basalt_cool_static_filler"),
                ModBulkResources.MICRONIZED_BASALT,
                1.2,
                MicronizerRecipe.TerrainConverter.INSTANCE);

        //TODO: actual recipe
//        EmergencyFabricatorRecipe.addFab(
//                ImmutableList.of(
//                        Matters.FLOWABLE_GRAPHITE.resource().withQuantity(VolumeUnits.LITER.nL / 24),
//                        Matters.FLOWABLE_SILICA.resource().withQuantity(VolumeUnits.LITER.nL * 10 / 4),
//                        Matters.RESIN_A.resource().withQuantity((long) (VolumeUnits.LITER.nL * 2.5 / 4)),
//                        Matters.RESIN_B.resource().withQuantity((long) (VolumeUnits.LITER.nL * 0.5 / 4))
//                        ), 
//                ImmutableList.of(ItemResource.fromItem(ModItems.duraplast_joining_tool).withQuantity(1)));

    
    }
    
}
