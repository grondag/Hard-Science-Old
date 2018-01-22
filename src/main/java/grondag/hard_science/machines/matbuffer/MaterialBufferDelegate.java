package grondag.hard_science.machines.matbuffer;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

public class MaterialBufferDelegate
{
    /**
     * 
     */
    private final MaterialBufferManager materialBufferManager;
    
    private final int index;
    
    MaterialBufferDelegate(MaterialBufferManager materialBufferManager, int index)
    {
        this.materialBufferManager = materialBufferManager;
        this.index = index;
    }
    
    public void addDemand(long demandNanoLiters)
    {
        this.materialBufferManager.demandManager.addDemand(this.index, demandNanoLiters);
    }
    
    /**
     * Returns number of items that can be accepted from the input stack. 
     * DOES NOT UPDATE THE STACK.
     * Updates internal buffer if simulate = false;
     */
    public int accept(@Nonnull ItemStack stack, boolean simulate)
    {
        return this.materialBufferManager.accept(this.index, stack, simulate);
    }
    
    /** indicates the buffer caused a processing failure due to shortage */
    public void blame()
    {
        this.materialBufferManager.blame(this.index);
    }
    
    public void forgive()
    {
        this.materialBufferManager.forgive(this.index);
    }
    
    public boolean canRestock()
    {
        return this.materialBufferManager.canRestock(this.index);      
    }
    
    /**
     * Empty capacity
     */
    public long emptySpaceNanoLiters()
    {
        return this.materialBufferManager.emptySpaceNanoLiters(this.index);  
    }
    
    /**
     * Extracts needed items from the given inventory if possible.
     * Returns true if any restocking happened.
     */
    public boolean restock(IItemHandler itemHandler)
    {
        return this.materialBufferManager.restock(this.index, itemHandler);
    }
    
    /**
     * Convenience.  Values 0-1.
     */
    public float fullness()
    {
        return this.materialBufferManager.fullness(this.index);
    }
    
    /**
     * Convenience.  Values 0-100.
     */
    public int fullnessPercent()
    {
        return this.materialBufferManager.fullnessPercent(this.index);
    }

    
    /**
     * Exponentially smoothed total of additions to this buffer within the past 3.2 seconds,
     * normalized to values between 0 and 1.  Value of 1 represents the full capacity of the buffer. <br><br>
     */
    @SideOnly(Side.CLIENT)
    public float getDeltaIn()
    {
        return this.materialBufferManager.getDeltaIn(this.index);
    }

    /** 
     * Exponentially smoothed total of removals from this buffer within the past 3.2 seconds,
     * normalized to values between 0 and 1.  Value of 1 represents the full capacity of the buffer. <br><br>
     */
    @SideOnly(Side.CLIENT)
    public float getDeltaOut()
    {
        return this.materialBufferManager.getDeltaOut(this.index);
    }
    
    public long getLevelNanoLiters()
    {
        return this.materialBufferManager.getLevelNanoLiters(this.index);
    }

    public boolean isFailureCause()
    {
        return this.materialBufferManager.isFailureCause(this.index);
    }

    public long maxCapacityNanoLiters()
    {
        return this.materialBufferManager.maxCapacityNanoLiters(this.index);
    }
    
    public void setLevelNanoLiters(long nanoLiters)
    {
        this.materialBufferManager.setLevelNanoLiters(this.index, nanoLiters);
    }

    /**
     * Decreases buffer by given amount, return amount actually decreased.
     * Return value may be lower than input if amount requested was not available.
     */
    public long use(long nanoLiters)
    {
        return this.materialBufferManager.use(this.index, nanoLiters);
    }

    /**
     * Same as {@link #use(long)} with additional options.
     */
    public long use(long nanoLiters, boolean allowPartial, boolean simulate)
    {
        return this.materialBufferManager.use(this.index, nanoLiters);
    }
    
    public String tooltipKey()
    {
        return this.materialBufferManager.specs[this.index].tooltipKey;
    }

    public boolean isEmpty()
    {
        return this.materialBufferManager.getLevelNanoLiters(this.index) == 0;
    }
}