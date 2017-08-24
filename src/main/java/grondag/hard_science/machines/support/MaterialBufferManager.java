package grondag.hard_science.machines.support;

import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.simulator.persistence.IReadWriteNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;

public class MaterialBufferManager implements IReadWriteNBT
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
}
