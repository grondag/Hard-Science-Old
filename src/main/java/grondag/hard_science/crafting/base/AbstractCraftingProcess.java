package grondag.hard_science.crafting.base;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.simulator.resource.BulkResource;
import grondag.hard_science.simulator.resource.FluidResource;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ItemResource;

public abstract class AbstractCraftingProcess<R extends IHardScienceRecipe> implements ICraftingProcess<R>
{
    private final boolean consumesEnergy;
    private final boolean producesEnergy;
    private final ImmutableList<IResource<?>> allInputs;
    private final ImmutableList<IResource<?>> allOutputs;
    private final ImmutableList<FluidResource> fluidInputs;
    private final ImmutableList<FluidResource> fluidOutputs;
    private final ImmutableList<ItemResource> itemInputs;
    private final ImmutableList<ItemResource> itemOutputs;
    private final ImmutableList<BulkResource> bulkInputs;
    private final ImmutableList<BulkResource> bulkOutputs;
    
    protected AbstractCraftingProcess(
            List<IResource<?>> inputs,
            List<IResource<?>> outputs
            )
    {
        this.allInputs = ImmutableList.copyOf(inputs);
        this.allOutputs = ImmutableList.copyOf(outputs);
        
        boolean consumesEnergy = false;
        boolean producesEnergy = false;
        ImmutableList.Builder<FluidResource> fluidInputs = ImmutableList.builder();
        ImmutableList.Builder<FluidResource> fluidOutputs = ImmutableList.builder();
        ImmutableList.Builder<ItemResource> itemInputs = ImmutableList.builder();
        ImmutableList.Builder<ItemResource> itemOutputs = ImmutableList.builder();
        ImmutableList.Builder<BulkResource> bulkInputs = ImmutableList.builder();
        ImmutableList.Builder<BulkResource> bulkOutputs = ImmutableList.builder();
        
        for(IResource<?> r : inputs)
        {
            switch(r.storageType().enumType)
            {
            case FLUID:
                fluidInputs.add((FluidResource) r);
                break;
            case ITEM:
                itemInputs.add((ItemResource) r);
                break;
            case PRIVATE:
                bulkInputs.add((BulkResource) r);
                break;
            case POWER:
                consumesEnergy = true;
                break;
            default:
                break;
            
            }
        }
        
        for(IResource<?> r : outputs)
        {
            switch(r.storageType().enumType)
            {
            case FLUID:
                fluidOutputs.add((FluidResource) r);
                break;
            case ITEM:
                itemOutputs.add((ItemResource) r);
                break;
            case PRIVATE:
                bulkOutputs.add((BulkResource) r);
                break;
            case POWER:
                producesEnergy = true;
                break;
            default:
                break;
            
            }
        }
        this.consumesEnergy = consumesEnergy;
        this.producesEnergy = producesEnergy;
        this.fluidInputs = fluidInputs.build();
        this.fluidOutputs = fluidOutputs.build();
        this.itemInputs = itemInputs.build();
        this.itemOutputs = itemOutputs.build();
        this.bulkInputs = bulkInputs.build();
        this.bulkOutputs = bulkOutputs.build();
    }
    
    @Override
    public ImmutableList<FluidResource> fluidInputs()
    {
        return this.fluidInputs;
    }

    @Override
    public ImmutableList<ItemResource> itemInputs()
    {
        return this.itemInputs;
    }
    
    @Override
    public ImmutableList<BulkResource> bulkInputs()
    {
        return this.bulkInputs;
    }

    @Override
    public ImmutableList<FluidResource> fluidOutputs()
    {
        return this.fluidOutputs;
    }

    @Override
    public ImmutableList<ItemResource> itemOutputs()
    {
        return this.itemOutputs;
    }

    @Override
    public ImmutableList<BulkResource> bulkOutputs()
    {
        return this.bulkOutputs;
    }
    
    public ImmutableList<IResource<?>> allInputs()
    {
        return this.allInputs;
    }

    public ImmutableList<IResource<?>> allOutputs()
    {
        return this.allOutputs;
    }
    
    @Override
    public boolean consumesEnergy()
    {
        return this.consumesEnergy;
    }

    @Override
    public boolean producesEnergy()
    {
        return this.producesEnergy;
    }
}
