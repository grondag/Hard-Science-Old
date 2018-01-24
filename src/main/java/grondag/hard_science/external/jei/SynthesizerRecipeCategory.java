package grondag.hard_science.external.jei;


import javax.annotation.Nonnull;
import grondag.hard_science.HardScience;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class SynthesizerRecipeCategory implements IRecipeCategory<SynthesizerRecipeWrapper>
{

    public static final String UID = "hard_science.synthesizer";
    private final IDrawableStatic background;
    private final String localizedName;

    public SynthesizerRecipeCategory(IGuiHelper guiHelper)
    {
        ResourceLocation location = new ResourceLocation("hard_science", "textures/gui/builder.png");
        background = guiHelper.createDrawable(location, 0, 0, 166, 65, 0, 0, 0, 0);
        localizedName = I18n.format("hard_science.jei.synthesizer");
    }

    @Nonnull
    @Override
    public String getUid()
    {
        return UID;
    }

    @Nonnull
    @Override
    public String getTitle()
    {
        return localizedName;
    }

    @Nonnull
    @Override
    public String getModName()
    {
        return HardScience.MODNAME;
    }

    @Nonnull
    @Override
    public IDrawable getBackground()
    {
        return background;
    }


    @Override
    public void setRecipe(IRecipeLayout recipeLayout, SynthesizerRecipeWrapper recipeWrapper, IIngredients ingredients)
    {
        recipeLayout.getFluidStacks();
    }

}