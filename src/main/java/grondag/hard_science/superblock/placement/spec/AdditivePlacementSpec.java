package grondag.hard_science.superblock.placement.spec;

import grondag.hard_science.superblock.placement.PlacementSpecType;

/**
 * Surface, but adds to height of existing block surfaces 
 * that may not be aligned with block boundaries.
 */
public class AdditivePlacementSpec extends SurfacePlacementSpec
{
    public AdditivePlacementSpec() {};
    
    protected AdditivePlacementSpec(AdditiveBuilder builder)
    {
        super(builder);
    }
    
    @Override
    public PlacementSpecType specType()
    {
        return PlacementSpecType.ADDITIVE;
    }
}