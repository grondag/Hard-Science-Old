package grondag.hard_science.machines.support;

import javax.annotation.Nonnull;

import grondag.hard_science.CommonProxy;
import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
    
    private boolean isDeltaTrackingEnabled = Configurator.MACHINES.enableDeltaTracking && FMLCommonHandler.instance().getSide() == Side.CLIENT;
    
    private int deltaPlus[] = isDeltaTrackingEnabled ? new int[16] : null;
    
    private int deltaMinus[] = isDeltaTrackingEnabled ? new int[16] : null;
    
    private int sampleCounter = 0;
    
    private int deltaTotalPlus = 0;
    private int deltaTotalMinus = 0;
    
    private float deltaAvgPlus = 0;
    private float deltaAvgMinus = 0;
       
    /**
     * Track timing for avgDelta
     */
    private long avgDeltaLastSampleMillis = 0;
    
    /**
     * Set to true if this buffer's (low) level has recently caused a 
     * processing failure.  Sent to clients for rendering to inform player
     * but not saved to world because is transient information.
     */
    private boolean isFailureCause;
    
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
    
    /** resetValue is used as initial value if we have stale data. */
    @SideOnly(Side.CLIENT)
    private void updateAvgDelta()
    {
        long millis = CommonProxy.currentTimeMillis();
        long diff = millis - this.avgDeltaLastSampleMillis;
        
        if(diff > 300)
        {
            /** if we miss a sample by much more than 1/5 second, start over. */
            this.deltaTotalMinus = 0;
            this.deltaTotalPlus = 0;
            this.deltaAvgPlus = 0;
            this.deltaAvgMinus = 0;
            java.util.Arrays.fill(deltaMinus, 0);
            java.util.Arrays.fill(deltaPlus, 0);
            this.sampleCounter = 0;
            this.avgDeltaLastSampleMillis = millis;
            Log.info("restart");
        }
        else if(diff >= 200)
        {
            // expire oldest sample and advance counter
            this.sampleCounter = (this.sampleCounter + 1) & 0xF;
            
            this.deltaTotalMinus -= this.deltaMinus[sampleCounter];
            this.deltaMinus[sampleCounter] = 0;
            
            this.deltaTotalPlus -= this.deltaPlus[sampleCounter];
            this.deltaPlus[sampleCounter] = 0;
           
            this.avgDeltaLastSampleMillis = millis;
                
        }
        
        
        // exponential smoothing happens every tick
        float newDeltaAvgMinus = this.deltaTotalMinus == 0 ? 0 : MathHelper.clamp(MathHelper.log2(this.deltaTotalMinus >> 8), 1, 8) / 8f;
        this.deltaAvgMinus = this.deltaAvgMinus * 0.8f + newDeltaAvgMinus * 0.2f;
        
        float newDeltaAvgPlus = this.deltaTotalPlus == 0 ? 0 : MathHelper.clamp(MathHelper.log2(this.deltaTotalPlus >> 8), 1, 8) / 8f;
        this.deltaAvgPlus = this.deltaAvgPlus * 0.8f + newDeltaAvgPlus * 0.2f;

    }
    
    /**
     * Note - this initiates the refresh and should be called before
     * {@link #getAvgDeltaMinus()}
     */
    @SideOnly(Side.CLIENT)
    public float getAvgDeltaPlus()
    {
        if(this.isDeltaTrackingEnabled)
        {
            this.updateAvgDelta();
            return this.deltaAvgPlus;
        }
        else return 0;
    }
    
    /** 
     * Call {@link #getAvgDeltaPlus()} before calling this.
     */
    @SideOnly(Side.CLIENT)
    public float getAvgDeltaMinus()
    {
        return this.isDeltaTrackingEnabled ? this.deltaAvgMinus : 0;
    }
    
    public int getLevel()
    {
        return level;
    }
    
    public void setLevel(int level)
    {
        int newLevel = MathHelper.clamp(level, 0, this.maxUnits);
        
        if(this.isDeltaTrackingEnabled && newLevel != this.level)
        {
            int diff = newLevel - this.level;
            if(diff < 0)
            {
                this.deltaMinus[this.sampleCounter] -= diff;
                this.deltaTotalMinus -= diff;
            }
            else
            {
                this.deltaPlus[this.sampleCounter] += diff;
                this.deltaTotalPlus += diff;
            }
        }
        this.level = MathHelper.clamp(level, 0, this.maxUnits);
    }
    
    /**
     * Empty capacity, in units
     */
    public int emptySpace()
    {
        return this.maxUnits - this.level;
    }

    public boolean isFailureCause()
    {
        return isFailureCause;
    }

    public void setFailureCause(boolean isFailureCause)
    {
        this.isFailureCause = isFailureCause;
    }
}
