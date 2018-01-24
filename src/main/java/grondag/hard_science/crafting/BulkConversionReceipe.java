package grondag.hard_science.crafting;

import java.util.List;
import javax.annotation.Nullable;

import org.magicwerk.brownies.collections.Key2List;

import com.google.common.collect.ImmutableList;
import grondag.hard_science.simulator.resource.FluidResource;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

/**
 * Represents a one-way conversion from a specific fluid 
 * or item resource to a general bulk (fluid) resource.
 * Used when a fabrication process can accept multiple 
 * inputs with varying types or conversion parameters
 * but no processing is required. <p>
 * 
 * Note that containerized fluids are NOT respresented
 * as item inputs for conversion. Machines must be smart
 * enough to know that they can obtain the containerized
 * source fluid as an item and then convert the fluid in it.<p>
 * 
 * An example is block fabrication, which requires some kind
 * of mineral filler, but this can sand, depleted mineral dust,
 * raw mineral dust, or possibly other materials.  In this 
 * case we define a general fluid like "flowable mineral filler"
 * and map those other resources to it.  <p>
 * 
 * No machine will craft depleted mineral dust, but machines 
 * that need depleted mineral dust will load any of the
 * mapped resources into their "flowable mineral filler" buffer.
 * Generally speaking, these resources are never exported -
 * the only place they will ever exist is in a machine input buffer.
 *
 */
public class BulkConversionReceipe<T extends StorageType<T>>
{
    private static final Key2List<BulkConversionReceipe<?>, FluidResource, IResource<?>> conversions 
    = new Key2List.Builder<BulkConversionReceipe<?>, FluidResource, IResource<?>>().
          withKey1Map(BulkConversionReceipe::outputResource).
          withKey2Map(BulkConversionReceipe::inputResource).
          build();
    
    public static List<BulkConversionReceipe<?>>allRecipes()
    {
        return ImmutableList.copyOf(conversions);
    }
    
    @Nullable
    public static List<BulkConversionReceipe<?>> getConversions(FluidResource bulkResource)
    {
        return ImmutableList.copyOf(conversions.getAllByKey1(bulkResource));
    }
    
    public static <V extends StorageType<V>> void addConversion(FluidResource outputResource, IResource<V> resource, double factor)
    { 
        BulkConversionReceipe<V> recipe = new BulkConversionReceipe<V>(outputResource, resource, factor);
        conversions.add(recipe);
    }
    
    /**
     * The bulk resource that is the result of this conversion.
     */
    private final FluidResource outputResource;
    public FluidResource outputResource() { return this.outputResource; }
    
    private final IResource<T> inputResource;
    public IResource<T> inputResource() { return this.inputResource; }
    
    /**
     * For item resource inputs, the number of nL output resulting
     * from a single stack of intput.<p>
     * 
     * For fluid resource inputs, the conversion factor from input
     * nL to output nL.
     */
    public final double conversionFactor;
    
    private BulkConversionReceipe(FluidResource outputResource, IResource<T> inputResource, double conversionFactor)
    {
        this.outputResource = outputResource;
        this.inputResource = inputResource;
        this.conversionFactor = conversionFactor;
    }
    
}
