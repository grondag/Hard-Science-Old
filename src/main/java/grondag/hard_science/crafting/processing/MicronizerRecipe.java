package grondag.hard_science.crafting.processing;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.magicwerk.brownies.collections.Key2List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.crafting.base.GenericRecipe;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.machines.energy.MachinePower;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.resource.BulkResource;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.IResourcePredicate;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.PowerResource;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.placement.PlacementItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;

public class MicronizerRecipe
{
    public static final long JOULES_PER_STONE_TONNE = MachinePower.JOULES_PER_KWH / 2;
    
    private static final Key2List<MicronizerRecipe, Ingredient, BulkResource> conversions 
    = new Key2List.Builder<MicronizerRecipe, Ingredient, BulkResource>().
          withKey1Map(MicronizerRecipe::inputIngredeient).
          withKey2Map(MicronizerRecipe::outputResource).
          build();
    
    /**
     * 
     * @param inputIngredeient
     * @param outputResource
     * @param powerFactor  1.0 = whole stone.  Use smaller values for materials that are softere
     *      or partially crushed.  Automatically considers output volume - do NOT
     *      reduce for items that are less than a full block.
     * @param converter
     */
    public static void add(
        Ingredient inputIngredeient,
        BulkResource outputResource,
        double powerFactor,
        Function<ItemStack, Long> converter)
    {
        conversions.add(new MicronizerRecipe(inputIngredeient, outputResource, powerFactor, converter));
    }
    
    public static List<Ingredient> allInputs()
    {
        return conversions.getAllKeys1();
    }
    
    public static List<BulkResource> allOutputs()
    {
        return conversions.getAllKeys2();
    }
    
    public static MicronizerRecipe getForInput(ItemStack stack)
    {
        return conversions.stream()
                .filter(i -> i.inputResource.apply(stack))
                .findFirst().orElse(null);
    }
    
    public static MicronizerRecipe getForInput(ItemResource input)
    {
        return getForInput(input.sampleItemStack());
    }
    
    public static List<MicronizerRecipe> getForOutput(BulkResource output)
    {
        return conversions.getAllByKey2(output);
    }
    
    public static final Predicate<ItemStack> STACK_PREDICATE;
    public static final IResourcePredicate<StorageTypeStack> RESOURCE_PREDICATE;
    
    static
    {
        STACK_PREDICATE = new Predicate<ItemStack>() 
        {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public boolean test(ItemStack t)
            {
                for(Predicate p : allInputs())
                {
                    if(p.test(t)) return true;
                }
                return false;
            }
        };
        
        RESOURCE_PREDICATE = new IResourcePredicate<StorageTypeStack>() 
        {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public boolean test(IResource<StorageTypeStack> t)
            {
                ItemStack stack = ((ItemResource)t).sampleItemStack();
                
                for(Predicate p : allInputs())
                {
                    if(p.test(stack)) return true;
                }
                
                return false;
            }
        };
    }
    
    private final Ingredient inputResource;
    private final BulkResource outputResource;
    private final double powerFactor;
    private final Function<ItemStack, Long> converter;
    
    private MicronizerRecipe(
        Ingredient inputIngredient,
        BulkResource outputResource,
        double powerFactor,
        Function<ItemStack, Long> converter)
    {
        this.inputResource = inputIngredient;
        this.outputResource = outputResource;
        this.powerFactor = powerFactor;
        this.converter = converter;
    }
    
    public Ingredient inputIngredeient()
    {
        return this.inputResource;
    }
    
    public BulkResource outputResource()
    {
        return this.outputResource;
    }
    
    public Function<ItemStack, Long> converter()
    {
        return this.converter;
    }
    
    /**
     * Nanoliters of output for the given input stack.
     * DOES multiply by the count on the stack.
     */
    public long outputForStack(ItemStack inputStack)
    {
        return this.converter.apply(inputStack) * inputStack.getCount();
    }
    
    /**
     * Total energy consumption that will be needed 
     * to process the given input stack, in joules.
     * DOES multiply by the count on the stack.
     */
    public long energyForStack(ItemStack inputStack)
    {
        return (long) (   VolumeUnits.nL2Blocks(outputForStack(inputStack)) 
                        * this.outputResource.tonnesPerBlock()
                        * this.powerFactor
                        * JOULES_PER_STONE_TONNE);
    }
    
    public GenericRecipe displayForStack(ItemStack inputStack)
    {
        return new GenericRecipe(
                ImmutableList.of(
                        ItemResource.fromStack(inputStack).withQuantity(inputStack.getCount()),
                        PowerResource.JOULES.withQuantity(this.energyForStack(inputStack))), 
                ImmutableList.of(
                        this.outputResource.withQuantity(this.outputForStack(inputStack))), 
                0) {};   
    }

    
    /**
     * returns nL of output that will result for each of the given item
     */
    public static class FixedConverter implements Function<ItemStack, Long> 
    {
        public static final FixedConverter FULL_BLOCK 
            = new MicronizerRecipe.FixedConverter(VolumeUnits.KILOLITER.nL);

        public static final FixedConverter HALF_BLOCK 
        = new MicronizerRecipe.FixedConverter(VolumeUnits.KILOLITER.nL / 2);
        
        public static final FixedConverter NINTH_BLOCK 
        = new MicronizerRecipe.FixedConverter(VolumeUnits.KILOLITER.nL / 9);
        
        private final long nlPerItem;
        
        public FixedConverter(long nlPerItem)
        {
            this.nlPerItem = nlPerItem;
        }
        
        @Override
        public Long apply(ItemStack t)
        {
            return this.nlPerItem;
        }
    }
    
    /**
     * returns nL of output that will result for each of the given item
     */
    public static class TerrainConverter implements Function<ItemStack, Long> 
    {
        public static final TerrainConverter INSTANCE = new TerrainConverter();
        
        private TerrainConverter()
        {
        }
        
        @Override
        public Long apply(ItemStack stack)
        {
            double volume = 0;
            ModelState modelState = PlacementItem.getStackModelState(stack);
            if(modelState == null) return 0L;
            for(AxisAlignedBB box : modelState.getShape().meshFactory().collisionHandler().getCollisionBoxes(modelState))
            {
                volume += Useful.volumeAABB(box);
            }
            return (long) (MathHelper.clamp(volume, 0, 1) * VolumeUnits.KILOLITER.nL);
        }
    }
}
