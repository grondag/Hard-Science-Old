package grondag.hard_science.machines;

import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class MaterialBufferManager
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
    
    public void restock(IItemHandler itemHandler)
    {
        if(itemHandler == null) return;
        
        int slotCount = itemHandler.getSlots();
        if(slotCount == 0) return;

        SimpleUnorderedArrayList<MaterialBuffer> needs = new SimpleUnorderedArrayList<MaterialBuffer>();
        for(MaterialBuffer buffer : buffers)
        {
            if(buffer.canRestock()) needs.add(buffer);
        }
        
        if(needs.size() == 0) return;
        
        for(int i = 0; i < slotCount; i++)
        {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if(stack == null || stack.isEmpty()) continue;
            
            int j = 0;
            while(j < needs.size())
            {
                MaterialBuffer b = needs.get(j);
                if(b.extract(stack, itemHandler, i) && !b.canRestock()) 
                {
                    needs.remove(j);
                }
                else
                {
                    j++;
                }
            }
            
            if(needs.size() == 0) break;
        }
    }    
}
