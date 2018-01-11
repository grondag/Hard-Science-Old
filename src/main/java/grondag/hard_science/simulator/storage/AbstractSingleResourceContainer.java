package grondag.hard_science.simulator.storage;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractSingleResourceContainer<T extends StorageType<T>>
    extends AbstractResourceContainer<T>
{
    /**
     * Resource contained in this storage.  
     * Null if not configured or contains no resource.
     */
    protected IResource<T> resource;
    
    /**
     * If true, then this container can only hold
     * the resource identified by {@link #resource} and
     * {@link #resource} will be non-null even when
     * container is empty.
     */
    protected boolean isFixedResource = false;
    
    public AbstractSingleResourceContainer(IDevice owner, ContainerUsage usage)
    {
        super(owner, usage);
    }
    
    @Override
    public IResource<T> resource()
    {
        return this.resource;
    }
    
    /**
     * Setting to a non-null value will configure this
     * container to accept only the given resource.  
     * Setting to null will remove this constraint.
     * Container must be empty or already contain
     * the given resource when called.
     */
    public void setFixedResource(@Nullable IResource<T> resource)
    {
        if((this.resource == null && resource != null) || !this.resource.isResourceEqual(resource))
        {
            if(this.used == 0)
            {
                this.resource = resource;
                this.isFixedResource = resource != null;
            }
            else assert false: "Attempt to configure non-empty resource container.";
        }
    }
    
    @Override
    public boolean isResourceAllowed(@Nonnull IResource<T> resource)
    {
        // fall back to predicate if resource not set
        return (this.resource == null && super.isResourceAllowed(resource))
                || resource.isResourceEqual(this.resource);
    }

    @Override
    public List<AbstractResourceWithQuantity<T>> find(@Nonnull Predicate<IResource<T>> predicate)
    {
        if(this.resource == null || this.used == 0 || !predicate.test(this.resource)) 
            return ImmutableList.of();
        else
            return ImmutableList.of(this.resource.withQuantity(this.used));
    }

    @Override
    public long getQuantityStored(@Nonnull IResource<T> resource)
    {
        return this.resource != null && resource.isResourceEqual(resource) ? this.used : 0;
    }

    @Override
    public long add(
            @Nonnull IResource<T> resource, 
            long howMany, 
            boolean simulate, 
            boolean allowPartial, 
            @Nullable IProcurementRequest<T> request)
    {
        if(howMany < 1 || !this.isResourceAllowed(resource)) return 0;
        
        long added = this.regulator.limitInput(howMany, simulate, allowPartial);
        
        added = Math.min(added, this.availableCapacity());
        
        if(added < 1 || (!allowPartial && added != howMany)) return 0;
        
        // should be caught by isResourceAllowed, but just to be sure...
        assert this.resource == null || this.resource.isResourceEqual(resource)
                : "Mismatched addition in resource container";
        
        if(!simulate)
        {
            if(this.resource == null) this.resource = resource;
            
            this.used += added;
            this.setDirty();
        }
        return added;
    }
    
    @Override
    public long takeUpTo(
            IResource<T> resource, 
            long limit, 
            boolean simulate, 
            boolean allowPartial, 
            IProcurementRequest<T> request)
    {
        if(limit < 1) return 0;
        
        long taken = this.regulator.limitOutput(limit, simulate, allowPartial);
        
        taken = Math.min(taken, this.used);
        
        if(!allowPartial && taken != limit) return 0;
        
        if(taken > 0 && !simulate)
        {
            this.used -= taken;
            
            if(!this.isFixedResource && this.used == 0) this.resource = null;
            
            this.setDirty();
        }
            
        return taken;
    }

    @Override
    public void serializeNBT(NBTTagCompound nbt)
    {
        super.serializeNBT(nbt);
        if(this.resource != null)
        {
            nbt.setTag(ModNBTTag.STORAGE_CONTENTS, this.resource.withQuantity(this.used).toNBT());
        }
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        super.deserializeNBT(nbt);
        if(nbt.hasKey(ModNBTTag.STORAGE_CONTENTS))
        {
            AbstractResourceWithQuantity<T> rwq 
                = this.storageType().fromNBTWithQty(nbt.getCompoundTag(ModNBTTag.STORAGE_CONTENTS));
            this.resource = rwq.resource();
            this.used = rwq.getQuantity();
        }
    }
    
    @Override
    public List<AbstractResourceWithQuantity<T>> slots()
    {
        return this.resource == null || this.used == 0
                ? ImmutableList.of()
                : ImmutableList.of(this.resource.withQuantity(this.used));
    }
}
