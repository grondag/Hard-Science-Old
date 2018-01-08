package grondag.hard_science.simulator.storage;


import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.support.VolumeUnits;
import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.FluidResource;
import grondag.hard_science.simulator.resource.FluidResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class FluidStorage extends AbstractStorage<StorageTypeFluid> implements IFluidHandler
{
    /**
     * Fluid contained in this storage.  Null if empty and not configured.
     */
    protected FluidResource fluidResource;
    
    protected final IFluidTankProperties myProps = new IFluidTankProperties()
    {

        @Override
        public FluidStack getContents()
        {
            FluidStack result = fluidResource.sampleFluidStack().copy();
            result.amount = (int) Math.min(
                    VolumeUnits.nL2Liters(FluidStorage.this.used),
                    Integer.MAX_VALUE);
            return result;
        }

        @Override
        public int getCapacity()
        {
            return (int) Math.min(
                    VolumeUnits.nL2Liters(FluidStorage.this.capacity),
                    Integer.MAX_VALUE);
        }

        @Override
        public boolean canFill()
        {
            return true;
        }

        @Override
        public boolean canDrain()
        {
            return true;
        }

        @Override
        public boolean canFillFluidType(FluidStack fluidStack)
        {
            return (FluidStorage.this.fluidResource == null 
                    || FluidStorage.this.fluidResource.isStackEqual(fluidStack))
                    && FluidStorage.this.availableCapacity() > 0;
        }

        @Override
        public boolean canDrainFluidType(FluidStack fluidStack)
        {
            return (FluidStorage.this.fluidResource != null 
                    && FluidStorage.this.fluidResource.isStackEqual(fluidStack))
                    && FluidStorage.this.usedCapacity() > 0;
        }
    };
    
    public FluidStorage(AbstractMachine owner)
    {
        super(owner);
        this.capacity = VolumeUnits.liters2nL(32000);
    }

    @Override
    public StorageTypeFluid storageType()
    {
        return StorageType.FLUID;
    }

    @Override
    public boolean isResourceAllowed(IResource<StorageTypeFluid> resource)
    {
        return this.fluidResource == null || this.fluidResource.isResourceEqual(resource);
    }

    @Override
    public void onConnect()
    {
        assert this.getDomain() != null : "Null domain on storage connect";
        
        if(this.getDomain() == null)
            Log.warn("Null domain on item storage connect");
        else
            FluidStorageEvent.postAfterStorageConnect(this);
    }

    @Override
    public void onDisconnect()
    {
        assert this.getDomain() != null : "Null domain on storage disconnect";
        
        if(this.getDomain() == null)
            Log.warn("Null domain on item storage connect");
        else
            FluidStorageEvent.postBeforeStorageDisconnect(this);
    }

    @Override
    public synchronized long takeUpTo(IResource<StorageTypeFluid> resource, long limit, boolean simulate, IProcurementRequest<StorageTypeFluid> request)
    {
        if(limit < 1) return 0;
        
        long taken = Math.min(limit, this.used);
        
        if(taken > 0 && !simulate)
        {
            this.used -= taken;
            
            if(this.used == 0) this.fluidResource = null;
            
            this.setDirty();
            
            if(this.isConnected() && this.getDomain() != null)
            {
                FluidStorageEvent.postStoredUpdate(this, resource, -taken, request);
            }
        }
            
        return taken;
    }

    @Override
    public synchronized long add(IResource<StorageTypeFluid> resource, long howMany, boolean simulate, IProcurementRequest<StorageTypeFluid> request)
    {
        if(howMany < 1 || !this.isResourceAllowed(resource)) return 0;
        
        long added = Math.min(howMany, this.availableCapacity());
        if(added < 1) return 0;
        
        // should be caught by isResourceAllowed, but just to be sure...
        assert this.fluidResource == null || this.fluidResource.isResourceEqual(resource)
                : "Mismatched addition in fluid storage";
        
        if(!simulate)
        {
            if(this.fluidResource == null) this.fluidResource = (FluidResource) resource;
            
            this.used += added;
            this.setDirty();
            
            if(this.isConnected() && this.getDomain() != null)
            {
                FluidStorageEvent.postStoredUpdate(this, resource, added, request);
            }
        }
        return added;
    }

    @Override
    public AbstractStorage<StorageTypeFluid> setCapacity(long capacity)
    {
        long delta = capacity - this.capacity;
        if(delta != 0 && this.isConnected() && this.getDomain() != null)
        {
            FluidStorageEvent.postCapacityChange(this, delta);
        }
        return super.setCapacity(capacity);
    }

    @Override
    public long getQuantityStored(IResource<StorageTypeFluid> resource)
    {
        return this.fluidResource != null && this.fluidResource.isResourceEqual(resource)
            ? this.used : 0;
    }

    @Override
    public List<AbstractResourceWithQuantity<StorageTypeFluid>> find(Predicate<IResource<StorageTypeFluid>> predicate)
    {
        if(this.fluidResource == null) return ImmutableList.of();
        
        return predicate.test(this.fluidResource)
                ? ImmutableList.of(this.fluidResource.withQuantity(this.used))
                : ImmutableList.of();
    }
    
    @Override
    public IFluidTankProperties[] getTankProperties()
    {
        return new IFluidTankProperties[] {this.myProps};
    }

    //TODO: move this to service thread
    @Override
    public int fill(FluidStack stack, boolean doFill)
    {
        if (stack == null || stack.amount <= 0)
        {
            return 0;
        }
        
        FluidResource resource;
        
        if(this.fluidResource == null)
        {
            resource = FluidResource.fromStack(stack);
            this.fluidResource = resource;
        }
        else
        {
            if(!this.fluidResource.isStackEqual(stack)) return 0;
            resource = this.fluidResource;
        }
        
         // Prevent fractional liters.
        long requested = VolumeUnits.liters2nL(VolumeUnits.nL2Liters(this.availableCapacity()));
        requested = Math.min(requested, VolumeUnits.liters2nL(stack.amount));
        long filled = this.add(resource, requested, !doFill, null);
        
        return (int) VolumeUnits.nL2Liters(filled);
    }

    //TODO: move this to service thread
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain)
    {
        if(resource == null) return null;
        
        if(this.fluidResource == null || !this.fluidResource.isStackEqual(resource))
        {
            FluidStack result = resource.copy();
            result.amount = 0;
            return result;
        }
        return this.drain(resource.amount, doDrain);
    }

    //TODO: move this to service thread
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain)
    {
        if(this.fluidResource == null) return null;
        // need to save a reference here because may become null on drain
        FluidResource resource = this.fluidResource;
        
        long drained = VolumeUnits.liters2nL(maxDrain);
        drained = this.takeUpTo(resource, drained, !doDrain, null);
        return resource.newStackWithLiters((int) VolumeUnits.nL2Liters(drained));
    }
    
    @Override
    public void serializeNBT(NBTTagCompound nbt)
    {
        super.serializeNBT(nbt);
        if(this.fluidResource != null)
        {
            nbt.setTag(ModNBTTag.STORAGE_CONTENTS, this.fluidResource.withQuantity(this.used).toNBT());
        }
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        super.deserializeNBT(nbt);
        if(nbt.hasKey(ModNBTTag.STORAGE_CONTENTS))
        {
            FluidResourceWithQuantity rwq 
                = new FluidResourceWithQuantity(nbt.getCompoundTag(ModNBTTag.STORAGE_CONTENTS));
            this.fluidResource = (FluidResource) rwq.resource();
            this.used = rwq.getQuantity();
        }
    }
}