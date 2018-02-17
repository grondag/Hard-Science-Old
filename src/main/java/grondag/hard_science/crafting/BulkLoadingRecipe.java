package grondag.hard_science.crafting;

import java.util.List;

import javax.annotation.Nullable;

import org.magicwerk.brownies.collections.Key2List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.HardScience;
import grondag.hard_science.crafting.base.AbstractRecipe;
import grondag.hard_science.crafting.base.ICraftingProcess;
import grondag.hard_science.crafting.base.SingleParameterModel;
import grondag.hard_science.crafting.base.SingleParameterModel.Result;
import grondag.hard_science.external.jei.AbstractRecipeCategory;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.BulkResource;
import grondag.hard_science.simulator.resource.BulkResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import mezz.jei.api.IGuiHelper;
import net.minecraft.util.ResourceLocation;

/**
 * Represents a one-way conversion from a specific fluid 
 * or item resource to a general bulk (fluid) resource.
 * Used when a fabrication process can accept multiple 
 * inputs with varying types or conversion parameters
 * but no processing is required. <p>
 * 
 * Note that containerized fluids are NOT represented
 * as item inputs for conversion. Machines must be smart
 * enough to know that they can obtain the containerized
 * source fluid as an item and then convert the fluid in it.<p>
 * 
 * An example is block fabrication, which requires some kind
 * of mineral filler, but this can be sand, depleted mineral dust,
 * raw mineral dust, or possibly other materials.  In this 
 * case we define a general fluid like "flowable mineral filler"
 * and map those other resources to it.  <p>
 * 
 * No machine will craft "flowable mineral filler", but machines 
 * that need it for fabrication will load any of the
 * mapped resources into their "flowable mineral filler" buffer.
 * Generally speaking, these resources are never exported -
 * the only place they will ever exist is in a machine input buffer.
 *
 */
public class BulkLoadingRecipe<T extends StorageType<T>> extends AbstractRecipe
{
    public static final String UID = HardScience.prefixName("bulk_conversion");

    private static final Key2List<Process<?>, BulkResource, IResource<?>> conversions 
    = new Key2List.Builder<Process<?>, BulkResource, IResource<?>>().
          withKey1Map(Process::outputResource).
          withKey2Map(Process::inputResource).
          build();
    
    /**
     * Generates list of sample recipes to display in JEI
     */
    public static List<BulkLoadingRecipe<?>>allRecipes()
    {
        ImmutableList.Builder<BulkLoadingRecipe<?>> builder = ImmutableList.builder();
        for(Process<?> p : conversions)
        {
            builder.add(p.configureFromOutputs(ImmutableList.of(p.outputResource.withQuantity(VolumeUnits.KILOLITER.nL))));
        }
        return builder.build();
    }
    
    public static List<Process<?>>allConversions()
    {
        return ImmutableList.copyOf(conversions);
    }
    
    @Nullable
    public static List<Process<?>> getConversions(BulkResource bulkResource)
    {
        return ImmutableList.copyOf(conversions.getAllByKey1(bulkResource));
    }
    
    public static <V extends StorageType<V>> void addConversion(BulkResource outputResource, IResource<V> resource, double factor)
    { 
        Process<V> recipe = new Process<V>(outputResource, resource, factor);
        conversions.add(recipe);
    }
    
    public BulkLoadingRecipe(AbstractResourceWithQuantity<?> input, BulkResourceWithQuantity output)
    {
        super(
                ImmutableList.of(input), 
                ImmutableList.of(output), 
                0);
    }

    public static class Category extends AbstractRecipeCategory<BulkLoadingRecipe<?>>
    {
        
        public Category(IGuiHelper guiHelper)
        {
            super(
                    guiHelper, 
                    1,
                    UID,
                    new ResourceLocation("hard_science", "textures/blocks/material_shortage.png"));
        }
    }
    
    public static class Process<T extends StorageType<T>> implements ICraftingProcess<BulkLoadingRecipe<T>>
    {
        
        private SingleParameterModel model = new SingleParameterModel();
        
        /**
         * The bulk resource that is the result of this conversion.
         */
        private final BulkResource outputResource;
        public BulkResource outputResource() { return this.outputResource; }
        
        private final IResource<T> inputResource;
        public IResource<T> inputResource() { return this.inputResource; }
        
        /**
         * For item resource inputs, conversion factor is nL output resulting
         * from a single stack of input.<p>
         * 
         * For fluid resource inputs, conversion factor simple maps input
         * nL to output nL.
         */
        private Process(BulkResource outputResource, IResource<T> inputResource, double conversionFactor)
        {
            this.outputResource = outputResource;
            this.inputResource = inputResource;
            this.model.createInput(inputResource, 1);
            this.model.createOutput(outputResource, conversionFactor);
        }
    
        @SuppressWarnings("unchecked")
        @Override
        public BulkLoadingRecipe<T> configureFromOutputs(
                List<AbstractResourceWithQuantity<?>> minOutputs)
        {
            if(minOutputs.size() != 1 || !minOutputs.get(0).resource().isResourceEqual(this.outputResource))
            {
                assert false : "Invalid crafting configuration.";
                return (BulkLoadingRecipe<T>) AbstractRecipe.EMPTY_RECIPE;
            }
            
            long outputNeeded = minOutputs.get(0).getQuantity();
            Result result = this.model.builder()
                    .ensureOutput(outputResource, outputNeeded)
                    .build();
    
            long inputNeeded = result.inputValueDiscrete(inputResource);
            
            return new BulkLoadingRecipe<T>(
                    this.inputResource.withQuantity(inputNeeded), 
                    this.outputResource.withQuantity(outputNeeded));
        }
    
        @SuppressWarnings("unchecked")
        @Override
        public BulkLoadingRecipe<T> configureFromInputs(
                List<AbstractResourceWithQuantity<?>> maxInputs)
        {
            if(maxInputs.size() != 1 || !maxInputs.get(0).resource().isResourceEqual(this.inputResource))
            {
                assert false : "Invalid crafting configuration.";
                return (BulkLoadingRecipe<T>) AbstractRecipe.EMPTY_RECIPE;
            }
            
            long inputAvailable = maxInputs.get(0).getQuantity();
            Result result = this.model.builder()
                    .limitInput(inputResource, inputAvailable)
                    .build();
    
            long output = result.outputValueDiscrete(outputResource);
            
            return new BulkLoadingRecipe<T>(
                    this.inputResource.withQuantity(inputAvailable), 
                    this.outputResource.withQuantity(output));
        }
    }
}
