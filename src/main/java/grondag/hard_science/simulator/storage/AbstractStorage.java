package grondag.hard_science.simulator.storage;


import javax.annotation.Nullable;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.world.Location;
import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.PortType;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractStorage<T extends StorageType<T>> extends AbstractSimpleMachine implements IStorage<T>
{

    protected AbstractStorage(CarrierLevel carrierLevel, PortType portType)
    {
        super(carrierLevel, portType);
    }

    protected long capacity = 2000;
    protected long used = 0;
    protected Location location;
    protected int id;
    
    
 
    
    @Override
    public void serializeNBT(NBTTagCompound nbt)
    {
        super.serializeNBT(nbt);
        nbt.setLong(ModNBTTag.STORAGE_CAPACITY, this.capacity);
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        super.deserializeNBT(nbt);
        this.setCapacity(nbt.getLong(ModNBTTag.STORAGE_CAPACITY));
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
