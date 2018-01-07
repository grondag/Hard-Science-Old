package grondag.hard_science.simulator.storage;


import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.PowerResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypePower;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.PortType;
import net.minecraft.nbt.NBTTagCompound;

public class PowerStorage extends AbstractStorage<StorageTypePower>
{
    
    public PowerStorage(CarrierLevel carrierLevel, PortType portType)
    {
        super(carrierLevel, portType);
        //FIXME
        this.capacity = 1000000;
    }

    @Override
    public StorageTypePower storageType()
    {
        return StorageType.POWER;
    }

    @Override
    public boolean isResourceAllowed(IResource<StorageTypePower> resource)
    {
        return true;
    }

    @Override
    public void onConnect()
    {
        super.onConnect();
        
        assert this.getDomain() != null : "Null domain on storage connect";
        
        if(this.getDomain() == null)
            Log.warn("Null domain on storage connect");
        else
            PowerStorageEvent.postAfterStorageConnect(this);
    }

    @Override
    public void onDisconnect()
    {
        assert this.getDomain() != null : "Null domain on storage disconnect";
        
        if(this.getDomain() == null)
            Log.warn("Null domain on storage connect");
        else
            PowerStorageEvent.postBeforeStorageDisconnect(this);
        super.onDisconnect();
    }

    @Override
    public void setDomain(Domain domain)
    {
        if(this.isConnected() && this.getDomain() != null)
        {
            PowerStorageEvent.postBeforeStorageDisconnect(this);
        }
        super.setDomain(domain);
        if(this.isConnected() && domain != null)
        {
            PowerStorageEvent.postAfterStorageConnect(this);
        }
    }

    @Override
    public synchronized long takeUpTo(IResource<StorageTypePower> resource, long limit, boolean simulate, IProcurementRequest<StorageTypePower> request)
    {
        if(limit < 1) return 0;
        
        long taken = Math.min(limit, this.used);
        
        if(taken > 0 && !simulate)
        {
            this.used -= taken;
            
            this.setDirty();
            
            if(this.isConnected() && this.getDomain() != null)
            {
                PowerStorageEvent.postStoredUpdate(this, resource, -taken, request);
            }
        }
            
        return taken;
    }

    @Override
    public synchronized long add(IResource<StorageTypePower> resource, long howMany, boolean simulate, IProcurementRequest<StorageTypePower> request)
    {
        if(howMany < 1 || !this.isResourceAllowed(resource)) return 0;
        
        long added = Math.min(howMany, this.availableCapacity());
        if(added < 1) return 0;
        
        if(!simulate)
        {
            this.used += added;
            this.setDirty();
            
            if(this.isConnected() && this.getDomain() != null)
            {
                PowerStorageEvent.postStoredUpdate(this, resource, added, request);
            }
        }
        return added;
    }

    @Override
    public AbstractStorage<StorageTypePower> setCapacity(long capacity)
    {
        long delta = capacity - this.capacity;
        if(delta != 0 && this.isConnected() && this.getDomain() != null)
        {
            PowerStorageEvent.postCapacityChange(this, delta);
        }
        return super.setCapacity(capacity);
    }

    @Override
    public long getQuantityStored(IResource<StorageTypePower> resource)
    {
        return this.used;
    }

    @Override
    public List<AbstractResourceWithQuantity<StorageTypePower>> find(Predicate<IResource<StorageTypePower>> predicate)
    {
        return this.used > 0 
                ? ImmutableList.of(PowerResource.JOULES.withQuantity(this.used))
                : ImmutableList.of();
    }
    
    @Override
    public void serializeNBT(NBTTagCompound nbt)
    {
        super.serializeNBT(nbt);
        nbt.setLong(ModNBTTag.STORAGE_CONTENTS, this.used);
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        super.deserializeNBT(nbt);
        this.used = nbt.getLong(ModNBTTag.STORAGE_CONTENTS);
    }
}