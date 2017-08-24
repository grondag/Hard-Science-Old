package grondag.hard_science.machines.support;

import javax.annotation.Nullable;

import grondag.hard_science.Log;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;

/**
 * Tracks a list of ingredients accepted by a machine with a unit value for each.
 */
public class WeightedIngredientList
{
    private WeightedIngredient[] accepted;
    
    public final int minUnits;
    public final int maxUnits;
    
    public WeightedIngredientList(WeightedIngredient... accepted)
    {
        this.accepted = accepted;
        
        if(accepted.length == 0)
        {
            this.maxUnits = 0;
            this.minUnits = 0;
        }
        else
        {
            int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
            for(WeightedIngredient ing : accepted)
            {
                if(ing.units < min) min = ing.units;
                if(ing.units > max) max = ing.units;
            }
            this.maxUnits = max;
            this.minUnits = min;
        }
    }
    
    /**
     * Returns 0 if stack is not an accepted ingredient, or weight originally provided otherwise.
     */
    public int getUnits(@Nullable ItemStack target)
    {
        if (target == null || target.isEmpty()) return 0;

        for (WeightedIngredient child : accepted)
        {
            if (child.ingredient.apply(target)) return child.units;
        }
        
        return 0;
    }
    
    public static class WeightedIngredient
    {
        private final Ingredient ingredient;
        private final int units;
        
        public WeightedIngredient(String ingredient, int units)
        {
            this.ingredient = CraftingHelper.getIngredient(ingredient);
            
            if(this.ingredient.apply(ItemStack.EMPTY) || this.ingredient.apply(Items.AIR.getDefaultInstance()))
                Log.warn("WeightedIngredient encountered invalid (empty) input ingredient.  This is a bug.");
            
            this.units = units;
        }
    }
}