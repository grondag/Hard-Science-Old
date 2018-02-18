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

public class BulkExportRecipe<T extends StorageType<T>> extends AbstractRecipe
{
    public static final String UID = HardScience.prefixName("bulk_export");

    private static final Key2List<Process<?>, IResource<?>, BulkResource> conversions 
    = new Key2List.Builder<Process<?>, IResource<?>, BulkResource>().
          withKey1Map(Process::outputResource).
          withKey2Map(Process::inputResource).
          build();
    
    /**
     * Generates list of sample recipes to display in JEI
     */
    public static List<BulkExportRecipe<?>>allRecipes()
    {
        ImmutableList.Builder<BulkExportRecipe<?>> builder = ImmutableList.builder();
        for(Process<?> p : conversions)
        {
            builder.add(p.configureFromOutputs(ImmutableList.of(p.outputResource.withQuantity(1))));
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
    
    public static <V extends StorageType<V>> void addConversion(IResource<V> outputResource, BulkResource inputResource, double factor)
    { 
        Process<V> recipe = new Process<V>(outputResource, inputResource, factor);
        conversions.add(recipe);
    }
    
    public BulkExportRecipe(BulkResourceWithQuantity input, AbstractResourceWithQuantity<?> output)
    {
        super(
                ImmutableList.of(input), 
                ImmutableList.of(output), 
                0);
    }

    public static class Category extends AbstractRecipeCategory<BulkExportRecipe<?>>
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
    
    public static class Process<T extends StorageType<T>> implements ICraftingProcess<BulkExportRecipe<T>>
    {
        
        private SingleParameterModel model = new SingleParameterModel();
        
        /**
         * The bulk resource that is the input for this conversion.
         */
        private final BulkResource inputResource;
        public BulkResource inputResource() { return this.inputResource; }
        
        /**
         * Fluid or item that is output for this conversion.
         */
        private final IResource<T> outputResource;
        public IResource<T> outputResource() { return this.outputResource; }
        
        /**
         * For item resource inputs, conversion factor is nL output resulting
         * from a single stack of input.<p>
         * 
         * For fluid resource inputs, conversion factor simple maps input
         * nL to output nL.
         */
        private Process(IResource<T> outputResource, BulkResource inputResource, double conversionFactor)
        {
            this.outputResource = outputResource;
            this.inputResource = inputResource;
            this.model.createInput(inputResource, conversionFactor);
            this.model.createOutput(outputResource, 1);
        }
    
        @SuppressWarnings("unchecked")
        @Override
        public BulkExportRecipe<T> configureFromOutputs(
                List<AbstractResourceWithQuantity<?>> minOutputs)
        {
            if(minOutputs.size() != 1 || !minOutputs.get(0).resource().isResourceEqual(this.outputResource))
            {
                assert false : "Invalid crafting configuration.";
                return (BulkExportRecipe<T>) AbstractRecipe.EMPTY_RECIPE;
            }
            
            long outputNeeded = minOutputs.get(0).getQuantity();
            Result result = this.model.builder()
                    .ensureOutput(outputResource, outputNeeded)
                    .build();
    
            long inputNeeded = result.inputValueDiscrete(inputResource);
            
            return new BulkExportRecipe<T>(
                    this.inputResource.withQuantity(inputNeeded), 
                    this.outputResource.withQuantity(outputNeeded));
        }
    
        @SuppressWarnings("unchecked")
        @Override
        public BulkExportRecipe<T> configureFromInputs(
                List<AbstractResourceWithQuantity<?>> maxInputs)
        {
            if(maxInputs.size() != 1 || !maxInputs.get(0).resource().isResourceEqual(this.inputResource))
            {
                assert false : "Invalid crafting configuration.";
                return (BulkExportRecipe<T>) AbstractRecipe.EMPTY_RECIPE;
            }
            
            long inputAvailable = maxInputs.get(0).getQuantity();
            Result result = this.model.builder()
                    .limitInput(inputResource, inputAvailable)
                    .build();
    
            long output = result.outputValueDiscrete(outputResource);
            
            return new BulkExportRecipe<T>(
                    this.inputResource.withQuantity(inputAvailable), 
                    this.outputResource.withQuantity(output));
        }
    }
}
