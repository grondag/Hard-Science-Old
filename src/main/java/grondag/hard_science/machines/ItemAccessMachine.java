package grondag.hard_science.machines;

import java.util.List;
import java.util.function.Predicate;

import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.resource.AbstractResourceDelegate;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.IStorage;
import grondag.hard_science.simulator.storage.IStorageListener;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.PortType;

/**
 * Provides IStorage interface on top of domain storage so can 
 * inquire and take storage actions at a domain level.
 */
public class ItemAccessMachine extends AbstractSimpleMachine implements IStorage<StorageTypeStack>
{
    protected ItemAccessMachine()
    {
        super(CarrierLevel.BOTTOM, PortType.CARRIER);
    }

    @Override
    public long getCapacity()
    {
        return this.getDomain().itemStorage.getCapacity();
    }

    @Override
    public long usedCapacity()
    {
        return this.getDomain().itemStorage.usedCapacity();
    }

    @Override
    public StorageTypeStack storageType()
    {
        return StorageType.ITEM;
    }

    @Override
    public List<AbstractResourceDelegate<StorageTypeStack>> findDelegates(Predicate<IResource<StorageTypeStack>> predicate)
    {
        return this.getDomain().itemStorage.findDelegates(predicate);
    }

    @Override
    public int getHandleForResource(IResource<StorageTypeStack> resource)
    {
        return this.getDomain().itemStorage.getHandleForResource(resource);
    }

    @Override
    public IResource<StorageTypeStack> getResourceForHandle(int handle)
    {
        return this.getDomain().itemStorage.getResourceForHandle(handle);
    }

    @Override
    public long getQuantityStored(IResource<StorageTypeStack> resource)
    {
        return this.getDomain().itemStorage.getQuantityStored(resource);
    }

    @Override
    public long add(IResource<StorageTypeStack> resource, long howMany, boolean simulate, IProcurementRequest<StorageTypeStack> request)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long takeUpTo(IResource<StorageTypeStack> resource, long limit, boolean simulate, IProcurementRequest<StorageTypeStack> request)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<AbstractResourceWithQuantity<StorageTypeStack>> find(Predicate<IResource<StorageTypeStack>> predicate)
    {
        return this.getDomain().itemStorage.findQuantityAvailable(predicate);
    }

    @Override
    public boolean hasOnOff()
    {
        return false;
    }

    @Override
    public boolean hasRedstoneControl()
    {
        return false;
    }

    
    /////////////////////////////////////////////////////////////////////////////////
    // All of the listener-related methods are delegated to domain item manager
    /////////////////////////////////////////////////////////////////////////////////
    
    @Override
    public SimpleUnorderedArrayList<IStorageListener<StorageTypeStack>> listeners()
    {
        return this.getDomain().itemStorage.listeners();
    }


    @Override
    public synchronized void removeListener(IStorageListener<StorageTypeStack> listener)
    {
        this.getDomain().itemStorage.removeListener(listener);
    }
    
    @Override
    public  void addListener(IStorageListener<StorageTypeStack> listener)
    {
        this.getDomain().itemStorage.addListener(listener);
    }
    
    @Override
    public void clearClosedListeners()
    {
        this.getDomain().itemStorage.clearClosedListeners();
    }
    
    @Override
    public void refreshAllListeners()
    {
        this.getDomain().itemStorage.refreshAllListeners();
    }
    
    @Override
    public void updateListeners(AbstractResourceWithQuantity<StorageTypeStack> update)
    {
        this.getDomain().itemStorage.updateListeners(update);
    }
}
