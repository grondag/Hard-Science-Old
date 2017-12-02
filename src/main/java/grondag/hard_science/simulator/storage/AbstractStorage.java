package grondag.hard_science.simulator.storage;


import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.library.world.Location;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.persistence.AssignedNumber;
import grondag.hard_science.simulator.persistence.IDirtListener;
import grondag.hard_science.simulator.resource.AbstractResourceDelegate;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public abstract class AbstractStorage<T extends StorageType<T>> implements IStorage<T>, IDirtListener
{
    /**
     * Map of all unique resources contained in this storage
     */
//    protected final Object2LongOpenHashMap<IResource<T>> map;
    protected final SimpleUnorderedArrayList<AbstractResourceWithQuantity<T>> slots = new SimpleUnorderedArrayList<AbstractResourceWithQuantity<T>>();
    
    protected long capacity = 2000;
    protected long used = 0;
    protected Location location;
    protected int id;
    protected AbstractStorageManager<T> owner = null;
    protected SimpleUnorderedArrayList<IStorageListener<T>> listeners = new SimpleUnorderedArrayList<IStorageListener<T>>();
    
    public AbstractStorage(@Nullable NBTTagCompound tag)
    {
        if(tag != null)
        {
            this.deserializeNBT(tag);
        }
    }
    
    @Override
    public Domain getDomain()
    {
        return this.owner == null ? null : this.owner.getDomain();
    }

    @Override
    public void setOwner(AbstractStorageManager<T> owner)
    {
        this.owner = owner;
    }
    
    @Override
    public long getQuantityStored(IResource<T> resource)
    {
        for(int i = 0; i < slots.size(); i++)
        {
            AbstractResourceWithQuantity<T> rwq = this.slots.get(i);
            if(rwq.resource().equals(resource)) return rwq.getQuantity();
        }
        return 0;
    }
    
    @Override
    public List<AbstractResourceWithQuantity<T>> find(Predicate<IResource<T>> predicate)
    {
        ImmutableList.Builder<AbstractResourceWithQuantity<T>> builder = ImmutableList.builder();
        
        for(int i = 0; i < slots.size(); i++)
        {
            AbstractResourceWithQuantity<T> rwq = this.slots.get(i);
            if(predicate.test(rwq.resource()))
            {
                builder.add(rwq.clone());
            }
        }
        
        return builder.build();
    }
    
    @Override
    public List<AbstractResourceDelegate<T>> findDelegates(Predicate<IResource<T>> predicate)
    {
        ImmutableList.Builder<AbstractResourceDelegate<T>> builder = ImmutableList.builder();
        
        for(int i = 0; i < slots.size(); i++)
        {
            AbstractResourceWithQuantity<T> rwq = this.slots.get(i);
            if(predicate.test(rwq.resource()))
            {
                builder.add(rwq.toDelegate());
            }
        }
        
        return builder.build();
    }
    
    @Override
    public synchronized long takeUpTo(IResource<T> resource, long limit, boolean simulate)
    {
        if(limit < 1) return 0;
        
        AbstractResourceWithQuantity<T> rwq = null;
        int foundIndex = -1;
        
        for(int i = 0; i < slots.size(); i++)
        {
            rwq = this.slots.get(i);
            if(rwq != null && rwq.resource().equals(resource))
            {
                foundIndex = i;
                break;
            }
        }
        
        long current = rwq == null ? 0 : rwq.getQuantity();
        
        long taken = Math.min(limit, current);
        
        if(taken > 0 && rwq != null && !simulate)
        {
            rwq.changeQuantity(-taken);
            this.used -= taken;
            
            if(rwq.getQuantity() == 0)
            {
                slots.remove(foundIndex);
            }
            
            if(this.owner != null) this.owner.notifyTaken(this, resource, taken);
            this.updateListeners(resource.withQuantity(rwq.getQuantity()));
            this.setDirty();
        }
        
        return taken;
    }

    @Override
    public synchronized long add(IResource<T> resource, long howMany, boolean simulate)
    {
        if(howMany < 1 || !this.isResourceAllowed(resource)) return 0;
        
        long added = Math.min(howMany, this.availableCapacity());
        
        if(added < 1) return 0;
        
        if(!simulate)
        {
            long newQuantity = -1;
            for(int i = 0; i < slots.size(); i++)
            {
                AbstractResourceWithQuantity<T> rwq = this.slots.get(i);
                if(rwq.resource().equals(resource))
                {
                    newQuantity = rwq.changeQuantity(added);
                    break;
                }
            }
            
            if(newQuantity == -1)
            {
                this.slots.add(resource.withQuantity(added));
                newQuantity = added;
            }

            
            this.used += added;
            if(this.owner != null) this.owner.notifyAdded(this, resource, added);
            this.updateListeners(resource.withQuantity(newQuantity));
            this.setDirty();
        }
        
        return added;
    }
    
    @Override
    public void serializeNBT(NBTTagCompound nbt)
    {
        this.serializeID(nbt);
        this.serializeLocation(nbt);
        nbt.setLong(ModNBTTag.STORAGE_CAPACITY, this.capacity);
        
        if(!this.slots.isEmpty())
        {
            NBTTagList nbtContents = new NBTTagList();
            
            for(int i = 0; i < slots.size(); i++)
            {
                AbstractResourceWithQuantity<T> rwq = this.slots.get(i);
                nbtContents.appendTag(rwq.serializeNBT());
            }
            nbt.setTag(ModNBTTag.STORAGE_CONTENTS, nbtContents);
        }
        //FIXME: remove
        Log.info("saved storage, id = " + this.id + " used =" + this.used);
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        this.deserializeID(nbt);
        this.deserializeLocation(nbt);
        this.setCapacity(nbt.getLong(ModNBTTag.STORAGE_CAPACITY));
        
        this.slots.clear();
        this.used = 0;
        
        T sType = this.storageType();

        NBTTagList nbtContents = nbt.getTagList(ModNBTTag.STORAGE_CONTENTS, 10);
        if( nbtContents != null && !nbtContents.hasNoTags())
        {
            for (int i = 0; i < nbtContents.tagCount(); ++i)
            {
                NBTTagCompound subTag = nbtContents.getCompoundTagAt(i);
                if(subTag != null)
                {
                    AbstractResourceWithQuantity<T> rwq = sType.fromNBTWithQty(subTag);
                    this.add(rwq, false);
                }
            }   
        }
        
        /** highly unlikely we have any at this point, but... */
        this.refreshAllListeners();
        
        //FIXME: remove
        Log.info("loaded storage, id = " + this.id + " used =" + this.used);
    }

    @Override
    public long getCapacity()
    {
        return capacity;
    }

    public AbstractStorage<T> setCapacity(long capacity)
    {
        if(this.owner != null) this.owner.notifyCapacityChanged(capacity - this.capacity);
        this.capacity = capacity;
        return this;
    }
    
    @Override
    public long usedCapacity()
    {
        return this.used;
    }

    @Override
    public void setLocation(Location location)
    {
        this.location = location;
        // no bookkeeping change, but force world save
        this.setDirty();
    }
    
    @Override
    public Location getLocation()
    {
        return this.location;
    }
  
    @Override
    public void setId(int id)
    {
        this.id = id;
        // no bookkeeping change, but force world save
        this.setDirty();
    }
    
    @Override
    public int getIdRaw()
    {
        return this.id;
    }

    @Override
    public AssignedNumber idType()
    {
        return AssignedNumber.STORAGE;
    }
    
    @Override
    public SimpleUnorderedArrayList<IStorageListener<T>> listeners()
    {
        return this.listeners;
    }
    
    @Override
    public void setDirty()
    {
        if(this.owner != null) this.owner.setDirty();
    }

 
}
