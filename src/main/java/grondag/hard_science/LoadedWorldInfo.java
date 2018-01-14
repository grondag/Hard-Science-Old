package grondag.hard_science;

import net.minecraft.world.World;

/**
 * Captures basic information about a loaded world
 * so that it can be safely accessed off tick.
 * Refreshed during each world tick in common proxy.
 */
public class LoadedWorldInfo
{
    public final int dimensionID;
    private float sunBrightnessFactor;
    
    public LoadedWorldInfo(World world)
    {
        this.dimensionID = world.provider.getDimension();
        this.sunBrightnessFactor = world.getSunBrightnessFactor(0);
    }
    
    public void update(World world)
    {
        this.sunBrightnessFactor = world.getSunBrightnessFactor(0);
    }
    
    public float sunBrightnessFactor()
    {
        return this.sunBrightnessFactor;
    }
}
