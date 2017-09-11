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
    
    /**
     * If true, client side delta tracking is turned on so can be displayed on machine face/GUI.
     */
    private boolean isDeltaTrackingEnabled = Configurator.MACHINES.enableDeltaTracking && FMLCommonHandler.instance().getSide() == Side.CLIENT;
    
    /**
     * Circular buffer with additions made in last 16 sample periods 
     */
    private int deltaIn[] = isDeltaTrackingEnabled ? new int[16] : null;
    
    /**
     * Circular buffer with removals made in last 16 sample periods.
     * Always positive values.
     */
    private int deltaOut[] = isDeltaTrackingEnabled ? new int[16] : null;
    
    /**
     * Current position of circular buffers {@link #deltaIn} and {@link #deltaOut}.
     * All changes in current sample period applied at this position.
     */
    private int sampleCounter = 0;
    
    /**
     * Total of all samples in {@link #deltaIn}.  Maintained incrementally.
     */
    private int deltaTotalIn = 0;
    
    /**
     * Total of all samples in {@link #deltaOut}.  Maintained incrementally.
     */
    private int deltaTotalOut = 0;
    
    /**
     * Exponentially smoothed average {@link #deltaTotalIn}.
     */
    private float deltaAvgIn = 0;
    
    /**
     * Exponentially smoothed average {@link #deltaTotalOut}.
     */
    private float deltaAvgOut = 0;
       
    /**
     * End of last sample period / start of current sample period.
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
    
    @SideOnly(Side.CLIENT)
    private void updateAvgDelta()
    {
        long millis = CommonProxy.currentTimeMillis();
        long diff = millis - this.avgDeltaLastSampleMillis;
        
        if(diff > 300)
        {
            /** if we miss a sample by much more than 1/5 second, start over. */
            this.deltaTotalOut = 0;
            this.deltaTotalIn = 0;
            this.deltaAvgIn = 0;
            this.deltaAvgOut = 0;
            java.util.Arrays.fill(deltaOut, 0);
            java.util.Arrays.fill(deltaIn, 0);
            this.sampleCounter = 0;
            this.avgDeltaLastSampleMillis = millis;
        }
        else if(diff >= 200)
        {
            // expire oldest sample and advance counter
            this.sampleCounter = (this.sampleCounter + 1) & 0xF;
            
            this.deltaTotalOut -= this.deltaOut[sampleCounter];
            this.deltaOut[sampleCounter] = 0;
            
            this.deltaTotalIn -= this.deltaIn[sampleCounter];
            this.deltaIn[sampleCounter] = 0;
           
            this.avgDeltaLastSampleMillis = millis;
        }
        
        // exponential smoothing happens every tick
        float newDeltaAvgMinus = this.deltaTotalOut == 0 ? 0 : MathHelper.clamp(MathHelper.log2(this.deltaTotalOut >> 8), 1, 8) / 8f;
        this.deltaAvgOut = this.deltaAvgOut * 0.8f + newDeltaAvgMinus * 0.2f;
        
        float newDeltaAvgPlus = this.deltaTotalIn == 0 ? 0 : MathHelper.clamp(MathHelper.log2(this.deltaTotalIn >> 8), 1, 8) / 8f;
        this.deltaAvgIn = this.deltaAvgIn * 0.8f + newDeltaAvgPlus * 0.2f;

    }
    
    /**
     * Exponentially smoothed total of additions to this buffer within the past 3.2 seconds,
     * on a logarithmic scale normalized to values between 0 and 1.  Value of 1 represents a full stack
     * and values of a single item are less have a value of ~ 1/8. <br><br>
     * 
     * Note - this initiates the refresh and should be called before
     * {@link #getAvgDeltaMinus()}
     */
    @SideOnly(Side.CLIENT)
    public float getAvgDeltaPlus()
    {
        if(this.isDeltaTrackingEnabled)
        {
            this.updateAvgDelta();
            return this.deltaAvgIn;
        }
        else return 0;
    }
    
    /** 
     * Exponentially smoothed total of removals from this buffer within the past 3.2 seconds,
     * on a logarithmic scale normalized to values between 0 and 1.  Value of 1 represents a full stack
     * and values of a single item are less have a value of ~ 1/8. <br><br>
     * 
     * Call {@link #getAvgDeltaPlus()} before calling this.
     */
    @SideOnly(Side.CLIENT)
    public float getAvgDeltaMinus()
    {
        return this.isDeltaTrackingEnabled ? this.deltaAvgOut : 0;
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
                this.deltaOut[this.sampleCounter] -= diff;
                this.deltaTotalOut -= diff;
            }
            else
            {
                this.deltaIn[this.sampleCounter] += diff;
                this.deltaTotalIn += diff;
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
