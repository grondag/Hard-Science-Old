package grondag.hard_science.crafting.base;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.BulkResource;
import grondag.hard_science.simulator.resource.FluidResource;
import grondag.hard_science.simulator.resource.ItemResource;

/**
 * Represents a (potentially configurable) production 
 * or fabrication process. Each instance does two things:
 * <li>Declares one or more outputs that can be produced 
 * by the process given a set of inputs.
 * <li>Determines specific amount(s) of needed inputs
 * to produce given minimum quantities of outputs, returing
 * the result as an instance of IHardScienceRecipe/p>
 * 
 */
public interface ICraftingProcess<R extends IHardScienceRecipe>
{
    public default ImmutableList<FluidResource>fluidInputs()
    {
        return ImmutableList.of();
    }
    
    public default ImmutableList<ItemResource>itemInputs()
    {
        return ImmutableList.of();
    }
    
    public default ImmutableList<BulkResource>bulkInputs()
    {
        return ImmutableList.of();
    }
    
    public default ImmutableList<FluidResource>fluidOutputs()
    {
        return ImmutableList.of();
    }
    
    /**
     * Output may be empty or represent sample outputs for crafting processes
     * that produce highly configurable items, like superblocks.
     */
    public default ImmutableList<ItemResource>itemOutputs()
    {
        return ImmutableList.of();
    }
    
    public default ImmutableList<BulkResource>bulkOutputs()
    {
        return ImmutableList.of();
    }
    
    /**
     * By products may result from this process but the process
     * should not be used to craft them because it would be
     * inefficient or result in a crafting loop.
     */
    public default ImmutableList<FluidResource>fluidByProducts()
    {
        return ImmutableList.of();
    }
    
    /**
     * By products may result from this process but the process
     * should not be used to craft them because it would be
     * inefficient or result in a crafting loop.
     */
    public default ImmutableList<ItemResource>itemByProducts()
    {
        return ImmutableList.of();
    }
    
    public default boolean consumesEnergy()
    {
        return false;
    }
    
    public default boolean producesEnergy()
    {
        return false;
    }
    
    public default boolean hasDuration()
    {
        return false;
    }
    
    /**
     * Total number of item and fluid inputs.
     */
    public default int inputSlots()
    {
        return this.fluidInputs().size() + this.itemInputs().size();
    }

    /**
     * Total number of item and fluid outputs.
     */
    public default int outputSlots()
    {
        return this.fluidOutputs().size() + this.itemOutputs().size();
    }
    
    /**
     * Larger of {@link #inputSlots()} and {@link #outputSlots()}.
     * Used to setup JEI page.
     */
    public default int maxSlots()
    {
        return Math.max(this.inputSlots(), this.outputSlots());
    }
    
    public R configureFromOutputs(
            @Nonnull List<AbstractResourceWithQuantity<?>> minOutputs);
    
    public default R configureFromOutputs(AbstractResourceWithQuantity<?> minOutput)
    {
        return this.configureFromInputs(ImmutableList.of(minOutput));
    }
    
    public R configureFromInputs(
            @Nonnull List<AbstractResourceWithQuantity<?>> maxInputs);
}
