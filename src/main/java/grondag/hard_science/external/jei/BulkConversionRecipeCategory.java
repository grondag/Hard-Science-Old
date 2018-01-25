package grondag.hard_science.external.jei;


import java.util.List;

import javax.annotation.Nonnull;

import grondag.hard_science.HardScience;
import grondag.hard_science.crafting.BulkConversionRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.config.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class BulkConversionRecipeCategory implements IRecipeCategory<BulkConversionRecipe<?>>
{

    public static final String UID = "hard_science.bulk_conversion";
    private final IDrawableStatic background;
    private final String localizedName;
    private final IDrawableAnimated arrow;
    private final IDrawableStatic slot;
    private final static int LEFT = 32;
    private final static int RIGHT = 80;
    private final IDrawable icon;
    
    public BulkConversionRecipeCategory(IGuiHelper guiHelper)
    {
        ResourceLocation location = new ResourceLocation("hard_science", "textures/blocks/material_shortage.png");
        icon = guiHelper.createDrawable(location, 0, 0, 16, 16, 16, 16);
        
        this.background = guiHelper.createBlankDrawable(128, 32);
        this.localizedName = I18n.format("hard_science.jei.bulk_conversion");
        this.slot = guiHelper.getSlotDrawable();
        IDrawableStatic arrowDrawable = guiHelper.createDrawable(Constants.RECIPE_GUI_VANILLA, 82, 128, 24, 17);
        this.arrow = guiHelper.createAnimatedDrawable(arrowDrawable, 40, IDrawableAnimated.StartDirection.LEFT, false);
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
    public void drawExtras(Minecraft minecraft)
    {
        this.slot.draw(minecraft, LEFT, 8);
        this.slot.draw(minecraft, RIGHT, 8);
        arrow.draw(minecraft, 52, 9);
    }

    @Override
    public IDrawable getIcon()
    {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, BulkConversionRecipe<?> recipe, IIngredients ingredients)
    {
        if(!(recipe instanceof  BulkConversionRecipe)) return;

        List<List<ItemStack>> itemInputs = ingredients.getInputs(ItemStack.class);
        List<List<FluidStack>> fluidInputs = ingredients.getInputs(FluidStack.class);
        
        int fluidIndex = 0;
        
        if(!itemInputs.isEmpty())
        {
            recipeLayout.getItemStacks().init(0, true, LEFT, 8);
            recipeLayout.getItemStacks().set(0, itemInputs.get(0));
        }
        else
        {
            recipeLayout.getFluidStacks().init(0, true, LEFT, 8);
            recipeLayout.getFluidStacks().set(0, fluidInputs.get(0));
            fluidIndex = 1;
        }

        recipeLayout.getFluidStacks().init(fluidIndex, false, RIGHT, 8);
        recipeLayout.getFluidStacks().set(fluidIndex, ingredients.getOutputs(FluidStack.class).get(0));
    }

}