package grondag.hard_science.init;

import grondag.hard_science.Configurator;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
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
    }
}
