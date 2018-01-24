package grondag.hard_science.external.jei;


import grondag.hard_science.crafting.BulkConversionReceipe;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.resource.FluidResource;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.StorageType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class BulkConversionRecipeWrapper implements IRecipeWrapper
{
    private final Object input;
    private final FluidStack output;
    
    public BulkConversionRecipeWrapper(BulkConversionReceipe<?> recipe)
    {
        int liters = 0;
        
        if(recipe.inputResource().storageType() == StorageType.FLUID)
        {
            FluidResource resource = (FluidResource)recipe.inputResource();
            this.input = resource.newStackWithLiters(Fluid.BUCKET_VOLUME);
            liters = (int) Math.ceil(recipe.conversionFactor * Fluid.BUCKET_VOLUME);
        }
        else
        {
            ItemResource resource = (ItemResource)recipe.inputResource();
            this.input = resource.sampleItemStack();
            liters = (int) Math.ceil(recipe.conversionFactor / VolumeUnits.LITER.nL);
        }

        this.output = recipe.outputResource().newStackWithLiters(liters);
    }
    
    @Override
    public void getIngredients(IIngredients ingredients)
    {
        if(this.input instanceof FluidStack)
        {
            ingredients.setInput(FluidStack.class, this.input);
        }
        else
        {
            ingredients.setInput(ItemStack.class, this.input);
        }
        ingredients.setOutput(FluidStack.class, this.output);
    }
}