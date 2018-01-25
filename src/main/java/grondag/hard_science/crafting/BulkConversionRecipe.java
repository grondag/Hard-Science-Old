package grondag.hard_science.crafting;

import java.util.List;
import javax.annotation.Nullable;

import org.magicwerk.brownies.collections.Key2List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.resource.FluidResource;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.StorageType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * Represents a one-way conversion from a specific fluid 
 * or item resource to a general bulk (fluid) resource.
 * Used when a fabrication process can accept multiple 
 * inputs with varying types or conversion parameters
 * but no processing is required. <p>
 * 
 * Note that containerized fluids are NOT respresented
 * as item inputs for conversion. Machines must be smart
 * enough to know that they can obtain the containerized
 * source fluid as an item and then convert the fluid in it.<p>
 * 
 * An example is block fabrication, which requires some kind
 * of mineral filler, but this can sand, depleted mineral dust,
 * raw mineral dust, or possibly other materials.  In this 
 * case we define a general fluid like "flowable mineral filler"
 * and map those other resources to it.  <p>
 * 
 * No machine will craft depleted mineral dust, but machines 
 * that need depleted mineral dust will load any of the
 * mapped resources into their "flowable mineral filler" buffer.
 * Generally speaking, these resources are never exported -
 * the only place they will ever exist is in a machine input buffer.
 *
 */
public class BulkConversionRecipe<T extends StorageType<T>> implements IRecipeWrapper
{
    private static final Key2List<BulkConversionRecipe<?>, FluidResource, IResource<?>> conversions 
    = new Key2List.Builder<BulkConversionRecipe<?>, FluidResource, IResource<?>>().
          withKey1Map(BulkConversionRecipe::outputResource).
          withKey2Map(BulkConversionRecipe::inputResource).
          build();
    
    public static List<BulkConversionRecipe<?>>allRecipes()
    {
        return ImmutableList.copyOf(conversions);
    }
    
    @Nullable
    public static List<BulkConversionRecipe<?>> getConversions(FluidResource bulkResource)
    {
        return ImmutableList.copyOf(conversions.getAllByKey1(bulkResource));
    }
    
    public static <V extends StorageType<V>> void addConversion(FluidResource outputResource, IResource<V> resource, double factor)
    { 
        BulkConversionRecipe<V> recipe = new BulkConversionRecipe<V>(outputResource, resource, factor);
        conversions.add(recipe);
    }
    
    /**
     * The bulk resource that is the result of this conversion.
     */
    private final FluidResource outputResource;
    public FluidResource outputResource() { return this.outputResource; }
    
    private final IResource<T> inputResource;
    public IResource<T> inputResource() { return this.inputResource; }
    
    /**
     * For item resource inputs, the number of nL output resulting
     * from a single stack of intput.<p>
     * 
     * For fluid resource inputs, the conversion factor from input
     * nL to output nL.
     */
    public final double conversionFactor;
    
    private BulkConversionRecipe(FluidResource outputResource, IResource<T> inputResource, double conversionFactor)
    {
        this.outputResource = outputResource;
        this.inputResource = inputResource;
        this.conversionFactor = conversionFactor;
    }

    @Override
    public void getIngredients(IIngredients ingredients)
    {
        int liters = 0;

        if(this.inputResource instanceof FluidResource)
        {
            FluidResource resource = (FluidResource)this.inputResource;
            FluidStack input = resource.newStackWithLiters(Fluid.BUCKET_VOLUME);
            liters = (int) Math.ceil(this.conversionFactor * Fluid.BUCKET_VOLUME);
            ingredients.setInput(FluidStack.class, input);
        }
        else
        {
            ItemResource resource = (ItemResource)this.inputResource;
            ItemStack input = resource.sampleItemStack();
            liters = (int) Math.ceil(this.conversionFactor / VolumeUnits.LITER.nL);

            ingredients.setInput(ItemStack.class, input);
        }
        ingredients.setOutput(FluidStack.class, this.outputResource.newStackWithLiters(liters));
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
    {
        // TODO Auto-generated method stub
        IRecipeWrapper.super.drawInfo(minecraft, recipeWidth, recipeHeight, mouseX, mouseY);
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY)
    {
        // TODO Auto-generated method stub
        return IRecipeWrapper.super.getTooltipStrings(mouseX, mouseY);
    }
    
    
    
}
