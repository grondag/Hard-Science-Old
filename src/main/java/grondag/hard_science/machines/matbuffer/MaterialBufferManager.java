package grondag.hard_science.machines.matbuffer;

import java.util.Arrays;
import java.util.BitSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.CommonProxy;
import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.varia.Useful;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

public class MaterialBufferManager implements IReadWriteNBT, IItemHandler
{
    final VolumetricBufferSpec[] specs;
    
    private final long[] levelsNanoLiters;
    
    private BitSet failureBits = new BitSet();

    private boolean isDirty;
   
    /**
     * If true, client side delta tracking is turned on so can be displayed on machine face/GUI.
     */
    private final boolean isDeltaTrackingEnabled;
    
    /**
     * Will be null if disabled.
     */
    private final DeltaTrackingData[] deltaTrackingData;
       
    /**
     * Current position of circular specs {@link #deltaIn} and {@link #deltaOut}.
     * All changes in current sample period applied at this position.
     */
    private int sampleCounter = 0;
    
    /**
     * End of last sample period / start of current sample period.
     */
    private long avgDeltaLastSampleMillis = 0;
    
    private final int hdpeIndex;
    
    /**
     * Tracks buffer demand. Only used on server.
     */
    DemandManager demandManager = null;
    
    public MaterialBufferManager(VolumetricBufferSpec... buffers)
    {
        this.specs = buffers;
        int size = buffers.length;
        
        int hdpeIndex = -1;
        for(int i = 0; i < size; i++)
        {
            if(buffers[i].isHDPE())
            {
                hdpeIndex = i;
                break;
            }
            
        }
        this.hdpeIndex = hdpeIndex;
        
        this.levelsNanoLiters = new long[size];
        
        this.isDeltaTrackingEnabled = Configurator.MACHINES.enableDeltaTracking && FMLCommonHandler.instance().getSide() == Side.CLIENT;
        if(this.isDeltaTrackingEnabled)
        {
            this.deltaTrackingData = new DeltaTrackingData[size];
            for(int i = 0; i < size; i++)
            {
                this.deltaTrackingData[i] = new DeltaTrackingData();
            }
        }
        else
        {
            this.deltaTrackingData = null; 
        }
    }
    
    /**
     * Returns HDPE material buffer if this machine has one.
     */
    @Nullable
    public MaterialBufferDelegate bufferHDPE()
    {
        return this.hdpeIndex == -1 ? null : this.getBuffer(this.hdpeIndex);
    }
    
    /**
     * Returns number of items that can be accepted from the input stack. 
     * DOES NOT UPDATE THE STACK.
     * Updates internal buffer if simulate = false;
     */
    public int accept(int bufferIndex, @Nonnull ItemStack stack, boolean simulate)
    {
        long nLPerItem = this.specs[bufferIndex].inputs.getNanoLitersPerItem(stack);
        if(nLPerItem == 0) return 0;

        int accepted = Math.min(stack.getCount(), (int)(this.emptySpaceNanoLiters(bufferIndex) / nLPerItem));
        
        if(accepted > 0 && ! simulate) this.add(bufferIndex, accepted * nLPerItem);

        return accepted;
    }
    
    /**
     * Use this instead of updating values directly to maintain stats if they are active. Ignores zero or negative values but does not check vs range.
     */
    private void add(int bufferIndex, long deltaIn)
    {
        if(deltaIn > 0)
        {
            this.levelsNanoLiters[bufferIndex] += deltaIn;
            if(this.isDeltaTrackingEnabled)
            {
                this.deltaTrackingData[bufferIndex].deltaIn[this.sampleCounter] += deltaIn;
                this.deltaTrackingData[bufferIndex].deltaTotalIn += deltaIn;
            }
        }
    }
    
    /** indicates the buffer caused a processing failure due to shortage */
    public void blame(int bufferIndex)
    {
        this.failureBits.set(bufferIndex);;
        this.setDirty(true);
    }
    
    public int bufferCount()
    {
        return this.specs.length;
    }
    
    public boolean canRestock(int bufferIndex)
    {
        return this.getLevelNanoLiters(bufferIndex) < this.specs[bufferIndex].fillLineNanoLiters;        
    }
    
    public boolean canRestockAny()
    {
        for(int i = specs.length - 1; i >= 0; i--)
        {
            if(canRestock(i)) return true;
        }
        return false;
    }
    
