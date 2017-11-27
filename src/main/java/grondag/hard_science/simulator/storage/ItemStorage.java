package grondag.hard_science.simulator.storage;

import grondag.hard_science.init.ModItems;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
/**
 * FIXME: going to have problems with IItemHandler if storages are modified concurrently
 * by simulation during world tick.  Tiles from other mods will expect item stack
 * to be unchanged since time they called getStackInSlot(). Will need to detect
 * when IStorage is being accessed this way, limit changes to server thread, etc.
 */
public class ItemStorage extends AbstractStorage<StorageTypeStack> implements IItemHandler
{
    public ItemStorage(NBTTagCompound nbt) 
    {
        super(nbt);
    }

    @Override
    public StorageTypeStack storageType()
    {
        return StorageType.ITEM;
    }
    
     /**
     * <i>If we have available capacity, then effectively one more slot available to add another items not already here.</i><br><br>
     * 
     * {@inheritDoc}
     */
    @Override
    public int getSlots()
    {
        return this.availableCapacity() == 0 ? this.slots.size() : this.slots.size() + 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        if(slot < 0 || slot >= this.slots.size()) return ItemStack.EMPTY;
        AbstractResourceWithQuantity<StorageTypeStack> rwq = this.slots.get(slot);
        
        ItemStack result = ItemStack.EMPTY;
        if(rwq != null)
        {
            result = ((ItemResource)rwq.resource()).sampleItemStack();
            if (result != null) result.setCount((int) Math.min(Integer.MAX_VALUE, rwq.getQuantity()));
        }
        return result;
    }

    /**
     * <i>Our storage doesn't care about stacking  but need to honor slot that is sent otherwise tend to get strangeness. </i><br><br>
     * 
     * {@inheritDoc}
     */
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if(slot < 0) return stack;
        
        ItemResource stackResource = ItemResource.fromStack(stack);

        if(slot < slots.size())
        {
            // reject if trying to put in a mismatched slot - will force it to add to end slot
            AbstractResourceWithQuantity<StorageTypeStack> rwq = this.slots.get(slot);
            if(!ItemHandlerHelper.canItemStacksStack(stack, ((ItemResource)rwq.resource()).sampleItemStack())) return stack;
        }
        
        long added = this.add(stackResource.withQuantity(stack.getCount()), simulate);
        
        if(added == 0)
        {
            return stack;
        }
        else if(added == stack.getCount())
        {
            return ItemStack.EMPTY;
        }
        else
        {
            ItemStack result = stack.copy();
            stack.shrink((int)added);
            return result;
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if(slot < 0 || slot >= this.slots.size()) return ItemStack.EMPTY;
        
        AbstractResourceWithQuantity<StorageTypeStack> rwq = this.slots.get(slot);
        
        if(rwq == null || rwq.getQuantity() == 0) return ItemStack.EMPTY;
        
        ItemStack stack = ((ItemResource)rwq.resource()).sampleItemStack();
        
        if(stack == null) return ItemStack.EMPTY;
        
        int limit = Math.min(amount, ((ItemResource)rwq.resource()).sampleItemStack().getMaxStackSize());

        long taken = this.takeUpTo(rwq.resource(), limit, simulate);
        
        if(taken == 0) return ItemStack.EMPTY;
        
        stack.setCount((int)taken);
        
        return stack;
        
    }


    @Override
    public int getSlotLimit(int slot)
    {
        if(slot < 0 || slot >= this.slots.size()) return (int) Math.min(Integer.MAX_VALUE, this.availableCapacity());
        
        AbstractResourceWithQuantity<StorageTypeStack> rwq = this.slots.get(slot);
        
        if(rwq != null)
        {
            return (int) Math.min(Integer.MAX_VALUE, rwq.getQuantity() + this.availableCapacity());
        }
        return 0;
    }

    @Override
    public boolean isResourceAllowed(IResource<StorageTypeStack> resource)
    {
        ItemResource itemRes = (ItemResource)resource;
        return !(itemRes.getItem() == ModItems.smart_chest && itemRes.hasTagCompound());
    }
    
    
}