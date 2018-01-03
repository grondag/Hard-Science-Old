package grondag.hard_science.simulator.storage;


import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.magicwerk.brownies.collections.Key2List;
import org.magicwerk.brownies.collections.function.IFunction;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.library.world.Location;
import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.resource.AbstractResourceDelegate;
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

    protected final static IFunction<AbstractResourceWithQuantity<?>, Integer> RESOURCE_HANDLE_MAPPER
         = new IFunction<AbstractResourceWithQuantity<?>, Integer>() {
            @Override
            public Integer apply(AbstractResourceWithQuantity<?> elem)
            {
                return elem.resource().handle();
            }};
    
    /**
     * All unique resources contained in this storage
     */
    protected Key2List<AbstractResourceWithQuantity<T>, IResource<T>, Integer> slots 
        = new Key2List.Builder<AbstractResourceWithQuantity<T>, IResource<T>, Integer>().
              withPrimaryKey1Map(AbstractResourceWithQuantity::resource).
              withUniqueKey2Map(RESOURCE_HANDLE_MAPPER).
              build();
      
    protected long capacity = 2000;
    protected long used = 0;
    protected Location location;
    protected int id;
    /** 
     * Currently connected storage manage. Null if not connected.
     */
    protected StorageManager<T> owner = null;
    protected SimpleUnorderedArrayList<IStorageListener<T>> listeners = new SimpleUnorderedArrayList<IStorageListener<T>>();
    
    /**
     * Set to true by {@link #takeUpTo(IResource, long, boolean, IProcurementRequest)}
     * whenever an existing slot becomes empty.  Set false when 
     * {@link #cleanupEmptySlots()} runs without any active listeners.
     */
    protected boolean hasEmptySlots = false;
    
    
    @Override
    public void setDomain(Domain domain)
    {
        // if already connected, switch to new domain
       if(this.isConnected() && this.owner != null)
       {
           this.owner.removeStore(this);
       }
       super.setDomain(domain);
       if(this.isConnected())
       {
           if(domain == null)
           {
               this.owner = null;
           }
           else
           {
               this.owner = domain.getStorageManager(this.storageType());
               this.owner.addStore(this);
           }
       }
    }
    
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
    public List<AbstractResourceDelegate<T>> findDelegates(Predicate<IResource<T>> predicate)
    {
        ImmutableList.Builder<AbstractResourceDelegate<T>> builder = ImmutableList.builder();
        
        for(AbstractResourceWithQuantity<T> rwq : this.slots)
        {
            if(predicate.test(rwq.resource()))
            {
                builder.add(rwq.toDelegate(rwq.resource().handle()));
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
            rwq.changeQuantity(-taken);
            this.used -= taken;
            
            if(rwq.getQuantity() == 0)
            {
                this.hasEmptySlots = true;
                this.cleanupEmptySlots();
            }
            
            if(this.owner != null) this.owner.notifyTaken(this, rwq.resource(), taken, request);
            
            // note that we have to use the resource instance from slot
            // for notification instead of the input resource so that
            // resource handle matches what listener saw earlier.
            this.updateListeners(rwq.resource().withQuantity(rwq.getQuantity()));
            this.setDirty();
        }
        
        return taken;
    }
    
    /**
     * Removes all empty slots if there are no active listeners
     * and {@link #hasEmptySlots} is true.  We do not want to
     * remove empty slots while there are active listeners
     * because resource handles are instance-specific and 
     * we would lose resource handles that may be held by listeners.
     */
    protected synchronized void cleanupEmptySlots()
    {
        if(this.hasEmptySlots && this.listeners.isEmpty())
        {
            this.hasEmptySlots = false;
            
            Iterator<AbstractResourceWithQuantity<T>> iterator 
             = this.slots.iterator();
            while(iterator.hasNext())
            {
                if(iterator.next().getQuantity() == 0)
                {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public synchronized void removeListener(IStorageListener<T> listener)
    {
        IStorage.super.removeListener(listener);
        this.cleanupEmptySlots();
    }
    
    @Override
    public synchronized long add(IResource<T> resource, long howMany, boolean simulate, @Nullable IProcurementRequest<T> request)
    {
        if(howMany < 1 || !this.isResourceAllowed(resource)) return 0;
        
        long added = Math.min(howMany, this.availableCapacity());
        
        if(added < 1) return 0;
        
        if(!simulate)
        {
            long newQuantity = -1;
            AbstractResourceWithQuantity<T> rwq = this.slots.getByKey1(resource);
            
            if(rwq != null)
            {
                newQuantity = rwq.changeQuantity(added);
            }
            else
            {
                rwq = resource.withQuantity(added);
                this.slots.add(rwq);
                newQuantity = added;
            }
            
            this.used += added;
            if(this.owner != null) this.owner.notifyAdded(this, rwq.resource(), added, request);
            
            // note that we have to use the resource instance from slot
            // for notification instead of the input resource so that
            // resource handle matches what listener saw earlier / will see later
            this.updateListeners(rwq.resource().withQuantity(newQuantity));
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
        
        /** highly unlikely we have any at this point, but... */
        this.refreshAllListeners();
    }

    @Override
    public long getCapacity()
    {
        return capacity;
    }

    public AbstractStorage<T> setCapacity(long capacity)
    {
        if(this.owner != null) this.owner.notifyCapacityChanged(capacity - this.capacity);
        this.capacity = capacity;
        return this;
    }
    
    @Override
    public long usedCapacity()
    {
        return this.used;
    }
    
    @Override
    public SimpleUnorderedArrayList<IStorageListener<T>> listeners()
    {
        return this.listeners;
    }
    
    @Override
    public int getHandleForResource(IResource<T> resource)
    {
        AbstractResourceWithQuantity<T> rwq = this.slots.getByKey1(resource);
        return rwq == null ? -1 : rwq.resource().handle();
    }

    @Override
    public IResource<T> getResourceForHandle(int handle)
    {
        AbstractResourceWithQuantity<T> rwq = this.slots.getByKey2(handle);
        return rwq == null ? null : rwq.resource();
    }
    
    @Override
    public void onConnect()
    {
        super.onConnect();
        
        // not possible without domain
        if(this.getDomain() == null) return;
        
        assert this.owner == null
                : "Storage connect request when already connected.";
        
        this.owner = this.getDomain().getStorageManager(this.storageType());
        this.owner.addStore(this);
    }

    @Override
    public void onDisconnect()
    {
        // won't be needed if no domain
        if(this.getDomain() == null) return;
        
        assert this.owner != null
                : "Storage disconnect request when not connected";
        this.owner.removeStore(this);
        this.owner = null;
        
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
