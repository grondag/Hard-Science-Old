package grondag.hard_science.init;

import static grondag.hard_science.HardScience.resource;

import java.util.ArrayList;

import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.Log;
import grondag.hard_science.crafting.processing.MicronizerRecipe;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.domain.ProcessManager;
import grondag.hard_science.simulator.resource.BulkResource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModRecipes
{
    public static void init(FMLInitializationEvent event) 
    {
        if(Configurator.VOLCANO.enableVolcano)
        {
            // smelt cobble to smooth basalt
            GameRegistry.addSmelting(ModBlocks.basalt_cobble, new ItemStack(ModItems.basalt_cut, 1, 0), 0.1F);
        }
        
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
                    Log.warn("Skipping invalid micronizer recipe: " + csv);
                    continue;
                }
                
                Ingredient ing = ProcessManager.readIngredient(args[0].trim());
                if(ing == null || ing == Ingredient.EMPTY)
                {
                    Log.warn("Skipping invalid micronizer recipe: " + csv);
                    continue;
                }
                
                Fluid fluid = FluidRegistry.getFluid(args[1].trim());
                BulkResource br = BulkResource.fromFluid(fluid);
                if(br == null)
                {
                    Log.warn("Skipping invalid micronizer recipe: " + csv);
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
                Log.error("Unable to read micronizer recipe: " + csv, e);
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

        
        addRecipe("basalt_cobble", 0, "AAAAAAAAA", "basalt_rubble");
    
    }
    
    private static final ResourceLocation group = resource(HardScience.MODID);
    
    private static void addRecipe(String itemName, int index, String recipe, String... inputs)
    {
        String[] lines = new String[3];
        lines[0] = recipe.substring(0, 3);
        lines[1] = recipe.substring(3, 6);
        lines[2] = recipe.substring(6, 9);
        
        final char[] symbols = "ABCDEFGHI".toCharArray();
        int i = 0;
        ArrayList<Object> params = new ArrayList<Object>();
        
        params.add(lines);
        
        for(String s : inputs)
        {
            params.add((Character)symbols[i]);
            params.add(ForgeRegistries.ITEMS.getValue(resource(s)));
            i++;
        }
        
        GameRegistry.addShapedRecipe(
                resource(itemName + index), 
                group,
                ForgeRegistries.ITEMS.getValue(resource(itemName)).getDefaultInstance(),
                params.toArray());
    }
}
