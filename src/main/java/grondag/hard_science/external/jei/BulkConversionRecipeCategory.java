package grondag.hard_science.external.jei;


import java.util.List;

import javax.annotation.Nonnull;
import grondag.hard_science.HardScience;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class BulkConversionRecipeCategory implements IRecipeCategory<BulkConversionRecipeWrapper>
{

    public static final String UID = "hard_science.bulk_conversion";
    private final IDrawableStatic background;
    private final String localizedName;

    public BulkConversionRecipeCategory(IGuiHelper guiHelper)
    {
        ResourceLocation location = new ResourceLocation("hard_science", "textures/gui/builder.png");
        background = guiHelper.createDrawable(location, 0, 0, 166, 65, 0, 0, 0, 0);
        localizedName = I18n.format("hard_science.jei.bulk_conversion");
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
    public void setRecipe(IRecipeLayout recipeLayout, BulkConversionRecipeWrapper recipeWrapper, IIngredients ingredients)
    {
        if(!(recipeWrapper instanceof BulkConversionRecipeWrapper)) return;

        List<List<ItemStack>> itemInputs = ingredients.getInputs(ItemStack.class);
        List<List<FluidStack>> fluidInputs = ingredients.getInputs(FluidStack.class);
        
        int fluidIndex = 0;
        
        if(!itemInputs.isEmpty())
        {
            recipeLayout.getItemStacks().init(0, true, 39, 41);
            recipeLayout.getItemStacks().set(0, itemInputs.get(0));
        }
        else
        {
            recipeLayout.getFluidStacks().init(0, true, 39, 41);
            recipeLayout.getFluidStacks().set(0, fluidInputs.get(0));
            fluidIndex = 1;
        }

        recipeLayout.getFluidStacks().init(fluidIndex, false, 87, 41);
        recipeLayout.getFluidStacks().set(fluidIndex, ingredients.getOutputs(FluidStack.class).get(0));
    }

}