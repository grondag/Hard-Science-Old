package grondag.hard_science.external.jei;


import java.util.List;

import javax.annotation.Nonnull;

import grondag.hard_science.HardScience;
import grondag.hard_science.crafting.base.GenericRecipe;
import grondag.hard_science.simulator.resource.BulkResourceWithQuantity;
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

public class AbstractRecipeCategory<T extends GenericRecipe> implements IRecipeCategory<T>
{
    
    private final String UID;
    private final IDrawableStatic background;
    private final String localizedName;
    private final IDrawable icon;
    
    public AbstractRecipeCategory(
            IGuiHelper guiHelper,
            int maxWidth,
            int maxHeight,
            String UID, 
            ResourceLocation iconLocation)
    {
        this.UID = UID;
        icon = guiHelper.createDrawable(iconLocation, 0, 0, 16, 16, 16, 16);
        this.background = guiHelper.createBlankDrawable(maxWidth, maxHeight);
        this.localizedName = I18n.format("jei_category." + UID);
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
    public IDrawable getIcon()
    {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, T recipe, IIngredients ingredients)
    {
        IRecipeFormat format = recipe.format();
        
        int fluidIndex = 0;
        int itemIndex = 0;
        int bulkIndex = 0;
        {
            int inputIndex = 0;
            List<List<ItemStack>> itemInputs = ingredients.getInputs(ItemStack.class);
            if(!itemInputs.isEmpty())
            {
     
                for(List<ItemStack> input : itemInputs)
                {
                    recipeLayout.getItemStacks().init(itemIndex, true, format.inputX(inputIndex), format.inputY(inputIndex));
                    recipeLayout.getItemStacks().set(itemIndex, input);
                    itemIndex++;
                    inputIndex++;
                }
            }
            
            //NB: add 1 to non-stack coordinates to allow for slot margin
            List<List<FluidStack>> fluidInputs = ingredients.getInputs(FluidStack.class);
            if(!fluidInputs.isEmpty())
            {
                for(List<FluidStack> input : fluidInputs)
                {
                    recipeLayout.getFluidStacks().init(fluidIndex, true, format.inputX(inputIndex)+1, format.inputY(inputIndex)+1);
                    recipeLayout.getFluidStacks().set(fluidIndex, input);
                    fluidIndex++;
                    inputIndex++;
                }
            }
            
            List<List<BulkResourceWithQuantity>> bulkInputs = ingredients.getInputs(BulkResourceWithQuantity.class);
            if(!bulkInputs.isEmpty())
            {
                for(List<BulkResourceWithQuantity> input : bulkInputs)
                {
                    recipeLayout.getIngredientsGroup(BulkResourceWithQuantity.class).init(bulkIndex, true, format.inputX(inputIndex)+1, format.inputY(inputIndex)+1);
                    recipeLayout.getIngredientsGroup(BulkResourceWithQuantity.class).set(bulkIndex, input);
                    bulkIndex++;
                    inputIndex++;
                }
            }
        }
        
        {
            int outputIndex = 0;
            
            List<List<ItemStack>> itemOutputs = ingredients.getOutputs(ItemStack.class);
            if(!itemOutputs.isEmpty())
            {
     
                for(List<ItemStack> output : itemOutputs)
                {
                    recipeLayout.getItemStacks().init(itemIndex, false, format.outputX(outputIndex), format.outputY(outputIndex));
                    recipeLayout.getItemStacks().set(itemIndex, output);
                    itemIndex++;
                    outputIndex++;
                }
            }
            
            List<List<FluidStack>> fluidOutputs = ingredients.getOutputs(FluidStack.class);
            if(!fluidOutputs.isEmpty())
            {
                for(List<FluidStack> output : fluidOutputs)
                {
                    recipeLayout.getFluidStacks().init(fluidIndex, false, format.outputX(outputIndex)+1, format.outputY(outputIndex)+1);
                    recipeLayout.getFluidStacks().set(fluidIndex, output);
                    fluidIndex++;
                    outputIndex++;
                }
            }
            
            List<List<BulkResourceWithQuantity>> bulkOutputs = ingredients.getOutputs(BulkResourceWithQuantity.class);
            if(!bulkOutputs.isEmpty())
            {
                for(List<BulkResourceWithQuantity> output : bulkOutputs)
                {
                    recipeLayout.getIngredientsGroup(BulkResourceWithQuantity.class).init(bulkIndex, false, format.outputX(outputIndex)+1, format.outputY(outputIndex)+1);
                    recipeLayout.getIngredientsGroup(BulkResourceWithQuantity.class).set(bulkIndex, output);
                    bulkIndex++;
                    outputIndex++;
                }
            }
        }      
    }
}