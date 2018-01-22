package grondag.hard_science.machines.matbuffer;

import grondag.hard_science.Log;
import grondag.hard_science.matter.CubeSize;
import grondag.hard_science.matter.Matter;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;

public class VolumetricIngredient
{
    final Ingredient ingredient;
    final long nanoLitersPerItem;
    
    public VolumetricIngredient(String ingredient, long nanoLitersPerItem)
    {
        this.ingredient = CraftingHelper.getIngredient(ingredient);
        
        if(this.ingredient == Ingredient.EMPTY)
            Log.warn("VolumetricIngredient encountered invalid (empty) input ingredient.  This is a bug.");
        
        this.nanoLitersPerItem = nanoLitersPerItem;
    }
    
    public VolumetricIngredient(Matter matter, CubeSize size)
    {
        this.ingredient = CraftingHelper.getIngredient(matter.getCube(size));
        
        if(this.ingredient == Ingredient.EMPTY)
            Log.warn("VolumetricIngredient encountered invalid (empty) input ingredient.  This is a bug.");
        
        this.nanoLitersPerItem = size.nanoLiters;
    }
}