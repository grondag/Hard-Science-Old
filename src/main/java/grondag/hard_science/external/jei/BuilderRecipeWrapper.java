package grondag.hard_science.external.jei;


import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.crafting.BuilderRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class BuilderRecipeWrapper implements IRecipeWrapper
{
    private final List<List<FluidStack>> fluidInputs;
    private final List<List<ItemStack>> itemInputs;
    private final List<ItemStack> output;

    /**
     * inputs are
     * resin a/b
     * flowable carbon black
     * flowable TiO2
     * reactive dyes
     * flowable mineral filler
     * mesh
     */
    
    public BuilderRecipeWrapper(BuilderRecipe recipe)
    {
//        ImmutableList.Builder<ImmutableList<ItemStack>> builder = ImmutableList.builder();
//        builder.add(ImmutableList.of(new ItemStack(ModItems.vial, 1, 0), new ItemStack(ModItems.vial, 1, 1)));
//        for(Object o : recipeBrew.getInputs()) {
//            if(o instanceof ItemStack) {
//                builder.add(ImmutableList.of((ItemStack) o));
//            }
//            if(o instanceof String) {
//                builder.add(OreDictionary.getOres((String) o));
//            }
//        }
//
//        input = builder.build();
//        output = ImmutableList.of(recipeBrew.getOutput(new ItemStack(ModItems.vial)).copy(), recipeBrew.getOutput(new ItemStack(ModItems.vial, 1, 1)).copy());
        this.fluidInputs = ImmutableList.of(ImmutableList.of());
        this.itemInputs = ImmutableList.of(ImmutableList.of());
        this.output = ImmutableList.of();
    }
    
    @Override
    public void getIngredients(IIngredients ingredients)
    {
        ingredients.setInputLists(ItemStack.class, this.itemInputs);
        ingredients.setInputLists(FluidStack.class, this.fluidInputs);
        ingredients.setOutputLists(ItemStack.class, ImmutableList.of(output));        
    }
}