package grondag.hard_science.simulator.storage;

import grondag.hard_science.Log;
import grondag.hard_science.init.ModItems;
import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.PortType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/**
 * FIXME: going to have problems with IItemHandler if storages are modified concurrently
 * by simulation during world tick.  Tiles from other mods will expect item stack
 * to be unchanged since time they called getStackInSlot(). Will need to detect
 * when IStorage is being accessed this way, limit changes to server thread, etc.
 */
public class ItemStorage extends AbstractStorage<StorageTypeStack> implements IItemHandler
{
    public ItemStorage(CarrierLevel carrierLevel, PortType portType)
    {
        super(carrierLevel, portType);
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
        

        if(slot < slots.size())
        {
            // reject if trying to put in a mismatched slot - will force it to add to end slot
            AbstractResourceWithQuantity<StorageTypeStack> rwq = this.slots.get(slot);
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

        long taken = this.takeUpTo(rwq.resource(), limit, simulate, null);
        
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

    @Override
    public void onConnect()
    {
        super.onConnect();
        
        //FIXME: put back
//        assert this.getDomain() != null : "Null domain on storage connect";
        
        if(this.getDomain() == null)
            Log.warn("Null domain on item storage connect");
        else
            StorageEvent.postItemStorageConnect(this);
    }

    @Override
    public void onDisconnect()
    {
        //FIXME: put back
//        assert this.getDomain() != null : "Null domain on storage disconnect";
        
        if(this.getDomain() == null)
            Log.warn("Null domain on item storage connect");
        else
            StorageEvent.postBeforeStorageDisconnect(this);
        super.onDisconnect();
    }

    @Override
    public void setDomain(Domain domain)
    {
        if(this.isConnected() && this.getDomain() != null)
        {
            StorageEvent.postBeforeStorageDisconnect(this);
        }
        super.setDomain(domain);
        if(domain != null)
        {
            StorageEvent.postItemStorageConnect(this);
        }
    }

    @Override
    public synchronized long takeUpTo(IResource<StorageTypeStack> resource, long limit, boolean simulate, IProcurementRequest<StorageTypeStack> request)
    {
        long result = super.takeUpTo(resource, limit, simulate, request);
        if(!simulate && result != 0) StorageEvent.postItemStoredUpdate(this, resource, -result);
        return result;
    }

    @Override
    public synchronized long add(IResource<StorageTypeStack> resource, long howMany, boolean simulate, IProcurementRequest<StorageTypeStack> request)
    {
        long result = super.add(resource, howMany, simulate, request);
        if(!simulate && result != 0) StorageEvent.postItemStoredUpdate(this, resource, result);
        return result;
    }
    
    
    
}