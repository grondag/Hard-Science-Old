package grondag.hard_science.crafting.processing;

import java.util.List;

import org.magicwerk.brownies.collections.Key1List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.HardScience;
import grondag.hard_science.crafting.base.AbstractCraftingProcess;
import grondag.hard_science.crafting.base.GenericRecipe;
import grondag.hard_science.crafting.base.IHardScienceRecipe;
import grondag.hard_science.crafting.base.AbstractSingleModelProcess;
import grondag.hard_science.crafting.base.SingleParameterModel.Result;
import grondag.hard_science.external.jei.AbstractRecipeCategory;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.BulkResourceWithQuantity;
import grondag.hard_science.simulator.resource.ItemResource;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class CrushinatorRecipe extends GenericRecipe
{
    public static final String UID = HardScience.prefixName("crushinator");

    private static final Key1List<Process, ItemResource> fabs 
    = new Key1List.Builder<Process, ItemResource>().
          withKey1Map(Process::inputResource).
          build();
    
    /**
     * Generates list of sample recipes to display in JEI
     */
    public static List<IHardScienceRecipe>allRecipes()
    {
        ImmutableList.Builder<IHardScienceRecipe> builder = ImmutableList.builder();
        for(Process p : fabs)
        {
            builder.add(p.configureFromInputs(ImmutableList.of(p.allInputs().get(0).withQuantity(1))));
        }
        return builder.build();
    }
    
    public static List<Process>allFabs()
    {
        return ImmutableList.copyOf(fabs);
    }
    

    public static void addFab(
            Item input, 
            BulkResourceWithQuantity... outputs)
    { 
        Process fab = new Process(
                ImmutableList.of(ItemResource.fromItem(input).withQuantity(1)),
                ImmutableList.copyOf(outputs));
        fabs.add(fab);
    }
    
    protected CrushinatorRecipe(AbstractCraftingProcess<?> process, Result result, int ticksDuration)
    {
        super(process, result, ticksDuration);
    }

    public CrushinatorRecipe(NBTTagCompound tag)
    {
        super(tag);
    }
    
    public CrushinatorRecipe(PacketBuffer pBuff)
    {
        super(pBuff);
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
        public ItemResource inputResource()
        {
            return this.itemInputs().get(0);
        }
        
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
