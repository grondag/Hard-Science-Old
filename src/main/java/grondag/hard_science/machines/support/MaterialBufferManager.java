package grondag.hard_science.machines.support;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;

public class MaterialBufferManager implements IReadWriteNBT, IItemHandler
{
    private final MaterialBuffer[] buffers;
    
    /** populated on client side only, during deserialization */
    private boolean hasMaterialFailure;
    
    public MaterialBufferManager(MaterialBuffer... buffers)
    {
        this.buffers = buffers;
    }
    
    public boolean canRestock()
    {
        for(MaterialBuffer buffer : buffers)
        {
            if(buffer.canRestock()) return true;
        }
        return false;
    }
    
    /**
     * Populated during deserialization on client side.
     * Must be maintained via {@link #blame()} on server side.
     * Used to indicate material shortage to machine render.
     */
    public boolean hasFailureCauseClientSideOnly()
    {
        return this.hasMaterialFailure;
    }
    
    public void forgiveAll()
    {
        for(MaterialBuffer buffer : buffers)
        {
            buffer.setFailureCause(false);
        }
        this.hasMaterialFailure = false;
    }
    
    /** indicates a general failure, does not update individual buffers. */
    public void blame()
    {
        this.hasMaterialFailure = true;
    }
    
    /**
     * Returns true if any restocking happened.
     */
    public boolean restock(IItemHandler itemHandler)
    {
        if(itemHandler == null) return false;
        
        int slotCount = itemHandler.getSlots();
        if(slotCount == 0) return false;

        SimpleUnorderedArrayList<MaterialBuffer> needs = new SimpleUnorderedArrayList<MaterialBuffer>();
        for(MaterialBuffer buffer : buffers)
        {
            if(buffer.canRestock()) needs.add(buffer);
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
                MaterialBuffer b = needs.get(j);
                if(b.extract(stack, itemHandler, i)) 
                {
                    didRestock = true;
                    if(b.canRestock()) j++; 
                    else needs.remove(j);
                }
                else j++;
            }
            
            if(needs.size() == 0) break;
        }
        
        return didRestock;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        for(MaterialBuffer b : this.buffers)
        {
            b.setLevel(tag.getInteger(b.nbtTag));
        }
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        for(MaterialBuffer b : this.buffers)
        {
            tag.setInteger(b.nbtTag, b.getLevel());
        }    
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

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if(slot != 0) return stack;
        for(MaterialBuffer buffer : buffers)
        {
            if(buffer.canRestock())
            {
                int taken = buffer.accept(stack, simulate);
                if(taken > 0)
                {
                    ItemStack result = stack.copy();
                    result.shrink(taken);
                    return result;
                }
            }
        }
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return slot == 0 ? 64 : 0;
    }

    /**
     * Restores state based on array from {@link #serializeToArray()}
     */
    public void deserializeFromArray(int[] values)
    {
        if(values == null) return;
        boolean hasFailure = false;
        
        int count = values.length;
        if(count == this.buffers.length)
        {
            for(int i = 0; i < count; i++)
            {
                int val = values[i];
                if(val < 0)
                {
                    this.buffers[i].setLevel(-1 - val);
                    this.buffers[i].setFailureCause(true);
                    hasFailure = true;
                }
                else
                {
                    this.buffers[i].setLevel(val);
                    this.buffers[i].setFailureCause(false);
                }
            }
        }
        
        this.hasMaterialFailure = hasFailure;
    }

    /** 
     * Returns an array for packet serialization.
     * Sign is used for failure true/false.
     * Because we need the sign, we subtract 1 from level when indicating failure so that we don't have zero values.
     */
    public int[] serializeToArray()
    {
        int count = this.buffers.length;
        int[] result = new int[count];
        if(count > 0)
        {
            for(int i = 0; i < count; i++)
            {
                MaterialBuffer buff = this.buffers[i];
                result[i] = buff.isFailureCause() ? -1 - buff.getLevel() : buff.getLevel();
            }
        }
        return result;
    }
    
    public int bufferCount()
    {
        return this.buffers.length;
    }
    
    public MaterialBuffer getBuffer(int index)
    {
        if(index < 0 || index >= this.buffers.length) return null;
        return this.buffers[index];
    }
}
