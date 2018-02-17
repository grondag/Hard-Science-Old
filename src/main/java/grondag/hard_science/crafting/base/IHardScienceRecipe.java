package grondag.hard_science.crafting.base;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.resource.FluidResource;
import grondag.hard_science.simulator.resource.FluidResourceWithQuantity;
import grondag.hard_science.simulator.resource.ItemResourceWithQuantity;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * Represents all inputs and output for a specific 
 * and static production or fabrication process.
 * Implementations are expected to be immutable.</p>
 * 
 * Some processes, especially fabrication processes,
 * are parameterized - meaning the inputs and outputs depend
 * on the specific parameters of the job at hand.</p>
 * 
 * These parameterized processes must provide a recipe
 * factory that will generate a specific instance  
 * implementing this interface for each distinct job.</p>
 * 
 * In JEI, "template" or "example" recipe instances should
 * be registered to provide discoverability and documentation
 * of the process and the range of things it can craft.
 */
public interface IHardScienceRecipe extends IRecipeWrapper
{
    public default ImmutableList<FluidResourceWithQuantity>fluidInputs()
    {
        return ImmutableList.of();
    }
    
    public default ImmutableList<ItemResourceWithQuantity>itemInputs()
    {
        return ImmutableList.of();
    }
    
    public default ImmutableList<FluidResourceWithQuantity>fluidOutputs()
    {
        return ImmutableList.of();
    }
    
    public default ImmutableList<ItemResourceWithQuantity>itemOutputs()
    {
        return ImmutableList.of();
    }
    
    public default long energyInputJoules()
    {
        return 0;
    }
    
    public default long energyOutputJoules()
    {
        return 0;
    }
    
    public default int ticksDuration()
    {
        return 0;
    }

    @Override
    public default void getIngredients(IIngredients ingredients)
    {
        if(!this.fluidInputs().isEmpty())
        {
            ImmutableList.Builder<FluidStack> builder = ImmutableList.builder();
            
            for(FluidResourceWithQuantity rwq : this.fluidInputs())
            {
                FluidResource resource = (FluidResource)rwq.resource();
                int liters = (int) Math.ceil((double)rwq.getQuantity() / VolumeUnits.LITER.nL);
                assert liters > 0 : "invalid recipe mapping";
                FluidStack input = resource.newStackWithLiters(liters);
                builder.add(input);
            }
            ingredients.setInputs(FluidStack.class, builder.build());
        }
        
        if(!this.itemInputs().isEmpty())
        {
            ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
            
            for(ItemResourceWithQuantity rwq : this.itemInputs())
            {
                builder.add(rwq.toStack());
            }
            ingredients.setInputs(ItemStack.class, builder.build());
        }
       
        if(!this.fluidOutputs().isEmpty())
        {
            ImmutableList.Builder<FluidStack> builder = ImmutableList.builder();
            
            for(FluidResourceWithQuantity rwq : this.fluidOutputs())
            {
                FluidResource resource = (FluidResource)rwq.resource();
                int liters = (int) Math.ceil((double)rwq.getQuantity() / VolumeUnits.LITER.nL);
                assert liters > 0 : "invalid recipe mapping";
                
                FluidStack input = resource.newStackWithLiters(liters);
                builder.add(input);
            }
            ingredients.setOutputs(FluidStack.class, builder.build());
        }
        
        if(!this.itemOutputs().isEmpty())
        {
            ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
            
            for(ItemResourceWithQuantity rwq : this.itemOutputs())
            {
                builder.add(rwq.toStack());
            }
            ingredients.setOutputs(ItemStack.class, builder.build());
        }
    }

}
