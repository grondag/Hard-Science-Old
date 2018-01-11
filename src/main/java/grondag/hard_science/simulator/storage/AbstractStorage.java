package grondag.hard_science.simulator.storage;


import java.util.List;
import java.util.function.Predicate;

import grondag.hard_science.Log;
import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import net.minecraft.nbt.NBTTagCompound;

/**
 * See comments for <tt>IStorage</tt>.
 * 
 * Implemented as a wrapper around a resource container, so 
 * can use same logic for both single and multi resource containers.
 */
public abstract class AbstractStorage<T extends StorageType<T>, V extends AbstractResourceContainer<T>> implements IStorage<T>
{
    protected final V wrappedContainer;
    
    protected AbstractStorage(IDevice owner)
    {
        this.wrappedContainer = this.createContainer(owner);
    }
    
    protected abstract V createContainer(IDevice owner);
    
    @Override
    public void onConnect()
    {
        assert this.getDomain() != null : "Null domain on storage connect";
        
        if(this.getDomain() == null)
            Log.warn("Null domain on storage connect");
        else
            this.storageType().eventFactory().postAfterStorageConnect(this);
    }
    
    @Override
    public void onDisconnect()
    {
        assert this.getDomain() != null : "Null domain on storage disconnect";
        
        if(this.getDomain() == null)
            Log.warn("Null domain on storage disconnect");
        else
            this.storageType().eventFactory().postBeforeStorageDisconnect(this);
    }
    
    @Override
    public long takeUpTo(IResource<T> resource, long limit, boolean simulate, boolean allowPartial, IProcurementRequest<T> request)
    {
        long taken = this.wrappedContainer.takeUpTo(resource, limit, simulate, allowPartial, request);
        if(this.wrappedContainer.isConnected() && this.wrappedContainer.getDomain() != null)
        {
            assert this.wrappedContainer.confirmServiceThread() : "storage operation outside service thread";
            if(!simulate) this.wrappedContainer.storageType().eventFactory().postStoredUpdate(this, resource, -taken, request);
        }
        return taken;
    }
    
    @Override
    public long add(IResource<T> resource, long howMany, boolean simulate, boolean allowPartial, IProcurementRequest<T> request)
    {
        long added = this.wrappedContainer.add(resource, howMany, simulate, allowPartial, request);
        if(this.wrappedContainer.isConnected() && this.wrappedContainer.getDomain() != null)
        {
            assert this.wrappedContainer.confirmServiceThread() : "storage operation outside service thread";
            if(!simulate) this.wrappedContainer.storageType().eventFactory().postStoredUpdate(this, resource, added, request);
        }
        return added;
    }
    
    @Override
    public void setCapacity(long capacity)
    {
        long oldCapacity = this.wrappedContainer.getCapacity();
        this.wrappedContainer.setCapacity(capacity);
        long delta = this.wrappedContainer.getCapacity() - oldCapacity;
        if(delta != 0 && this.wrappedContainer.isConnected() && this.wrappedContainer.getDomain() != null)
        {
            assert this.wrappedContainer.confirmServiceThread() : "storage operation outside service thread";
            this.wrappedContainer.storageType().eventFactory().postCapacityChange(this, delta);
        }
    }

    @Override
    public List<AbstractResourceWithQuantity<T>> find(Predicate<IResource<T>> predicate)
    {
        return this.wrappedContainer.find(predicate);
    }

    @Override
    public long getQuantityStored(IResource<T> resource)
    {
        return this.wrappedContainer.getQuantityStored(resource);
    }

    @Override
    public boolean isResourceAllowed(IResource<T> resource)
    {
        return this.wrappedContainer.isResourceAllowed(resource);
    }

    @Override
    public long getCapacity()
    {
        return this.wrappedContainer.getCapacity();
    }

    @Override
    public long usedCapacity()
    {
        return this.wrappedContainer.usedCapacity();
    }

    @Override
    public T storageType()
    {
        return this.wrappedContainer.storageType();
    }

    @Override
    public IDevice device()
    {
        return this.wrappedContainer.device();
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        this.wrappedContainer.deserializeNBT(tag);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        this.wrappedContainer.serializeNBT(tag);
    }
    
    @Override
    public ContainerUsage containerUsage()
    {
        return this.wrappedContainer.containerUsage();
    }
}
