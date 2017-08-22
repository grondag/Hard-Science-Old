package grondag.hard_science.machines;

import grondag.hard_science.Log;
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
    
    private int level;
    
    /**
     * Units value of a normal stack. Some inputs may be worth more or less.
     * Buffer creation is specified in stacks, not units.
     */
    static final int UNITS_PER_ITEM = 1024;
    
    public MaterialBuffer(WeightedIngredientList inputs, int maxStacks, String nbtTag)
    {
        if(maxStacks < 1) maxStacks = 1;
        this.inputs = inputs;
        this.maxStacks = maxStacks;
        this.maxUnits = maxStacks * UNITS_PER_ITEM;
        this.mostlyFullUnits = this.maxUnits - inputs.minUnits + 1;
        this.nbtTag = nbtTag;
    }
    
    public boolean canRestock()
    {
        return this.level < this.mostlyFullUnits;
    }
    
    /**
     * Extracts needed items from the input stack if found
     * and increases buffer according to amount accepted. 
     * Returns true if items were taken.
     */
    public boolean extract(ItemStack stack, IItemHandler itemHandler, int slot)
    {
        int unitsPerItem = this.inputs.getUnits(stack);
        if(unitsPerItem == 0) return false;
        int requestedCount = Math.min(stack.getCount(), this.emptySpace() / unitsPerItem);
        
        int foundCount = itemHandler.extractItem(slot, requestedCount, false).getCount();

        if(foundCount > 0)
        {
            this.level += foundCount * unitsPerItem;
            
            //FIXME: remove
            Log.info("Restocked %d %s for %d units", foundCount, stack.getDisplayName(), foundCount * unitsPerItem);
            return true;
        }
        else
        {
            return false;
        }
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
