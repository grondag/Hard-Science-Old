package grondag.hard_science.simulator.storage;

import grondag.hard_science.Log;
import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

/**
 * Base implementation for buffers and storage classes.
 * Imposes the semantics specified in ContainerUsage
 * on a wrapped container.
 */
public class ResourceContainer<T extends StorageType<T>> extends ForwardingResourceContainer<T>
{
    public ResourceContainer(IResourceContainer<T> wrappedContainer)
    {
        super(wrappedContainer);
    }
    
    @Override
    public void onConnect()
    {
        super.onConnect();
        if(this.containerUsage().isListed)
        {
            if(this.confirmServiceThread())
                this.onConnectListed();
            else this.storageType().service().executor.execute(() ->
            {
                this.onConnectListed();
            });
        }
    }
    
    private void onConnectListed()
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
        super.onDisconnect();
        if(this.containerUsage().isListed)
        {
            if(this.confirmServiceThread())
                this.onDisconnectListed();
            else this.storageType().service().executor.execute(() ->
            {
                this.onDisconnectListed();
            });
        }
    }
    
    private void onDisconnectListed()
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
        long taken;
        
        if(this.containerUsage().needsSynch)
        {
            synchronized(this)
            {
                taken = super.takeUpTo(resource, limit, simulate, allowPartial, request);
            }
        }
        else taken = super.takeUpTo(resource, limit, simulate, allowPartial, request);
        
        if(this.isConnected())
        {
            assert !this.containerUsage().isOutputThreadRestricted || this.confirmServiceThread() 
                : "storage operation outside service thread";
                
            if(!simulate && taken != 0 && this.containerUsage().isListed && this.getDomain() != null)
            {
                if(this.confirmServiceThread())
                {
                    this.storageType().eventFactory().postStoredUpdate(this, resource, -taken, request);
                }
                else this.storageType().service().executor.execute(() ->
                {
                    this.storageType().eventFactory().postStoredUpdate(this, resource, -taken, request);
                });
            }
        }
        return taken;
    }
    
    @Override
    public long add(IResource<T> resource, long howMany, boolean simulate, boolean allowPartial, IProcurementRequest<T> request)
    {
        long added;
        
        if(this.containerUsage().needsSynch)
        {
            synchronized(this)
            {
                added = super.add(resource, howMany, simulate, allowPartial, request);
            }
        }
        else added = super.add(resource, howMany, simulate, allowPartial, request);
        
        if(this.isConnected())
        {
            assert !this.containerUsage().isInputThreadRestricted || this.confirmServiceThread() 
                : "storage operation outside service thread";
                
            if(!simulate && added != 0 && this.containerUsage().isListed && this.getDomain() != null)
            {
                if(this.confirmServiceThread())
                {
                    this.storageType().eventFactory().postStoredUpdate(this, resource, added, request);
                }
                else this.storageType().service().executor.execute(() ->
                {
                    this.storageType().eventFactory().postStoredUpdate(this, resource, added, request);
                });
            }
        }
        return added;
    }
    
    @Override
    public void setCapacity(long capacity)
    {
        long oldCapacity, delta;
        if(this.containerUsage().needsSynch)
        {
            synchronized(this)
            {
                oldCapacity = this.getCapacity();
                super.setCapacity(capacity);
                delta = this.getCapacity() - oldCapacity;
            }
        }
        else
        {
            assert !this.isConnected() || this.confirmServiceThread() 
                : "storage operation outside service thread";
            
            oldCapacity = this.getCapacity();
            super.setCapacity(capacity);
            delta = this.getCapacity() - oldCapacity;
        }
      
        if(delta != 0 && this.isConnected() && this.getDomain() != null)
        {
            if(this.confirmServiceThread())
            {
                this.storageType().eventFactory().postCapacityChange(this, delta);
            }
            else this.storageType().service().executor.execute(() ->
            {
                this.storageType().eventFactory().postCapacityChange(this, delta);
            });
        }
    }
}
