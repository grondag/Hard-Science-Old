package grondag.hard_science.simulator.wip;


import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.world.Location;
import grondag.hard_science.simulator.wip.DomainManager.Domain;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public abstract class AbstractStorage<T extends StorageType<T>> implements IStorage<T>
{
    protected final Object2LongOpenHashMap<IResource<T>> map;
    protected long capacity = 1000;
    protected long used = 0;
    protected Location location;
    protected int id;
    protected AbstractStorageManager<T> owner = null;
    
    public AbstractStorage()
    {
        this.map = new Object2LongOpenHashMap<IResource<T>>();
        this.map.defaultReturnValue(0);
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
    public List<ResourceWithQuantity<T>> find(Predicate<IResource<T>> predicate)
    {
        ImmutableList.Builder<ResourceWithQuantity<T>> builder = ImmutableList.builder();
        
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
            long current = this.map.getLong(resource);
            this.map.put(resource, current + added);
            this.used += added;
            if(this.owner != null) this.owner.notifyAdded(this, resource, added);
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
    }

    @Nullable
    protected abstract IResource<T> makeResource(NBTTagCompound nbt);
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        this.deserializeID(nbt);
        this.deserializeLocation(nbt);
        this.setCapacity(nbt.getLong("cap"));
        
        this.map.clear();
        this.used = 0;
        

        NBTTagList nbtContents = nbt.getTagList("contents", 10);
        if( nbtContents != null && !nbtContents.hasNoTags())
        {
            for (int i = 0; i < nbtContents.tagCount(); ++i)
            {
                NBTTagCompound subTag = nbtContents.getCompoundTagAt(i);
                if(subTag != null)
                {
                    IResource<T> resource = makeResource(subTag);
                    this.add(resource, subTag.getLong("qty"), false);
                }
            }   
        }
    }

    @Override
    public long getCapacity()
    {
        return capacity;
    }

    protected void setCapacity(long capacity)
    {
        if(this.owner != null) this.owner.natifyCapacityChanged(capacity - this.capacity);
        this.capacity = capacity;
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
}
