package grondag.hard_science.crafting.base;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import grondag.hard_science.crafting.base.SingleParameterModel.Result;
import grondag.hard_science.crafting.base.SingleParameterModel.ResultBuilder;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;

public abstract class AbstractSingleModelProcess<R extends IHardScienceRecipe> extends AbstractCraftingProcess<R>
{
    protected final SingleParameterModel model = new SingleParameterModel();

    protected AbstractSingleModelProcess(List<IResource<?>> inputs, List<IResource<?>> outputs)
    {
        super(inputs, outputs);
    }

    protected abstract R makeRecipe(AbstractSingleModelProcess<R> abstractSingleModelProcess, Result result, int ticksDuration);
    
    @Override
    public R configureFromOutputs(@Nonnull List<AbstractResourceWithQuantity<?>> minOutputs)
    {
        assert this.allOutputs().containsAll(
                minOutputs.stream().map(p -> p.resource()).collect(Collectors.toList()))
            : "Invalid crafting configuration - output does not exist";
        
        assert !minOutputs.isEmpty()
        : "Invalid crafting configuration - no output specified";
        
        ResultBuilder builder = this.model.builder();
        for(AbstractResourceWithQuantity<?> rwq : minOutputs)
        {
            builder.ensureOutput(rwq.resource(), rwq.getQuantity());
        }
        
        return makeRecipe(this, builder.build(), 0);
    }

    @Override
    public R configureFromInputs(@Nonnull List<AbstractResourceWithQuantity<?>> maxInputs)
    {
        assert this.allInputs().containsAll(
                maxInputs.stream().map(p -> p.resource()).collect(Collectors.toList()))
        : "Invalid crafting configuration - input does not exist";
    
        assert !maxInputs.isEmpty()
        : "Invalid crafting configuration - no output specified";
        
        ResultBuilder builder = this.model.builder();
        for(AbstractResourceWithQuantity<?> rwq : maxInputs)
        {
            builder.limitInput(rwq.resource(), rwq.getQuantity());
        }
        
        return makeRecipe(this, builder.build(), 1);    }
}
