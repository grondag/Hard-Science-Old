package grondag.hard_science.materials;

import net.minecraft.item.Item;

public class ResourceCube extends Item
{
    /** number of times a 1M block was divided to get this cube */
    public final int divisionLevel;
    
    
    public ResourceCube(int divisionLevel)
    {
        super();
        this.divisionLevel = divisionLevel;
    }
}
