package grondag.hard_science.simulator.storage;

import java.util.List;
import java.util.function.Predicate;

import grondag.hard_science.machines.support.ThroughputRegulator;
import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Forwards all calls to a wrapped instance.
 * Main purpose is to enable subclassing of wrapped types.
 * Specifically, lets us reuse ContainerUsage semantics 
 * implementation for both single and multi-resource containers.
 * May find other uses down the road.
 */
public class ForwardingResourceContainer<T extends StorageType<T>> implements IResourceContainer<T>
{
    private final IResourceContainer<T> wrappedContainer;
    
    public ForwardingResourceContainer(IResourceContainer<T> wrappedContainer)
    {
        this.wrappedContainer = wrappedContainer;
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
    public ContainerUsage containerUsage()
    {
        return this.wrappedContainer.containerUsage();
    }

    @Override
    public long add(IResource<T> resource, long howMany, boolean simulate, boolean allowPartial, IProcurementRequest<T> request)
    {
        return this.wrappedContainer.add(resource, howMany, simulate, allowPartial, request);
    }

    @Override
    public long takeUpTo(IResource<T> resource, long limit, boolean simulate, boolean allowPartial, IProcurementRequest<T> request)
    {
        return this.wrappedContainer.takeUpTo(resource, limit, simulate, allowPartial, request);
    }
    
    @Override
    public IResource<T> resource()
    {
        return this.wrappedContainer.resource();
    }

    @Override
    public ThroughputRegulator getRegulator()
    {
        return this.wrappedContainer.getRegulator();
    }

    @Override
    public void setRegulator(ThroughputRegulator regulator)
    {
        this.wrappedContainer.setRegulator(regulator);
    }

    @Override
    public void setContentPredicate(Predicate<IResource<T>> predicate)
    {
        this.wrappedContainer.setContentPredicate(predicate);
    }

    @Override
    public Predicate<IResource<T>> getContentPredicate()
    {
        return this.wrappedContainer.getContentPredicate();
    }

    @Override
    public List<AbstractResourceWithQuantity<T>> slots()
    {
        return this.wrappedContainer.slots();
    }

    @Override
    public long availableCapacity()
    {
        return this.wrappedContainer.availableCapacity();
    }

    @Override
    public void setCapacity(long capacity)
    {
        this.wrappedContainer.setCapacity(capacity);
    }

    @Override
    public long availableCapacityFor(IResource<T> resource)
    {
        return this.wrappedContainer.availableCapacityFor(resource);
    }

    @Override
    public void onConnect()
    {
        this.wrappedContainer.onConnect();
    }

    @Override
    public void onDisconnect()
    {
        this.wrappedContainer.onDisconnect();
    }
    
    
}
