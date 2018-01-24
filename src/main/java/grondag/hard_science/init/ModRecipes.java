package grondag.hard_science.init;

import java.util.ArrayList;

import grondag.hard_science.Configurator;
import grondag.hard_science.crafting.BuilderRecipe;
import grondag.hard_science.crafting.BulkConversionReceipe;
import grondag.hard_science.crafting.SynthesizerRecipe;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.resource.ItemResource;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModRecipes
{
    public static final ArrayList<BuilderRecipe> builderRecipes = new ArrayList<BuilderRecipe>();
    public static final ArrayList<SynthesizerRecipe> synthesizerRecipes = new ArrayList<SynthesizerRecipe>();
    
    public static void init(FMLInitializationEvent event) 
    {
        if(Configurator.VOLCANO.enableVolcano)
        {
            // smelt cobble to smooth basalt
            GameRegistry.addSmelting(ModBlocks.basalt_cobble, new ItemStack(ModItems.basalt_cut, 1, 0), 0.1F);
        }
        
        BulkConversionReceipe.addConversion(
                ModFluids.FLOWABLE_MINERAL_FILLER_RESOURCE,
                ItemResource.fromStack(Item.getItemFromBlock(Blocks.SAND).getDefaultInstance()), 
                VolumeUnits.KILOLITER.nL);

        BulkConversionReceipe.addConversion(
                ModFluids.FLOWABLE_MINERAL_FILLER_RESOURCE,
                ModFluids.DEPLETED_MINERAL_DUST_RESOURCE,
                1.0);
        
        BulkConversionReceipe.addConversion(
                ModFluids.FLOWABLE_MINERAL_FILLER_RESOURCE,
                ModFluids.RAW_MINERAL_DUST_RESOURCE,
                1.0);
    }
}
