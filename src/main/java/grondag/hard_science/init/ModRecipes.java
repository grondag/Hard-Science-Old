package grondag.hard_science.init;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModRecipes
{
    public static void init(FMLInitializationEvent event) 
    {

        // convert rubble to cobble
        GameRegistry.addRecipe(new ItemStack(ModBlocks.basalt_cobble), new Object[]{
                "CCC",
                "CCC",
                "CCC",
                'C', ModItems.basalt_rubble
        });
        
        // convert cobble to rubble
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.basalt_rubble, 9), 
                new Object[]{new ItemStack(ModBlocks.basalt_cobble, 1)}
        );
        
        // smelt cobble to smooth basalt
        GameRegistry.addSmelting(ModBlocks.basalt_cobble, new ItemStack(ModItems.basalt_cut, 1, 0), 0.1F);

    }
}
