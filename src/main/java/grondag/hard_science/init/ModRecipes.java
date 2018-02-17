package grondag.hard_science.init;

import static grondag.hard_science.HardScience.resource;

import java.util.ArrayList;

import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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
        
//        BulkConversionRecipe.addConversion(
//                Matters.MINERAL_FILLER.fluidResource(),
//                ItemResource.fromStack(Item.getItemFromBlock(Blocks.SAND).getDefaultInstance()), 
//                VolumeUnits.KILOLITER.nL);
        
//        BulkConversionRecipe.addConversion(
//                Matters.MINERAL_FILLER.fluidResource(),
//                Matters.RAW_MINERAL_DUST.fluidResource(),
//                1.0);
    

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
