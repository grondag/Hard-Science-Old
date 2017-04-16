package grondag.adversity.init;

import grondag.adversity.niceblock.NiceBlockRegistrar;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModRecipes
{
    public static void init(FMLInitializationEvent event) 
    {

        // convert rubble to cobble
        GameRegistry.addRecipe(new ItemStack(NiceBlockRegistrar.BASALT_COBBLE_BLOCK), new Object[]{
                "CCC",
                "CCC",
                "CCC",
                'C', ModItems.basalt_rubble
        });
        
        // convert cobble to rubble
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.basalt_rubble, 9), 
                new Object[]{new ItemStack(NiceBlockRegistrar.BASALT_COBBLE_BLOCK, 1)}
        );
        
        // smelt cobble to smooth basalt
        GameRegistry.addSmelting(NiceBlockRegistrar.BASALT_COBBLE_BLOCK, new ItemStack(NiceBlockRegistrar.COOL_SQUARE_BASALT_ITEM, 1, 0), 0.1F);

    }
}