    public DemandManager demandManager()
    {
        if(this.demandManager == null)
        {
            this.demandManager = new DemandManager(this);
        }
        return this.demandManager;
    }
    
    /**
     * Restores state based on array from {@link #serializeToArray()}
     */
    public void deserializeFromArray(long[] values)
    {
        if(values == null) return;
        this.failureBits.clear();
        
        int count = values.length;
        if(count == this.specs.length)
        {
            for(int i = 0; i < count; i++)
            {
                long val = values[i];
                
                if(val < 0)
                {
                    this.setLevelNanoLiters(i, -1 - val);
                    this.failureBits.set(i);
                }
                else
                {
                    this.setLevelNanoLiters(i, val);
                }
            }
        }
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        for(int i = specs.length - 1; i >= 0; i--)
        {
            this.levelsNanoLiters[i] = tag.getLong(this.specs[i].nbtTag);
        }
    }
    
    /**
     * Empty capacity
     */
    public long emptySpaceNanoLiters(int bufferIndex)
    {
        return this.maxCapacityNanoLiters(bufferIndex) - this.getLevelNanoLiters(bufferIndex);
    }
    
    /**
     * Extracts needed items from the input stack if checked
     * and increases buffer according to amount accepted. 
     * Assumes caller checked for null / empty stack before calling.
     * Returns true if items were taken.
     */
    public boolean receive(int bufferIndex, @Nonnull ItemStack stack, IItemHandler itemHandler, int slot)
    {
        if(stack.isEmpty() || stack.getItem() == Items.AIR)
            Log.warn("Material Buffer extract encountered invalid (empty) input ingredient.  This is a bug.");
        
        long nLPerItem = this.specs[bufferIndex].inputs.getNanoLitersPerItem(stack);
        
        if(nLPerItem == 0) return false;
        int requestedCount = Math.min(stack.getCount(), (int)(this.emptySpaceNanoLiters(bufferIndex) / nLPerItem));
        
        ItemStack found = itemHandler.extractItem(slot, requestedCount, false);
        if(found.isEmpty() || found.getItem() != stack.getItem()) 
                return false;
        
        int foundCount = found.getCount();
        
        if(foundCount > 0)
        {
            this.add(bufferIndex, foundCount * nLPerItem);
            return true;
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        return ItemStack.EMPTY;
    }
    
    public void forgiveAll()
    {
        if(!this.failureBits.isEmpty())
        {
            this.failureBits.clear();
            this.setDirty(true);
        }
    }
    
    public void forgive(int bufferIndex)
    {
        if(!this.failureBits.isEmpty())
        {
            this.failureBits.clear(bufferIndex);;
            this.setDirty(true);
        }
    }
    
    /**
     * Convenience.  Values 0-1.
     */
    public float fullness(int bufferIndex)
    {
        return (float) this.getLevelNanoLiters(bufferIndex) / this.maxCapacityNanoLiters(bufferIndex);
    }
    
    /**
     * Convenience.  Values 0-100.
     */
    public int fullnessPercent(int bufferIndex)
    {
        return (int) (this.getLevelNanoLiters(bufferIndex) * 100 / this.maxCapacityNanoLiters(bufferIndex));
    }
   
    public MaterialBufferDelegate getBuffer(int index)
    {
        if(index < 0 || index >= this.specs.length) 
            return null;
        return new MaterialBufferDelegate(this, index);
    }
    
    /**
     * Exponentially smoothed total of additions to this buffer within the past 3.2 seconds,
     * normalized to values between 0 and 1.  Value of 1 represents the full capacity of the buffer. <br><br>
     */
    @SideOnly(Side.CLIENT)
    public float getDeltaIn(int bufferIndex)
    {
        return this.isDeltaTrackingEnabled ? this.deltaTrackingData[bufferIndex].deltaAvgIn : 0;
    }

    /** 
     * Exponentially smoothed total of removals from this buffer within the past 3.2 seconds,
     * normalized to values between 0 and 1.  Value of 1 represents the full capacity of the buffer. <br><br>
     */
    @SideOnly(Side.CLIENT)
    public float getDeltaOut(int bufferIndex)
    {
        return this.isDeltaTrackingEnabled ? this.deltaTrackingData[bufferIndex].deltaAvgOut : 0;
    }
    
    public long getLevelNanoLiters(int bufferIndex)
    {
        return this.levelsNanoLiters[bufferIndex];
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return slot == 0 ? 64 : 0;
    }

    @Override
    public int getSlots()
    {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        return ItemStack.EMPTY;
    }

    /**
     * Must be maintained via {@link #blame()} on server side.
     * Used to indicate material shortage to machine render.
     */
    public boolean hasFailureCauseClientSideOnly()
    {
        return !this.failureBits.isEmpty();
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if(slot != 0) return stack;
        for(int i = specs.length - 1; i >= 0; i--)
        {
            if(canRestock(i))
            {
                int taken = accept(i, stack, simulate);
                if(taken > 0)
                {
                    ItemStack result = stack.copy();
                    result.shrink(taken);
                    this.setDirty(true);
                    return result;
                }
            }
        }
        return stack;
    }

    public boolean isDirty()
    {
        return isDirty;
    }

    public boolean isFailureCause(int bufferIndex)
    {
        return this.failureBits.get(bufferIndex);
    }

    public long maxCapacityNanoLiters(int bufferIndex)
    {
        return this.specs[bufferIndex].maxCapacityNanoLiters;
    }

    /**
     * Use this instead of updating values directly to maintain stats if they are active. Ignores zero or negative values but does not check vs range.
     */
    private void remove(int bufferIndex, long deltaOut)
    {
        if(deltaOut > 0)
        {
            this.levelsNanoLiters[bufferIndex] -= deltaOut;
            if(this.isDeltaTrackingEnabled && deltaOut > 0)
            {
                this.deltaTrackingData[bufferIndex].deltaOut[this.sampleCounter] += deltaOut;
                this.deltaTrackingData[bufferIndex].deltaTotalOut += deltaOut;
            }
        }
    }
    
    /**
     * Returns true if any restocking happened.
     */
    public boolean restock(IItemHandler itemHandler)
    {
        if(itemHandler == null) return false;
        
        int slotCount = itemHandler.getSlots();
        if(slotCount == 0) return false;

        IntArrayList needs = new IntArrayList(this.specs.length);
        
        for(int i = specs.length - 1; i >= 0; i--)
        {
            if(canRestock(i)) needs.add(i);
        }
        
        if(needs.size() == 0) return false;
        
        boolean didRestock = false;
        
        for(int i = 0; i < slotCount; i++)
        {
            // important to make a copy here because stack may be modified by extract
            // and then wouldn't be useful for comparisons
            ItemStack stack = itemHandler.getStackInSlot(i).copy();
            if(stack == null || stack.isEmpty()) continue;
            
            int j = 0;
            while(j < needs.size())
            {
                int bufferIndex = needs.getInt(j);
                if(receive(bufferIndex, stack, itemHandler, i)) 
                {
                    didRestock = true;
                    if(canRestock(bufferIndex)) j++; 
                    else needs.remove(j);
                }
                else j++;
            }
            
            if(needs.size() == 0) break;
        }
        
        if(didRestock) this.setDirty(true);
        
        return didRestock;
    }
    
    /**
     * Restocks a single buffer identified by index.
     * Useful for restocking fuel only.
     * Returns true if any restocking happened.
     */
    public boolean restock(int bufferIndex, IItemHandler itemHandler)
    {
        if(itemHandler == null) return false;
        
        int slotCount = itemHandler.getSlots();
        if(slotCount == 0) return false;

        if(!canRestock(bufferIndex)) return false;
        
        boolean didRestock = false;
        
        for(int i = 0; i < slotCount; i++)
        {
            // important to make a copy here because stack may be modified by extract
            // and then wouldn't be useful for comparisons
            ItemStack stack = itemHandler.getStackInSlot(i).copy();
            if(stack == null || stack.isEmpty()) continue;
           
            if(receive(bufferIndex, stack, itemHandler, i)) 
            {
                didRestock = true;
                if(!canRestock(bufferIndex)) break; 
            }
        }
        
        if(didRestock) this.setDirty(true);
        
        return didRestock;
    }
    
    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        for(int i = specs.length - 1; i >= 0; i--)
        {
            tag.setLong(this.specs[i].nbtTag, this.getLevelNanoLiters(i));
        }
    }
    
