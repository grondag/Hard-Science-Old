package grondag.hard_science.simulator.storage;

import grondag.hard_science.Log;
import grondag.hard_science.init.ModItems;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.transport.management.LogisticsService;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/**
 * /!\ May have problems with IItemHandler if storages are modified concurrently
 * by simulation during world tick.  Tiles from other mods will expect item stack
 * to be unchanged since time they called getStackInSlot(). Will need to detect
 * when IStorage is being accessed this way, limit changes to server thread, etc.
 */
public class ItemStorage extends AbstractStorage<StorageTypeStack, AbstractMultiResourceContainer<StorageTypeStack>> implements IItemHandler
{

    public ItemStorage(AbstractMachine owner)
    {
        super(owner);
    }

    @Override
    protected AbstractMultiResourceContainer<StorageTypeStack> createContainer(IDevice owner)
    {
        AbstractMultiResourceContainer<StorageTypeStack> result = new AbstractMultiResourceContainer<StorageTypeStack>(owner)
        {
            @Override
            public StorageTypeStack storageType() { return StorageType.ITEM; }

            @Override
            public ContainerUsage containerUsage()
            {
                return ContainerUsage.STORAGE;
            }
        };
        
        // don't allow nested smart chests with contents
        result.predicate = r -> r != null 
                && !(((ItemResource)r).getItem() == ModItems.smart_chest 
                        && ((ItemResource)r).hasTagCompound());
        return result;
    }
    
     /**
     * <i>If we have available capacity, then effectively one more slot available to add another items not already here.</i><br><br>
     * 
     * {@inheritDoc}
     */
    @Override
    public int getSlots()
    {
        try
        {
            return LogisticsService.ITEM_SERVICE.executor.submit( () ->
            {
                return this.availableCapacity() == 0 ? this.wrappedContainer.slots.size() : this.wrappedContainer.slots.size() + 1;

            }, true).get();
        }
        catch (Exception e)
        {
            Log.error("Error in item handler", e);
            return 0;
        }
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        try
        {
            return LogisticsService.ITEM_SERVICE.executor.submit( () ->
            {
                if(slot < 0 || slot >= this.wrappedContainer.slots.size()) return ItemStack.EMPTY;
                AbstractResourceWithQuantity<StorageTypeStack> rwq = this.wrappedContainer.slots.get(slot);
                
                ItemStack result = ItemStack.EMPTY;
                if(rwq != null)
                {
                    result = ((ItemResource)rwq.resource()).sampleItemStack();
                    if (result != null) result.setCount((int) Math.min(Integer.MAX_VALUE, rwq.getQuantity()));
                }
                return result;
            }, true).get();
        }
        catch (Exception e)
        {
            Log.error("Error in item handler", e);
            return ItemStack.EMPTY;
        }
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
        
        try
        {
            return LogisticsService.ITEM_SERVICE.executor.submit( () ->
            {
                if(slot < this.wrappedContainer.slots.size())
                {
                    // reject if trying to put in a mismatched slot - will force it to add to end slot
                    AbstractResourceWithQuantity<StorageTypeStack> rwq = this.wrappedContainer.slots.get(slot);
                    if(!((ItemResource)rwq.resource()).isStackEqual(stack)) return stack;
                }
                
                ItemResource stackResource = ItemResource.fromStack(stack);
                
                long added = this.add(stackResource, stack.getCount(), simulate, null);
                
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
                    result.shrink((int)added);
                    return result;
                }
            }, true).get();
        }
        catch (Exception e)
        {
            Log.error("Error in item handler", e);
            return stack;
        }
    }


    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        try
        {
            return LogisticsService.ITEM_SERVICE.executor.submit( () ->
            {
                if(slot < 0 || slot >= this.wrappedContainer.slots.size()) return ItemStack.EMPTY;
                
                AbstractResourceWithQuantity<StorageTypeStack> rwq = this.wrappedContainer.slots.get(slot);
                
                if(rwq == null || rwq.getQuantity() == 0) return ItemStack.EMPTY;
                
                ItemStack stack = ((ItemResource)rwq.resource()).sampleItemStack();
                
                if(stack == null) return ItemStack.EMPTY;
                
                int limit = Math.min(amount, ((ItemResource)rwq.resource()).sampleItemStack().getMaxStackSize());

                long taken = this.takeUpTo(rwq.resource(), limit, simulate, null);
                
                if(taken == 0) return ItemStack.EMPTY;
                
                stack.setCount((int)taken);
                
                return stack;
            }, true).get();
        }
        catch (Exception e)
        {
            Log.error("Error in item handler", e);
            return ItemStack.EMPTY;
        }
    }

    @Override
    public int getSlotLimit(int slot)
    {
        try
        {
            return LogisticsService.ITEM_SERVICE.executor.submit( () ->
            {
                if(slot < 0 || slot >= this.wrappedContainer.slots.size()) return (int) Math.min(Integer.MAX_VALUE, this.availableCapacity());
                
                AbstractResourceWithQuantity<StorageTypeStack> rwq = this.wrappedContainer.slots.get(slot);
                
                if(rwq != null)
                {
                    return (int) Math.min(Integer.MAX_VALUE, rwq.getQuantity() + this.availableCapacity());
                }
                return 0;
            }, true).get();
        }
        catch (Exception e)
        {
            Log.error("Error in item handler", e);
            return 0;
        }
    }
    
}