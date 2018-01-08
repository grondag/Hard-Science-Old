package grondag.hard_science.simulator.storage;


import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.Log;
import grondag.hard_science.machines.support.Battery;
import grondag.hard_science.machines.support.BatteryChemistry;
import grondag.hard_science.machines.support.MachinePower;
import grondag.hard_science.machines.support.MachinePowerSupply;
import grondag.hard_science.machines.support.PowerReceiver;
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

//TODO: need to merge this with battery somehow so that capture all the events
public class PowerStorage extends AbstractStorage<StorageTypePower>
{
    
    public PowerStorage(CarrierLevel carrierLevel, PortType portType)
    {
        super(carrierLevel, portType);
    }

    @Override
    protected @Nullable MachinePowerSupply createPowerSuppy()
    {
        return new MachinePowerSupply(
                null, 
                new Battery(775193798450L, BatteryChemistry.SILICON), 
                new PowerReceiver(MachinePower.POWER_BUS_JOULES_PER_TICK));
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
        long taken =this.getPowerSupply().battery().provideEnergy(this, limit, true, simulate);
        
        if(taken > 0 && !simulate)
        {
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
        long added = this.getPowerSupply().battery().acceptEnergy(howMany, true, simulate);
        
        if(added > 0 && !simulate)
        {
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
        throw new UnsupportedOperationException("Attempt to set power storage capacity. Power capacity is determined by battery subsystem.");
    }

    @Override
    public long getCapacity()
    {
        return this.getPowerSupply().battery().maxStoredEnergyJoules();
    }
    
    @Override
    public long getQuantityStored(IResource<StorageTypePower> resource)
    {
        return this.getPowerSupply().battery().storedEnergyJoules();
    }
    
    @Override
    public long usedCapacity()
    {
        return this.getPowerSupply().battery().storedEnergyJoules();
    }

    @Override
    public List<AbstractResourceWithQuantity<StorageTypePower>> find(Predicate<IResource<StorageTypePower>> predicate)
    {
        return this.usedCapacity() > 0 
                ? ImmutableList.of(PowerResource.JOULES.withQuantity(this.usedCapacity()))
                : ImmutableList.of();
    }
    
    @Override
    public void serializeNBT(NBTTagCompound nbt)
    {
        super.serializeNBT(nbt);
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        super.deserializeNBT(nbt);
    }
}