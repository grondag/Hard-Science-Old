package grondag.hard_science.init;

import static grondag.hard_science.HardScience.resource;

import java.util.ArrayList;

import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.crafting.processing.MicronizerRecipe;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreIngredient;

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

        MicronizerRecipe.add(
                new OreIngredient("sand"), 
                ModBulkResources.MICRONIZED_STONE,
                0.5,
                MicronizerRecipe.FixedConverter.FULL_BLOCK);

        MicronizerRecipe.add(
                new OreIngredient("gravel"), 
                ModBulkResources.MICRONIZED_STONE,
                0.65,
                MicronizerRecipe.FixedConverter.FULL_BLOCK);
        
        MicronizerRecipe.add(
                new OreIngredient("sandstone"), 
                ModBulkResources.MICRONIZED_STONE,
                0.7,
                MicronizerRecipe.FixedConverter.FULL_BLOCK);
        
        MicronizerRecipe.add(
                new OreIngredient("cobblestone"), 
                ModBulkResources.MICRONIZED_STONE,
                0.8,
                MicronizerRecipe.FixedConverter.FULL_BLOCK);
        
        MicronizerRecipe.add(
                new OreIngredient("stone"), 
                ModBulkResources.MICRONIZED_STONE,
                1.0,
                MicronizerRecipe.FixedConverter.FULL_BLOCK);
        
        MicronizerRecipe.add(
                Ingredient.fromItem(Item.getItemFromBlock(Blocks.COBBLESTONE_WALL)), 
                ModBulkResources.MICRONIZED_STONE,
                0.8,
                MicronizerRecipe.FixedConverter.FULL_BLOCK);
        
        MicronizerRecipe.add(
                Ingredient.fromItem(Item.getItemFromBlock(Blocks.STONE_SLAB)), 
                ModBulkResources.MICRONIZED_STONE,
                1.0,
                MicronizerRecipe.FixedConverter.HALF_BLOCK);
        
        MicronizerRecipe.add(
                Ingredient.fromItem(Item.getItemFromBlock(Blocks.STONE_SLAB2)), 
                ModBulkResources.MICRONIZED_STONE,
                1.0,
                MicronizerRecipe.FixedConverter.HALF_BLOCK);
        
        MicronizerRecipe.add(
                Ingredient.fromItem(Item.getItemFromBlock(ModBlocks.basalt_cobble)), 
                ModBulkResources.MICRONIZED_BASALT,
                1.0,
                MicronizerRecipe.FixedConverter.FULL_BLOCK);
        
        MicronizerRecipe.add(
                Ingredient.fromItem(Item.getItemFromBlock(ModBlocks.basalt_cut)), 
                ModBulkResources.MICRONIZED_BASALT,
                1.2,
                MicronizerRecipe.FixedConverter.FULL_BLOCK);

        MicronizerRecipe.add(
                Ingredient.fromItem(ModItems.basalt_rubble), 
                ModBulkResources.MICRONIZED_BASALT,
                1.0,
                MicronizerRecipe.FixedConverter.NINTH_BLOCK);
        
        MicronizerRecipe.add(
                Ingredient.fromItem(Item.getItemFromBlock(ModBlocks.basalt_cool_static_height)),
                ModBulkResources.MICRONIZED_BASALT,
                1.2,
                MicronizerRecipe.TerrainConverter.INSTANCE);
                
        MicronizerRecipe.add(
                Ingredient.fromItem(Item.getItemFromBlock(ModBlocks.basalt_cool_static_filler)),
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
