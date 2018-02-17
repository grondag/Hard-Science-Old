package grondag.hard_science.crafting.processing;

import static grondag.hard_science.HardScience.resource;

import java.util.List;

import javax.annotation.Nullable;

import org.magicwerk.brownies.collections.Key1List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.HardScience;
import grondag.hard_science.crafting.base.AbstractCraftingProcess;
import grondag.hard_science.crafting.base.AbstractRecipe;
import grondag.hard_science.crafting.base.AbstractSingleModelProcess;
import grondag.hard_science.crafting.base.SingleParameterModel.Result;
import grondag.hard_science.external.jei.AbstractRecipeCategory;
import grondag.hard_science.init.ModBulkResources;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.BulkResource;
import grondag.hard_science.simulator.resource.ItemResource;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CrushinatorRecipe extends AbstractRecipe
{
    public static final String UID = HardScience.prefixName("crushinator");

    private static final Key1List<Process, ItemResource> fabs 
    = new Key1List.Builder<Process, ItemResource>().
          withKey1Map(Process::outputResource).
          build();
    
    /**
     * Generates list of sample recipes to display in JEI
     */
    public static List<CrushinatorRecipe>allRecipes()
    {
        ImmutableList.Builder<CrushinatorRecipe> builder = ImmutableList.builder();
        for(Process p : fabs)
        {
            builder.add(p.configureFromOutputs(ImmutableList.of(p.outputResource().withQuantity(1))));
        }
        return builder.build();
    }
    
    public static List<Process>allFabs()
    {
        return ImmutableList.copyOf(fabs);
    }
    
    @Nullable
    public static List<Process> getFabs(ItemResource itemResource)
    {
        return ImmutableList.copyOf(fabs.getAllByKey1(itemResource));
    }
    
    /**
     * First output is primary output
     */
    public static void addFab(
            List<AbstractResourceWithQuantity<?>> inputs, 
            List<AbstractResourceWithQuantity<?>> outputs)
    { 
        Process fab = new Process(inputs, outputs);
        fabs.add(fab);
    }
    
    /**
     * First output is primary output
     */
    public static void addFab(
            Object[] inputs,
            String itemName, 
            long quantity)
    { 
        ImmutableList.Builder<AbstractResourceWithQuantity<?>> inBuilder = ImmutableList.builder();
        for(int i = 0; i < inputs.length; i += 2)
        {
            BulkResource m = ModBulkResources.get((String)inputs[i]);
            inBuilder.add(m.fluidResource().withQuantity((long)inputs[i+1]));
        }
        
        Item item = ForgeRegistries.ITEMS.getValue(resource(itemName));
        
        addFab(inBuilder.build(), 
                ImmutableList.of(ItemResource.fromItem(item).withQuantity(quantity)));
    }
    
    protected CrushinatorRecipe(AbstractCraftingProcess<?> process, Result result, int ticksDuration)
    {
        super(process, result, ticksDuration);
    }

    public static class Category extends AbstractRecipeCategory<CrushinatorRecipe>
    {
        public Category(IGuiHelper guiHelper)
        {
            super(
                    guiHelper, 
                    4,
                    UID,
                    new ResourceLocation("hard_science", "textures/blocks/linear_marks_128.png"));
        }
    }
    
    public static class Process extends AbstractSingleModelProcess<CrushinatorRecipe>
    {
        public ItemResource outputResource()
        {
            return this.itemOutputs().get(0);
        }
        
        /**
         * First item output is the primary output
         */
        protected Process(List<AbstractResourceWithQuantity<?>> inputs, List<AbstractResourceWithQuantity<?>> outputs)
        {
            super(
                    inputs.stream().map(p -> p.resource())
                        .collect(ImmutableList.toImmutableList()),
                    outputs.stream().map(p -> p.resource())
                        .collect(ImmutableList.toImmutableList()));
            
            inputs.stream().forEach(p -> 
            {
                model.createInput(p.resource(), p.getQuantity());
            });
            
            outputs.stream().forEach(p -> 
            {
                model.createOutput(p.resource(), p.getQuantity());
            });
        }

        @Override
        protected CrushinatorRecipe makeRecipe(AbstractSingleModelProcess<CrushinatorRecipe> abstractSingleModelProcess, Result result,
                int ticksDuration)
        {
            return new CrushinatorRecipe(this, result, ticksDuration);
        }
      
    }
}
