package grondag.hard_science.simulator.storage;


import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.magicwerk.brownies.collections.Key2List;
import org.magicwerk.brownies.collections.function.IFunction;

import com.google.common.collect.ImmutableList;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.library.world.Location;
import grondag.hard_science.simulator.demand.IProcurementRequest;
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

    protected final static IFunction<AbstractResourceWithQuantity<?>, Integer> handleMapper
         = new IFunction<AbstractResourceWithQuantity<?>, Integer>() {
            @Override
            public Integer apply(AbstractResourceWithQuantity<?> elem)
            {
                return elem.resource().handle();
            }};
    
    /**
     * All unique resources contained in this storage
     */
    protected Key2List<AbstractResourceWithQuantity<T>, IResource<T>, Integer> slots 
        = new Key2List.Builder<AbstractResourceWithQuantity<T>, IResource<T>, Integer>().
              withPrimaryKey1Map(AbstractResourceWithQuantity::resource).
              withUniqueKey2Map(handleMapper).
              build();
      
    protected long capacity = 2000;
    protected long used = 0;
    protected Location location;
    protected int id;
    protected StorageManager<T> owner = null;
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
    public void setOwner(StorageManager<T> owner)
    {
        this.owner = owner;
    }
    
    @Override
    public long getQuantityStored(IResource<T> resource)
    {
        AbstractResourceWithQuantity<T> rwq = this.slots.getByKey1(resource);
        return rwq == null ? 0 : rwq.getQuantity();
    }
    
    @Override
    public List<AbstractResourceWithQuantity<T>> find(Predicate<IResource<T>> predicate)
    {
        ImmutableList.Builder<AbstractResourceWithQuantity<T>> builder = ImmutableList.builder();
        
        for(AbstractResourceWithQuantity<T> rwq : this.slots)
        {
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
        
        for(AbstractResourceWithQuantity<T> rwq : this.slots)
        {
            if(predicate.test(rwq.resource()))
            {
                builder.add(rwq.toDelegate(rwq.resource().handle()));
            }
        }
        
        return builder.build();
    }
    
    @Override
    public synchronized long takeUpTo(IResource<T> resource, long limit, boolean simulate, @Nullable IProcurementRequest<T> request)
    {
        if(limit < 1) return 0;
        
        AbstractResourceWithQuantity<T> rwq = this.slots.getByKey1(resource);

        if(rwq == null) return 0;
        
        long current = rwq.getQuantity();
        
        long taken = Math.min(limit, current);
        
        if(taken > 0 && !simulate)
        {
            rwq.changeQuantity(-taken);
            this.used -= taken;
            
            if(rwq.getQuantity() == 0)
            {
                slots.removeByKey1(resource);
            }
            
            if(this.owner != null) this.owner.notifyTaken(this, resource, taken, request);
            this.updateListeners(resource.withQuantity(rwq.getQuantity()));
            this.setDirty();
        }
        
        return taken;
    }

    @Override
    public synchronized long add(IResource<T> resource, long howMany, boolean simulate, @Nullable IProcurementRequest<T> request)
    {
        if(howMany < 1 || !this.isResourceAllowed(resource)) return 0;
        
        long added = Math.min(howMany, this.availableCapacity());
        
        if(added < 1) return 0;
        
        if(!simulate)
        {
            long newQuantity = -1;
            AbstractResourceWithQuantity<T> rwq = this.slots.getByKey1(resource);
            
            if(rwq != null)
            {
                newQuantity = rwq.changeQuantity(added);
            }
            else
            {
                this.slots.add(resource.withQuantity(added));
                newQuantity = added;
            }
            
            this.used += added;
            if(this.owner != null) this.owner.notifyAdded(this, resource, added, request);
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
            
            for(AbstractResourceWithQuantity<T> rwq : this.slots)
            {
                nbtContents.appendTag(rwq.toNBT());
            }
            nbt.setTag(ModNBTTag.STORAGE_CONTENTS, nbtContents);
        }
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
                    this.add(rwq, false, null);
                }
            }   
        }
        
        /** highly unlikely we have any at this point, but... */
        this.refreshAllListeners();
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
    
    @Override
    public int getHandleForResource(IResource<T> resource)
    {
        AbstractResourceWithQuantity<T> rwq = this.slots.getByKey1(resource);
        return rwq == null ? -1 : rwq.resource().handle();
    }

    @Override
    public IResource<T> getResourceForHandle(int handle)
    {
        AbstractResourceWithQuantity<T> rwq = this.slots.getByKey2(handle);
        return rwq == null ? null : rwq.resource();
    }
}
