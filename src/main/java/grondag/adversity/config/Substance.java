package grondag.adversity.config;

public class Substance 
{
    public final int hardness;
    public final String harvestTool;
    public final int harvestLevel;
    public final int resistance;

    
    public Substance(int hardness, String harvestTool, int harvestLevel, int resistance)
    {
        this.hardness = hardness;
        this.harvestTool = harvestTool;
        this.harvestLevel = harvestLevel;
        this.resistance = resistance;
    }
}