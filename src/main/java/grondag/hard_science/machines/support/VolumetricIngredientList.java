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
public class VolumetricIngredientList
{
    private VolumetricIngredient[] accepted;
    
    public final long minNanoLitersPerItem;
    public final long maxNanoLitersPerItem;
    
    public VolumetricIngredientList(VolumetricIngredient... accepted)
    {
        this.accepted = accepted;
        
        if(accepted.length == 0)
        {
            this.minNanoLitersPerItem = 0;
            this.maxNanoLitersPerItem = 0;
        }
        else
        {
            long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
            for(VolumetricIngredient ing : accepted)
            {
                if(ing.nanoLitersPerItem < min) min = ing.nanoLitersPerItem;
                if(ing.nanoLitersPerItem > max) max = ing.nanoLitersPerItem;
            }
            this.minNanoLitersPerItem = min;
            this.maxNanoLitersPerItem = max;
        }
    }
    
    /**
     * Returns zero Item if stack is not an accepted ingredient, or nano liters per item if it us.
     * Does NOT multiply by quantity in the stack.
     */
    public long getNanoLitersPerItem(@Nullable ItemStack target)
    {
        if (target == null || target.isEmpty()) return 0;

        for (VolumetricIngredient child : accepted)
        {
            if (child.ingredient.apply(target)) return child.nanoLitersPerItem;
        }
        
        return 0;
    }
    
    public static class VolumetricIngredient
    {
        private final Ingredient ingredient;
        private final long nanoLitersPerItem;
        
        public VolumetricIngredient(String ingredient, long nanoLitersPerItem)
        {
            this.ingredient = CraftingHelper.getIngredient(ingredient);
            
            if(this.ingredient.apply(ItemStack.EMPTY) || this.ingredient.apply(Items.AIR.getDefaultInstance()))
                Log.warn("VolumetricIngredient encountered invalid (empty) input ingredient.  This is a bug.");
            
            this.nanoLitersPerItem = nanoLitersPerItem;
        }
    }
}