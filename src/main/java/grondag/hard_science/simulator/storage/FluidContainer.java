package grondag.hard_science.simulator.storage;

import grondag.hard_science.Log;
import grondag.hard_science.machines.support.VolumeUnits;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.FluidResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.transport.management.LogisticsService;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class FluidContainer extends ResourceContainer<StorageTypeFluid> implements IFluidHandler
{
    public FluidContainer(IDevice owner, ContainerUsage usage)
    {
        super(new FluidInner(owner, usage));
        this.setCapacity(VolumeUnits.liters2nL(32000));
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
            return (FluidContainer.this.resource() == null 
                    || ((FluidResource)FluidContainer.this.resource()).isStackEqual(fluidStack))
                    && FluidContainer.this.availableCapacity() > 0;
        }

        @Override
        public boolean canDrainFluidType(FluidStack fluidStack)
        {
            return FluidContainer.this.resource() != null 
                    && ((FluidResource)FluidContainer.this.resource()).isStackEqual(fluidStack)
                    && FluidContainer.this.usedCapacity() > 0;
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