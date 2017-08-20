package grondag.hard_science.simulator.wip;


import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.Log;
import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.library.world.Location;
import grondag.hard_science.simulator.wip.DomainManager.Domain;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public abstract class AbstractStorage<T extends StorageType<T>> implements IStorage<T>
{
    protected final Object2LongOpenHashMap<IResource<T>> map;
    protected long capacity = 2000;
    protected long used = 0;
    protected Location location;
    protected int id;
    protected AbstractStorageManager<T> owner = null;
    protected SimpleUnorderedArrayList<IStorageListener<T>> listeners = new SimpleUnorderedArrayList<IStorageListener<T>>();
    
    public AbstractStorage(@Nullable NBTTagCompound tag)
    {
        this.map = new Object2LongOpenHashMap<IResource<T>>();
        this.map.defaultReturnValue(0);
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
        return map.getLong(resource);
    }
    
    @Override
    public List<AbstractResourceWithQuantity<T>> find(Predicate<IResource<T>> predicate)
    {
        ImmutableList.Builder<AbstractResourceWithQuantity<T>> builder = ImmutableList.builder();
        
        for(Entry<IResource<T>> entry : map.object2LongEntrySet())
        {
            if(predicate.test(entry.getKey()))
            {
                builder.add(entry.getKey().withQuantity(entry.getLongValue()));
            }
        }
        
        return builder.build();
    }
    
    @Override
    public synchronized long takeUpTo(IResource<T> resource, long limit, boolean simulate)
    {
        if(limit < 1) return 0;
        
        long current = map.getLong(resource);
        
        long taken = Math.min(limit, current);
        
        if(taken > 0 && !simulate)
        {
            current -= taken;
            this.used -= taken;
            
            if(current == 0)
            {
                map.removeLong(resource);
            }
            else
            {
                map.put(resource, current);
            }
            
            if(this.owner != null) this.owner.notifyTaken(this, resource, taken);
            this.updateListeners(resource.withQuantity(current));
        }
        
        return taken;
    }

    @Override
    public synchronized long add(IResource<T> resource, long howMany, boolean simulate)
    {
        if(howMany < 1) return 0;
        
        long added = Math.min(howMany, this.availableCapacity());
        
        if(added < 1) return 0;
        
        if(!simulate)
        {
            long quantity = this.map.getLong(resource) + added;
            this.map.put(resource, quantity);
            this.used += added;
            if(this.owner != null) this.owner.notifyAdded(this, resource, added);
            this.updateListeners(resource.withQuantity(quantity));
        }
        
        return added;
    }
    
    @Override
    public void serializeNBT(NBTTagCompound nbt)
    {
        this.serializeID(nbt);
        this.serializeLocation(nbt);
        nbt.setLong("cap", this.capacity);
        
        if(!this.map.isEmpty())
        {
            NBTTagList nbtContents = new NBTTagList();
            
            for(Entry<IResource<T>> entry : map.object2LongEntrySet())
            {
                NBTTagCompound subTag = entry.getKey().serializeNBT();
                subTag.setLong("qty", entry.getLongValue());
                nbtContents.appendTag(subTag);
            }
            nbt.setTag("contents", nbtContents);
        }
        //FIXME: remove
        Log.info("saved storage, id = " + this.id + " used =" + this.used);
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        this.deserializeID(nbt);
        this.deserializeLocation(nbt);
        this.setCapacity(nbt.getLong("cap"));
        
        this.map.clear();
        this.used = 0;
        
        T sType = this.storageType();

        NBTTagList nbtContents = nbt.getTagList("contents", 10);
        if( nbtContents != null && !nbtContents.hasNoTags())
        {
            for (int i = 0; i < nbtContents.tagCount(); ++i)
            {
                NBTTagCompound subTag = nbtContents.getCompoundTagAt(i);
                if(subTag != null)
                {
                    IResource<T> resource = sType.makeResource(subTag);
                    this.add(resource, subTag.getLong("qty"), false);
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
        if(this.owner != null) this.owner.setDirty();
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
        if(this.owner != null) this.owner.setDirty();
    }
    
    @Override
    public int getId()
    {
        return this.id;
    }

    @Override
    public SimpleUnorderedArrayList<IStorageListener<T>> listeners()
    {
        return this.listeners;
    }

 
}
