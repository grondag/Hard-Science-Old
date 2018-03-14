package grondag.hard_science.crafting.processing;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;

import grondag.hard_science.HardScience;
import grondag.hard_science.crafting.base.GenericRecipe;
import grondag.hard_science.external.jei.AbstractRecipeCategory;
import grondag.hard_science.external.jei.RecipeFormat;
import grondag.hard_science.init.ModBulkResources;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.BulkResource;
import grondag.hard_science.simulator.resource.FluidResource;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.IResourcePredicate;
import grondag.hard_science.simulator.resource.PowerResource;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import mezz.jei.api.IGuiHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class DigesterRecipe
{
    public static final String JEI_UID = HardScience.prefixName("digester");

    private static final HashMap<Fluid, DigesterRecipe> byInput = new HashMap<>();
    
    private static final ArrayListMultimap<Fluid, DigesterRecipe> byOutput = ArrayListMultimap.create();
    
    /**
     * 
     * @param inputIngredeient
     * @param outputResource
     * @param powerFactor  1.0 = whole stone.  Use smaller values for materials that are softere
     *      or partially crushed.  Automatically considers output volume - do NOT
     *      reduce for items that are less than a full block.
     * @param converter
     */
    public static void add(BulkResource resource)
    {
        DigesterRecipe r = new DigesterRecipe(resource);
                
        byInput.put(r.inputFluid, r);
        
        for(Entry<Fluid> e : r.outputFactors.object2DoubleEntrySet())
        {
            byOutput.put(e.getKey(), r);
        }
    }
    
    private static Set<Fluid> wrappedInputs;
    
    public static Set<Fluid> allInputs()
    {
        if(wrappedInputs == null)
        {
            wrappedInputs = Collections.unmodifiableSet(byInput.keySet());
        }
        return wrappedInputs;
    }
    
    private static Set<Fluid> wrappedOutputs;
    
    public static Set<Fluid> allOutputs()
    {
        if(wrappedOutputs == null)
        {
            wrappedOutputs = Collections.unmodifiableSet(byOutput.keySet());
        }
        return wrappedOutputs;
    }
    
    public static DigesterRecipe getForInput(Fluid input)
    {
        return byInput.get(input);
    }
    
    public static List<DigesterRecipe> getForOutput(Fluid output)
    {
        return byOutput.get(output);
    }
    
    public static final Predicate<FluidStack> INPUT_STACK_PREDICATE;
    public static final IResourcePredicate<StorageTypeFluid> INPUT_RESOURCE_PREDICATE;
    public static final IResourcePredicate<StorageTypeFluid> OUTPUT_RESOURCE_PREDICATE;
    
    static
    {
        INPUT_STACK_PREDICATE = new Predicate<FluidStack>() 
        {
            @Override
            public boolean test(FluidStack t)
            {
                return byInput.containsKey(t.getFluid());
            }
        };
        
        INPUT_RESOURCE_PREDICATE = new IResourcePredicate<StorageTypeFluid>() 
        {
            @Override
            public boolean test(IResource<StorageTypeFluid> t)
            {
                return byInput.containsKey(((FluidResource)t).getFluid());
            }
        };
        
        OUTPUT_RESOURCE_PREDICATE = new IResourcePredicate<StorageTypeFluid>() 
        {
            @Override
            public boolean test(IResource<StorageTypeFluid> t)
            {
                return byOutput.containsKey(((FluidResource)t).getFluid());
            }
        };
    }
    
    /**
     * Lists all possible inputs and outputs for JEI registration.
     * Assuming will be called only 1X
     */ 
    public static ImmutableList<GenericRecipe> allForJEI()
    {
        ImmutableList.Builder<GenericRecipe> builder = ImmutableList.builder();
        for(DigesterRecipe r : byInput.values())
        {
            builder.add(r.displayForInputLiters(1000));
        }
        return builder.build();
    }

    private final Fluid inputFluid;
    private final double waterInputLitersPerInputLiter;
    private final double airInputLitersPerInputLiter;
    private final double joulesPerInputLiter;
    private final Object2DoubleMap<Fluid> outputFactors;
    
    private DigesterRecipe(BulkResource inputResource)
    {
        this.inputFluid = inputResource.fluid();
        DigesterAnalysis digest = DigesterAnalysis.get(inputResource);
        
        this.waterInputLitersPerInputLiter = digest.waterInputLitersPerInputLiter();
        this.airInputLitersPerInputLiter = digest.airInputLitersPerInputLiter();
        this.joulesPerInputLiter = digest.joulesPerInputLiter();
        
        // need to add residual to outputs if there is any
        // Analysis can't include it because it may not
        // exist yet at the time the analysis is constructed.
        // (Analysis is used earlier to construct the residue compound/fluid.)
        Object2DoubleMap<Fluid> factors = new Object2DoubleOpenHashMap<>();
        factors.putAll(digest.outputFactors());
        BulkResource outputResource = ModBulkResources.get(ModBulkResources.digesterOutputNameFor(inputResource));
        if(outputResource != null && digest.residueOutputMolsPerInputLiter() > 0)
        {
            factors.put(outputResource.fluid(), digest.residueOutputMolsPerInputLiter() * outputResource.litersPerMol());
        }
        this.outputFactors = Object2DoubleMaps.unmodifiable(factors);
    }
    
    public Fluid inputFluid()
    {
        return this.inputFluid;
    }
    
    public double waterLitersPerInputLiter()
    {
        return this.waterInputLitersPerInputLiter;
    }
    
    public double airLitersPerInputLiter()
    {
        return this.airInputLitersPerInputLiter;
    }
    
    public double joulesPerInputLiter()
    {
        return this.joulesPerInputLiter;
    }
    
    public Object2DoubleMap<Fluid> outputFactors()
    {
        return this.outputFactors;
    }
    
    private GenericRecipe displayForInputLiters(int liters)
    {
        long nL = VolumeUnits.liters2nL(liters);
        
        return new GenericRecipe(
            ImmutableList.of(
                    new FluidResource(this.inputFluid, null).withQuantity(nL),
                    PowerResource.JOULES.withQuantity((long) (this.joulesPerInputLiter * liters))), 
            
            this.outputFactors.object2DoubleEntrySet().stream()
                .<AbstractResourceWithQuantity<?>>map(e -> new FluidResource(e.getKey(), null).withQuantity((long)(e.getDoubleValue() * nL)))
                .collect(ImmutableList.toImmutableList()),
            
            0);   
    }
    
    public static class Category extends AbstractRecipeCategory<GenericRecipe>
    {
        public Category(IGuiHelper guiHelper)
        {
            super(
                    guiHelper, 
                    new RecipeFormat(1, DigesterAnalysis.maxOuputRowsJEI()),
                    JEI_UID,
                    //TODO: better icon
                    new ResourceLocation("hard_science", "textures/blocks/linear_marks_128.png"));
        }
    }
}
