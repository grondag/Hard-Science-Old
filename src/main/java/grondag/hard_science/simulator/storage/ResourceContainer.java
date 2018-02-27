package grondag.hard_science.simulator.storage;

import grondag.hard_science.simulator.fobs.NewProcurementTask;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

/**
 * Base implementation for buffers and storage classes.
 * Imposes the semantics specified in ContainerUsage
 * on a wrapped container.
 */
public class ResourceContainer<T extends StorageType<T>> extends ForwardingResourceContainer<T>
{
    protected ResourceContainer(AbstractResourceContainer<T> wrappedContainer)
    {
        super(wrappedContainer);
    }
    
    /**
     * True if running on service thread or does not need to be.
     */
    protected boolean isThreadOK()
    {
        return !this.containerUsage().isListed || !this.isConnected() || this.confirmServiceThread();
    }
    
    @Override
    public void onConnect()
    {
        super.onConnect();
        if(this.containerUsage().isListed)
        {
            assert this.getDomain() != null : "Null domain on storage connect";
            assert this.confirmServiceThread() : "Storage connect outside service thread";
            this.storageType().eventFactory().postAfterStorageConnect(this);
        }
    }
    
    @Override
    public void onDisconnect()
    {
        if(this.containerUsage().isListed)
        {
            assert this.getDomain() != null : "Null domain on storage disconnect";
            assert this.confirmServiceThread() : "Storage disconnect outside service thread";
            this.storageType().eventFactory().postBeforeStorageDisconnect(this);
        }
        super.onDisconnect();
    }
    
    
    @Override
    public long takeUpTo(IResource<T> resource, long limit, boolean simulate, boolean allowPartial, NewProcurementTask<T> request)
    {
        long taken;
        
        if(this.containerUsage().isListed)
        {
            taken = super.takeUpTo(resource, limit, simulate, allowPartial, request);
            if(this.isConnected() && this.getDomain() != null)
            {
                assert this.confirmServiceThread() : "storage operation outside service thread";
                if(!simulate) this.storageType().eventFactory().postStoredUpdate(this, resource, -taken, request);
            }
        }
        else
        {
            synchronized(this)
            {
                taken = super.takeUpTo(resource, limit, simulate, allowPartial, request);
            }
        }
        return taken;
    }
    
    @Override
    public long add(IResource<T> resource, long howMany, boolean simulate, boolean allowPartial, NewProcurementTask<T> request)
    {
        long added;
        
        if(this.containerUsage().isListed)
        {
            added = super.add(resource, howMany, simulate, allowPartial, request);
            if(this.isConnected() && this.getDomain() != null)
            {
                assert this.confirmServiceThread() : "storage operation outside service thread";
                if(!simulate) this.storageType().eventFactory().postStoredUpdate(this, resource, added, request);
            }
        }
        else
        {
            synchronized(this)
            {
                added = super.add(resource, howMany, simulate, allowPartial, request);
            }
        }
        return added;
    }
    
    @Override
    public void setCapacity(long capacity)
    {
        if(this.containerUsage().isListed)
        {
            long oldCapacity = this.getCapacity();
            super.setCapacity(capacity);
            long delta = this.getCapacity() - oldCapacity;
            if(delta != 0 && this.isConnected() && this.getDomain() != null)
            {
                assert this.confirmServiceThread() : "storage operation outside service thread";
                this.storageType().eventFactory().postCapacityChange(this, delta);
            }
        }
        else
        {
            synchronized(this)
            {
                super.setCapacity(capacity);
            }
        }
    }
}
