package grondag.hard_science.external.jei;

import javax.annotation.Nonnull;

import grondag.hard_science.crafting.BulkConversionRecipe;
import grondag.hard_science.init.ModFluids;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;


@JEIPlugin
public class HardScienceJEIPlugIn implements IModPlugin
{

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry)
    {
        registry.addRecipeCategories(
                new BulkConversionRecipeCategory(registry.getJeiHelpers().getGuiHelper())
//                new BuilderRecipeCategory(registry.getJeiHelpers().getGuiHelper()),
//                new SynthesizerRecipeCategory(registry.getJeiHelpers().getGuiHelper())
                );
    }

    @Override
    public void register(@Nonnull IModRegistry registry)
    {

//        registry.handleRecipes(BuilderRecipe.class, BuilderRecipeWrapper::new, BuilderRecipeCategory.UID);
//        registry.handleRecipes(SynthesizerRecipe.class, SynthesizerRecipeWrapper::new, SynthesizerRecipeCategory.UID);

        registry.addRecipes(BulkConversionRecipe.allRecipes(), BulkConversionRecipeCategory.UID);
        registry.addIngredientInfo(ModFluids.FLOWABLE_MINERAL_FILLER_RESOURCE.newStackWithLiters(Fluid.BUCKET_VOLUME), FluidStack.class, "fluid.flowable_mineral_filler.desc");
//        registry.addRecipes(ModRecipes.builderRecipes, BuilderRecipeCategory.UID);
//        registry.addRecipes(ModRecipes.synthesizerRecipes, SynthesizerRecipeCategory.UID);

//        registry.addRecipeCatalyst(new ItemStack(ModBlocks.block_fabricator), BuilderRecipeCategory.UID);

//        registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerCraftingHalo.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
    }
}
