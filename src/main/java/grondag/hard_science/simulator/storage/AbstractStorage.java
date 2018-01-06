package grondag.hard_science.simulator.storage;


import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.magicwerk.brownies.collections.Key1List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.world.Location;
import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.PortType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public abstract class AbstractStorage<T extends StorageType<T>> extends AbstractSimpleMachine implements IStorage<T>
{

    protected AbstractStorage(CarrierLevel carrierLevel, PortType portType)
    {
        super(carrierLevel, portType);
    }

    /**
     * All unique resources contained in this storage
     */
    protected Key1List<AbstractResourceWithQuantity<T>, IResource<T>> slots 
        = new Key1List.Builder<AbstractResourceWithQuantity<T>, IResource<T>>().
              withPrimaryKey1Map(AbstractResourceWithQuantity::resource).
              build();
      
    protected long capacity = 2000;
    protected long used = 0;
    protected Location location;
    protected int id;
    
    
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
    public synchronized long takeUpTo(IResource<T> resource, long limit, boolean simulate, @Nullable IProcurementRequest<T> request)
    {
        if(limit < 1) return 0;
        
        AbstractResourceWithQuantity<T> rwq = this.slots.getByKey1(resource);

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
            AbstractResourceWithQuantity<T> rwq = this.slots.getByKey1(resource);
            
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
        }
        
        return added;
    }
    
    @Override
    public void serializeNBT(NBTTagCompound nbt)
    {
        super.serializeNBT(nbt);
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
        super.deserializeNBT(nbt);
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
    }

    @Override
    public long getCapacity()
    {
        return capacity;
    }

    public AbstractStorage<T> setCapacity(long capacity)
    {
        this.capacity = capacity;
        return this;
    }
    
    @Override
    public long usedCapacity()
    {
        return this.used;
    }

    @Override
    public void onConnect()
    {
        super.onConnect();
        
        // not possible without domain
        if(this.getDomain() == null) return;
    }

    @Override
    public void onDisconnect()
    {
        // won't be needed if no domain
        if(this.getDomain() == null) return;
        
        super.onDisconnect();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public long onProduceImpl(IResource<?> resource, long quantity, boolean simulate, @Nullable IProcurementRequest<?> request)
    {
        return this.takeUpTo((IResource<T>)resource, quantity, simulate, (IProcurementRequest<T>)request);
    }

    @SuppressWarnings("unchecked")
    @Override
    public long onConsumeImpl(IResource<?> resource, long quantity, boolean simulate, @Nullable IProcurementRequest<?> request)
    {
        return this.add((IResource<T>)resource, quantity, simulate, (IProcurementRequest<T>)request);
    }
}
