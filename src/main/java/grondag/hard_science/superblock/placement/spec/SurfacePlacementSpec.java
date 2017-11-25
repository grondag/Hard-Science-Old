package grondag.hard_science.superblock.placement.spec;

import grondag.hard_science.superblock.placement.PlacementSpecType;

/**
 * Builder's wand type of placement
 */
public class SurfacePlacementSpec extends SingleStackPlacementSpec
{
    public SurfacePlacementSpec() {};
    
    protected SurfacePlacementSpec(SurfaceBuilder builder)
    {
        super(builder);
    }

    @Override
    public PlacementSpecType specType()
    {
        return PlacementSpecType.SURFACE;
    }
}