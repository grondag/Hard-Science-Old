package grondag.hard_science.simulator.storage;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.magicwerk.brownies.collections.Key1List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.Log;
import grondag.hard_science.init.ModItems;
import grondag.hard_science.library.serialization.ModNBTTag;
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.items.IItemHandler;

/**
 * FIXME: going to have problems with IItemHandler if storages are modified concurrently
 * by simulation during world tick.  Tiles from other mods will expect item stack
 * to be unchanged since time they called getStackInSlot(). Will need to detect
 * when IStorage is being accessed this way, limit changes to server thread, etc.
 */
public class ItemStorage extends AbstractStorage<StorageTypeStack> implements IItemHandler
{
    /**
     * All unique resources contained in this storage
     */
    protected Key1List<AbstractResourceWithQuantity<StorageTypeStack>, IResource<StorageTypeStack>> slots 
        = new Key1List.Builder<AbstractResourceWithQuantity<StorageTypeStack>, IResource<StorageTypeStack>>().
              withPrimaryKey1Map(AbstractResourceWithQuantity::resource).
              build();
    
    public ItemStorage(CarrierLevel carrierLevel, PortType portType)
    {
        super(carrierLevel, portType);
    }

    @Override
    public StorageTypeStack storageType()
    {
        return StorageType.ITEM;
    }
    
    @Override
    public long getQuantityStored(IResource<StorageTypeStack> resource)
    {
        AbstractResourceWithQuantity<StorageTypeStack> rwq = this.slots.getByKey1(resource);
        return rwq == null ? 0 : rwq.getQuantity();
    }
    
    @Override
    public List<AbstractResourceWithQuantity<StorageTypeStack>> find(Predicate<IResource<StorageTypeStack>> predicate)
    {
        ImmutableList.Builder<AbstractResourceWithQuantity<StorageTypeStack>> builder = ImmutableList.builder();
        
        for(AbstractResourceWithQuantity<StorageTypeStack> rwq : this.slots)
        {
            if(predicate.test(rwq.resource()))
            {
                builder.add(rwq.clone());
            }
        }
        
        return builder.build();
    }
    
    @Override
    public synchronized long takeUpTo(IResource<StorageTypeStack> resource, long limit, boolean simulate, @Nullable IProcurementRequest<StorageTypeStack> request)
    {
        if(limit < 1) return 0;
        
        AbstractResourceWithQuantity<StorageTypeStack> rwq = this.slots.getByKey1(resource);

        if(rwq == null) return 0;
        
        long current = rwq.getQuantity();
        
        long taken = Math.min(limit, current);
        
        if(taken > 0 && !simulate)
        {
            if(rwq.changeQuantity(-taken) == 0)
            {
                this.slots.removeByKey1(resource);
            }
            
            this.used -= taken;
            this.setDirty();
            
            if(this.isConnected() && this.getDomain() != null)
            {
                StorageEvent.postStoredUpdate(this, resource, -taken, request);
            }
        }
        
        return taken;
    }
    
    @Override
    public synchronized long add(IResource<StorageTypeStack> resource, long howMany, boolean simulate, @Nullable IProcurementRequest<StorageTypeStack> request)
    {
        if(howMany < 1 || !this.isResourceAllowed(resource)) return 0;
        
        long added = Math.min(howMany, this.availableCapacity());
        
        if(added < 1) return 0;
        
        if(!simulate)
        {
            AbstractResourceWithQuantity<StorageTypeStack> rwq = this.slots.getByKey1(resource);
            
            if(rwq != null)
            {
                rwq.changeQuantity(added);
            }
            else
            {
                rwq = resource.withQuantity(added);
                this.slots.add(rwq);
            }
            
            this.used += added;
            this.setDirty();
            
            if(this.isConnected() && this.getDomain() != null)
            {
                StorageEvent.postStoredUpdate(this, resource, added, request);
            }
        }
        
        return added;
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
        
        assert this.getDomain() != null : "Null domain on storage connect";
        
        if(this.getDomain() == null)
            Log.warn("Null domain on item storage connect");
        else
            StorageEvent.postAfterStorageConnect(this);
    }

    @Override
    public void onDisconnect()
    {
        assert this.getDomain() != null : "Null domain on storage disconnect";
        
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
        if(this.isConnected() && domain != null)
        {
            StorageEvent.postAfterStorageConnect(this);
        }
    }

    @Override
    public AbstractStorage<StorageTypeStack> setCapacity(long capacity)
    {
        long delta = capacity - this.capacity;
        if(delta != 0 && this.isConnected() && this.getDomain() != null)
        {
            StorageEvent.postCapacityChange(this, delta);
        }
        return super.setCapacity(capacity);
    }
    
    @Override
    public void serializeNBT(NBTTagCompound nbt)
    {
        super.serializeNBT(nbt);
        
        if(!this.slots.isEmpty())
        {
            NBTTagList nbtContents = new NBTTagList();
            
            for(AbstractResourceWithQuantity<StorageTypeStack> rwq : this.slots)
            {
                nbtContents.appendTag(rwq.toNBT());
            }
            nbt.setTag(ModNBTTag.STORAGE_CONTENTS, nbtContents);
        }
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        super.deserializeNBT(nbt);
        
        this.slots.clear();
        this.used = 0;

        NBTTagList nbtContents = nbt.getTagList(ModNBTTag.STORAGE_CONTENTS, 10);
        if( nbtContents != null && !nbtContents.hasNoTags())
        {
            for (int i = 0; i < nbtContents.tagCount(); ++i)
            {
                NBTTagCompound subTag = nbtContents.getCompoundTagAt(i);
                if(subTag != null)
                {
                    AbstractResourceWithQuantity<StorageTypeStack> rwq = StorageType.ITEM.fromNBTWithQty(subTag);
                    this.add(rwq, false, null);
                }
            }   
        }
    }
}