package grondag.hard_science.machines.support;

import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.simulator.persistence.IReadWriteNBT;
import grondag.hard_science.simulator.wip.IMessagePlus;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandler;

public class MaterialBufferManager implements IReadWriteNBT, IItemHandler, IMessagePlus
{
    private final MaterialBuffer[] buffers;
    
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

    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        int count = pBuff.readInt();
        if(count == 0) return;
        for(int i = 0; i < count; i++)
        {
            int val = pBuff.readInt();
            if(i < this.buffers.length) this.buffers[i].setLevel(val);
        }
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        int count = this.buffers.length;
        pBuff.writeInt(count);
        if(count > 0)
        {
            for(int i = 0; i < count; i++)
            {
                pBuff.writeInt(this.buffers[i].getLevel());
            }
        }        
    }
}
