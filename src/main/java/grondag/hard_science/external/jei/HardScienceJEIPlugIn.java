package grondag.hard_science.external.jei;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.crafting.BulkConversionRecipe;
import grondag.hard_science.crafting.synthesis.SolarAmmoniaRecipe;
import grondag.hard_science.crafting.synthesis.SolarElectrolysisRecipe;
import grondag.hard_science.crafting.synthesis.SolarEtheneRecipe;
import grondag.hard_science.matter.Matters;
import grondag.hard_science.matter.VolumeUnits;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;


@JEIPlugin
public class HardScienceJEIPlugIn implements IModPlugin
{

    private static IJeiRuntime runtime;
    public static IJeiRuntime runtime() { return runtime; }
    
    private static IModRegistry registry;
    public static IModRegistry registry() { return registry; }
    
    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
    {
        runtime = jeiRuntime;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry)
    {
        registry.addRecipeCategories(
                new BulkConversionRecipe.Category(registry.getJeiHelpers().getGuiHelper()),
                new SolarElectrolysisRecipe.Category(registry.getJeiHelpers().getGuiHelper()),
                new SolarAmmoniaRecipe.Category(registry.getJeiHelpers().getGuiHelper()),
                new SolarEtheneRecipe.Category(registry.getJeiHelpers().getGuiHelper())
                );
    }

    @Override
    public void register(@Nonnull IModRegistry registry)
    {
        HardScienceJEIPlugIn.registry = registry;
        
//        registry.handleRecipes(BuilderRecipe.class, BuilderRecipeWrapper::new, BuilderRecipeCategory.UID);
//        registry.handleRecipes(SynthesizerRecipe.class, SynthesizerRecipeWrapper::new, SynthesizerRecipeCategory.UID);

        registry.addRecipes(BulkConversionRecipe.allRecipes(), BulkConversionRecipe.UID);
        registry.addIngredientInfo(Matters.FLOWABLE_MINERAL_FILLER.resource().newStackWithLiters(Fluid.BUCKET_VOLUME), FluidStack.class, "fluid.flowable_mineral_filler.desc");

        registry.addRecipes(
                ImmutableList.of(SolarElectrolysisRecipe.PROCESS
                        .configureFromInputs(ImmutableList.of(Matters.H2O.resource().withQuantity(VolumeUnits.LITER.nL)))), 
                SolarElectrolysisRecipe.UID);
        
        registry.addRecipes(
                ImmutableList.of(SolarAmmoniaRecipe.PROCESS
                        .configureFromInputs(ImmutableList.of(Matters.H2O.resource().withQuantity(VolumeUnits.LITER.nL)))), 
                SolarAmmoniaRecipe.UID);
        
        registry.addRecipes(
                ImmutableList.of(SolarEtheneRecipe.PROCESS
                        .configureFromInputs(ImmutableList.of(Matters.H2O.resource().withQuantity(VolumeUnits.LITER.nL)))), 
                SolarEtheneRecipe.UID);

        
        //        registry.addRecipes(ModRecipes.builderRecipes, BuilderRecipeCategory.UID);
//        registry.addRecipes(ModRecipes.synthesizerRecipes, SynthesizerRecipeCategory.UID);

//        registry.addRecipeCatalyst(new ItemStack(ModBlocks.block_fabricator), BuilderRecipeCategory.UID);

//        registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerCraftingHalo.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
    }
}
