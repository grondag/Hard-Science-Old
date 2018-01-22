package grondag.hard_science.machines.matbuffer;
//package grondag.hard_science.machines.support;
//
//import javax.annotation.Nonnull;
//
//import grondag.hard_science.CommonProxy;
//import grondag.hard_science.Configurator;
//import grondag.hard_science.Log;
//import grondag.hard_science.library.varia.Useful;
//import net.minecraft.init.Items;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.math.MathHelper;
//import net.minecraftforge.fml.common.FMLCommonHandler;
//import net.minecraftforge.fml.relauncher.Side;
//import net.minecraftforge.fml.relauncher.SideOnly;
//import net.minecraftforge.items.IItemHandler;
//
//public class MaterialBuffer
//{
//    public final VolumetricIngredientList inputs;
//    public final long maxCapacityNanoLiters;
//    
//    /**
//     * Level at which no more inputs can be accepted.
//     */
//    public final long fillLineNanoLiters;
//    
//    public final String nbtTag;
//    
//    public final String tooltipKey;
//    
//    private long currentLevelNanoLiters;
//    
//    /**
//     * Index within containing manager. Set when added to the manager.
//     */
//    private byte ordinal;
//    
//    /**
//     * If true, client side delta tracking is turned on so can be displayed on machine face/GUI.
//     */
//    private boolean isDeltaTrackingEnabled = Configurator.MACHINES.enableDeltaTracking && FMLCommonHandler.instance().getSide() == Side.CLIENT;
//    
//    /**
//     * Circular buffer with additions made in last 16 sample periods 
//     */
//    private int deltaIn[] = isDeltaTrackingEnabled ? new int[16] : null;
//    
//    /**
//     * Circular buffer with removals made in last 16 sample periods.
//     * Always positive values.
//     */
//    private int deltaOut[] = isDeltaTrackingEnabled ? new int[16] : null;
//    
//    /**
//     * Current position of circular buffers {@link #deltaIn} and {@link #deltaOut}.
//     * All changes in current sample period applied at this position.
//     */
//    private int sampleCounter = 0;
//    
//    /**
//     * Total of all samples in {@link #deltaIn}.  Maintained incrementally.
//     */
//    private int deltaTotalIn = 0;
//    
//    /**
//     * Total of all samples in {@link #deltaOut}.  Maintained incrementally.
//     */
//    private int deltaTotalOut = 0;
//    
//    /**
//     * Exponentially smoothed average {@link #deltaTotalIn}.
//     */
//    private float deltaAvgIn = 0;
//    
//    /**
//     * Exponentially smoothed average {@link #deltaTotalOut}.
//     */
//    private float deltaAvgOut = 0;
//       
//    /**
//     * End of last sample period / start of current sample period.
//     */
//    private long avgDeltaLastSampleMillis = 0;
//    
//    /**
//     * Set to true if this buffer's (low) currentLevelNanoLiters has recently caused a 
//     * processing failure.  Sent to clients for rendering to inform player
//     * but not saved to world because is transient information.
//     */
//    private boolean isFailureCause;
//    
//   
//    public MaterialBuffer(VolumetricIngredientList inputs, long maxCapacityNanoLiters, String key)
//    {
//        // would be strange, but whatever...  sometime I do strange things.
//        if(maxCapacityNanoLiters < 1) maxCapacityNanoLiters = 1;
//        
//        this.inputs = inputs;
//        this.maxCapacityNanoLiters = maxCapacityNanoLiters;
//        this.fillLineNanoLiters = this.maxCapacityNanoLiters - inputs.minNanoLitersPerItem + 1;
//        this.nbtTag = "mbl_" + key;
//        this.tooltipKey = "machine.buffer_" + key;
//    }
//    
//    public boolean canRestock()
//    {
//        return this.currentLevelNanoLiters < this.currentLevelNanoLiters;
//    }
//    
//    /**
//     * Extracts needed items from the input stack if checked
//     * and increases buffer according to amount accepted. 
//     * Assumes caller checked for null / empty stack before calling.
//     * Returns true if items were taken.
//     */
//    public boolean extract(@Nonnull ItemStack stack, IItemHandler itemHandler, int slot)
//    {
//        if(stack.isEmpty() || stack.getItem() == Items.AIR)
//            Log.warn("Material Buffer extract encountered invalid (empty) input ingredient.  This is a bug.");
//        
//        long nLPerItem = this.inputs.getNanoLitersPerItem(stack);
//        if(nLPerItem == 0) return false;
//        int requestedCount = Math.min(stack.getCount(), (int)(this.emptySpace() / nLPerItem));
//        
//        ItemStack checked = itemHandler.extractItem(slot, requestedCount, false);
//        if(checked.isEmpty() || checked.getItem() != stack.getItem()) 
//                return false;
//        
//        int foundCount = checked.getCount();
//        
//        if(foundCount > 0)
//        {
//            this.currentLevelNanoLiters += foundCount * nLPerItem;
//            return true;
//        }
//        else
//        {
//            return false;
//        }
//    }
//    
//    /**
//     * Returns number of items that can be accepted from the input stack. 
//     * DOES NOT UPDATE THE STACK.
//     * Updates internal buffer if simulate = false;
//     */
//    public int accept(@Nonnull ItemStack stack, boolean simulate)
//    {
//        long nLPerItem = this.inputs.getNanoLitersPerItem(stack);
//        if(nLPerItem == 0) return 0;
//        int accepted = Math.min(stack.getCount(), (int)(this.emptySpace() / nLPerItem));
//        
//        if(accepted > 0 && ! simulate)
//        {
//            this.currentLevelNanoLiters += accepted * nLPerItem;
//        }     
//        return accepted;
//    }
//    
//    /**
//     * Decreases buffer by given amount, return amount actually decreased.
//     * Does not permit negatives.
//     */
//    public long use(long nanoLiters)
//    {
//        long result = Math.min(nanoLiters, this.currentLevelNanoLiters);
//        this.currentLevelNanoLiters -= result;
//        
//        if(Log.DEBUG_MODE && nanoLiters != result)
//        {
//            Log.warn("Machine material buffered received request higher than currentLevelNanoLiters. %d vs %d", nanoLiters, this.currentLevelNanoLiters);
//        }
//        return result;
//    }
//    
//    @SideOnly(Side.CLIENT)
//    private void updateAvgDelta()
//    {
//        long millis = CommonProxy.currentTimeMillis();
//        long diff = millis - this.avgDeltaLastSampleMillis;
//        
//        if(diff > 300)
//        {
//            /** if we miss a sample by much more than 1/5 second, start over. */
//            this.deltaTotalOut = 0;
//            this.deltaTotalIn = 0;
//            this.deltaAvgIn = 0;
//            this.deltaAvgOut = 0;
//            java.util.Arrays.fill(deltaOut, 0);
//            java.util.Arrays.fill(deltaIn, 0);
//            this.sampleCounter = 0;
//            this.avgDeltaLastSampleMillis = millis;
//        }
//        else if(diff >= 200)
//        {
//            // expire oldest sample and advance counter
//            this.sampleCounter = (this.sampleCounter + 1) & 0xF;
//            
//            this.deltaTotalOut -= this.deltaOut[sampleCounter];
//            this.deltaOut[sampleCounter] = 0;
//            
//            this.deltaTotalIn -= this.deltaIn[sampleCounter];
//            this.deltaIn[sampleCounter] = 0;
//           
//            this.avgDeltaLastSampleMillis = millis;
//        }
//        
//        // exponential smoothing happens every tick
//        float newDeltaAvgMinus = this.deltaTotalOut == 0 ? 0 : MathHelper.clamp(MathHelper.log2(this.deltaTotalOut >> 8), 1, 8) / 8f;
//        this.deltaAvgOut = this.deltaAvgOut * 0.8f + newDeltaAvgMinus * 0.2f;
//        
//        float newDeltaAvgPlus = this.deltaTotalIn == 0 ? 0 : MathHelper.clamp(MathHelper.log2(this.deltaTotalIn >> 8), 1, 8) / 8f;
//        this.deltaAvgIn = this.deltaAvgIn * 0.8f + newDeltaAvgPlus * 0.2f;
//
//    }
//    
//    /**
//     * Exponentially smoothed total of additions to this buffer within the past 3.2 seconds,
//     * on a logarithmic scale normalized to values between 0 and 1.  Value of 1 represents a full stack
//     * and values of a single item are less have a value of ~ 1/8. <br><br>
//     * 
//     * Note - this initiates the refresh and should be called before
//     * {@link #getAvgDeltaMinus()}
//     */
//    @SideOnly(Side.CLIENT)
//    public float getAvgDeltaPlus()
//    {
//        if(this.isDeltaTrackingEnabled)
//        {
//            this.updateAvgDelta();
//            return this.deltaAvgIn;
//        }
//        else return 0;
//    }
//    
//    /** 
//     * Exponentially smoothed total of removals from this buffer within the past 3.2 seconds,
//     * on a logarithmic scale normalized to values between 0 and 1.  Value of 1 represents a full stack
//     * and values of a single item are less have a value of ~ 1/8. <br><br>
//     * 
//     * Call {@link #getAvgDeltaPlus()} before calling this.
//     */
//    @SideOnly(Side.CLIENT)
//    public float getAvgDeltaMinus()
//    {
//        return this.isDeltaTrackingEnabled ? this.deltaAvgOut : 0;
//    }
//    
//    public long getLevel()
//    {
//        return currentLevelNanoLiters;
//    }
//    
//    public void setLevel(long level)
//    {
//        long newLevel = Useful.clamp(level, 0L, this.maxCapacityNanoLiters);
//        
//        if(this.isDeltaTrackingEnabled && newLevel != this.currentLevelNanoLiters)
//        {
//            long diff = newLevel - this.currentLevelNanoLiters;
//            if(diff < 0)
//            {
//                this.deltaOut[this.sampleCounter] -= diff;
//                this.deltaTotalOut -= diff;
//            }
//            else
//            {
//                this.deltaIn[this.sampleCounter] += diff;
//                this.deltaTotalIn += diff;
//            }
//        }
//        this.currentLevelNanoLiters = Useful.clamp(level, 0L, this.maxCapacityNanoLiters);
//    }
//    
//    /**
//     * Empty capacity, in units
//     */
//    public long emptySpace()
//    {
//        return this.maxCapacityNanoLiters - this.currentLevelNanoLiters;
//    }
//    
//    /**
//     * Convenience.  Values 0-100.
//     */
//    public int fullnessPercent()
//    {
//        return (int) (this.currentLevelNanoLiters * 100 / this.maxCapacityNanoLiters);
//    }
//    
//    /**
//     * Convenience.  Values 0-1.
//     */
//    public float fullness()
//    {
//        return (float) this.currentLevelNanoLiters / this.maxCapacityNanoLiters;
//    }
//
//    public boolean isFailureCause()
//    {
//        return isFailureCause;
//    }
//
//    public void setFailureCause(boolean isFailureCause)
//    {
//        this.isFailureCause = isFailureCause;
//    }
//
//    public byte getOrdinal()
//    {
//        return ordinal;
//    }
//
//    public void setOrdinal(byte ordinal)
//    {
//        this.ordinal = ordinal;
//    }
//}
