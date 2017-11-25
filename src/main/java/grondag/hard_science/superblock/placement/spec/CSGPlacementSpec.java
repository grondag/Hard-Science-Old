package grondag.hard_science.superblock.placement.spec;

import grondag.hard_science.superblock.placement.PlacementSpecType;

/** placeholder class for CSG multiblock placements */
public class CSGPlacementSpec extends VolumetricPlacementSpec
{
    public CSGPlacementSpec() {};
    
    protected CSGPlacementSpec(CSGBuilder builder)
    {
        super(builder);
    }
    
    @Override
    public PlacementSpecType specType()
    {
        return PlacementSpecType.CSG;
    }
}