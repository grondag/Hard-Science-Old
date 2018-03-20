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

public class BuilderRecipeCategory implements IRecipeCategory<BuilderRecipeWrapper>
{

    public static final String UID = "hard_science.builder";
    private final IDrawableStatic background;
    private final String localizedName;

    public BuilderRecipeCategory(IGuiHelper guiHelper)
    {
        ResourceLocation location = new ResourceLocation("hard_science", "textures/gui/builder.png");
        background = guiHelper.createDrawable(location, 0, 0, 166, 65, 0, 0, 0, 0);
        localizedName = I18n.format("hard_science.jei.builder");
    }

    @Override
    public String getUid()
    {
        return UID;
    }

    @Override
    public String getTitle()
    {
        return localizedName;
    }

    @Override
    public String getModName()
    {
        return HardScience.MODNAME;
    }

    @Override
    public IDrawable getBackground()
    {
        return background;
    }


    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull BuilderRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients)
    {
        // TODO
    }

}