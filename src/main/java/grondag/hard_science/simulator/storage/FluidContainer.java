package grondag.hard_science.simulator.storage;

import javax.annotation.Nonnull;

import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.machines.matbuffer.BulkBufferPurpose;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.FluidResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.transport.management.LogisticsService;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class FluidContainer extends ResourceContainer<StorageTypeFluid> implements IFluidHandler
{
    private BulkBufferPurpose bufferPurpose;
    
    /**
     * Content key should be either at BulkBufferPurpose entry
     * or a specific fluid resource.  If it is specific fluid
     * resource, this container will only accept that fluid.
     */
    public FluidContainer(IDevice owner, ContainerUsage usage, @Nonnull BulkBufferPurpose bufferPurpose)
    {
        super(new FluidInner(owner, usage));
        this.bufferPurpose = bufferPurpose;
        if(bufferPurpose.fluidResource != null)
        {
            this.setContentPredicate(bufferPurpose.fluidResource);
        }
        this.setCapacity(VolumeUnits.liters2nL(32000));
    }
    
    /**
     * Will be a fluid resource if this container is limited
     * to a specific fluid.
     * @return
     */
    public final BulkBufferPurpose bufferPurpose()
    {
        return this.bufferPurpose;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        super.deserializeNBT(tag);
        this.bufferPurpose = Useful.safeEnumFromTag(tag, ModNBTTag.BUFFER_PURPOSE, BulkBufferPurpose.INVALID);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        super.serializeNBT(tag);
        Useful.saveEnumToTag(tag, ModNBTTag.BUFFER_PURPOSE, this.bufferPurpose);
    }



    private static class FluidInner extends AbstractSingleResourceContainer<StorageTypeFluid>
    {
        public FluidInner(IDevice owner, ContainerUsage usage)
        {
            super(owner, usage);
        }

        @Override
        public StorageTypeFluid storageType()
        {
            return StorageType.FLUID;
        }
    }
    
    protected final IFluidTankProperties myProps = new IFluidTankProperties()
    {

        @Override
        public FluidStack getContents()
        {
            if(FluidContainer.this.usedCapacity() == 0) return null;
            
            FluidStack result = ((FluidResource)FluidContainer.this.resource()).sampleFluidStack().copy();
            result.amount = (int) Math.min(
                    VolumeUnits.nL2Liters(FluidContainer.this.usedCapacity()),
                    Integer.MAX_VALUE);
            return result;
        }

        @Override
        public int getCapacity()
        {
            return (int) Math.min(
                    VolumeUnits.nL2Liters(FluidContainer.this.getCapacity()),
                    Integer.MAX_VALUE);
        }

        @Override
        public boolean canFill()
        {
            return FluidContainer.this.availableCapacity() >= VolumeUnits.LITER.nL;
        }

        @Override
        public boolean canDrain()
        {
            return FluidContainer.this.usedCapacity() >= VolumeUnits.LITER.nL;
        }

        @Override
        public boolean canFillFluidType(FluidStack fluidStack)
        {
            // must have capacity for at least 1mb
            return FluidContainer.this.availableCapacityFor(FluidResource.fromStack(fluidStack)) >= VolumeUnits.LITER.nL;
        }

        @Override
        public boolean canDrainFluidType(FluidStack fluidStack)
        {
            // must contain at least 1mb
            return FluidContainer.this.takeUpTo(
                    FluidResource.fromStack(fluidStack), 
                    VolumeUnits.LITER.nL, true) == VolumeUnits.LITER.nL; 
        }
    };
    
    @Override
    public IFluidTankProperties[] getTankProperties()
    {
        return new IFluidTankProperties[] {this.myProps};
    }

    @Override
    public int fill(FluidStack stack, boolean doFill)
    {
        if (stack == null || stack.amount <= 0)
        {
            return 0;
        }
        
        FluidResource resource;
        
        if(FluidContainer.this.resource() == null)
        {
            resource = FluidResource.fromStack(stack);
        }
        else
        {
            FluidResource myResource = (FluidResource)FluidContainer.this.resource();
            if(!myResource.isStackEqual(stack)) return 0;
            resource = myResource;
        }
        
        try
        {
            return LogisticsService.FLUID_SERVICE.executor.submit( () ->
            {
                // Prevent fractional liters.
                long requested = VolumeUnits.liters2nL(VolumeUnits.nL2Liters(this.availableCapacity()));
                requested = Math.min(requested, VolumeUnits.liters2nL(stack.amount));
                long filled = this.add(resource, requested, !doFill, null);
                
                return (int) VolumeUnits.nL2Liters(filled);
            }, true).get();
        }
        catch (Exception e)
        {
            Log.error("Error in fluid handler", e);
            return 0;
        }
        
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain)
    {
        if(resource == null) return null;
        
        if(FluidContainer.this.resource() == null 
                || !((FluidResource)FluidContainer.this.resource()).isStackEqual(resource))
        {
            FluidStack result = resource.copy();
            result.amount = 0;
            return result;
        }
        return this.drain(resource.amount, doDrain);
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain)
    {
        try
        {
            return LogisticsService.FLUID_SERVICE.executor.submit( () ->
            {
                if(FluidContainer.this.resource() == null) return null;
                // need to save a reference here because may become null on drain
                FluidResource resource = (FluidResource) FluidContainer.this.resource();
                
                long drained = VolumeUnits.liters2nL(maxDrain);
                drained = this.takeUpTo(resource, drained, !doDrain, null);
                return resource.newStackWithLiters((int) VolumeUnits.nL2Liters(drained));
            }, true).get();
        }
        catch (Exception e)
        {
            Log.error("Error in fluid handler", e);
            return null;
        }
    }
}