    /** 
     * Returns an array for packet serialization.
     * Sign is used for failure true/false.
     * Because we need the sign, we subtract 1 from level when indicating failure so that we don't have zero values.
     */
    public long[] serializeToArray()
    {
        int count = this.specs.length;
        long[] result = Arrays.copyOf(levelsNanoLiters, count);
        if(count > 0)
        {
            for(int i = 0; i < count; i++)
            {
                if(this.failureBits.get(i))
                {
                    result[i] =  -1 - result[i];
                }
            }
        }
        this.setDirty(false);
        return result;
    }
    
    private void setDirty(boolean isDirty)
    {
        this.isDirty = isDirty;
    }
    
    public void setLevelNanoLiters(int bufferIndex, long nanoLiters)
    {
        final long newLevel = Useful.clamp(nanoLiters, 0L, this.maxCapacityNanoLiters(bufferIndex));
        final long diff = newLevel - this.getLevelNanoLiters(bufferIndex);
        if(diff < 0)
        {
            this.remove(bufferIndex, -diff);
        }
        else if(diff > 0)
        {
            this.add(bufferIndex, diff);
        }
    }

    /**
     * Needs to be called during client-side tick event to keep status up to date.
     */
    public void updateAvgDelta()
    {
        if(this.isDeltaTrackingEnabled)
        {    
            long millis = CommonProxy.currentTimeMillis();
            long diff = millis - this.avgDeltaLastSampleMillis;
            
            if(diff > 300)
            {
                /** if we miss a sample by much more than 1/5 second, start over. */
                for(int i = specs.length - 1; i >= 0; i--)
                {
                    this.deltaTrackingData[i].deltaTotalOut = 0;
                    this.deltaTrackingData[i].deltaTotalIn = 0;
                    this.deltaTrackingData[i].deltaAvgIn = 0;
                    this.deltaTrackingData[i].deltaAvgOut = 0;
                    java.util.Arrays.fill(deltaTrackingData[i].deltaOut, 0);
                    java.util.Arrays.fill(deltaTrackingData[i].deltaIn, 0);
                }
                this.sampleCounter = 0;
                this.avgDeltaLastSampleMillis = millis;
            }
            else if(diff >= 200)
            {
                // expire oldest sample and advance counter
                this.sampleCounter = (this.sampleCounter + 1) & 0xF;
                
                for(int i = specs.length - 1; i >= 0; i--)
                {
                    this.deltaTrackingData[i].deltaTotalOut -= this.deltaTrackingData[i].deltaOut[sampleCounter];
                    this.deltaTrackingData[i].deltaOut[sampleCounter] = 0;
                    
                    this.deltaTrackingData[i].deltaTotalIn -= this.deltaTrackingData[i].deltaIn[sampleCounter];
                    this.deltaTrackingData[i].deltaIn[sampleCounter] = 0;

                    // exponential smoothing happens every tick
                    final long max = this.maxCapacityNanoLiters(i);
                    final float newDeltaAvgOut = (float) this.deltaTrackingData[i].deltaTotalOut / max;
                    this.deltaTrackingData[i].deltaAvgOut = this.deltaTrackingData[i].deltaAvgOut * 0.8f + newDeltaAvgOut * 0.2f;
                    
                    final float newDeltaAvgIn = (float) this.deltaTrackingData[i].deltaTotalIn / max;
                    this.deltaTrackingData[i].deltaAvgIn = this.deltaTrackingData[i].deltaAvgIn * 0.8f + newDeltaAvgIn * 0.2f;
                }
                this.avgDeltaLastSampleMillis = millis;
            }
            
        }
    }

    /**
     * Decreases buffer by given amount, return amount actually decreased.
     * Return value may be lower than input if amount requested was not available.
     */
    public long use(int bufferIndex, long nanoLiters)
    {
        return this.use(bufferIndex, nanoLiters, true, false);
    }
    
    /**
     * Same as {@link #use(int, long)} with additional options.
     */
    public long use(int bufferIndex, long nanoLiters, boolean allowPartial, boolean simulate)
    {
        long result = Math.min(nanoLiters, this.levelsNanoLiters[bufferIndex]);
        
        if(result > 0)
        {
            if(!allowPartial && result != nanoLiters) return 0;
        
            if(!simulate) 
            {
                this.remove(bufferIndex, result);
                this.forgive(bufferIndex);
            }
        }
        return result;
    }
}