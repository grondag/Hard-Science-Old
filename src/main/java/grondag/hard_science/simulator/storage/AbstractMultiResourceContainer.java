package grondag.hard_science.simulator.storage;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.magicwerk.brownies.collections.Key1List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.machines.support.ThroughputRegulator;
import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public abstract class AbstractMultiResourceContainer<T extends StorageType<T>>
    extends AbstractResourceContainer<T>
{
    /**
     * If non-null, restricts what may be placed in this container.
     */
    protected Predicate<IResource<T>> predicate; 
    
    /**
     * Make this something other than the dummy regulator during
     * constructor if you want limits or accounting.
     */
    protected ThroughputRegulator regulator = ThroughputRegulator.DUMMY;
    
    /**
     * All unique resources contained in this storage
     */
    protected Key1List<AbstractResourceWithQuantity<T>, IResource<T>> slots 
        = new Key1List.Builder<AbstractResourceWithQuantity<T>, IResource<T>>().
              withPrimaryKey1Map(AbstractResourceWithQuantity::resource).
              build();
    
    public AbstractMultiResourceContainer(IDevice owner)
    {
        super(owner);
    }

    /**
     * Setting to a non-null value will configure this
     * container to accept only resources that match the given predicate.  
     * Setting to null will remove this constraint.
     * Container must be empty when set to a non-null value.
     */
    public void setContentPredicate(Predicate<IResource<T>> predicate)
    {
        if(this.predicate != predicate && predicate != null)
        {
            if(this.used == 0)
            {
                this.predicate = predicate;
            }
            else assert false: "Attempt to configure non-empty resource container.";
        }
    }
    
    /**
     * See {@link #setContentPredicate(Predicate)}
     */
    public Predicate<IResource<T>> getContentPredicate()
    {
        return this.predicate;
    }
    
    @Override
    public boolean isResourceAllowed(@Nonnull IResource<T> resource)
    {
        return this.predicate == null || predicate.test(resource);
    }
    
    @Override
    public List<AbstractResourceWithQuantity<T>> find(@Nonnull Predicate<IResource<T>> predicate)
    {
        if(this.slots.isEmpty()) return ImmutableList.of();
        
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
    public long getQuantityStored(@Nonnull IResource<T> resource)
    {
        AbstractResourceWithQuantity<T> rwq = this.slots.getByKey1(resource);
        return rwq == null ? 0 : rwq.getQuantity();
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
        
        added = Math.min(howMany, this.availableCapacity());
        
        if(added < 1 || (!allowPartial && added != howMany)) return 0;
        
        if(!simulate)
        {
            AbstractResourceWithQuantity<T> rwq = this.slots.getByKey1(resource);
            
            if(rwq != null)
            {
                rwq.changeQuantity(added);
            }
            else
            {
                rwq = resource.withQuantity(added);
                this.slots.add(rwq);
            }
            
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
        
        AbstractResourceWithQuantity<T> rwq = this.slots.getByKey1(resource);

        if(rwq == null) return 0;
        
        long current = rwq.getQuantity();
        
        long taken = this.regulator.limitOutput(limit, simulate, allowPartial);
        
        taken = Math.min(limit, current);
        
        if(!allowPartial && taken != limit) return 0;
        
        if(taken > 0 && !simulate)
        {
            if(rwq.changeQuantity(-taken) == 0)
            {
                this.slots.removeByKey1(resource);
            }
            
            this.used -= taken;
            this.setDirty();
        }
        
        return taken;   
    }

    @Override
    public void serializeNBT(NBTTagCompound nbt)
    {
        super.serializeNBT(nbt);
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
        this.slots.clear();
        this.used = 0;

        NBTTagList nbtContents = nbt.getTagList(ModNBTTag.STORAGE_CONTENTS, 10);
        if( nbtContents != null && !nbtContents.hasNoTags())
        {
            for (int i = 0; i < nbtContents.tagCount(); ++i)
            {
                NBTTagCompound subTag = nbtContents.getCompoundTagAt(i);
                if(subTag != null)
                {
                    AbstractResourceWithQuantity<T> rwq = this.storageType().fromNBTWithQty(subTag);
                    this.add(rwq, false, null);
                }
            }   
        }
    }
}
