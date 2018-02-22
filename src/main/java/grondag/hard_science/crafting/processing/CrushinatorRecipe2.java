package grondag.hard_science.crafting.processing;

import java.util.List;

import org.magicwerk.brownies.collections.Key2List;

import grondag.hard_science.simulator.resource.BulkResource;

public class CrushinatorRecipe2
{
    private static final Key2List<CrushinatorRecipe2, BulkResource, BulkResource> conversions 
    = new Key2List.Builder<CrushinatorRecipe2, BulkResource, BulkResource>().
          withKey1Map(CrushinatorRecipe2::inputResource).
          withKey2Map(CrushinatorRecipe2::outputResource).
          build();
    
    public static void add(
        BulkResource inputResource,
        BulkResource outputResource,
        double conversionFactor)
    {
        conversions.add(new CrushinatorRecipe2(inputResource, outputResource, conversionFactor));
    }
    
    public static List<BulkResource> allInputs()
    {
        return conversions.getAllKeys1();
    }
    
    public static List<BulkResource> allOutputs()
    {
        return conversions.getAllKeys2();
    }
    
    public static List<CrushinatorRecipe2> getForInput(BulkResource input)
    {
        return conversions.getAllByKey1(input);
    }
    
    public static List<CrushinatorRecipe2> getForOutput(BulkResource output)
    {
        return conversions.getAllByKey2(output);
    }
    
    private final BulkResource inputResource;
    private final BulkResource outputResource;
    private final double conversionFactor;
    
    private CrushinatorRecipe2(
        BulkResource inputResource,
        BulkResource outputResource,
        double conversionFactor)
    {
        this.inputResource = inputResource;
        this.outputResource = outputResource;
        this.conversionFactor = conversionFactor;
    }
    
    public BulkResource inputResource()
    {
        return this.inputResource;
    }
    
    public BulkResource outputResource()
    {
        return this.outputResource;
    }
    
    public double conversionFactor()
    {
        return this.conversionFactor;
    }
    
    public long inputFromOutput(long nlOutput)
    {
        return (long) (nlOutput / this.conversionFactor);
    }
    
    public long outputFromInput(long nlInput)
    {
        return (long) (nlInput * this.conversionFactor);
    }
}
