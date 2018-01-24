package grondag.hard_science.external.jei;

import javax.annotation.Nonnull;

import grondag.hard_science.crafting.BulkConversionReceipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;


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
        registry.handleRecipes(BulkConversionReceipe.class, BulkConversionRecipeWrapper::new, BulkConversionRecipeCategory.UID);

//        registry.handleRecipes(BuilderRecipe.class, BuilderRecipeWrapper::new, BuilderRecipeCategory.UID);
//        registry.handleRecipes(SynthesizerRecipe.class, SynthesizerRecipeWrapper::new, SynthesizerRecipeCategory.UID);

        registry.addRecipes(BulkConversionReceipe.allRecipes(), BulkConversionRecipeCategory.UID);

//        registry.addRecipes(ModRecipes.builderRecipes, BuilderRecipeCategory.UID);
//        registry.addRecipes(ModRecipes.synthesizerRecipes, SynthesizerRecipeCategory.UID);

//        registry.addRecipeCatalyst(new ItemStack(ModBlocks.block_fabricator), BuilderRecipeCategory.UID);

//        registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerCraftingHalo.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
    }
}
