package grondag.hard_science.simulator.storage;


import javax.annotation.Nullable;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.world.Location;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractStorage<T extends StorageType<T>>  implements IStorage<T>
{

    protected final AbstractMachine owner;
    
    protected AbstractStorage(AbstractMachine owner)
    {
        this.owner = owner;
    }

    protected long capacity = 2000;
    protected long used = 0;
    protected Location location;
    protected int id;
 
    
//    @Override
    public void serializeNBT(NBTTagCompound nbt)
    {
//        super.serializeNBT(nbt);
        nbt.setLong(ModNBTTag.STORAGE_CAPACITY, this.capacity);
    }
    
//    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
//        super.deserializeNBT(nbt);
        this.capacity = nbt.getLong(ModNBTTag.STORAGE_CAPACITY);
    }

    public AbstractMachine machine()
    {
        return this.owner;
    }
    
    /**
     * Shorthand for {@link #machine()#getDomain()}
     */
    public Domain getDomain()
    {
        return this.owner.getDomain();
    }
    
    /**
     * Shorthand for {@link #machine()#isConnected()}
     */
    public boolean isConnected()
    {
        return this.owner.isConnected();
    }
    
    /**
     * Shorthand for {@link #machine()#setDirty()}
     */
    public void setDirty()
    {
        this.owner.setDirty();
    }
    
    /**
     * Shorthand for {@link #machine()#isOn()}
     */
    public boolean isOn()
    {
        return this.owner.isOn();
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
    
    @SuppressWarnings("unchecked")
//    @Override
    public long onProduceImpl(IResource<?> resource, long quantity, boolean simulate, @Nullable IProcurementRequest<?> request)
    {
        return this.takeUpTo((IResource<T>)resource, quantity, simulate, (IProcurementRequest<T>)request);
    }

    @SuppressWarnings("unchecked")
//    @Override
    public long onConsumeImpl(IResource<?> resource, long quantity, boolean simulate, @Nullable IProcurementRequest<?> request)
    {
        return this.add((IResource<T>)resource, quantity, simulate, (IProcurementRequest<T>)request);
    }
}
