package grondag.hard_science.machines.support;

import javax.annotation.Nonnull;

import grondag.hard_science.Log;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.IItemHandler;

public class MaterialBuffer
{
    public final WeightedIngredientList inputs;
    public final int maxStacks;
    public final int maxUnits;
    
    /**
     * Level at which no more inputs can be accepted.
     */
    public final int mostlyFullUnits;
    
    public final String nbtTag;
    
    public final String tooltipKey;
    
    private int level;
    
    /**
     * Units value of a normal stack. Some inputs may be worth more or less.
     * Buffer creation is specified in stacks, not units.
     */
    public static final int UNITS_PER_ITEM = 1024;
    
    public MaterialBuffer(WeightedIngredientList inputs, int maxStacks, String key)
    {
        if(maxStacks < 1) maxStacks = 1;
        this.inputs = inputs;
        this.maxStacks = maxStacks;
        this.maxUnits = maxStacks * UNITS_PER_ITEM;
        this.mostlyFullUnits = this.maxUnits - inputs.minUnits + 1;
        this.nbtTag = "mbl_" + key;
        this.tooltipKey = "machine.buffer_" + key;
    }
    
    public boolean canRestock()
    {
        return this.level < this.mostlyFullUnits;
    }
    
    /**
     * Extracts needed items from the input stack if found
     * and increases buffer according to amount accepted. 
     * Assumes caller checked for null / empty stack before calling.
     * Returns true if items were taken.
     */
    public boolean extract(@Nonnull ItemStack stack, IItemHandler itemHandler, int slot)
    {
        if(stack.isEmpty() || stack.getItem() == Items.AIR)
            Log.warn("Material Buffer extract encountered invalid (empty) input ingredient.  This is a bug.");
        
        int unitsPerItem = this.inputs.getUnits(stack);
        if(unitsPerItem == 0) return false;
        int requestedCount = Math.min(stack.getCount(), this.emptySpace() / unitsPerItem);
        
        ItemStack found = itemHandler.extractItem(slot, requestedCount, false);
        if(found.isEmpty() || found.getItem() != stack.getItem()) 
                return false;
        
        int foundCount = found.getCount();
        
        if(foundCount > 0)
        {
            this.level += foundCount * unitsPerItem;
            
            //FIXME: remove
            Log.info("Restocked %d %s for %d units", foundCount, found.getDisplayName(), foundCount * unitsPerItem);
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Returns number of items that can be accepted from the input stack. 
     * DOES NOT UPDATE THE STACK.
     * Updates internal buffer if simulate = false;
     */
    public int accept(@Nonnull ItemStack stack, boolean simulate)
    {
        int unitsPerItem = this.inputs.getUnits(stack);
        if(unitsPerItem == 0) return 0;
        int accepted = Math.min(stack.getCount(), this.emptySpace() / unitsPerItem);
        
        if(accepted > 0 && ! simulate)
        {
            this.level += accepted * unitsPerItem;
        }     
        return accepted;
    }
    
    /**
     * Decreases buffer by given amount, return amount actually decreased.
     * Does not permit negatives.
     */
    public int use(int units)
    {
        int result = Math.min(units, this.level);
        this.level -= result;
        
        if(Log.DEBUG_MODE && units != result)
        {
            Log.warn("Machine material buffered received request higher than level. %d vs %d", units, this.level);
        }
        return result;
    }
    
    public int getLevel()
    {
        return level;
    }
    
    public void setLevel(int level)
    {
        this.level = MathHelper.clamp(level, 0, this.maxUnits);
    }
    
    /**
     * Empty capacity, in units
     */
    public int emptySpace()
    {
        return this.maxUnits - this.level;
    }
}
